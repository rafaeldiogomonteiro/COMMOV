package com.example.commov.ui.projects

import android.os.Bundle
import androidx.activity.compose.setContent
import com.example.commov.ui.ComMovActivity
import com.example.commov.ui.compose.CreateTaskScreen

class CreateTaskActivity : ComMovActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreateTaskScreen(
                projectId = intent.getIntExtra(EXTRA_PROJECT_ID, 0),
                projectName = intent.getStringExtra(EXTRA_PROJECT_NAME)
            )
        }
    }

    companion object {
        const val EXTRA_PROJECT_ID = "projectId"
        const val EXTRA_PROJECT_NAME = "projectName"
    }
}
