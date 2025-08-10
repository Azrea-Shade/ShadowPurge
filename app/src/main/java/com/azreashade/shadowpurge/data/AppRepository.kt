package com.azreashade.shadowpurge.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

class AppRepository(private val context: Context) {

    val userApps: SnapshotStateList<AppInfo> = mutableStateListOf()
    val systemApps: SnapshotStateList<AppInfo> = mutableStateListOf()

    fun loadApps() {
        userApps.clear()
        systemApps.clear()

        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (app in apps) {
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val appName = pm.getApplicationLabel(app).toString()
            val appInfo = AppInfo(app.packageName, appName, isSystem)

            if (isSystem) {
                systemApps.add(appInfo)
            } else {
                userApps.add(appInfo)
            }
        }
    }
}
