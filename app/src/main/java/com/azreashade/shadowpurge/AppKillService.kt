package com.azreashade.shadowpurge

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.azreashade.shadowpurge.data.AppRepository
import com.azreashade.shadowpurge.data.ExclusionManager
import kotlinx.coroutines.*

class AppKillService : Service() {

    private val channelId = "shadow_purge_channel"
    private val notificationId = 1

    private var job: Job? = null
    private var intervalMinutes: Long = 30

    private lateinit var appRepository: AppRepository
    private lateinit var exclusionManager: ExclusionManager

    override fun onCreate() {
        super.onCreate()
        appRepository = AppRepository(this)
        exclusionManager = ExclusionManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intervalMinutes = intent?.getLongExtra("interval_minutes", 30) ?: 30
        startForeground(notificationId, buildNotification())

        // Cancel previous job if running
        job?.cancel()

        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                performAppKill()
                delay(intervalMinutes * 60 * 1000) // convert minutes to ms
            }
        }

        return START_STICKY
    }

    private suspend fun performAppKill() {
        appRepository.loadApps()
        val excluded = exclusionManager.getAllExcluded()
        val pm = packageManager

        val appsToKill = appRepository.userApps.filter { appInfo ->
            !excluded.contains(appInfo.packageName)
        }

        Log.d("AppKillService", "Killing apps: ${appsToKill.map { it.packageName }}")

        for (app in appsToKill) {
            try {
                // Attempt to kill app process
                killApp(app.packageName)
            } catch (e: Exception) {
                Log.e("AppKillService", "Failed to kill ${app.packageName}: ${e.message}")
            }
        }
    }

    private fun killApp(packageName: String) {
        // For non-rooted devices, killing apps is limited.
        // Here we try to use ActivityManager's killBackgroundProcesses.
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.killBackgroundProcesses(packageName)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, AppKillService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val pendingStopIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Shadow Purge Running")
            .setContentText("Killing background apps every $intervalMinutes minutes")
            .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
            .addAction(android.R.drawable.ic_media_pause, "Stop", pendingStopIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Shadow Purge Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    }
}
