package com.pyinsights.reminderapp.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pyinsights.reminderapp.R
import com.pyinsights.reminderapp.base.LoadingViewState
import com.pyinsights.reminderapp.databinding.FragmentHomeBinding
import com.pyinsights.reminderapp.models.ReminderModel
import com.pyinsights.reminderapp.repository.MainRepository
import com.pyinsights.reminderapp.screens.rdp.RDPFragment
import com.pyinsights.reminderapp.utils.NotificationUtils
import com.pyinsights.reminderapp.utils.TtsUtils

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(MainRepository()) }
    private lateinit var reminderAdapter: ReminderAdapter

    private val requestPermissionLauncher = 
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(requireContext(), "Notifications permission denied. You won't receive reminders.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TtsUtils.initialize(requireContext())
        NotificationUtils.createNotificationChannel(requireContext())
        requestNotificationPermission()

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupFab()
        setupFragmentResultListener()

        viewModel.fetchData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        TtsUtils.shutdown()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Notification Permission Needed")
                        .setMessage("This permission is required to show reminder notifications.")
                        .setPositiveButton("OK") { _, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.reminders_toolbar_title)
    }

    private fun setupFab() {
        binding.fabAddReminder.setOnClickListener {
            openRdpFragment()
        }
    }

    private fun openRdpFragment(reminderId: Long? = null) {
        val fragment = RDPFragment().apply {
            arguments = Bundle().apply {
                reminderId?.let { putLong("reminder_id", it) }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("reminder_saved", viewLifecycleOwner) { _, bundle ->
            val shouldRefresh = bundle.getBoolean("refresh")
            if (shouldRefresh) {
                viewModel.refreshReminders()
            }
        }
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter(
            emptyList(),
            onItemClicked = { reminder ->
                openRdpFragment(reminder.id)
            },
            onDeleteClicked = { reminder ->
                viewModel.deleteReminder(reminder)
                NotificationUtils.cancelNotification(requireContext(), reminder.id)
            }
        )
        binding.recyclerView.apply {
            adapter = reminderAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoadingViewState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.GONE
                }
                is LoadingViewState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                    reminderAdapter.updateItems(state.data)
                }
                is LoadingViewState.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                }
                is LoadingViewState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
