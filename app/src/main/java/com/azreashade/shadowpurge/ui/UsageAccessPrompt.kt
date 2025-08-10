package com.azreashade.shadowpurge.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.content.Context
import com.azreashade.shadowpurge.utils.UsageAccessUtils

@Composable
fun UsageAccessPrompt(context: Context, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Usage Access Required") },
        text = {
            Column {
                Text("Shadow Purge requires Usage Access permission to monitor running apps and function properly.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please grant this permission in the settings.")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                UsageAccessUtils.requestUsageAccess(context)
            }) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
