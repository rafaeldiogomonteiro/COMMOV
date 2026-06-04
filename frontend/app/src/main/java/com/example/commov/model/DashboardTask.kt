package com.example.commov.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class DashboardTask(
    @StringRes val titleResId: Int,
    @StringRes val metaResId: Int,
    @StringRes val statusResId: Int,
    @DrawableRes val iconResId: Int,
    @ColorRes val accentColorResId: Int,
    @ColorRes val iconBackgroundColorResId: Int,
    @ColorRes val statusBackgroundColorResId: Int,
    @ColorRes val statusTextColorResId: Int,
    val titleText: String? = null,
    val metaText: String? = null,
    val statusText: String? = null,
    val taskId: Int = 0
)
