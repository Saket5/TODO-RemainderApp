package com.pyinsights.reminderapp.base

import com.pyinsights.reminderapp.models.BaseModel

sealed class LoadingViewState {
    object Loading : LoadingViewState()
    data class Success(val data: List<BaseModel>) : LoadingViewState()
    object Empty : LoadingViewState()
    data class Error(val message: String) : LoadingViewState()
}