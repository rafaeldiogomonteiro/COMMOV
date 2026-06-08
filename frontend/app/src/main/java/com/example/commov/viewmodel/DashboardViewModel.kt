package com.example.commov.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.commov.R
import com.example.commov.data.local.SessionManager
import com.example.commov.data.remote.AuthApi
import com.example.commov.data.remote.ApiTask
import com.example.commov.data.remote.CheckLoginResult
import com.example.commov.data.remote.DashboardApi
import com.example.commov.data.remote.DashboardResult
import com.example.commov.model.DashboardTask
import java.util.Locale

class DashboardViewModel(
    context: Context,
    private val authApi: AuthApi = AuthApi(),
    private val dashboardApi: DashboardApi = DashboardApi(),
    private val sessionManager: SessionManager = SessionManager(context.applicationContext)
) {
    fun interface StateObserver {
        fun onStateChanged(state: DashboardUiState)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var observer: StateObserver? = null
    private var state = emptyState(userName = sessionManager.currentUser()?.name?.trim().orEmpty())

    fun observe(observer: StateObserver) {
        this.observer = observer
        publish(state)
        refreshSession()
    }

    private fun refreshSession() {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            state = state.copy(requiresLogin = true)
            publish(state)
            return
        }

        Thread {
            val result = authApi.checkLogin(token)
            mainHandler.post {
                when (result) {
                    is CheckLoginResult.LoggedIn -> {
                        sessionManager.saveSession(token, result.user)
                        state = state.copy(
                            userName = result.user.name.trim(),
                            requiresLogin = false
                        )
                        publish(state)
                        refreshDashboard(token)
                    }
                    CheckLoginResult.LoggedOut -> {
                        sessionManager.clear()
                        state = state.copy(requiresLogin = true)
                        publish(state)
                    }
                    CheckLoginResult.NetworkError,
                    is CheckLoginResult.ServerError -> {
                        state = state.copy(requiresLogin = false)
                        publish(state)
                    }
                }
            }
        }.start()
    }

    private fun refreshDashboard(token: String) {
        Thread {
            val result = dashboardApi.dashboard(token)
            mainHandler.post {
                when (result) {
                    is DashboardResult.Success -> {
                        state = state.copy(
                            pendingTasks = result.tasks.count { !it.status.equals("completed", ignoreCase = true) },
                            completedTasks = result.tasks.count { it.status.equals("completed", ignoreCase = true) },
                            pendingProgress = result.tasks.percent { !it.status.equals("completed", ignoreCase = true) },
                            completedProgress = result.tasks.percent { it.status.equals("completed", ignoreCase = true) },
                            tasks = result.tasks
                                .filter { !it.status.equals("completed", ignoreCase = true) }
                                .sortedWith(compareBy<ApiTask> { it.estimatedEndDate == null }.thenBy { it.estimatedEndDate })
                                .take(4)
                                .map { it.toDashboardTask(result.projectsById) },
                            requiresLogin = false
                        )
                    }
                    DashboardResult.Unauthorized -> {
                        sessionManager.clear()
                        state = state.copy(requiresLogin = true)
                    }
                    DashboardResult.NetworkError,
                    is DashboardResult.ServerError -> {
                        state = state.copy(requiresLogin = false)
                    }
                }
                publish(state)
            }
        }.start()
    }

    private fun publish(state: DashboardUiState) {
        observer?.onStateChanged(state)
    }

    private fun emptyState(userName: String): DashboardUiState {
        return DashboardUiState(
            userName = userName,
            pendingTasks = 0,
            completedTasks = 0,
            pendingProgress = 0,
            completedProgress = 0,
            tasks = emptyList(),
            requiresLogin = false
        )
    }

    private fun List<ApiTask>.percent(predicate: (ApiTask) -> Boolean): Int {
        if (isEmpty()) {
            return 0
        }

        return ((count(predicate).toFloat() / size) * 100).toInt()
    }

    private fun ApiTask.toDashboardTask(projectsById: Map<Int, String>): DashboardTask {
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
            metaText = taskMeta(projectsById[projectId], estimatedEndDate),
            statusText = style.statusKey,
            taskId = taskId
        )
    }

    private fun taskMeta(projectName: String?, estimatedEndDate: String?): String {
        val project = projectName ?: "Project"
        val dueDate = estimatedEndDate?.toDisplayDate()
        return if (dueDate == null) {
            project
        } else {
            "$project • $dueDate"
        }
    }

    private fun String.toDisplayDate(): String? {
        return take(10).takeIf { it.isNotBlank() }
    }

    private data class TaskStyle(
        val statusKey: String,
        val iconResId: Int,
        val accentColorResId: Int,
        val iconBackgroundColorResId: Int,
        val statusBackgroundColorResId: Int,
        val statusTextColorResId: Int
    )
}
