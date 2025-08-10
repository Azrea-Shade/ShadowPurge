package com.azreashade.shadowpurge.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean
)

class AppRepository(private val context: Context) {

    var userApps: List<AppInfo> = emptyList()
        private set

    var systemApps: List<AppInfo> = emptyList()
        private set

    suspend fun loadApps() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val userAppList = mutableListOf<AppInfo>()
        val systemAppList = mutableListOf<AppInfo>()

        for (app in packages) {
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val appInfo = AppInfo(
                appName = pm.getApplicationLabel(app).toString(),
                packageName = app.packageName,
                isSystemApp = isSystem
            )
            if (isSystem) {
                systemAppList.add(appInfo)
            } else {
                userAppList.add(appInfo)
            }
        }

        userApps = userAppList.sortedBy { it.appName.lowercase() }
        systemApps = systemAppList.sortedBy { it.appName.lowercase() }
    }
}
