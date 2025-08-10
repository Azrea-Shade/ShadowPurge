package com.azreashade.shadowpurge.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("User Apps", "System Apps")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shadow Purge") }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
                0 -> UserAppsTab()
                1 -> SystemAppsTab()
            }
        }
    }
}

@Composable
fun UserAppsTab() {
    Text("List of user apps that can be killed will appear here.")
}

@Composable
fun SystemAppsTab() {
    Text("List of system apps that cannot be killed will appear here.")
}
