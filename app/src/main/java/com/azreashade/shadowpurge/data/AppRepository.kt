package com.azreashade.shadowpurge.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class AppInfo(val appName: String, val packageName: String)

class AppRepository(private val context: Context) {

    fun getUserApps(): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { app ->
            (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }.map {
            AppInfo(pm.getApplicationLabel(it).toString(), it.packageName)
        }.sortedBy { it.appName.lowercase() }
    }

    fun getSystemApps(): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { app ->
            (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }.map {
            AppInfo(pm.getApplicationLabel(it).toString(), it.packageName)
        }.sortedBy { it.appName.lowercase() }
    }
}
