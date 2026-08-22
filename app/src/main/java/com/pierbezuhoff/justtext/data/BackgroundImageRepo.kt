package com.pierbezuhoff.justtext.data

import android.content.Context
import android.net.Uri
import okio.FileNotFoundException
import java.io.File

class BackgroundImageRepo(
    private val applicationContext: Context,
) {
    private val file = File(applicationContext.filesDir, FILENAME)

    private fun getTaggedUri(): TaggedUri =
        TaggedUri(Uri.fromFile(file))

    fun load(): Result<TaggedUri> {
        if (file.exists()) {
            return Result.success(getTaggedUri())
        }
        return Result.failure(FileNotFoundException())
    }

    fun loadAndOverwrite(uri: Uri): Result<TaggedUri> =
        runCatching {
            applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
                val newTaggedUri = getTaggedUri()
                println("finished copying new bg image $uri -> $newTaggedUri")
                newTaggedUri
            } ?: throw Error("cannot copy new bg image")
        }.onFailure { e ->
            e.printStackTrace()
            println("failed to copy new bg image")
        }

    companion object {
        private const val FILENAME = "background-image.jpg"
    }
}