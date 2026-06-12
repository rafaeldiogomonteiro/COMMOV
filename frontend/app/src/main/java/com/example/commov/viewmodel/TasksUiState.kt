package com.example.commov.viewmodel

import com.example.commov.model.DashboardTask

data class TasksUiState(
    val tasks: List<DashboardTask>,
    val requiresLogin: Boolean,
    val isLoading: Boolean = true
)
