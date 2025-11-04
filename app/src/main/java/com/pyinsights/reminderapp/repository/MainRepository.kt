package com.pyinsights.reminderapp.repository

import com.pyinsights.reminderapp.database.DatabaseManager.reminderDao as reminderDao
import com.pyinsights.reminderapp.models.ReminderModel
import com.pyinsights.reminderapp.network.RetrofitClient.todoService as service

class MainRepository() {

    suspend fun getTodos() = service.getTodos()

    suspend fun getReminders() = reminderDao.getReminderList()

    suspend fun insertReminder(reminder: ReminderModel) = reminderDao.insert(reminder)

    suspend fun deleteReminder(reminder: ReminderModel) = reminderDao.delete(reminder)

    suspend fun updateReminder(reminder: ReminderModel) = reminderDao.update(reminder)

    suspend fun getReminderById(reminderId: Long) = reminderDao.getReminderById(reminderId)

}