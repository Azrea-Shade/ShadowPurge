package com.azreashade.shadowpurge.ui

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.util.Log

fun checkUsageAccess(context: Context): Boolean {
    try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        Log.e("UsageAccessUtils", "Error checking usage access", e)
        return false
    }
}
