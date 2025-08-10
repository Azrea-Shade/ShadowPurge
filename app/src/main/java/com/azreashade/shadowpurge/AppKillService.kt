package com.azreashade.shadowpurge

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.azreashade.shadowpurge.data.AppInfo
import com.azreashade.shadowpurge.data.AppRepository
import com.azreashade.shadowpurge.data.ExclusionManager
import kotlinx.coroutines.*

class AppKillService : Service() {

    private val CHANNEL_ID = "ShadowPurgeChannel"
    private val NOTIFICATION_ID = 1

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var killIntervalMillis: Long = 30 * 60 * 1000L // default 30 minutes

    private lateinit var appRepository: AppRepository
    private lateinit var exclusionManager: ExclusionManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        appRepository = AppRepository(applicationContext)
        exclusionManager = ExclusionManager(applicationContext)
        appRepository.loadApps()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intervalMinutes = intent?.getLongExtra("interval_minutes", 30) ?: 30
        killIntervalMillis = (intervalMinutes * 60 * 1000)

        startForeground(NOTIFICATION_ID, createNotification())
        startKillingLoop()

        return START_STICKY
    }

    private fun startKillingLoop() {
        serviceScope.launch {
            while (isActive) {
                killSelectedApps()
                delay(killIntervalMillis)
            }
        }
    }

    private fun killSelectedApps() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Iterate user apps and kill those not excluded
        for (app in appRepository.userApps) {
            if (!exclusionManager.isExcluded(app.packageName)) {
                try {
                    activityManager.killBackgroundProcesses(app.packageName)
                    Log.i("ShadowPurge", "Killed app: ${app.appName} (${app.packageName})")
                } catch (e: Exception) {
                    Log.w("ShadowPurge", "Failed to kill app: ${app.appName} (${app.packageName}): ${e.message}")
                }
            }
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shadow Purge Running")
            .setContentText("Automatically killing selected apps")
            .setSmallIcon(android.R.drawable.ic_menu_delete)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shadow Purge Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
