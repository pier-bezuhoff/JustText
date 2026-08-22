package com.pierbezuhoff.justtext

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.pierbezuhoff.justtext.data.EncryptedData
import com.pierbezuhoff.justtext.data.buildEncryptedDataStore
import kotlin.getValue

class JustTextApplication : Application() {
    // NOTE: by-preferencesDataStore singleton is created differently
    val encryptedDataStoreInstance: DataStore<EncryptedData> by lazy {
        applicationContext.buildEncryptedDataStore(
            fileName = "encrypted_data.json",
            EncryptedData.DatastoreSerializer,
        )
    }
}

val Context.encryptedDataStore: DataStore<EncryptedData> get() =
    (this.applicationContext as JustTextApplication).encryptedDataStoreInstance

val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "save")

