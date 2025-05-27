package com.pierbezuhoff.justtext.ui

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.pierbezuhoff.justtext.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class JustTextViewModel(
    private val applicationContext: Context,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {
    val initialTextFlow = MutableStateFlow("...")
    val uiStateFlow = MutableStateFlow(UiState(""))

    private val backgroundImageFile = File(applicationContext.filesDir, "background-image.jpg")
    val backgroundImageUri = MutableStateFlow<Uri?>(null)

    init {
        viewModelScope.launch {
            loadInitialTextFromDataStore()
            loadBackgroundImageFromFile()
        }
    }

    private suspend fun loadInitialTextFromDataStore() {
        val dataStoredText = dataStore.data.map { it[TEXT_KEY] }.first()
        if (dataStoredText != null) {
            initialTextFlow.update { dataStoredText }
        }
    }

    private fun loadBackgroundImageFromFile() {
        if (backgroundImageFile.exists()) {
            backgroundImageUri.value = Uri.fromFile(backgroundImageFile)
        }
    }

    fun updateText(newText: String) {
        uiStateFlow.update { it.copy(text = newText) }
    }

    fun saveToDatastore() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[TEXT_KEY] = uiStateFlow.value.text
            }
        }
    }

    fun loadNewBackgroundImage(uri: Uri) {
        backgroundImageUri.value = uri
        viewModelScope.launch(Dispatchers.IO) {
            try {
                applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                    backgroundImageFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        // reference: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                //val savedStateHandle = extras.createSavedStateHandle()
                return JustTextViewModel(
                    application.applicationContext,
                    application.dataStore,
                ) as T
            }
        }

        private val TEXT_KEY = stringPreferencesKey("text")
    }
}