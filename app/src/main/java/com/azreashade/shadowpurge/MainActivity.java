package com.azreashade.shadowpurge;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private int intervalMinutes = 30; // default

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prompt for Usage Access if needed
        if (!UsageAccessUtils.hasUsageAccess(this)) {
            Toast.makeText(this, "Please grant Usage Access for Shadow Purge", Toast.LENGTH_LONG).show();
            UsageAccessUtils.openUsageAccess(this);
        }

        // Tabs + Pager
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            @Override public int getItemCount() { return 2; }
            @Override public androidx.fragment.app.Fragment createFragment(int position) {
                // position 0 = User Apps, 1 = System Apps
                return AppsFragment.newInstance(position == 1);
            }
        });
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "User Apps" : "System Apps")
        ).attach();

        // Interval spinner
        Spinner spinner = findViewById(R.id.spinnerInterval);
        String[] options = new String[]{"15 min", "30 min", "60 min"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options));
        spinner.setSelection(1); // default to 30 min
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                intervalMinutes = (position == 0 ? 15 : position == 1 ? 30 : 60);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Start / Stop buttons
        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop  = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(v -> {
            if (!UsageAccessUtils.hasUsageAccess(this)) {
                Toast.makeText(this, "Grant Usage Access first", Toast.LENGTH_SHORT).show();
                UsageAccessUtils.openUsageAccess(this);
                return;
            }
            Intent svc = new Intent(this, AppKillService.class);
            svc.putExtra("interval_minutes", intervalMinutes);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
            Toast.makeText(this, "Service started (" + intervalMinutes + " min)", Toast.LENGTH_SHORT).show();
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, AppKillService.class));
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        });
    }
}
