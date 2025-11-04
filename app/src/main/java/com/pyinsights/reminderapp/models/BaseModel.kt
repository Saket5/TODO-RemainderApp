package com.pyinsights.reminderapp.models

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

interface BaseModel {
    val title: String?
}