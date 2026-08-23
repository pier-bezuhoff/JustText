package com.pierbezuhoff.justtext.data

import android.content.Context

class TextFileRepo(
    private val applicationContext: Context,
) {

    fun load(): Result<String> =
        runCatching {
            applicationContext.openFileInput(FILENAME)
                .bufferedReader()
                .useLines { lines ->
                    val text = lines.joinToString("\n")
                    text
                }
        }.onFailure { e ->
            // triggers on first install
            println("No $FILENAME found")
        }

    fun save(text: String): Result<Unit> =
        runCatching {
            applicationContext.openFileOutput(FILENAME, Context.MODE_PRIVATE).use {
                it.write(text.toByteArray())
            }
            println("text saved (${text.length} characters)")
        }.onFailure { it.printStackTrace() }

    companion object {
        private const val FILENAME = "saved-text.txt"
    }
}