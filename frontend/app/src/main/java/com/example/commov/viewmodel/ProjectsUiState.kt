package com.example.commov.viewmodel

import com.example.commov.model.Project

data class ProjectsUiState(
    val projects: List<Project>,
    val canCreateTasks: Boolean = false,
    val requiresLogin: Boolean = false
)
