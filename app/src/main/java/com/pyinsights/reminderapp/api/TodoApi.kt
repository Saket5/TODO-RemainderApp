package com.pyinsights.reminderapp.api

import com.pyinsights.reminderapp.models.TodoModel
import retrofit2.http.GET

interface TodoApi {
    @GET("/todos")
    suspend fun getTodos(): List<TodoModel>?
}
