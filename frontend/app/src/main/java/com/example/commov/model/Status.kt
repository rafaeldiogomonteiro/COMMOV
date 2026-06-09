package com.example.commov.model

object Status {
    const val PROJECT_ACTIVE = "active"
    const val PROJECT_COMPLETED = "completed"
    const val PROJECT_ON_HOLD = "on_hold"
    const val PROJECT_CANCELLED = "cancelled"

    const val TASK_COMPLETED = "completed"

    private val validProjectStatuses = setOf(
        PROJECT_ACTIVE,
        PROJECT_COMPLETED,
        PROJECT_ON_HOLD,
        PROJECT_CANCELLED
    )

    fun normalizeProjectStatus(raw: String): String? {
        val normalized = raw.trim().lowercase().ifEmpty { PROJECT_ACTIVE }
        return normalized.takeIf { it in validProjectStatuses }
    }

    fun isTaskCompleted(raw: String): Boolean {
        return raw.trim().equals(TASK_COMPLETED, ignoreCase = true)
    }

    fun formatTaskMeta(projectName: String, estimatedEndDate: String?): String {
        val dueDate = estimatedEndDate?.take(10)?.takeIf { it.isNotBlank() }
        return if (dueDate == null) projectName else "$projectName • $dueDate"
    }

    fun memberInitials(name: String): String {
        return name
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "PR" }
    }
}
