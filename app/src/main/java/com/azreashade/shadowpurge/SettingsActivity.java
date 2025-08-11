package com.azreashade.shadowpurge;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS = "shadow_purge_prefs";
    private static final String KEY_AUTO_KILL = "auto_kill_enabled";
    private static final String KEY_INTERVAL = "auto_kill_interval_minutes";
    private static final String KEY_THEME = "theme_mode"; // "system" | "light" | "dark"

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_simple);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Settings");
        }

        final SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Views
        Switch switchAuto = findViewById(R.id.switch_auto_kill);
        Spinner spinnerInterval = findViewById(R.id.spinner_interval);
        Spinner spinnerTheme = findViewById(R.id.spinner_theme);

        // Interval spinner values (minutes): 15, 30, 60
        String[] intervalLabels = new String[] { "Every 15 minutes", "Every 30 minutes", "Every hour" };
        Integer[] intervalValues = new Integer[] { 15, 30, 60 };
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, intervalLabels);
        spinnerInterval.setAdapter(intervalAdapter);

        // Theme spinner: system, light, dark
        String[] themeLabels = new String[] { "System default", "Light", "Dark" };
        String[] themeValues = new String[] { "system", "light", "dark" };
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, themeLabels);
        spinnerTheme.setAdapter(themeAdapter);

        // Load current values
        boolean autoKill = prefs.getBoolean(KEY_AUTO_KILL, false);
        int interval = prefs.getInt(KEY_INTERVAL, 30);
        String theme = prefs.getString(KEY_THEME, "system");

        switchAuto.setChecked(autoKill);

        // Set current selection for interval
        int intervalIndex = 1; // default 30
        for (int i = 0; i < intervalValues.length; i++) {
            if (intervalValues[i] == interval) {
                intervalIndex = i;
                break;
            }
        }
        spinnerInterval.setSelection(intervalIndex);

        // Set current selection for theme
        int themeIndex = 0;
        for (int i = 0; i < themeValues.length; i++) {
            if (themeValues[i].equals(theme)) {
                themeIndex = i;
                break;
            }
        }
        spinnerTheme.setSelection(themeIndex);

        // Save on changes
        switchAuto.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_AUTO_KILL, isChecked).apply());

        spinnerInterval.setOnItemSelectedListener(new SimpleOnItemSelected((position) ->
                prefs.edit().putInt(KEY_INTERVAL, intervalValues[position]).apply()));

        spinnerTheme.setOnItemSelectedListener(new SimpleOnItemSelected((position) ->
                prefs.edit().putString(KEY_THEME, themeValues[position]).apply()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
