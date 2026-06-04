package com.example.commov.viewmodel

import com.example.commov.model.DashboardTask

data class DashboardUiState(
    val userName: String,
    val pendingTasks: Int,
    val completedTasks: Int,
    val pendingProgress: Int,
    val completedProgress: Int,
    val tasks: List<DashboardTask>,
    val requiresLogin: Boolean
)
