package com.pierbezuhoff.justtext.data

import androidx.compose.runtime.Immutable
import androidx.datastore.core.Serializer
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.KeysetHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Immutable
@Serializable
data class EncryptedData(
    val noteEndpoint: String? = null,
    val notePassword: String? = null,
) {

    object DatastoreSerializer : Serializer<EncryptedData> {
        override val defaultValue: EncryptedData = EncryptedData()

        override suspend fun readFrom(input: InputStream): EncryptedData {
            return withContext(Dispatchers.IO) {
                val s = input.readBytes().decodeToString()
                JSON_FORMAT.decodeFromString(s)
            }
        }
        override suspend fun writeTo(t: EncryptedData, output: OutputStream) {
            withContext(Dispatchers.IO) {
                val s = JSON_FORMAT.encodeToString(t)
                output.write(s.encodeToByteArray())
            }
        }
    }

    companion object {
        val JSON_FORMAT = Json {
            encodeDefaults = false
        }
    }
}
