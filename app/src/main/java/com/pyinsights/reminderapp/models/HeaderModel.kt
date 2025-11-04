package com.pyinsights.reminderapp.models

import androidx.annotation.StringRes

data class HeaderModel(
    @StringRes val titleResId: Int
) : BaseModel {
    override val title: String? = null
}
