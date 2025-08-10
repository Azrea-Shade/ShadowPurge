package com.azreashade.shadowpurge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.azreashade.shadowpurge.ui.theme.ShadowPurgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShadowPurgeTheme {
                // TODO: Add your app UI here
            }
        }
    }
}
