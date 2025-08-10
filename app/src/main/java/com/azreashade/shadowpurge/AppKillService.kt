package com.azreashade.shadowpurge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AppKillService : Service() {

    private val CHANNEL_ID = "ShadowPurgeChannel"
    private val NOTIFICATION_ID = 1

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var killIntervalMillis: Long = 30 * 60 * 1000L // default 30 minutes

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
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
        // TODO: Add app killing logic here using the exclusion list and user apps
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
