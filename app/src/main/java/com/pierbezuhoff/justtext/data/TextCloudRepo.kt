package com.pierbezuhoff.justtext.data

import android.accounts.NetworkErrorException
import androidx.compose.runtime.Immutable
import com.pierbezuhoff.justtext.byteArrayOf
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.io.encoding.Base64

val ENCRYPTED_ENDPOINT_PACKAGE =
    byteArrayOf(
        15, 145, 5, 32, 248, 55, 101, 40, 14, 63, 165, 70, 56, 46, 151, 103,
    ) + byteArrayOf(
        208, 155, 157, 2, 39, 163, 175, 80, 2, 64, 151, 98,
    ) + byteArrayOf(
        176, 13, 167, 68, 144, 242, 7, 241, 196, 250, 252, 194, 51, 138,
        102, 89, 179, 192, 229, 212, 207, 162, 223, 168, 136, 61, 189, 249,
        19, 164, 177, 93, 102, 135, 142, 146, 169, 38, 88, 137, 58, 63, 29,
        112, 1, 135, 67, 250, 97, 253, 89, 214, 130, 43, 71, 15, 96, 196,
        243, 242, 84, 149, 23, 118, 202, 173, 226, 37, 12, 138, 28, 128,
        152, 53, 28, 38, 156, 129, 80, 231, 37, 82, 29, 221, 230, 31, 113,
        39, 123, 27, 87, 44, 175, 148, 96, 230, 198, 171, 47, 224, 59, 254,
        204, 146, 177, 5, 151, 255, 44, 28, 154, 48, 171, 219, 26, 94, 20,
        168, 97, 132, 81, 56, 173, 152, 223, 37, 150, 39, 53, 70, 193, 208,
        72, 183, 32, 1, 101, 108, 21, 74, 239, 150, 234, 29, 181, 25, 174,
        63, 107, 108, 43, 243, 218, 124, 95, 202, 77, 72, 195, 9, 152, 119,
        245, 128, 198, 173, 169, 196, 12, 50, 211, 255, 165,
    )

class TextCloudRepo(
    private val endpoint: String,
    private val password: String,
) {
    @Immutable
    data class Properties(
        val endpoint: String,
        val password: String,
    )

    constructor(properties: Properties) : this(
        properties.endpoint, properties.password
    )

    private val client = HttpClient() {
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.HEADERS
        }
    }

    private suspend fun get(): Result<ByteArray> {
        return runCatching {
            client.use {
                client.get(endpoint)
            }
        }.mapCatching { response ->
            println(response)
            if (response.status.isSuccess())
                response.bodyAsBytes()
            else
                throw NetworkErrorException("Status: ${response.status} from $this")
        }
    }

    private suspend fun post(text: String): Result<Unit> {
        return runCatching {
            client.use {
                client.post(endpoint) {
                    setBody(text)
                }
            }
        }.mapCatching { response ->
            if (response.status.isSuccess())
                Unit
            else
                throw NetworkErrorException("Status: ${response.status} from $response")
        }
    }

    suspend fun pull(): Result<String> = withContext(Dispatchers.IO) {
        val result = get().mapCatching { base64 ->
//            yield()
            val encryptedPackage = Base64.decode(base64)
//            yield()
            TextEncryption.decryptWithPassword(encryptedPackage, password)
        }
        result
    }

    suspend fun push(text: String): Result<Unit> {
        // am scared
//        return post(text)
        return Result.success(Unit)
    }

}
