package com.example.commov.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_profile_photos")
data class PendingProfilePhotoEntity(
    @PrimaryKey val id: Int = SINGLE_ENTRY_ID,
    val fileName: String,
    val mimeType: String,
    val imageBytes: ByteArray,
    val createdAt: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PendingProfilePhotoEntity) return false
        return id == other.id &&
            fileName == other.fileName &&
            mimeType == other.mimeType &&
            imageBytes.contentEquals(other.imageBytes) &&
            createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + imageBytes.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }

    companion object {
        const val SINGLE_ENTRY_ID = 1
    }
}
