package com.pierbezuhoff.justtext.data

import android.content.Context
import com.pierbezuhoff.justtext.runCatchingOnly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

class TextFileRepo(
    private val applicationContext: Context,
) {

    suspend fun load(): Result<String> = withContext(Dispatchers.IO) {
        runCatchingOnly({ it is IOException || it is SecurityException }) {
            applicationContext.openFileInput(FILENAME)
                ?.bufferedReader()
                ?.useLines { lines ->
                    val text = lines.joinToString("\n")
                    text
                }
                ?: throw IOException("null openFileInput($FILENAME)")
        }
    }

    suspend fun save(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatchingOnly({ it is IOException || it is SecurityException }) {
            applicationContext.openFileOutput(FILENAME, Context.MODE_PRIVATE)?.use {
                it.write(text.toByteArray())
            } ?: throw IOException("null openFileInput($FILENAME)")
            println("text saved (${text.length} characters)")
        }
    }

    companion object {
        private const val FILENAME = "saved-text.txt"
    }
}