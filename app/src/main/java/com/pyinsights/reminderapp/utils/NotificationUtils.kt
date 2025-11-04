package com.pyinsights.reminderapp.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.pyinsights.reminderapp.models.ReminderModel
import com.pyinsights.reminderapp.receivers.NotificationReceiver

object NotificationUtils {

    private const val CHANNEL_ID = "reminder_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder Notifications"
            val descriptionText = "Notifications for your scheduled reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 100, 300)
                setShowBadge(true)
                enableLights(true)
                lightColor = 0xFFFF8A00.toInt()
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(context: Context, reminder: ReminderModel): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.d(
            "NotificationUtils",
            "scheduleNotification called for reminderId=${reminder.id}, title=${reminder.title}, time=${reminder.reminderTime}, isRepeating=${reminder.isRepeating}, interval=${reminder.recurrenceInterval}"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("NotificationUtils", "Cannot schedule exact alarms on this device.")
                return false
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_description", reminder.description)
            putExtra("reminder_id", reminder.id)
            putExtra("is_repeating", reminder.isRepeating)
            putExtra("recurrence_interval", reminder.recurrenceInterval ?: 0L)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("NotificationUtils", "Alarm canceled for reminderId=${reminder.id}")

        reminder.reminderTime?.let { triggerAtMillis ->
            val currentTime = System.currentTimeMillis()

            if (!reminder.isRepeating && triggerAtMillis <= currentTime) {
                Log.w(
                    "NotificationUtils",
                    "Reminder time is in the past and non-repeating, not scheduling notification for reminderId=${reminder.id}"
                )
                return false
            }


            var adjustedTriggerTime = triggerAtMillis
            if (reminder.isRepeating && reminder.recurrenceInterval != null && reminder.recurrenceInterval!! > 0) {
                val intervalMillis = reminder.recurrenceInterval!! * 60 * 1000

                if (triggerAtMillis <= currentTime) {
                    val timeDiff = currentTime - triggerAtMillis
                    val intervalsPassed = (timeDiff / intervalMillis) + 1
                    adjustedTriggerTime = triggerAtMillis + (intervalsPassed * intervalMillis)
                    Log.d(
                        "NotificationUtils",
                        "Repeating reminder adjusted to next occurrence at $adjustedTriggerTime (reminderId=${reminder.id})"
                    )
                }

                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    adjustedTriggerTime,
                    pendingIntent
                )
                Log.d(
                    "NotificationUtils",
                    "Exact alarm scheduled: reminderId=${reminder.id}, time=$adjustedTriggerTime"
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    adjustedTriggerTime,
                    pendingIntent
                )
                Log.d(
                    "NotificationUtils",
                    "Exact alarm scheduled: reminderId=${reminder.id}, time=$adjustedTriggerTime"
                )
            }
        }
        return true
    }

    fun cancelNotification(context: Context, reminderId: Long) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(reminderId.toInt())

        Log.d("NotificationUtils", "Alarm canceled for reminderId=$reminderId")
    }

    fun cancelRepeatingReminder(context: Context, reminderId: Long) {
        cancelNotification(context, reminderId)
        Log.d("NotificationUtils", "Repeating reminder canceled for reminderId=$reminderId")
    }
}
