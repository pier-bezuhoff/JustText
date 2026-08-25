package com.pierbezuhoff.justtext.data

import android.content.Context
import com.pierbezuhoff.justtext.runCatching1
import kotlinx.io.IOException

class TextFileRepo(
    private val applicationContext: Context,
) {

    fun load(): Result<String> =
        runCatching1<String, IOException> {
            applicationContext.openFileInput(FILENAME)
                ?.bufferedReader()
                ?.useLines { lines ->
                    val text = lines.joinToString("\n")
                    text
                }
                ?: throw IOException("null openFileInput($FILENAME)")
        }

    fun save(text: String): Result<Unit> =
        runCatching1<Unit, IOException> {
            applicationContext.openFileOutput(FILENAME, Context.MODE_PRIVATE)?.use {
                it.write(text.toByteArray())
            } ?: throw IOException("null openFileInput($FILENAME)")
            println("text saved (${text.length} characters)")
        }

    companion object {
        private const val FILENAME = "saved-text.txt"
    }
}