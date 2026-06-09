package com.example.commov.data.local

import android.content.Context
import com.example.commov.data.local.db.ComMovDatabase
import com.example.commov.data.local.db.PendingProfilePhotoEntity
import java.io.File

class PendingProfilePhotoStore(context: Context) {
    private val appContext = context.applicationContext
    private val dao = ComMovDatabase.getInstance(appContext).pendingProfilePhotoDao()

    fun hasPendingPhoto(): Boolean = dao.getPending() != null

    fun save(fileName: String, mimeType: String, imageBytes: ByteArray) {
        val imageFile = pendingPhotoFile(appContext)
        imageFile.parentFile?.mkdirs()
        imageFile.writeBytes(imageBytes)
        dao.save(
            PendingProfilePhotoEntity(
                fileName = fileName,
                mimeType = mimeType,
                imagePath = imageFile.absolutePath,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun getPending(): PendingProfilePhoto? {
        val entity = dao.getPending() ?: return null
        val imageFile = File(entity.imagePath)
        if (!imageFile.exists()) {
            clear()
            return null
        }
        return PendingProfilePhoto(
            fileName = entity.fileName,
            mimeType = entity.mimeType,
            imageBytes = imageFile.readBytes()
        )
    }

    fun clear() {
        dao.getPending()?.let { entity ->
            File(entity.imagePath).delete()
        }
        dao.deleteAll()
    }

    companion object {
        fun pendingPhotoFile(context: Context): File {
            return File(context.filesDir, "pending_profile_photo.jpg")
        }
    }
}

data class PendingProfilePhoto(
    val fileName: String,
    val mimeType: String,
    val imageBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PendingProfilePhoto) return false
        return fileName == other.fileName &&
            mimeType == other.mimeType &&
            imageBytes.contentEquals(other.imageBytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + imageBytes.contentHashCode()
        return result
    }
}
