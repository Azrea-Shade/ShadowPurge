package com.azreashade.shadowpurge.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.getSystemService

@Composable
fun UsageAccessPrompt(
    context: Context = LocalContext.current,
    onDismiss: () -> Unit = {}
) {
    val hasUsageAccess = remember { mutableStateOf(false) }

    // Check usage access permission
    LaunchedEffect(Unit) {
        hasUsageAccess.value = checkUsageAccess(context)
    }

    if (!hasUsageAccess.value) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Usage Access Required") },
            text = {
                Text(
                    "Shadow Purge needs Usage Access permission to monitor and close apps. " +
                    "Please grant this permission in the next screen."
                )
            },
            confirmButton = {
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun checkUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}
