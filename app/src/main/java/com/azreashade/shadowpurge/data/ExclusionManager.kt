package com.azreashade.shadowpurge.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class ExclusionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("exclusions", Context.MODE_PRIVATE)

    val excludedPackages: SnapshotStateList<String> = mutableStateListOf()

    init {
        loadExclusions()
    }

    private fun loadExclusions() {
        val saved = prefs.getStringSet("excluded_packages", emptySet()) ?: emptySet()
        excludedPackages.clear()
        excludedPackages.addAll(saved)
    }

    fun toggleExclusion(packageName: String) {
        if (excludedPackages.contains(packageName)) {
            excludedPackages.remove(packageName)
        } else {
            excludedPackages.add(packageName)
        }
        saveExclusions()
    }

    private fun saveExclusions() {
        prefs.edit().putStringSet("excluded_packages", excludedPackages.toSet()).apply()
    }

    fun isExcluded(packageName: String): Boolean {
        return excludedPackages.contains(packageName)
    }
}
