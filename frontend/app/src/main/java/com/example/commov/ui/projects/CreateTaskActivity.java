package com.example.commov.ui.projects;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.commov.R;
import com.example.commov.data.local.LocaleHelper;
import com.example.commov.ui.common.BottomNavigationBar;

import java.util.Calendar;
import java.util.Locale;

public class CreateTaskActivity extends AppCompatActivity {
    public static final String EXTRA_PROJECT_ID = "projectId";
    public static final String EXTRA_PROJECT_NAME = "projectName";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applySavedLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        bindProjectHeader();
        bindSpinners();
        bindPickerInputs();
        bindActions();
        new BottomNavigationBar(this, BottomNavigationBar.Destination.PROJECTS).bind();
    }

    private void bindProjectHeader() {
        String projectName = getIntent().getStringExtra(EXTRA_PROJECT_NAME);
        if (projectName == null || projectName.trim().isEmpty()) {
            projectName = getString(R.string.project_alpha_name);
        }

        ((TextView) findViewById(R.id.createTaskProject))
                .setText(getString(R.string.create_task_project) + ": " + projectName);
    }

    private void bindSpinners() {
        bindSpinner(
                R.id.taskUserInput,
                new String[]{"Ricardo Silva", "Ana Costa", "Marta Reis", "Joao Lima"}
        );
        bindSpinner(
                R.id.taskStatusInput,
                new String[]{"pending", "in_progress", "completed", "blocked"}
        );
    }

    private void bindSpinner(int spinnerId, String[] values) {
        Spinner spinner = findViewById(spinnerId);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                values
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void bindPickerInputs() {
        configurePickerInput(R.id.taskEstimatedEndDateInput, R.drawable.ic_calendar_clock);
        configurePickerInput(R.id.taskWorkDateInput, R.drawable.ic_calendar_clock);
        configurePickerInput(R.id.taskActualEndDateInput, R.drawable.ic_calendar_clock);
        configurePickerInput(R.id.taskEstimatedTimeInput, R.drawable.ic_clock);
        configurePickerInput(R.id.taskTimeSpentInput, R.drawable.ic_clock);
        configurePickerInput(R.id.taskCompletionRateInput, R.drawable.ic_check_circle);

        findViewById(R.id.taskEstimatedEndDateInput)
                .setOnClickListener(view -> showDatePicker((EditText) view));
        findViewById(R.id.taskWorkDateInput)
                .setOnClickListener(view -> showDatePicker((EditText) view));
        findViewById(R.id.taskActualEndDateInput)
                .setOnClickListener(view -> showDatePicker((EditText) view));
        findViewById(R.id.taskEstimatedTimeInput)
                .setOnClickListener(view -> showTimePicker((EditText) view));
        findViewById(R.id.taskTimeSpentInput)
                .setOnClickListener(view -> showTimePicker((EditText) view));
        findViewById(R.id.taskCompletionRateInput)
                .setOnClickListener(view -> showCompletionPicker((EditText) view));
    }

    private void configurePickerInput(int inputId, int iconResId) {
        EditText input = findViewById(inputId);
        input.setFocusable(false);
        input.setCursorVisible(false);
        input.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconResId, 0);
        input.setCompoundDrawablePadding(dp(8));
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> target.setText(String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        dayOfMonth
                )),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.setTitle(R.string.create_task_pick_date);
        dialog.show();
    }

    private void showTimePicker(EditText target) {
        NumberPicker hoursPicker = new NumberPicker(this);
        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(24);
        hoursPicker.setWrapSelectorWheel(false);

        NumberPicker minutesPicker = new NumberPicker(this);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(3);
        minutesPicker.setDisplayedValues(new String[]{"00", "15", "30", "45"});
        minutesPicker.setWrapSelectorWheel(false);

        LinearLayout pickerRow = new LinearLayout(this);
        pickerRow.setOrientation(LinearLayout.HORIZONTAL);
        pickerRow.setPadding(dp(18), dp(8), dp(18), 0);
        pickerRow.addView(hoursPicker, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        pickerRow.addView(minutesPicker, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        new AlertDialog.Builder(this)
                .setTitle(R.string.create_task_pick_time)
                .setView(pickerRow)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    int minutes = minutesPicker.getValue() * 15;
                    target.setText(String.format(Locale.US, "%dh %02dm", hoursPicker.getValue(), minutes));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showCompletionPicker(EditText target) {
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(0);
        picker.setMaxValue(100);
        picker.setWrapSelectorWheel(false);
        picker.setValue(0);
        picker.setFormatter(value -> value + "%");
        picker.setPadding(dp(18), dp(8), dp(18), 0);

        new AlertDialog.Builder(this)
                .setTitle(R.string.create_task_pick_completion)
                .setView(picker)
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        target.setText(String.format(Locale.US, "%d%%", picker.getValue())))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void bindActions() {
        findViewById(R.id.saveTaskButton).setOnClickListener(view -> validateAndSave());
    }

    private void validateAndSave() {
        EditText titleInput = findViewById(R.id.taskTitleInput);
        String title = titleInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError(getString(R.string.create_task_title_error));
            return;
        }

        Toast.makeText(this, R.string.create_task_saved, Toast.LENGTH_LONG).show();
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
