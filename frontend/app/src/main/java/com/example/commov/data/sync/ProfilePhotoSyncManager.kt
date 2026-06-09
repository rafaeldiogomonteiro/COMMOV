package com.example.commov.data.sync

import android.content.Context
import com.example.commov.data.local.PendingProfilePhotoStore
import com.example.commov.data.local.SessionManager
import com.example.commov.data.remote.PhotoApi
import com.example.commov.data.remote.PhotoUploadResult
import com.example.commov.data.remote.ProfileUpdateResult
import com.example.commov.data.remote.UserApi

object ProfilePhotoSyncManager {
    fun syncIfNeeded(context: Context): Boolean {
        val appContext = context.applicationContext
        val sessionManager = SessionManager(appContext)
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            return false
        }

        val store = PendingProfilePhotoStore(appContext)
        val pending = store.getPending() ?: return false

        val photoApi = PhotoApi()
        val userApi = UserApi()

        return when (
            val uploadResult = photoApi.uploadPhoto(
                token,
                pending.fileName,
                pending.imageBytes,
                pending.mimeType
            )
        ) {
            is PhotoUploadResult.Success -> {
                when (val profileResult = userApi.updateProfile(token, uploadResult.path)) {
                    is ProfileUpdateResult.Success -> {
                        sessionManager.updateUser(profileResult.user)
                        store.clear()
                        ProfilePhotoNotifier.showUploadSuccess(appContext)
                        true
                    }
                    else -> false
                }
            }
            else -> false
        }
    }
}
