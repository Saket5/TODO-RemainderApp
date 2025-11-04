package com.pyinsights.reminderapp.database

import android.content.Context
import androidx.room.Room

object DatabaseManager {

    private lateinit var db: AppDatabase

    val reminderDao by lazy { db.reminderDao() }

    fun init(context: Context) {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "reminder-database"
        ).fallbackToDestructiveMigration()
            .build()
    }
}