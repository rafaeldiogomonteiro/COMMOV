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
import com.example.commov.ui.ComMovActivity
import com.example.commov.ui.compose.IntroScreen
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
                    navigateToLogin()
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
                            navigateToLogin()
                        }
                    },
                    onSkip = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                onboardingStore.markIntroSeen()
                            }
                            navigateToLogin()
                        }
                    },
                )
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
