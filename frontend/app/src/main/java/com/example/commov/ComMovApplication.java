package com.example.commov;

import android.app.Application;

import com.example.commov.data.local.LocaleHelper;

public class ComMovApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LocaleHelper.applySavedAppLocale(this);
    }
}
