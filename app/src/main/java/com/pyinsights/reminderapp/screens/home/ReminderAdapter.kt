package com.pyinsights.reminderapp.screens.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.pyinsights.reminderapp.R
import com.pyinsights.reminderapp.models.BaseModel
import com.pyinsights.reminderapp.models.HeaderModel
import com.pyinsights.reminderapp.models.LoadingModel
import com.pyinsights.reminderapp.models.ReminderModel
import com.pyinsights.reminderapp.models.TodoModel
import com.pyinsights.reminderapp.utils.DateUtils
import java.util.Calendar

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_REMINDER = 1
private const val ITEM_VIEW_TYPE_TODO = 2

private const val ITEM_VIEW_TYPE_LOADING = 3

class ReminderAdapter(
    private var items: List<BaseModel>,
    private val onItemClicked: (ReminderModel) -> Unit,
    private val onDeleteClicked: (ReminderModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            ITEM_VIEW_TYPE_REMINDER -> {
                val view = inflater.inflate(R.layout.item_reminder, parent, false)
                ReminderViewHolder(view)
            }
            ITEM_VIEW_TYPE_TODO -> {
                val view = inflater.inflate(R.layout.item_todo, parent, false)
                TodoViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HeaderModel -> (holder as HeaderViewHolder).bind(item)
            is ReminderModel -> (holder as ReminderViewHolder).bind(item)
            is TodoModel -> (holder as TodoViewHolder).bind(item)
            is LoadingModel -> (holder as LoadingViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HeaderModel -> ITEM_VIEW_TYPE_HEADER
            is ReminderModel -> ITEM_VIEW_TYPE_REMINDER
            is TodoModel -> ITEM_VIEW_TYPE_TODO
            is LoadingModel -> ITEM_VIEW_TYPE_LOADING
            else -> throw IllegalStateException("Unsupported item type")
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<BaseModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTitle: TextView = itemView.findViewById(R.id.header_title)
        fun bind(header: HeaderModel) {
            headerTitle.text = itemView.context.getString(header.titleResId)
        }
    }

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val reminderTitle: TextView = itemView.findViewById(R.id.title)
        private val reminderDesc: TextView = itemView.findViewById(R.id.desc)
        private val reminderDate: TextView = itemView.findViewById(R.id.date)
        private val reminderRepeat: TextView = itemView.findViewById(R.id.repeat)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = items[position]
                    if (item is ReminderModel) {
                        onItemClicked(item)
                    }
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = items[position]
                    if (item is ReminderModel) {
                        AlertDialog.Builder(it.context)
                            .setTitle(it.context.getString(R.string.delete_reminder_dialog_title))
                            .setMessage(it.context.getString(R.string.delete_reminder_dialog_message))
                            .setPositiveButton(it.context.getString(R.string.delete_button_text)) { _, _ ->
                                onDeleteClicked(item)
                            }
                            .setNegativeButton(it.context.getString(R.string.cancel_button_text), null)
                            .show()
                    }
                }
                true
            }
        }

        fun bind(reminder: ReminderModel) {
            reminderTitle.text = reminder.title

            if (!reminder.description.isNullOrEmpty()) {
                reminderDesc.text = reminder.description
                reminderDesc.visibility = View.VISIBLE
            } else {
                reminderDesc.visibility = View.GONE
            }

            val dateText: String? = reminder.reminderTime?.let {
                DateUtils.getFormattedDateTime(it)
            }

            if (dateText != null) {
                reminderDate.text = dateText
                reminderDate.visibility = View.VISIBLE
            } else {
                reminderDate.visibility = View.GONE
            }

            val repeatText: String?
            if (reminder.isRepeating && reminder.recurrenceInterval != null && reminder.recurrenceInterval > 0) {
                val interval = reminder.recurrenceInterval
                repeatText = when {
                    interval == 60L -> "Repeats every hour"
                    interval == 24 * 60L -> "Repeats every day"
                    interval > 60 && interval % 60 == 0L -> "Repeats every ${interval / 60} hours"
                    else -> "Repeats every $interval minutes"
                }
                reminderRepeat.text = repeatText
                reminderRepeat.visibility = View.VISIBLE
            } else {
                repeatText = null
                reminderRepeat.visibility = View.GONE
            }

            val contentDescription = buildString {
                append(reminder.title)
                if (!reminder.description.isNullOrEmpty()) {
                    append(". ")
                    append(reminder.description)
                }
                dateText?.let {
                    append(". Due on ")
                    append(it)
                }
                repeatText?.let {
                    append(". ")
                    append(it)
                }
            }
            itemView.contentDescription = contentDescription
        }
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val todoText: TextView = itemView.findViewById(R.id.title)
        private val todoDate: TextView = itemView.findViewById(R.id.date)

        fun bind(todo: TodoModel) {
            todoText.text = todo.title

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            todoDate.text = DateUtils.getFormattedDate(calendar)
            todoDate.visibility = View.VISIBLE
        }
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(todo: LoadingModel) {
        }
    }
}
