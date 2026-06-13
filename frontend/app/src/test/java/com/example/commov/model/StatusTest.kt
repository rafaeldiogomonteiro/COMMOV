package com.example.commov.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusTest {
    @Test
    fun isProjectCancelled_detectsCancelledStatus() {
        assertTrue(Status.isProjectCancelled(Status.PROJECT_CANCELLED))
        assertTrue(Status.isProjectCancelled(" CANCELLED "))
        assertFalse(Status.isProjectCancelled(Status.PROJECT_ACTIVE))
        assertFalse(Status.isProjectCancelled(Status.PROJECT_COMPLETED))
    }

    @Test
    fun normalizeProjectStatus_acceptsKnownValues() {
        assertEquals(Status.PROJECT_ACTIVE, Status.normalizeProjectStatus("active"))
        assertEquals(Status.PROJECT_COMPLETED, Status.normalizeProjectStatus(" COMPLETED "))
        assertEquals(Status.PROJECT_ON_HOLD, Status.normalizeProjectStatus("on_hold"))
        assertEquals(Status.PROJECT_ACTIVE, Status.normalizeProjectStatus(""))
    }

    @Test
    fun normalizeProjectStatus_rejectsUnknownValues() {
        assertNull(Status.normalizeProjectStatus("archived"))
    }

    @Test
    fun isTaskCompleted_matchesCompletedOnly() {
        assertTrue(Status.isTaskCompleted("completed"))
        assertTrue(Status.isTaskCompleted(" COMPLETED "))
        assertFalse(Status.isTaskCompleted("pending"))
    }

    @Test
    fun normalizeTaskStatus_mapsEveryOpenStatusToTodo() {
        assertEquals(Status.TASK_TODO, Status.normalizeTaskStatus(""))
        assertEquals(Status.TASK_TODO, Status.normalizeTaskStatus("pending"))
        assertEquals(Status.TASK_TODO, Status.normalizeTaskStatus("in_progress"))
        assertEquals(Status.TASK_TODO, Status.normalizeTaskStatus("blocked"))
        assertEquals(Status.TASK_COMPLETED, Status.normalizeTaskStatus("completed"))
    }

    @Test
    fun formatTaskMeta_includesDueDateWhenPresent() {
        assertEquals("Alpha", Status.formatTaskMeta("Alpha", null))
        assertEquals("Alpha • 2026-06-09", Status.formatTaskMeta("Alpha", "2026-06-09T00:00:00Z"))
    }

    @Test
    fun memberInitials_usesFirstLettersOrFallback() {
        assertEquals("JD", Status.memberInitials("Jane Doe"))
        assertEquals("PR", Status.memberInitials("   "))
    }
}
