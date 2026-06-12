package com.example.commov.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.commov.MainActivity
import com.example.commov.data.local.OnboardingStore
import com.example.commov.data.local.SessionManager
import com.example.commov.data.local.SessionRestorer
import com.example.commov.ui.ComMovActivity
import com.example.commov.ui.compose.IntroScreen
import com.example.commov.ui.dashboard.DashboardActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IntroActivity : ComMovActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val onboardingStore = remember { OnboardingStore(applicationContext) }
            val scope = rememberCoroutineScope()
            var showIntro by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(Unit) {
                val hasSeenIntro = withContext(Dispatchers.IO) {
                    onboardingStore.hasSeenIntro()
                }
                if (hasSeenIntro) {
                    navigateAfterIntro()
                } else {
                    showIntro = true
                }
            }

            if (showIntro == true) {
                IntroScreen(
                    onComplete = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                onboardingStore.markIntroSeen()
                            }
                            navigateAfterIntro()
                        }
                    },
                    onSkip = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                onboardingStore.markIntroSeen()
                            }
                            navigateAfterIntro()
                        }
                    },
                )
            }
        }
    }

    private fun navigateAfterIntro() {
        val sessionManager = SessionManager(applicationContext)
        Thread {
            val result = SessionRestorer.validate(sessionManager)
            runOnUiThread {
                val destination = when (result) {
                    SessionRestorer.Result.Valid,
                    SessionRestorer.Result.OfflineValid -> DashboardActivity::class.java
                    SessionRestorer.Result.NeedsLogin -> MainActivity::class.java
                }
                startActivity(Intent(this, destination))
                finish()
            }
        }.start()
    }
}
