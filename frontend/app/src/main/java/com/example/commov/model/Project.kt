package com.example.commov.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes

data class Project(
    val projectId: Int,
    @StringRes val nameResId: Int,
    @StringRes val descriptionResId: Int,
    val initials: String,
    val taskCount: Int,
    val members: List<ProjectMember>,
    val tasks: List<ProjectTask>,
    @ColorRes val accentColorResId: Int,
    @ColorRes val badgeColorResId: Int,
    val nameText: String? = null,
    val descriptionText: String? = null
)
