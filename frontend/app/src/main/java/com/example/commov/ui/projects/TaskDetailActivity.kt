package com.example.commov.ui.projects

import android.os.Bundle
import androidx.activity.compose.setContent
import com.example.commov.ui.ComMovActivity
import com.example.commov.ui.compose.TaskDetailScreen

class TaskDetailActivity : ComMovActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskDetailScreen(intent.getIntExtra(EXTRA_TASK_ID, 0))
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "taskId"
    }
}
