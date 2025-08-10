package com.azreashade.shadowpurge

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.azreashade.shadowpurge.data.ExclusionManager
import kotlinx.coroutines.*

class AppKillService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var pm: PackageManager
    private lateinit var exclusionManager: ExclusionManager

    private var killIntervalMinutes: Int = 30

    companion object {
        const val CHANNEL_ID = "ShadowPurgeServiceChannel"
        const val NOTIF_ID = 123456
        const val EXTRA_INTERVAL_MINUTES = "interval_minutes"
    }

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        pm = packageManager
        exclusionManager = ExclusionManager(this)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        killIntervalMinutes = intent?.getIntExtra(EXTRA_INTERVAL_MINUTES, 30) ?: 30

        startForeground(NOTIF_ID, buildNotification())

        serviceScope.launch {
            while (isActive) {
                performAppKilling()
                delay(killIntervalMinutes * 60 * 1000L)
            }
        }

        return START_STICKY
    }

    private suspend fun performAppKilling() {
        val excluded = exclusionManager.loadExclusions() ?: emptyList()

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 * 60 // last 1 hour

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, beginTime, endTime
        )

        if (usageStatsList.isNullOrEmpty()) return

        // Get list of running apps sorted by last time used descending
        val runningApps = usageStatsList
            .filter { it.lastTimeUsed > 0 }
            .sortedByDescending { it.lastTimeUsed }
            .map { it.packageName }
            .distinct()

        for (pkg in runningApps) {
            if (pkg == packageName) continue // skip self
            if (excluded.contains(pkg)) continue

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pm.getPackageInfo(pkg, 0) // check if app exists

                    // Use ActivityManager to force stop app
                    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    am.killBackgroundProcesses(pkg)
                    // Note: FORCE_STOP requires root or system privileges, killBackgroundProcesses is best we can do without root
                }
            } catch (e: Exception) {
                // App not found or can't kill, ignore
            }
        }
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shadow Purge Running")
            .setContentText("Automatically closing selected apps every $killIntervalMinutes minutes")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shadow Purge Service",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Notification channel for Shadow Purge foreground service"

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
