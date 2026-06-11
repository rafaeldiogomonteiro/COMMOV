package com.example.commov.ui.statistics

import android.os.Bundle
import androidx.activity.compose.setContent
import com.example.commov.ui.ComMovActivity
import com.example.commov.ui.compose.StatisticsScreen

class StatisticsActivity : ComMovActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StatisticsScreen()
        }
    }
}
