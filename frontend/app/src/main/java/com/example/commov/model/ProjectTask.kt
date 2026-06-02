package com.example.commov.model

data class ProjectTask(
    val task: DashboardTask,
    val assignees: List<ProjectMember>
)
