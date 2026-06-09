package com.example.commov.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_profile_photos")
data class PendingProfilePhotoEntity(
    @PrimaryKey val id: Int = SINGLE_ENTRY_ID,
    val fileName: String,
    val mimeType: String,
    val imagePath: String,
    val createdAt: Long
) {
    companion object {
        const val SINGLE_ENTRY_ID = 1
    }
}
