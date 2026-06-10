package com.example.commov.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "onboarding_state")
data class OnboardingEntity(
    @PrimaryKey val id: Int = SINGLE_ENTRY_ID,
    val introShown: Boolean,
    val shownAt: Long
) {
    companion object {
        const val SINGLE_ENTRY_ID = 1
    }
}
