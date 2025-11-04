package com.pyinsights.reminderapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pyinsights.reminderapp.models.ReminderModel

@Database(entities = [ReminderModel::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}
