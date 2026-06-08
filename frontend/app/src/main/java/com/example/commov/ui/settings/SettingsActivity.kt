package com.example.commov.ui.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import com.example.commov.ui.ComMovActivity
import com.example.commov.ui.compose.SettingsScreen

class SettingsActivity : ComMovActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen()
        }
    }
}
