package com.azreashade.shadowpurge.data

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ExclusionFileManager(private val context: Context) {

    // Save exclusions to a file Uri
    fun exportExclusions(exclusions: Set<String>, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                BufferedWriter(OutputStreamWriter(os)).use { writer ->
                    exclusions.forEach { pkg ->
                        writer.write(pkg)
                        writer.newLine()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Load exclusions from a file Uri
    fun importExclusions(uri: Uri): Set<String>? {
        return try {
            val result = mutableSetOf<String>()
            context.contentResolver.openInputStream(uri)?.use { ins ->
                BufferedReader(InputStreamReader(ins)).useLines { lines ->
                    lines.forEach { line ->
                        if (line.isNotBlank()) {
                            result.add(line.trim())
                        }
                    }
                }
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
