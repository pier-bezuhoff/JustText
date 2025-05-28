package com.pierbezuhoff.justtext.ui

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    val uiStateFlow = MutableStateFlow(UiState(
        text = ".",
        textColor = Color.White.value,
        textFieldBackgroundColor = Color.DarkGray.copy(alpha = 0.4f).value,
        backgroundColor = Color.Black.value,
    ))
    val initialTextFlow = MutableStateFlow("...")
    private val backgroundImageFile = File(applicationContext.filesDir, BACKGROUND_IMAGE_FILENAME)
    val backgroundImageUri = MutableStateFlow<Uri?>(null)

    init {
        viewModelScope.launch {
            loadInitialTextFromFile()
            loadBackgroundImageFromFile()
        }
    }

    private fun loadInitialTextFromFile() {
        applicationContext.openFileInput(SAVED_TEXT_FILENAME).bufferedReader().useLines { lines ->
            val text = lines.fold("") { some, nextLine ->
                "$some\n$nextLine"
            }
            initialTextFlow.update { text }
            updateText(text)
            println("text loaded")
        }
    }

    private suspend fun loadInitialTextFromDataStore() {
        val dataStoredText = dataStore.data.map { it[TEXT_KEY] }.first()
        if (dataStoredText != null) {
            initialTextFlow.update { dataStoredText }
            updateText(dataStoredText)
        } else {
            println("no stored text found")
        }
    }

    private fun loadBackgroundImageFromFile() {
        if (backgroundImageFile.exists()) {
            backgroundImageUri.value = Uri.fromFile(backgroundImageFile)
        }
    }

    fun setDefaultColors(
        textColor: Color,
        textFieldBackgroundColor: Color,
        backgroundColor: Color,
    ) {
        uiStateFlow.update {
            it.copy(
                textColor = textColor.value,
                textFieldBackgroundColor = textFieldBackgroundColor.value,
                backgroundColor = backgroundColor.value,
            )
        }
    }

    fun updateText(newText: String) {
        uiStateFlow.update { it.copy(text = newText) }
    }

    fun persistState() {
        saveTextToFile()
    }

    fun saveTextToDatastore() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[TEXT_KEY] = uiStateFlow.value.text
            }
        }
    }

    fun saveTextToFile() {
        try {
            val text = uiStateFlow.value.text
            applicationContext.openFileOutput(SAVED_TEXT_FILENAME, Context.MODE_PRIVATE).use {
                it.write(text.toByteArray())
            }
            println("text saved")
        } catch (e: Exception) {
            e.printStackTrace()
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
        private const val SAVED_TEXT_FILENAME = "saved-text.txt"
        private const val BACKGROUND_IMAGE_FILENAME = "background-image.jpg"
    }
}