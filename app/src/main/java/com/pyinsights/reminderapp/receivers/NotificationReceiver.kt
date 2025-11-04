package com.pyinsights.reminderapp.receivers

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pyinsights.reminderapp.MainActivity
import com.pyinsights.reminderapp.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderTitle = intent.getStringExtra("reminder_title")
        val reminderDescription = intent.getStringExtra("reminder_description")
        val reminderId = intent.getLongExtra("reminder_id", 0)
        val isRepeating = intent.getBooleanExtra("is_repeating", false)
        val recurrenceInterval = intent.getLongExtra("recurrence_interval", 0L)

        Log.d(
            "NotificationReceiver",
            "Received notification for reminder: $reminderTitle, isRepeating: $isRepeating"
        )

        showNotification(context, reminderTitle, reminderDescription, reminderId)

        if (isRepeating && recurrenceInterval > 0) {
            scheduleNextOccurrence(context, intent, recurrenceInterval)
        }
    }

    private fun showNotification(
        context: Context,
        title: String?,
        description: String?,
        reminderId: Long
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val largeIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setLargeIcon(largeIcon)
            .setContentTitle("⏰ $title")
            .setContentText(description ?: "Time for your reminder!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(description ?: "Time for your reminder!")
                    .setBigContentTitle("⏰ $title")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 300, 100, 300))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(ContextCompat.getColor(context, R.color.orange_dark))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setFullScreenIntent(contentPendingIntent, true)
            .build()

        notificationManager.notify(reminderId.toInt(), notification)
        Log.d("NotificationReceiver", "Notification shown for reminder: $title")
    }

    private fun scheduleNextOccurrence(
        context: Context,
        originalIntent: Intent,
        recurrenceInterval: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(
                    "NotificationReceiver",
                    "Cannot schedule exact alarms, repeating reminder will not continue"
                )
                return
            }
        }

        val reminderId = originalIntent.getLongExtra("reminder_id", 0)
        val intervalMillis = recurrenceInterval * 60 * 1000 // Convert minutes to milliseconds
        val nextTriggerTime = System.currentTimeMillis() + intervalMillis

        val nextIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("reminder_title", originalIntent.getStringExtra("reminder_title"))
            putExtra("reminder_description", originalIntent.getStringExtra("reminder_description"))
            putExtra("reminder_id", reminderId)
            putExtra("is_repeating", true)
            putExtra("recurrence_interval", recurrenceInterval)
        }

        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            nextTriggerTime,
            nextPendingIntent
        )

        Log.d(
            "NotificationReceiver",
            "Next occurrence scheduled for reminder $reminderId at $nextTriggerTime (in ${recurrenceInterval} minutes)"
        )
    }
}
