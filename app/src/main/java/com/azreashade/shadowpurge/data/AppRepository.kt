package com.azreashade.shadowpurge.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class AppInfo(
    val appName: String,
    val packageName: String
)

class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    /** Returns list of installed user apps (non-system) */
    fun getUserApps(): List<AppInfo> {
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { isUserApp(it) }
            .map { AppInfo(pm.getApplicationLabel(it).toString(), it.packageName) }
            .sortedBy { it.appName.lowercase() }
    }

    /** Returns list of installed system apps */
    fun getSystemApps(): List<AppInfo> {
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { !isUserApp(it) }
            .map { AppInfo(pm.getApplicationLabel(it).toString(), it.packageName) }
            .sortedBy { it.appName.lowercase() }
    }

    /** Helper: checks if app is user-installed */
    private fun isUserApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
    }
}
