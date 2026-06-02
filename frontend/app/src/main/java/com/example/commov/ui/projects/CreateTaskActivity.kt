package com.example.commov.ui.projects

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.commov.data.local.LocaleHelper
import com.example.commov.ui.compose.CreateTaskScreen

class CreateTaskActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applySavedLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreateTaskScreen(intent.getStringExtra(EXTRA_PROJECT_NAME))
        }
    }

    companion object {
        const val EXTRA_PROJECT_ID = "projectId"
        const val EXTRA_PROJECT_NAME = "projectName"
    }
}
