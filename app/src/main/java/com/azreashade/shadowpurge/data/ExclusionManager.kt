package com.azreashade.shadowpurge.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class ExclusionManager(private val context: Context) {

    private val filename = "excluded_apps.json"

    fun loadExclusions(): MutableList<String>? {
        return try {
            val file = File(context.filesDir, filename)
            if (!file.exists()) return mutableListOf()
            val json = file.readText()
            Json.decodeFromString<MutableList<String>>(json)
        } catch (e: Exception) {
            Log.e("ExclusionManager", "Failed to load exclusions", e)
            null
        }
    }

    fun saveExclusions(exclusions: List<String>) {
        try {
            val file = File(context.filesDir, filename)
            val json = Json.encodeToString(exclusions)
            file.writeText(json)
        } catch (e: Exception) {
            Log.e("ExclusionManager", "Failed to save exclusions", e)
        }
    }

    fun exportExclusions(exportFile: File): Boolean {
        return try {
            val exclusions = loadExclusions() ?: listOf()
            val json = Json.encodeToString(exclusions)
            exportFile.writeText(json)
            true
        } catch (e: Exception) {
            Log.e("ExclusionManager", "Export failed", e)
            false
        }
    }

    fun importExclusions(importFile: File): Boolean {
        return try {
            if (!importFile.exists()) return false
            val json = importFile.readText()
            val exclusions = Json.decodeFromString<MutableList<String>>(json)
            saveExclusions(exclusions)
            true
        } catch (e: Exception) {
            Log.e("ExclusionManager", "Import failed", e)
            false
        }
    }
}
