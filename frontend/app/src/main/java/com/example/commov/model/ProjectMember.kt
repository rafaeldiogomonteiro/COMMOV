package com.example.commov.model

import androidx.annotation.ColorRes

data class ProjectMember(
    val userId: Int = 0,
    val name: String,
    val initials: String,
    @ColorRes val avatarColorResId: Int,
    val isManager: Boolean = false,
    val photo: String = ""
)
