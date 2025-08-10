package com.azreashade.shadowpurge.ui

import com.azreashade.shadowpurge.ui.checkUsageAccess
import com.azreashade.shadowpurge.ui.UsageAccessPrompt
import com.azreashade.shadowpurge.ui.checkUsageAccess
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.azreashade.shadowpurge.AppKillService
import com.azreashade.shadowpurge.data.AppInfo
import com.azreashade.shadowpurge.data.AppRepository
import com.azreashade.shadowpurge.data.ExclusionFileManager
import com.azreashade.shadowpurge.data.ExclusionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("User Apps", "System Apps")

    val appRepository = remember { AppRepository(context) }
    val exclusionManager = remember { ExclusionManager(context) }

    var killInterval by remember { mutableStateOf(30) } // default 30 mins
    var serviceRunning by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }

    // Usage Access permission check and prompt
    val showUsagePrompt = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showUsagePrompt.value = !checkUsageAccess(context)
    }

    if (showUsagePrompt.value) {
        UsageAccessPrompt(
            context = context,
            onDismiss = { showUsagePrompt.value = false }
        )
    }

    LaunchedEffect(Unit) {
        appRepository.loadApps()
    }

    // SAF launchers for export/import
    val fileManager = remember { ExclusionFileManager(context) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val success = fileManager.exportExclusions(exclusionManager.getAllExcluded(), uri)
                    message = if (success) "Export successful!" else "Export failed."
                }
            } else {
                message = "Export cancelled."
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val imported = fileManager.importExclusions(uri)
                    if (imported != null) {
                        exclusionManager.setExcludedApps(imported)
                        message = "Import successful!"
                    } else {
                        message = "Import failed."
                    }
                }
            } else {
                message = "Import cancelled."
            }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shadow Purge") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> AppList(appRepository.userApps, exclusionManager)
                1 -> AppList(appRepository.systemApps, exclusionManager)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Kill Interval:")
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                listOf(15, 30, 60).forEach { minutes ->
                    Button(
                        onClick = { killInterval = minutes },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        colors = if (killInterval == minutes) ButtonDefaults.buttonColors()
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("$minutes min")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Button(
                    onClick = {
                        if (!serviceRunning) {
                            val intent = Intent(context, AppKillService::class.java).apply {
                                putExtra("interval_minutes", killInterval.toLong())
                            }
                            context.startForegroundService(intent)
                            serviceRunning = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (serviceRunning) {
                            val intent = Intent(context, AppKillService::class.java)
                            context.stopService(intent)
                            serviceRunning = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { exportLauncher.launch("excluded_apps.txt") }) {
                    Text("Export Exclusions")
                }

                Button(onClick = { importLauncher.launch(arrayOf("text/plain")) }) {
                    Text("Import Exclusions")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (message.isNotEmpty()) {
                Text(text = message)
            }
        }
    }
}

@Composable
fun AppList(apps: List<AppInfo>, exclusionManager: ExclusionManager) {
    LazyColumn {
        items(apps) { app ->
            AppRow(app, exclusionManager)
        }
    }
}

@Composable
fun AppRow(app: AppInfo, exclusionManager: ExclusionManager) {
    val isExcluded = exclusionManager.isExcluded(app.packageName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { exclusionManager.toggleExclusion(app.packageName) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = app.appName)
        Checkbox(
            checked = isExcluded,
            onCheckedChange = { exclusionManager.toggleExclusion(app.packageName) }
        )
    }
}
