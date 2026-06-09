package com.example.commov.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingProfilePhotoDao {
    @Query("SELECT * FROM pending_profile_photos LIMIT 1")
    fun getPending(): PendingProfilePhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(photo: PendingProfilePhotoEntity)

    @Query("DELETE FROM pending_profile_photos")
    fun deleteAll()
}
