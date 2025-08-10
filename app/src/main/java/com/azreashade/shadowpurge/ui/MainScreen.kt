package com.azreashade.shadowpurge.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.azreashade.shadowpurge.data.AppInfo
import com.azreashade.shadowpurge.data.AppRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("User Apps", "System Apps")

    val appRepository = remember { AppRepository(context) }
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
                0 -> AppList(appRepository.userApps)
                1 -> AppList(appRepository.systemApps)
            }
        }
    }
}

@Composable
fun AppList(apps: List<AppInfo>) {
    LazyColumn {
        items(apps) { app ->
            AppRow(app)
        }
    }
}

@Composable
fun AppRow(app: AppInfo) {
    Text(
        text = app.appName,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
