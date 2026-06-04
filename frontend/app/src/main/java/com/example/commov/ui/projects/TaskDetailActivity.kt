package com.example.commov.ui.projects

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.commov.data.local.LocaleHelper
import com.example.commov.ui.compose.TaskDetailScreen

class TaskDetailActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applySavedLocale(newBase))
    }

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
