package com.example.commov.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.commov.data.local.SessionManager
import com.example.commov.data.remote.DashboardApi
import com.example.commov.data.remote.DashboardResult
import com.example.commov.model.DashboardTask

class TasksViewModel(
    context: Context,
    private val dashboardApi: DashboardApi = DashboardApi(),
    private val sessionManager: SessionManager = SessionManager(context.applicationContext)
) {
    fun interface StateObserver {
        fun onStateChanged(state: TasksUiState)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var observer: StateObserver? = null
    private var state = TasksUiState(tasks = emptyList(), requiresLogin = false, isLoading = true)

    fun observe(observer: StateObserver) {
        this.observer = observer
        publish(state)
        refresh()
    }

    fun reload() {
        refresh()
    }

    private fun refresh() {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            state = state.copy(requiresLogin = true, isLoading = false)
            publish(state)
            return
        }

        Thread {
            val result = dashboardApi.dashboard(token)
            mainHandler.post {
                state = when (result) {
                    is DashboardResult.Success -> {
                        val activeProjects = DashboardPresentation.activeProjects(result.projects)
                        val activeTasks = DashboardPresentation.tasksFromActiveProjects(result.tasks, result.projects)
                        val projectsById = activeProjects.associate { it.projectId to it.name }
                        val tasks = DashboardPresentation.openTasks(activeTasks)
                            .sortedWith(
                                compareBy<com.example.commov.data.remote.ApiTask> {
                                    !DashboardPresentation.isOverdue(it.estimatedEndDate, it.status)
                                }.thenBy { it.estimatedEndDate == null }
                                    .thenBy { it.estimatedEndDate }
                                    .thenBy { it.title.lowercase() }
                            )
                            .map { task ->
                                DashboardPresentation.toDashboardTask(task, projectsById[task.projectId])
                            }
                        state.copy(tasks = tasks, requiresLogin = false, isLoading = false)
                    }
                    DashboardResult.Unauthorized -> {
                        sessionManager.clear()
                        state.copy(requiresLogin = true, isLoading = false)
                    }
                    DashboardResult.NetworkError,
                    is DashboardResult.ServerError -> {
                        state.copy(requiresLogin = false, isLoading = false)
                    }
                }
                publish(state)
            }
        }.start()
    }

    private fun publish(state: TasksUiState) {
        observer?.onStateChanged(state)
    }
}
