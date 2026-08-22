package com.pierbezuhoff.justtext.data

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.deviceProtectedDataStoreFile
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath

// reference for encrypted datastore:
// https://developer.android.com/jetpack/androidx/releases/datastore#1.3.0-alpha07

fun Context.buildKeysetHandle(): KeysetHandle {
    AeadConfig.register()
    return AndroidKeysetManager.Builder()
        .withSharedPref(applicationContext, "keyset", "keyset_prefs")
        .withKeyTemplate(KeyTemplate.createFrom(PredefinedAeadParameters.AES256_GCM))
        .withMasterKeyUri("android-keystore://master_key")
        .build()
        .keysetHandle
}

fun <T> buildAeadSerializer(
    keysetHandle: KeysetHandle,
    wrappedSerializer: Serializer<T>,
    uniqueName: String,
): AeadSerializer<T> {
    return AeadSerializer(
        // Use tink APIs to create an Aead object to encrypt/decrypt data.
        aead = keysetHandle.getPrimitive(
            RegistryConfiguration.get(),
            Aead::class.java,
        ),
        // AeadSerializer can wrap an existing serializer.
        wrappedSerializer = wrappedSerializer,
        // Specify a unique name to prevent a ciphertext swapping attack.
        associatedData = uniqueName.encodeToByteArray(),
    )
}


fun <T> Context.buildEncryptedDataStore(
    fileName: String,
    serializer: Serializer<T>,
    corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    migrations: List<DataMigration<T>> = listOf(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
): DataStore<T> {
    val keysetHandle = buildKeysetHandle()
    val aeadSerializer = buildAeadSerializer(keysetHandle, serializer, fileName)
    val context =
        if (scope.coroutineContext[Job] == null) {
            scope.coroutineContext + Job()
        } else {
            scope.coroutineContext
        }
    return DataStore.Builder(
        storage = OkioStorage(FileSystem.SYSTEM, OkioSerializerWrapper(aeadSerializer)) {
            applicationContext.deviceProtectedDataStoreFile(fileName)
                .absolutePath
                .toPath()
        },
        context = context
    )
        .apply { corruptionHandler?.let { setCorruptionHandler(it) } }
        .addMigrations(migrations)
        .build()
//    return DataStoreFactory.createInDeviceProtectedStorage(
//        context = this,
//        fileName = fileName,
//        serializer = aeadSerializer,
//        corruptionHandler = corruptionHandler,
//        migrations = migrations,
//        scope = scope
//    )
}

internal class OkioSerializerWrapper<T>(private val delegate: Serializer<T>) : OkioSerializer<T> {
    override val defaultValue: T
        get() = delegate.defaultValue

    override suspend fun readFrom(source: BufferedSource): T {
        return delegate.readFrom(source.inputStream())
    }

    override suspend fun writeTo(t: T, sink: BufferedSink) {
        delegate.writeTo(t, sink.outputStream())
    }
}
