package com.azreashade.shadowpurge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.azreashade.shadowpurge.data.ExclusionManager

class AppKillService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var killIntervalMinutes = 30
    private lateinit var exclusionManager: ExclusionManager

    override fun onCreate() {
        super.onCreate()
        exclusionManager = ExclusionManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        killIntervalMinutes = intent?.getIntExtra("interval_minutes", 30) ?: 30

        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shadow Purge Running")
            .setContentText("Automatically closing selected apps")
            .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        runnable = object : Runnable {
            override fun run() {
                killApps()
                handler.postDelayed(this, killIntervalMinutes * 60 * 1000L)
            }
        }

        handler.post(runnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    private fun killApps() {
        val exclusions = exclusionManager.loadExclusions() ?: listOf()
        // TODO: Implement actual app killing here.
        android.util.Log.d("AppKillService", "Killing apps except: $exclusions")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Shadow Purge Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ShadowPurgeServiceChannel"
        const val NOTIFICATION_ID = 1
    }
}
