package com.example.commov

import android.os.Bundle
import androidx.activity.compose.setContent
import com.example.commov.ui.ComMovActivity
import com.example.commov.ui.compose.LoginScreen

class MainActivity : ComMovActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}
