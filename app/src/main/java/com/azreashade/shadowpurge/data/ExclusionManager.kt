package com.azreashade.shadowpurge.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

class ExclusionManager(private val context: Context) {

    private val fileName = "exclusions.json"

    /** Save exclusions list to internal storage */
    fun saveExclusions(excludedPackages: List<String>) {
        try {
            val jsonArray = JSONArray()
            excludedPackages.forEach { jsonArray.put(it) }
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { fos ->
                fos.write(jsonArray.toString().toByteArray())
            }
        } catch (e: Exception) {
            Log.e("ExclusionManager", "Error saving exclusions: ${e.message}")
        }
    }

    /** Load exclusions list from internal storage */
    fun loadExclusions(): List<String>? {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return emptyList()
            val content = file.readText()
            val jsonArray = JSONArray(content)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            Log.e("ExclusionManager", "Error loading exclusions: ${e.message}")
            null
        }
    }

    /** Export exclusions to given file (e.g., external storage) */
    fun exportExclusions(exportFile: File): Boolean {
        return try {
            val exclusions = loadExclusions() ?: emptyList()
            val jsonArray = JSONArray()
            exclusions.forEach { jsonArray.put(it) }
            exportFile.writeText(jsonArray.toString())
            true
        } catch (e: Exception) {
            Log.e("ExclusionManager", "Error exporting exclusions: ${e.message}")
            false
        }
    }

    /** Import exclusions from given file */
    fun importExclusions(importFile: File): Boolean {
        return try {
            if (!importFile.exists()) return false
            val content = importFile.readText()
            val jsonArray = JSONArray(content)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            saveExclusions(list)
            true
        } catch (e: Exception) {
            Log.e("ExclusionManager", "Error importing exclusions: ${e.message}")
            false
        }
    }
}
