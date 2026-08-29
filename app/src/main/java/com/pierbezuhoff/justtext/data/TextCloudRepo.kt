package com.pierbezuhoff.justtext.data

import android.accounts.NetworkErrorException
import androidx.compose.runtime.Immutable
import com.pierbezuhoff.justtext.flatMapCatchingOnly
import io.ktor.client.HttpClient
import io.ktor.client.call.DoubleReceiveException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
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
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.io.encoding.Base64

/**
 * @param[password] if it's empty the text is sent untransformed,
 * otherwise the text is encrypted and then encoded as base 64 before sending
 */
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
        properties.endpoint,
        properties.password
    )

    private val client: HttpClient = HttpClient(Android) {
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.HEADERS
        }
        install(HttpRedirect) {
            checkHttpMethod = false
        }
        // post retries can behave erratically with race conditioning get
        // leading to data erasure
        install(HttpRequestRetry) {
            // weird: the more retries i put the more it uses (302) until getting the response...
            // impatient much?
            retryIf(2) { request, response ->
                request.method == HttpMethod.Get &&
                !response.status.isSuccess()
            }
            exponentialDelay()
            modifyRequest { request ->
                request.headers.append("x-retry-count", retryCount.toString())
            }
        }
        install(HttpTimeout)
        followRedirects = true
    }

    private suspend fun get(): Result<ByteArray> {
        return runCatchingOnlyNet {
            val response = client.get(endpoint)
            println(response)
            if (response.status.isSuccess())
                response.bodyAsBytes()
            else
                throw IOException("Status: ${response.status} from $this")
        }
    }

    private suspend fun post(text: String): Result<Unit> {
        return runCatchingOnlyNet {
            val response = client.post(endpoint) {
                contentType(ContentType.Text.Plain)
                setBody(text)
            }
            // NOTE: apps script endpoint usually returns 403 or 405 while actually
            //  uploading successfully, so post mostly 'fails'
            if (response.status.isSuccess())
                Unit
            else
                throw IOException("Status: ${response.status} from $response")
        }
    }

    suspend fun pull(): Result<String> = withContext(Dispatchers.IO) {
        get().flatMapCatchingOnly({
            it is IllegalArgumentException || it is IndexOutOfBoundsException
        }) { bytes ->
            if (password.isEmpty()) {
                Result.success(
                    bytes.decodeToString()
                )
            } else {
                val encryptedPackage = Base64.decode(bytes)
                TextEncryption.decryptWithPassword(encryptedPackage, password)
            }
        }
    }

    suspend fun push(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (password.isEmpty()) {
            post(text)
        } else {
            TextEncryption.encryptWithPassword(text, password)
                .flatMapCatchingOnly({
                    it is IllegalArgumentException || it is IndexOutOfBoundsException
                }) { encryptedPackage ->
                    val base64 = Base64.encode(encryptedPackage)
                    post(base64)
//                Result.success(Unit)
                }
        }
    }

    fun freeResources() {
        client.close()
    }
}

private inline fun <T, R> T.runCatchingOnlyNet(
    block: T.() -> R,
): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        when (e) {
            is IOException,
            is NetworkErrorException,
            is ResponseException,
            is NoTransformationFoundException,
            is DoubleReceiveException,
            is SerializationException ->
                Result.failure(e)
            else -> throw e
        }
    }
}
