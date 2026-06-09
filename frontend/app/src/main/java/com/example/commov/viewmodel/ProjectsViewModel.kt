package com.example.commov.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.commov.R
import com.example.commov.data.local.SessionManager
import com.example.commov.data.remote.ApiProject
import com.example.commov.data.remote.ApiTask
import com.example.commov.data.remote.ApiUser
import com.example.commov.data.remote.ProjectsApi
import com.example.commov.data.remote.ProjectsResult
import com.example.commov.model.DashboardTask
import com.example.commov.model.Project
import com.example.commov.model.ProjectMember
import com.example.commov.model.ProjectTask
import java.util.Locale

class ProjectsViewModel(
    context: Context,
    private val projectsApi: ProjectsApi = ProjectsApi(),
    private val sessionManager: SessionManager = SessionManager(context.applicationContext)
) {
    fun interface StateObserver {
        fun onStateChanged(state: ProjectsUiState)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var observer: StateObserver? = null
    private var state = ProjectsUiState(
        projects = emptyList(),
        canCreateTasks = sessionManager.canManageProjects(),
        requiresLogin = false
    )

    fun observe(observer: StateObserver) {
        this.observer = observer
        publish()
        refresh()
    }

    fun reload() {
        refresh()
    }

    private fun refresh() {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            state = state.copy(requiresLogin = true)
            publish()
            return
        }

        Thread {
            val result = projectsApi.projects(token)
            mainHandler.post {
                when (result) {
                    is ProjectsResult.Success -> {
                        state = state.copy(
                            projects = result.projects.mapIndexed { index, project -> project.toProject(index) },
                            canCreateTasks = sessionManager.canManageProjects(),
                            requiresLogin = false
                        )
                    }
                    ProjectsResult.Unauthorized -> {
                        sessionManager.clear()
                        state = state.copy(requiresLogin = true)
                    }
                    ProjectsResult.NetworkError,
                    is ProjectsResult.ServerError -> {
                        state = state.copy(requiresLogin = false)
                    }
                }
                publish()
            }
        }.start()
    }

    private fun publish() {
        observer?.onStateChanged(state)
    }

    private fun ApiProject.toProject(index: Int): Project {
        val colors = projectColors(index)
        val manager = members.firstOrNull { it.userId == managerId }
        val projectMembers = members.mapIndexed { memberIndex, user ->
            user.toProjectMember(memberIndex, user.userId == managerId)
        }
        val membersByUserId = projectMembers.associateBy { it.userId }

        return Project(
            projectId = projectId,
            nameResId = 0,
            descriptionResId = 0,
            initials = initials(name),
            taskCount = tasks.size,
            members = projectMembers,
            tasks = tasks.map { task ->
                ProjectTask(
                    task = task.toDashboardTask(name),
                    assignees = listOfNotNull(membersByUserId[task.userId])
                )
            },
            accentColorResId = colors.accentColorResId,
            badgeColorResId = colors.badgeColorResId,
            nameText = name,
            descriptionText = description,
            status = status,
            managerId = managerId,
            managerName = manager?.name,
            startDate = startDate?.take(10),
            estimatedEndDate = estimatedEndDate?.take(10),
            actualEndDate = actualEndDate?.take(10)
        )
    }

    private fun ApiUser.toProjectMember(index: Int, isManager: Boolean): ProjectMember {
        return ProjectMember(
            userId = userId,
            name = name,
            initials = initials(name),
            avatarColorResId = avatarColor(index),
            isManager = isManager,
            photo = photo
        )
    }

    private fun ApiTask.toDashboardTask(projectName: String): DashboardTask {
        val normalizedStatus = status.lowercase(Locale.getDefault())
        val style = when (normalizedStatus) {
            "completed" -> TaskStyle(
                statusKey = "completed",
                iconResId = R.drawable.ic_check_circle,
                accentColorResId = R.color.project_green,
                iconBackgroundColorResId = R.color.project_green_soft,
                statusBackgroundColorResId = R.color.project_green_soft,
                statusTextColorResId = R.color.project_green
            )
            "blocked" -> TaskStyle(
                statusKey = "blocked",
                iconResId = R.drawable.ic_alert_triangle,
                accentColorResId = R.color.task_red,
                iconBackgroundColorResId = R.color.task_red_soft,
                statusBackgroundColorResId = R.color.task_red_soft,
                statusTextColorResId = R.color.task_red
            )
            "in_progress" -> TaskStyle(
                statusKey = "in_progress",
                iconResId = R.drawable.ic_clock,
                accentColorResId = R.color.task_orange,
                iconBackgroundColorResId = R.color.task_orange_soft,
                statusBackgroundColorResId = R.color.task_orange_soft,
                statusTextColorResId = R.color.task_orange
            )
            else -> TaskStyle(
                statusKey = "pending",
                iconResId = R.drawable.ic_document,
                accentColorResId = R.color.task_blue,
                iconBackgroundColorResId = R.color.task_blue_soft,
                statusBackgroundColorResId = R.color.task_status_gray_bg,
                statusTextColorResId = R.color.task_status_gray_text
            )
        }

        return DashboardTask(
            titleResId = 0,
            metaResId = 0,
            statusResId = 0,
            iconResId = style.iconResId,
            accentColorResId = style.accentColorResId,
            iconBackgroundColorResId = style.iconBackgroundColorResId,
            statusBackgroundColorResId = style.statusBackgroundColorResId,
            statusTextColorResId = style.statusTextColorResId,
            titleText = title,
            metaText = taskMeta(projectName, estimatedEndDate),
            statusText = style.statusKey,
            taskId = taskId
        )
    }

    private fun taskMeta(projectName: String, estimatedEndDate: String?): String {
        val dueDate = estimatedEndDate?.take(10)?.takeIf { it.isNotBlank() }
        return if (dueDate == null) projectName else "$projectName • $dueDate"
    }

    private fun initials(name: String): String {
        return name
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "PR" }
    }

    private fun projectColors(index: Int): ProjectColors {
        return when (index % 4) {
            0 -> ProjectColors(R.color.bottom_nav_selected, R.color.task_blue_soft)
            1 -> ProjectColors(R.color.project_green, R.color.project_green_soft)
            2 -> ProjectColors(R.color.task_orange, R.color.task_orange_soft)
            else -> ProjectColors(R.color.project_purple, R.color.project_purple_soft)
        }
    }

    private fun avatarColor(index: Int): Int {
        return when (index % 5) {
            0 -> R.color.bottom_nav_selected
            1 -> R.color.project_green
            2 -> R.color.task_orange
            3 -> R.color.project_purple
            else -> R.color.task_status_gray_text
        }
    }

    private data class ProjectColors(
        val accentColorResId: Int,
        val badgeColorResId: Int
    )

    private data class TaskStyle(
        val statusKey: String,
        val iconResId: Int,
        val accentColorResId: Int,
        val iconBackgroundColorResId: Int,
        val statusBackgroundColorResId: Int,
        val statusTextColorResId: Int
    )
}
