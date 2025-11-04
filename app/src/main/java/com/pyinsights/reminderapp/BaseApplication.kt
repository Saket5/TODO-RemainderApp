package com.pyinsights.reminderapp

import android.app.Application
import com.pyinsights.reminderapp.database.DatabaseManager
import com.pyinsights.reminderapp.network.RetrofitClient
import com.pyinsights.reminderapp.utils.NotificationUtils

class BaseApplication  : Application() {

    override fun onCreate() {
        super.onCreate()
        DatabaseManager.init(this)
        RetrofitClient.todoService
        NotificationUtils.createNotificationChannel(this)
    }
}