package com.azreashade.shadowpurge.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.azreashade.shadowpurge.data.AppInfo
import com.azreashade.shadowpurge.data.AppRepository
import com.azreashade.shadowpurge.data.ExclusionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("User Apps", "System Apps")

    val appRepository = remember { AppRepository(context) }
    val exclusionManager = remember { ExclusionManager(context) }

    LaunchedEffect(Unit) {
        appRepository.loadApps()
    }

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
            when (selectedTab) {
                0 -> AppList(appRepository.userApps, exclusionManager)
                1 -> AppList(appRepository.systemApps, exclusionManager)
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
