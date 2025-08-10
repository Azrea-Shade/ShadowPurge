package com.azreashade.shadowpurge.ui

import com.azreashade.shadowpurge.ui.checkUsageAccess
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.azreashade.shadowpurge.data.AppInfo
import com.azreashade.shadowpurge.data.AppRepository
import com.azreashade.shadowpurge.data.ExclusionManager

@Composable
fun MainScreen(context: Context) {
    var showUsageAccessDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showUsageAccessDialog = !checkUsageAccess(context)
    }

    if (showUsageAccessDialog) {
        UsageAccessPrompt(context) {
            showUsageAccessDialog = false
        }
    } else {
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("User Apps", "System Apps")

        // Load apps on first composition
        val userApps = remember { mutableStateListOf<AppInfo>() }
        val systemApps = remember { mutableStateListOf<AppInfo>() }

        LaunchedEffect(Unit) {
            val repo = AppRepository(context)
            userApps.clear()
            userApps.addAll(repo.getUserApps())
            systemApps.clear()
            systemApps.addAll(repo.getSystemApps())
        }

        // Manage exclusions
        val exclusionManager = remember { ExclusionManager(context) }
        val excludedPackages = remember { mutableStateListOf<String>() }

        LaunchedEffect(Unit) {
            exclusionManager.loadExclusions()?.let {
                excludedPackages.clear()
                excludedPackages.addAll(it)
            }
        }

        // Kill interval selection
        var selectedInterval by remember { mutableStateOf(30) }
        val intervals = listOf(15, 30, 60)

        Scaffold(
            topBar = {
                SmallTopAppBar(title = { Text("Shadow Purge") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val appsToShow = if (selectedTab == 0) userApps else systemApps
                    if (appsToShow.isEmpty()) {
                        Text("No apps to display", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            itemsIndexed(appsToShow) { _, app ->
                                AppListItem(
                                    app = app,
                                    isExcluded = excludedPackages.contains(app.packageName),
                                    onExcludeToggle = { exclude ->
                                        if (exclude) excludedPackages.add(app.packageName)
                                        else excludedPackages.remove(app.packageName)
                                        exclusionManager.saveExclusions(excludedPackages)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Kill interval:", style = MaterialTheme.typography.titleMedium)
                    Row {
                        intervals.forEach { interval ->
                            FilterChip(
                                selected = selectedInterval == interval,
                                onClick = { selectedInterval = interval },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("$interval min")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(onClick = { /* TODO: Start Service with selectedInterval and exclusions */ }, modifier = Modifier.weight(1f)) {
                            Text("Start")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = { /* TODO: Stop Service */ }, modifier = Modifier.weight(1f)) {
                            Text("Stop")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        OutlinedButton(onClick = { /* TODO: Export exclusions */ }, modifier = Modifier.weight(1f)) {
                            Text("Export Exclusions")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedButton(onClick = { /* TODO: Import exclusions */ }, modifier = Modifier.weight(1f)) {
                            Text("Import Exclusions")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun AppListItem(app: AppInfo, isExcluded: Boolean, onExcludeToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(app.appName)
        Checkbox(
            checked = isExcluded,
            onCheckedChange = onExcludeToggle
        )
    }
}
