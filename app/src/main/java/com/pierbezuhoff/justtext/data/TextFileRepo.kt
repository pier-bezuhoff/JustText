package com.pierbezuhoff.justtext.data

import android.content.Context
import com.pierbezuhoff.justtext.runCatchingOnly
import kotlinx.io.IOException

class TextFileRepo(
    private val applicationContext: Context,
) {

    fun load(): Result<String> =
        runCatchingOnly({ it is IOException }) {
            applicationContext.openFileInput(FILENAME)
                ?.bufferedReader()
                ?.useLines { lines ->
                    val text = lines.joinToString("\n")
                    text
                }
                ?: throw IOException("null openFileInput($FILENAME)")
        }

    fun save(text: String): Result<Unit> =
        runCatchingOnly({ it is IOException }) {
            applicationContext.openFileOutput(FILENAME, Context.MODE_PRIVATE)?.use {
                it.write(text.toByteArray())
            } ?: throw IOException("null openFileInput($FILENAME)")
            println("text saved (${text.length} characters)")
        }

    companion object {
        private const val FILENAME = "saved-text.txt"
    }
}