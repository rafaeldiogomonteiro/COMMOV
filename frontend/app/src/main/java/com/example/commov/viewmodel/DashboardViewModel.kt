package com.example.commov.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.commov.data.local.SessionManager
import com.example.commov.data.remote.AuthApi
import com.example.commov.data.remote.CheckLoginResult
import com.example.commov.data.remote.DashboardApi
import com.example.commov.data.remote.DashboardResult
import com.example.commov.model.DashboardTask
import com.example.commov.model.Status

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
    private var refreshGeneration = 0
    private var state = emptyState(userName = sessionManager.currentUser()?.name?.trim().orEmpty())

    fun observe(observer: StateObserver) {
        this.observer = observer
        publish(state)
        refreshSession()
    }

    fun reload() {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            state = state.copy(requiresLogin = true, isLoading = false, isRefreshing = false)
            publish(state)
            return
        }

        state = state.copy(isRefreshing = !state.isLoading)
        publish(state)
        refreshDashboard(token)
    }

    private fun refreshSession() {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            state = state.copy(requiresLogin = true, isLoading = false)
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
                        state = state.copy(requiresLogin = true, isLoading = false)
                        publish(state)
                    }
                    CheckLoginResult.NetworkError,
                    is CheckLoginResult.ServerError -> {
                        state = state.copy(requiresLogin = false, isLoading = false)
                        publish(state)
                    }
                }
            }
        }.start()
    }

    private fun refreshDashboard(token: String) {
        val generation = ++refreshGeneration
        Thread {
            val result = dashboardApi.dashboard(token)
            val mappedState = when (result) {
                is DashboardResult.Success -> buildState(result, token)
                DashboardResult.Unauthorized -> {
                    sessionManager.clear()
                    state.copy(requiresLogin = true, isLoading = false, isRefreshing = false)
                }
                DashboardResult.NetworkError,
                is DashboardResult.ServerError -> {
                    state.copy(requiresLogin = false, isLoading = false, isRefreshing = false)
                }
            }

            mainHandler.post {
                if (generation != refreshGeneration) {
                    return@post
                }
                state = mappedState.copy(isLoading = false, isRefreshing = false)
                publish(state)
            }
        }.start()
    }

    private fun buildState(result: DashboardResult.Success, token: String): DashboardUiState {
        val activeProjects = DashboardPresentation.activeProjects(result.projects)
        val activeTasks = DashboardPresentation.tasksFromActiveProjects(result.tasks, result.projects)
        val projectsById = activeProjects.associate { it.projectId to it.name }
        val openTasks = DashboardPresentation.openTasks(activeTasks)
        val mapTask: (com.example.commov.data.remote.ApiTask) -> DashboardTask = { task ->
            DashboardPresentation.toDashboardTask(task, projectsById[task.projectId])
        }

        return state.copy(
            todoTasks = openTasks.size,
            completedTasks = activeTasks.count { Status.isTaskCompleted(it.status) },
            todoProgress = activeTasks.percent { !Status.isTaskCompleted(it.status) },
            completedProgress = activeTasks.percent { Status.isTaskCompleted(it.status) },
            tasks = openTasks
                .sortedWith(compareBy<com.example.commov.data.remote.ApiTask> { it.estimatedEndDate == null }.thenBy { it.estimatedEndDate })
                .take(4)
                .map(mapTask),
            overdueTasks = DashboardPresentation.overdueTasks(activeTasks).map(mapTask),
            todayTasks = DashboardPresentation.todayTasks(activeTasks).map(mapTask),
            weekTasks = DashboardPresentation.weekTasks(activeTasks).map(mapTask),
            projects = DashboardPresentation.previewProjects(result.projects, activeTasks),
            hoursLoggedThisWeek = DashboardPresentation.weeklyHoursLogged(token, activeTasks),
            tasksOverEstimate = DashboardPresentation.tasksOverEstimateCount(activeTasks),
            canManageProjects = sessionManager.canManageProjects(),
            requiresLogin = false
        )
    }

    private fun publish(state: DashboardUiState) {
        observer?.onStateChanged(state)
    }

    private fun emptyState(userName: String): DashboardUiState {
        return DashboardUiState(
            userName = userName,
            todoTasks = 0,
            completedTasks = 0,
            todoProgress = 0,
            completedProgress = 0,
            tasks = emptyList(),
            overdueTasks = emptyList(),
            todayTasks = emptyList(),
            weekTasks = emptyList(),
            projects = emptyList(),
            hoursLoggedThisWeek = 0.0,
            tasksOverEstimate = 0,
            canManageProjects = sessionManager.canManageProjects(),
            requiresLogin = false,
            isLoading = true
        )
    }

    private fun List<com.example.commov.data.remote.ApiTask>.percent(predicate: (com.example.commov.data.remote.ApiTask) -> Boolean): Int {
        if (isEmpty()) {
            return 0
        }

        return ((count(predicate).toFloat() / size) * 100).toInt()
    }
}
