package com.pyinsights.reminderapp.screens.rdp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pyinsights.reminderapp.R
import com.pyinsights.reminderapp.databinding.FragmentRdpBinding
import com.pyinsights.reminderapp.models.ReminderModel
import com.pyinsights.reminderapp.repository.MainRepository
import com.pyinsights.reminderapp.utils.NotificationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RDPFragment : Fragment() {

    private lateinit var binding: FragmentRdpBinding

    private val viewModel: RDPViewModel by viewModels { RDPViewModelFactory(MainRepository()) }
    private val calendar: Calendar = Calendar.getInstance()
    private var reminderId: Long? = null
    private var initialReminderState: ReminderModel? = null
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    showUnsavedChangesDialog()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRdpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reminderId = arguments?.getLong("reminder_id")

        setupToolbar()
        setupListeners()
        observeViewModel()

        if (reminderId != null && reminderId != 0L) {
            viewModel.getReminderById(reminderId!!)
        } else {
            binding.markAsCompletedButton.visibility = View.GONE
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        val currentState = createReminderFromUI()
        return initialReminderState != currentState
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Do you want to save them?")
            .setPositiveButton("Save") { _, _ ->
                saveReminder(andExit = true, isBackPressed = true)
            }
            .setNegativeButton("Discard") { _, _ ->
                onBackPressedCallback.isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun setupToolbar() {
        binding.toolbar.title = if (reminderId != null && reminderId != 0L) getString(R.string.edit_reminder_toolbar_title) else getString(R.string.add_reminder_toolbar_title)
        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        binding.toolbar.setNavigationIconTint(ContextCompat.getColor(requireContext(), R.color.icon_color))
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        if (reminderId != null && reminderId != 0L) {
            binding.toolbar.inflateMenu(R.menu.rdp_fragment_menu)
            binding.toolbar.menu.findItem(R.id.action_delete)?.icon?.mutate()?.setTint(ContextCompat.getColor(requireContext(), R.color.icon_delete_color))
            binding.toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_reminder_dialog_title))
            .setMessage(getString(R.string.delete_reminder_dialog_message))
            .setPositiveButton(getString(R.string.delete_button_text)) { _, _ ->
                viewModel.reminder.value?.let {
                    viewModel.deleteReminder(it)
                    NotificationUtils.cancelNotification(requireContext(), it.id)
                    Toast.makeText(context, getString(R.string.reminder_deleted_toast), Toast.LENGTH_SHORT).show()
                    parentFragmentManager.setFragmentResult("reminder_saved", bundleOf("refresh" to true))
                    parentFragmentManager.popBackStack()
                }
            }
            .setNegativeButton(getString(R.string.cancel_button_text), null)
            .show()
    }

    private fun setupListeners() {
        binding.dateEditText.setOnClickListener { showDatePicker() }
        binding.timeEditText.setOnClickListener { showTimePicker() }

        binding.repeatSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.recurrenceOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.recurrenceOptions.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != R.id.radio_custom) {
                binding.minutesEditText.text?.clear()
            }
        }

        binding.minutesEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    if (!binding.radioCustom.isChecked) {
                        binding.radioCustom.isChecked = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.saveButton.setOnClickListener { saveReminder(true) }

        binding.markAsCompletedButton.setOnClickListener {
            viewModel.reminder.value?.let { reminder ->
                viewModel.markAsCompleted(reminder)
                NotificationUtils.cancelNotification(requireContext(), reminder.id) // Also cancel notification when marked as completed
                Toast.makeText(context, getString(R.string.reminder_marked_as_completed_toast), Toast.LENGTH_SHORT).show()
                parentFragmentManager.setFragmentResult("reminder_saved", bundleOf("refresh" to true))
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.reminder.observe(viewLifecycleOwner) { reminder ->
            if (reminder != null) {
                populateUi(reminder)
                if (initialReminderState == null) {
                    initialReminderState = reminder
                }
            }
        }
    }

    private fun populateUi(reminder: ReminderModel) {
        binding.titleEditText.setText(reminder.title)
        binding.descriptionEditText.setText(reminder.description)
        reminder.reminderTime?.let {
            calendar.timeInMillis = it
            updateDateEditText()
            updateTimeEditText()
        }
        binding.repeatSwitch.isChecked = reminder.isRepeating
        if (reminder.isRepeating) {
            binding.recurrenceOptions.visibility = View.VISIBLE
            when (reminder.recurrenceInterval) {
                60L -> binding.radioHourly.isChecked = true
                (24 * 60L) -> binding.radioDaily.isChecked = true
                else -> {
                    binding.radioCustom.isChecked = true
                    val interval = reminder.recurrenceInterval
                    if (interval != null && interval > 0) {
                        binding.minutesEditText.setText(interval.toString())
                    } else {
                        binding.minutesEditText.setText("")
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            updateDateEditText()
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            updateTimeEditText()
        }, hour, minute, true).show()
    }

    private fun updateDateEditText() {
        val format = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        binding.dateEditText.setText(sdf.format(calendar.time))
        binding.descriptionEditText.clearFocus()
    }

    private fun updateTimeEditText() {
        val format = "h:mm a"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        binding.timeEditText.setText(sdf.format(calendar.time))
        binding.descriptionEditText.clearFocus()
    }

    private fun saveReminder(andExit: Boolean = false, isBackPressed: Boolean = false) {
        val reminder = createReminderFromUI()

        if (reminder.title.isEmpty()) {
            Toast.makeText(context, getString(R.string.title_cannot_be_empty_toast), Toast.LENGTH_SHORT).show()
            return
        }

        if (reminder.isRepeating && reminder.recurrenceInterval == null) {
            Toast.makeText(context, "Please select a repeat interval.", Toast.LENGTH_SHORT).show()
            return
        }

        if (reminder.isRepeating && reminder.recurrenceInterval != null && reminder.recurrenceInterval!! <= 0) {
            Toast.makeText(context, "Please enter a valid number of minutes for custom repeat.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveReminder(reminder)
        val scheduled = NotificationUtils.scheduleNotification(requireContext(), reminder)

        if (!scheduled) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog()
            } else {
                Toast.makeText(context, "Failed to schedule reminder. Please check the date and time.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, getString(R.string.reminder_saved_toast), Toast.LENGTH_SHORT).show()
            parentFragmentManager.setFragmentResult("reminder_saved", bundleOf("refresh" to true))
            initialReminderState = reminder // Crucially, update the state after a successful save

            if (andExit && isBackPressed) {
                onBackPressedCallback.isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else if (andExit) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun createReminderFromUI(): ReminderModel {
        val title = binding.titleEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()
        val isRepeating = binding.repeatSwitch.isChecked
        var recurrenceInterval: Long? = null

        if (isRepeating) {
            recurrenceInterval = when {
                binding.radioHourly.isChecked -> 60L
                binding.radioDaily.isChecked -> 24 * 60L
                binding.radioCustom.isChecked -> {
                    binding.minutesEditText.text.toString().toLongOrNull()
                }
                else -> null // No interval selected
            }
        }

        return ReminderModel(
            id = reminderId ?: 0,
            title = title,
            description = description,
            reminderTime = calendar.timeInMillis,
            isRepeating = isRepeating,
            recurrenceInterval = recurrenceInterval
        )
    }

    private fun showExactAlarmPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("To ensure your reminders are delivered on time, this app needs permission to schedule exact alarms. Please grant this permission in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
