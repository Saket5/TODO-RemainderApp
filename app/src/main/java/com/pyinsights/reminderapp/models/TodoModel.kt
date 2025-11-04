package com.pyinsights.reminderapp.models

import com.google.gson.annotations.SerializedName

data class TodoModel(
    @SerializedName("id")
    val id: Int?= null,
    @SerializedName("title")
    override val title: String?= null,
    var remainingTime: Long? = null,
) : BaseModel
