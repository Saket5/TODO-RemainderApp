package com.pyinsights.reminderapp.screens.rdp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyinsights.reminderapp.models.ReminderModel
import com.pyinsights.reminderapp.repository.MainRepository
import kotlinx.coroutines.launch

class RDPViewModel(private val repository: MainRepository) : ViewModel() {

    private val _reminder = MutableLiveData<ReminderModel?>()
    val reminder: LiveData<ReminderModel?> = _reminder

    private val _saveResult = MutableLiveData<ReminderModel?>()
    val saveResult: LiveData<ReminderModel?> = _saveResult

    fun getReminderById(id: Long) {
        viewModelScope.launch {
            _reminder.value = repository.getReminderById(id)
        }
    }

    fun saveReminder(reminder: ReminderModel) {
        viewModelScope.launch {
            if (reminder.id == 0L) {
                val newId = repository.insertReminder(reminder)
                val savedReminder = reminder.copy(id = newId)
                Log.d("RDPViewModel", "Saved reminder: $savedReminder")
                _saveResult.value = savedReminder
            } else {
                repository.updateReminder(reminder)
                Log.d("RDPViewModel", "Updated reminder: $reminder")
                _saveResult.value = reminder
            }
        }
    }

    fun markAsCompleted(reminder: ReminderModel) {
        viewModelScope.launch {
            val updatedReminder = reminder.copy(isCompleted = true)
            repository.updateReminder(updatedReminder)
        }
    }

    fun deleteReminder(reminder: ReminderModel) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }
}
