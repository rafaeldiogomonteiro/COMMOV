package com.example.commov

import android.app.Application
import com.example.commov.data.local.LocaleHelper
import com.example.commov.data.sync.ProfilePhotoNotifier
import com.example.commov.data.sync.ProfilePhotoSyncManager

class ComMovApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LocaleHelper.applySavedAppLocale(this)
        ProfilePhotoNotifier.ensureChannel(this)
        Thread {
            ProfilePhotoSyncManager.syncIfNeeded(this)
        }.start()
    }
}
