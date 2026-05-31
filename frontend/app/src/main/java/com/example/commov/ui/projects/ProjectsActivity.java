package com.example.commov.ui.projects;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.commov.R;
import com.example.commov.data.local.LocaleHelper;
import com.example.commov.ui.common.BottomNavigationBar;

public class ProjectsActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySavedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        new ProjectsScreen(this).bind();
        new BottomNavigationBar(this, BottomNavigationBar.Destination.PROJECTS).bind();
    }
}
