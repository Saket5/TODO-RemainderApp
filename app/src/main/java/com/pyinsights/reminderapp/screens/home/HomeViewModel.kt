package com.pyinsights.reminderapp.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyinsights.reminderapp.R
import com.pyinsights.reminderapp.base.LoadingViewState
import com.pyinsights.reminderapp.models.BaseModel
import com.pyinsights.reminderapp.models.HeaderModel
import com.pyinsights.reminderapp.models.ReminderModel
import com.pyinsights.reminderapp.models.TodoModel
import com.pyinsights.reminderapp.repository.MainRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MainRepository) : ViewModel() {

    private val _viewState = MutableLiveData<LoadingViewState>()
    val viewState: LiveData<LoadingViewState> = _viewState

    private var todoList: List<TodoModel> = emptyList()

    fun fetchData() {
        _viewState.value = LoadingViewState.Loading
        viewModelScope.launch {
            try {
                val remindersDeferred = async { repository.getReminders() ?: emptyList() }
                val todosDeferred = async {
                    try {
                        repository.getTodos() ?: emptyList()
                    } catch (e: Exception) {
                        // Gracefully handle network errors by returning an empty list
                        emptyList<TodoModel>()
                    }
                }

                val reminderList = remindersDeferred.await()
                todoList = todosDeferred.await()

                updateViewState(reminderList, todoList)
            } catch (e: Exception) {
                // This will now primarily catch errors from the local database
                _viewState.postValue(LoadingViewState.Error("Failed to fetch data."))
            }
        }
    }

    fun refreshReminders() {
        viewModelScope.launch {
            try {
                val reminderList = repository.getReminders() ?: emptyList()
                updateViewState(reminderList, todoList)
            } catch (e: Exception) {
                _viewState.postValue(LoadingViewState.Error("Failed to refresh reminders."))
            }
        }
    }

    private fun updateViewState(reminders: List<ReminderModel>, todos: List<TodoModel>) {
        if (reminders.isEmpty() && todos.isEmpty()) {
            _viewState.postValue(LoadingViewState.Empty)
        } else {
            val combinedList = mutableListOf<BaseModel>()
            if (reminders.isNotEmpty()) {
                combinedList.add(HeaderModel(R.string.reminders_header))
                combinedList.addAll(reminders)
            } else {
                combinedList.add(HeaderModel(R.string.no_reminders))
            }
            if (todos.isNotEmpty()) {
                combinedList.add(HeaderModel(R.string.todos_header))
                combinedList.addAll(todos)
            }
            _viewState.postValue(LoadingViewState.Success(combinedList))
        }
    }

    fun deleteReminder(reminder: ReminderModel) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            refreshReminders()
        }
    }
}
