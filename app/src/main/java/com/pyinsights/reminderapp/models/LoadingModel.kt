package com.pyinsights.reminderapp.models

data class LoadingModel(
    var type: LoadingType?,
    override var title: String? = null
) : BaseModel

enum class LoadingType {
    LOADING,
    ERROR,
    EMPTY
}