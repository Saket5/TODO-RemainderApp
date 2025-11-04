package com.pyinsights.reminderapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_table")
data class ReminderModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    override val title: String,
    val description: String? = null,
    val reminderTime: Long? = null,
    val isRepeating: Boolean = false,
    val recurrenceInterval: Long? = null, // in minutes
    val isCompleted: Boolean = false
) : BaseModel
