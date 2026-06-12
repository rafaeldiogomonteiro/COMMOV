package com.example.commov.viewmodel

import com.example.commov.model.DashboardTask
import com.example.commov.model.Project

data class DashboardUiState(
    val userName: String,
    val todoTasks: Int,
    val completedTasks: Int,
    val todoProgress: Int,
    val completedProgress: Int,
    val tasks: List<DashboardTask>,
    val overdueTasks: List<DashboardTask>,
    val todayTasks: List<DashboardTask>,
    val weekTasks: List<DashboardTask>,
    val projects: List<Project>,
    val hoursLoggedThisWeek: Double,
    val tasksOverEstimate: Int,
    val canManageProjects: Boolean,
    val requiresLogin: Boolean,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)
