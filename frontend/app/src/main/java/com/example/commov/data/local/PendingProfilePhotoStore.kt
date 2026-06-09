package com.example.commov.data.local

import android.content.Context
import com.example.commov.data.local.db.ComMovDatabase
import com.example.commov.data.local.db.PendingProfilePhotoEntity

class PendingProfilePhotoStore(context: Context) {
    private val dao = ComMovDatabase.getInstance(context.applicationContext).pendingProfilePhotoDao()

    fun hasPendingPhoto(): Boolean = dao.getPending() != null

    fun save(fileName: String, mimeType: String, imageBytes: ByteArray) {
        dao.save(
            PendingProfilePhotoEntity(
                fileName = fileName,
                mimeType = mimeType,
                imageBytes = imageBytes,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun getPending(): PendingProfilePhotoEntity? = dao.getPending()

    fun clear() {
        dao.deleteAll()
    }
}
