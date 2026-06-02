package com.example.commov.model

import androidx.annotation.ColorRes

data class ProjectMember(
    val name: String,
    val initials: String,
    @ColorRes val avatarColorResId: Int
)
