package com.example.commov.viewmodel

import com.example.commov.R
import com.example.commov.data.remote.ApiProjectSummary
import com.example.commov.data.remote.ApiTask
import com.example.commov.data.remote.ApiTaskTimeEntry
import com.example.commov.data.remote.TaskApi
import com.example.commov.data.remote.TaskTimeEntriesResult
import com.example.commov.model.DashboardTask
import com.example.commov.model.Project
import com.example.commov.model.Status
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DashboardPresentation {
    fun activeProjects(projects: List<ApiProjectSummary>): List<ApiProjectSummary> {
        return projects.filter { !Status.isProjectCancelled(it.status) }
    }

    fun tasksFromActiveProjects(
        tasks: List<ApiTask>,
        projects: List<ApiProjectSummary>
    ): List<ApiTask> {
        val cancelledProjectIds = projects
            .filter { Status.isProjectCancelled(it.status) }
            .map { it.projectId }
            .toSet()
        if (cancelledProjectIds.isEmpty()) {
            return tasks
        }
        return tasks.filter { it.projectId !in cancelledProjectIds }
    }

    fun openTasks(tasks: List<ApiTask>): List<ApiTask> {
        return tasks.filter { !Status.isTaskCompleted(it.status) }
    }

    fun overdueTasks(tasks: List<ApiTask>): List<ApiTask> {
        return openTasks(tasks)
            .filter { isOverdue(it.estimatedEndDate, it.status) }
            .sortedBy { it.estimatedEndDate }
    }

    fun todayTasks(tasks: List<ApiTask>): List<ApiTask> {
        return openTasks(tasks)
            .filter { isToday(it.estimatedEndDate) }
            .sortedBy { it.title.lowercase(Locale.getDefault()) }
    }

    fun weekTasks(tasks: List<ApiTask>): List<ApiTask> {
        return openTasks(tasks)
            .filter { isThisWeek(it.estimatedEndDate) && !isToday(it.estimatedEndDate) }
            .sortedBy { it.estimatedEndDate }
    }

    fun tasksOverEstimateCount(tasks: List<ApiTask>): Int {
        return openTasks(tasks).count { task ->
            task.estimatedTime > 0 && task.timeSpent > task.estimatedTime
        }
    }

    fun weeklyHoursLogged(token: String, tasks: List<ApiTask>, taskApi: TaskApi = TaskApi()): Double {
        val weekStart = startOfWeek()
        val weekEnd = endOfWeek()
        var total = 0.0

        for (task in tasks) {
            when (val result = taskApi.listTimeEntries(token, task.taskId)) {
                is TaskTimeEntriesResult.Success -> {
                    total += result.entries.sumOf { entry ->
                        entry.hoursInWeek(weekStart, weekEnd)
                    }
                }
                else -> Unit
            }
        }

        return total
    }

    fun previewProjects(
        projects: List<ApiProjectSummary>,
        tasks: List<ApiTask>,
        limit: Int = 3
    ): List<Project> {
        val activeProjects = activeProjects(projects)
        val taskCounts = tasks.groupBy { it.projectId }.mapValues { (_, projectTasks) -> projectTasks.size }
        return activeProjects
            .take(limit)
            .mapIndexed { index, project ->
                project.toProject(
                    index = index,
                    taskCount = taskCounts[project.projectId] ?: 0
                )
            }
    }

    fun toDashboardTask(task: ApiTask, projectName: String?): DashboardTask {
        val normalizedStatus = Status.normalizeTaskStatus(task.status)
        val overdue = isOverdue(task.estimatedEndDate, task.status)
        val overEstimate = task.estimatedTime > 0 && task.timeSpent > task.estimatedTime
        val style = when {
            overdue -> TaskStyle(
                statusKey = Status.TASK_TODO,
                iconResId = R.drawable.ic_alert_triangle,
                accentColorResId = R.color.task_red,
                iconBackgroundColorResId = R.color.task_red_soft,
                statusBackgroundColorResId = R.color.task_red_soft,
                statusTextColorResId = R.color.task_red
            )
            normalizedStatus == Status.TASK_COMPLETED -> TaskStyle(
                statusKey = Status.TASK_COMPLETED,
                iconResId = R.drawable.ic_check_circle,
                accentColorResId = R.color.project_green,
                iconBackgroundColorResId = R.color.project_green_soft,
                statusBackgroundColorResId = R.color.project_green_soft,
                statusTextColorResId = R.color.project_green
            )
            else -> TaskStyle(
                statusKey = Status.TASK_TODO,
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
            titleText = task.title,
            metaText = taskMeta(projectName, task.estimatedEndDate, overdue),
            statusText = style.statusKey,
            taskId = task.taskId,
            isOverdue = overdue,
            estimatedEndDate = task.estimatedEndDate?.take(10),
            isOverEstimate = overEstimate
        )
    }

    fun todayLabel(): String {
        return SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    fun isOverdue(estimatedEndDate: String?, status: String): Boolean {
        if (status.equals("completed", ignoreCase = true)) {
            return false
        }
        val dueDate = parseIsoDate(estimatedEndDate?.take(10)) ?: return false
        return dueDate.before(startOfDay(today()))
    }

    fun isToday(estimatedEndDate: String?): Boolean {
        val dueDate = parseIsoDate(estimatedEndDate?.take(10)) ?: return false
        return isSameDay(dueDate, today())
    }

    fun isThisWeek(estimatedEndDate: String?): Boolean {
        val dueDate = parseIsoDate(estimatedEndDate?.take(10)) ?: return false
        val weekStart = startOfWeek()
        val weekEnd = endOfWeek()
        return !dueDate.before(weekStart) && !dueDate.after(weekEnd)
    }

    private fun taskMeta(projectName: String?, estimatedEndDate: String?, overdue: Boolean): String {
        val project = projectName ?: "Project"
        val dueDate = estimatedEndDate?.take(10)?.takeIf { it.isNotBlank() }
        return when {
            overdue && dueDate != null -> "$project • $dueDate"
            dueDate != null -> "$project • $dueDate"
            else -> project
        }
    }

    private fun ApiProjectSummary.toProject(index: Int, taskCount: Int): Project {
        val colors = projectColors(index)
        return Project(
            projectId = projectId,
            nameResId = 0,
            descriptionResId = 0,
            initials = initials(name),
            taskCount = taskCount,
            members = emptyList(),
            tasks = emptyList(),
            accentColorResId = colors.accentColorResId,
            badgeColorResId = colors.badgeColorResId,
            nameText = name,
            descriptionText = description,
            status = status
        )
    }

    private fun ApiTaskTimeEntry.hoursInWeek(weekStart: Calendar, weekEnd: Calendar): Double {
        val workDay = parseIsoDate(workDate?.take(10)) ?: return 0.0
        return if (!workDay.before(weekStart) && !workDay.after(weekEnd)) {
            timeSpent
        } else {
            0.0
        }
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

    private fun today(): Calendar = startOfDay(Calendar.getInstance())

    private fun startOfWeek(): Calendar {
        val calendar = startOfDay(Calendar.getInstance())
        val dayOffset = (calendar.get(Calendar.DAY_OF_WEEK) - calendar.firstDayOfWeek + 7) % 7
        calendar.add(Calendar.DAY_OF_MONTH, -dayOffset)
        return calendar
    }

    private fun endOfWeek(): Calendar {
        val calendar = startOfWeek()
        calendar.add(Calendar.DAY_OF_MONTH, 6)
        return calendar
    }

    private fun parseIsoDate(value: String?): Calendar? {
        if (value.isNullOrBlank() || value.length < 10) {
            return null
        }

        return runCatching {
            val parts = value.take(10).split("-")
            if (parts.size != 3) {
                return null
            }
            startOfDay(
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, parts[0].toInt())
                    set(Calendar.MONTH, parts[1].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                }
            )
        }.getOrNull()
    }

    private fun startOfDay(calendar: Calendar): Calendar {
        return calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun isSameDay(first: Calendar, second: Calendar): Boolean {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
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
