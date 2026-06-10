package com.example.commov.data.local

import android.content.Context
import com.example.commov.data.local.db.ComMovDatabase
import com.example.commov.data.local.db.OnboardingEntity

class OnboardingStore(context: Context) {
    private val dao = ComMovDatabase.getInstance(context.applicationContext).onboardingDao()

    fun hasSeenIntro(): Boolean = dao.isIntroShown() == true

    fun markIntroSeen() {
        dao.saveState(
            OnboardingEntity(
                introShown = true,
                shownAt = System.currentTimeMillis()
            )
        )
    }

    fun resetIntro() {
        dao.clear()
    }
}
