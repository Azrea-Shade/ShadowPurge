package com.azreashade.shadowpurge.data

import android.content.Context
import android.content.SharedPreferences

class ExclusionManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "shadow_purge_prefs"
        private const val KEY_EXCLUDED_APPS = "excluded_apps"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var excludedApps: MutableSet<String> = loadExcludedApps()

    private fun loadExcludedApps(): MutableSet<String> {
        return prefs.getStringSet(KEY_EXCLUDED_APPS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveExcludedApps() {
        prefs.edit().putStringSet(KEY_EXCLUDED_APPS, excludedApps).apply()
    }

    fun isExcluded(packageName: String): Boolean {
        return excludedApps.contains(packageName)
    }

    fun toggleExclusion(packageName: String) {
        if (excludedApps.contains(packageName)) {
            excludedApps.remove(packageName)
        } else {
            excludedApps.add(packageName)
        }
        saveExcludedApps()
    }

    fun getAllExcluded(): Set<String> = excludedApps.toSet()

    fun setExcludedApps(newSet: Set<String>) {
        excludedApps = newSet.toMutableSet()
        saveExcludedApps()
    }
}
