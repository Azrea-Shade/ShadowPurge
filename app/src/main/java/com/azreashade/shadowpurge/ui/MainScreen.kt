package com.azreashade.shadowpurge.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.azreashade.shadowpurge.AppKillService
import com.azreashade.shadowpurge.data.AppInfo
import com.azreashade.shadowpurge.data.AppRepository
import com.azreashade.shadowpurge.data.ExclusionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val appRepository = remember { AppRepository(context) }
    val exclusionManager = remember { ExclusionManager(context) }

    var usageAccessGranted by remember { mutableStateOf(false) }

    // Check usage access permission on composition
    LaunchedEffect(Unit) {
        usageAccessGranted = checkUsageAccess(context)
    }

    var selectedTab by remember { mutableStateOf(0) } // 0=user, 1=system

    // Apps lists
    var userApps by remember { mutableStateOf(listOf<AppInfo>()) }
    var systemApps by remember { mutableStateOf(listOf<AppInfo>()) }

    // Excluded package names
    var exclusions by remember { mutableStateOf(setOf<String>()) }

    // Kill interval options
    val intervals = listOf(15, 30, 60)
    var selectedInterval by remember { mutableStateOf(30) }

    // Service running state (simple toggle state)
    var serviceRunning by remember { mutableStateOf(false) }

    // File picker launcher for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val success = importExclusionsFromUri(context, uri, exclusionManager)
                if (success) {
                    exclusions = exclusionManager.loadExclusions()?.toSet() ?: emptySet()
                }
            }
        }
    )

    // Load apps and exclusions when screen opens or tab changes
    LaunchedEffect(selectedTab) {
        userApps = appRepository.getUserApps()
        systemApps = appRepository.getSystemApps()
        exclusions = exclusionManager.loadExclusions()?.toSet() ?: emptySet()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Shadow Purge") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                if (!usageAccessGranted) {
                    UsageAccessPrompt { openUsageAccessSettings(context) }
                    return@Column
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("User Apps", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("System Apps", modifier = Modifier.padding(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Crossfade(targetState = selectedTab) { tab ->
                    val apps = if (tab == 0) userApps else systemApps
                    AppList(
                        apps = apps,
                        exclusions = exclusions,
                        onToggleExclude = { pkg, excluded ->
                            exclusions = if (excluded) {
                                exclusions - pkg
                            } else {
                                exclusions + pkg
                            }
                            exclusionManager.saveExclusions(exclusions.toList())
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Kill Interval:")
                    DropdownMenuWrapper(
                        options = intervals.map { "$it minutes" },
                        selectedIndex = intervals.indexOf(selectedInterval),
                        onSelect = { selectedInterval = intervals[it] }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            startAppKillService(context, selectedInterval)
                            serviceRunning = true
                        },
                        enabled = !serviceRunning
                    ) {
                        Text("Start Service")
                    }
                    Button(
                        onClick = {
                            stopAppKillService(context)
                            serviceRunning = false
                        },
                        enabled = serviceRunning
                    ) {
                        Text("Stop Service")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { exportExclusions(context, exclusionManager) }
                    ) {
                        Text("Export Exclusions")
                    }
                    Button(
                        onClick = { importLauncher.launch("*/*") }
                    ) {
                        Text("Import Exclusions")
                    }
                }
            }
        }
    )
}

@Composable
fun UsageAccessPrompt(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Usage Access permission is required.")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onOpenSettings) {
            Text("Open Settings")
        }
    }
}

@Composable
fun AppList(
    apps: List<AppInfo>,
    exclusions: Set<String>,
    onToggleExclude: (String, Boolean) -> Unit
) {
    LazyColumn {
        items(apps) { app ->
            val excluded = exclusions.contains(app.packageName)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = excluded,
                    onCheckedChange = { onToggleExclude(app.packageName, excluded) }
                )
                Text(app.appName, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun DropdownMenuWrapper(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(options[selectedIndex])
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Helpers for usage access permission
fun checkUsageAccess(context: Context): Boolean {
    val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOpsManager.checkOpNoThrow(
        "android:get_usage_stats",
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

fun openUsageAccessSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

// Start/stop service helpers
fun startAppKillService(context: Context, intervalMinutes: Int) {
    val intent = Intent(context, AppKillService::class.java).apply {
        putExtra(AppKillService.EXTRA_INTERVAL_MINUTES, intervalMinutes)
    }
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

fun stopAppKillService(context: Context) {
    val intent = Intent(context, AppKillService::class.java)
    context.stopService(intent)
}

// Export exclusions to Downloads folder (simple example)
fun exportExclusions(context: Context, exclusionManager: ExclusionManager) {
    val exportFile = context.getExternalFilesDir(null)?.resolve("exclusions_export.json")
    if (exportFile != null) {
        val success = exclusionManager.exportExclusions(exportFile)
        // In a real app, you'd show Toast or Snackbar for success/failure here
    }
}

// Import exclusions from URI
fun importExclusionsFromUri(context: Context, uri: Uri, exclusionManager: ExclusionManager): Boolean {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return false
        val content = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = org.json.JSONArray(content)
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        exclusionManager.saveExclusions(list)
        true
    } catch (e: Exception) {
        false
    }
}