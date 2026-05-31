package com.example.commov.ui.dashboard;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.commov.R;
import com.example.commov.data.local.LocaleHelper;
import com.example.commov.ui.common.BottomNavigationBar;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySavedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        new DashboardScreen(this).bind();
        new BottomNavigationBar(this, BottomNavigationBar.Destination.HOME).bind();
    }
}
