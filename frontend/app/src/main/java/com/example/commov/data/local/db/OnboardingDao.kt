package com.example.commov.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OnboardingDao {
    @Query("SELECT introShown FROM onboarding_state WHERE id = 1 LIMIT 1")
    fun isIntroShown(): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveState(state: OnboardingEntity)

    @Query("DELETE FROM onboarding_state")
    fun clear()
}
