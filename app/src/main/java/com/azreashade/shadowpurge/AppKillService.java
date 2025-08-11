package com.azreashade.shadowpurge;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Set;

public class AppKillService extends Service {

    private static final String CH_ID = "shadow_purge_ch";
    private static final int NOTIF_ID = 101;

    private final Handler handler = new Handler();
    private int intervalMinutes = 30;
    private Runnable loop;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        intervalMinutes = (intent != null) ? intent.getIntExtra("interval_minutes", 30) : 30;

        Notification n = new NotificationCompat.Builder(this, CH_ID)
                .setContentTitle("Shadow Purge")
                .setContentText("Running every " + intervalMinutes + " minutes")
                .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setOngoing(true)
                .build();
        startForeground(NOTIF_ID, n);

        loop = new Runnable() {
            @Override
            public void run() {
                try { killAppsOnce(); } catch (Exception ignored) {}
                handler.postDelayed(this, intervalMinutes * 60_000L);
            }
        };
        handler.post(loop);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(loop);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(CH_ID, "Shadow Purge", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }

    private void killAppsOnce() {
        ExclusionManager excl = new ExclusionManager(this);
        Set<String> excluded = excl.getExcluded();

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        AppRepository repo = new AppRepository(this);
        List<AppInfo> users = repo.getUserApps();

        for (AppInfo a : users) {
            if (excluded.contains(a.packageName)) continue;
            if (a.packageName.equals(getPackageName())) continue;
            try {
                am.killBackgroundProcesses(a.packageName);
            } catch (Exception ignored) {}
        }
    }
            }
