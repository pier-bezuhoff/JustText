package com.pierbezuhoff.justtext.data

import android.accounts.NetworkErrorException
import androidx.compose.runtime.Immutable
import com.pierbezuhoff.justtext.flatMapCatching2
import io.ktor.client.HttpClient
import io.ktor.client.call.DoubleReceiveException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.io.encoding.Base64

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

    private val client = HttpClient(Android) {
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.HEADERS
        }
        followRedirects = true
    }

    private suspend fun get(): Result<ByteArray> {
        return runCatchingOnlyNet {
            val response = client.use {
                client.get(endpoint)
            }
            println(response)
            if (response.status.isSuccess())
                response.bodyAsBytes()
            else
                throw NetworkErrorException("Status: ${response.status} from $this")
        }
    }

    private suspend fun post(text: String): Result<Unit> {
        return runCatchingOnlyNet {
            val response = client.use {
                client.post(endpoint) {
                    contentType(ContentType.Text.Plain)
                    setBody(text)
                }
            }
            if (response.status.isSuccess())
                Unit
            else
                throw NetworkErrorException("Status: ${response.status} from $response")
        }
    }

    suspend fun pull(): Result<String> {
        return get().flatMapCatching2<ByteArray, String,
                IllegalArgumentException, IndexOutOfBoundsException> { base64 ->
            val encryptedPackage = Base64.decode(base64)
            TextEncryption.decryptWithPassword(encryptedPackage, password)
        }
    }

    suspend fun push(text: String): Result<Unit> {
        return TextEncryption.encryptWithPassword(text, password)
            .flatMapCatching2<ByteArray, Unit,
                    IllegalArgumentException, IndexOutOfBoundsException> { encryptedPackage ->
                val base64 = Base64.encode(encryptedPackage)
                post(base64)
//                println(base64)
//                Result.success(Unit)
            }
    }
}

private inline fun <T, R> T.runCatchingOnlyNet(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        when (e) {
            is IOException,
            is ResponseException,
            is NoTransformationFoundException,
            is DoubleReceiveException,
            is SerializationException,
                 -> Result.failure(e)
            else -> throw e
        }
    }
}
