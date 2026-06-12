package com.example.commov.ui.projects

import android.os.Bundle
import androidx.activity.compose.setContent
import com.example.commov.ui.ComMovActivity
import com.example.commov.ui.compose.AllTasksScreen

class TasksActivity : ComMovActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AllTasksScreen()
        }
    }
}
