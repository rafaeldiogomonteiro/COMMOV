package com.example.commov

import android.app.Application
import com.example.commov.data.local.LocaleHelper

class ComMovApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LocaleHelper.applySavedAppLocale(this)
    }
}
