package com.pierbezuhoff.justtext.ui

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.pierbezuhoff.justtext.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
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
    ))
    val initialTextFlow = MutableStateFlow<String>("...")
    val initialCursorLocationFlow = MutableStateFlow<Int>(0)
    private val backgroundImageFile = File(applicationContext.filesDir, BACKGROUND_IMAGE_FILENAME)
    val backgroundImageUri = MutableStateFlow<Uri?>(null)

    init {
        viewModelScope.launch {
            loadInitialTextFromFile()
            loadBackgroundImageFromFile()
            loadDataStoreData()
        }
    }

    private fun loadInitialTextFromFile() {
        applicationContext.openFileInput(SAVED_TEXT_FILENAME).bufferedReader().useLines { lines ->
            val text = lines.joinToString("\n")
            initialTextFlow.update { text }
            setText(text)
        }
    }

    private suspend fun loadDataStoreData() {
        dataStore.data.firstOrNull()?.let { data ->
            data[IMAGE_BACKGROUND_COLOR_KEY]?.toULong()?.let { color ->
                println("loaded text color $color")
                uiStateFlow.update { it.copy(imageBackgroundColor = color) }
            }
            data[TEXT_BACKGROUND_COLOR_KEY]?.toULong()?.let { color ->
                println("loaded text bg color $color")
                uiStateFlow.update { it.copy(textBackgroundColor = color) }
            }
            data[TEXT_COLOR_KEY]?.toULong()?.let { color ->
                println("loaded image bg color $color")
                uiStateFlow.update { it.copy(imageBackgroundColor = color) }
            }
            data[CURSOR_LOCATION]?.let { cursorLocation ->
                initialCursorLocationFlow.update { cursorLocation }
                uiStateFlow.update { it.copy(cursorLocation = cursorLocation) }
            }
        }
    }

    private fun loadBackgroundImageFromFile() {
        if (backgroundImageFile.exists()) {
            backgroundImageUri.value = Uri.fromFile(backgroundImageFile)
        }
    }

    fun setCursorLocation(cursorLocation: Int) {
        uiStateFlow.update {
            it.copy(cursorLocation = cursorLocation)
        }
    }

    fun setTextColor(color: Color) {
        uiStateFlow.update {
            it.copy(textColor = color.value)
        }
    }

    fun setTextBackgroundColor(color: Color) {
        uiStateFlow.update {
            it.copy(textBackgroundColor = color.value)
        }
    }

    fun setImageBackgroundColor(color: Color) {
        uiStateFlow.update {
            it.copy(imageBackgroundColor = color.value)
        }
    }

    fun setText(text: String) {
        uiStateFlow.update { it.copy(text = text) }
    }

    fun persistState() {
        saveTextToFile()
        saveDatastoreData()
    }

    fun saveDatastoreData() {
        runBlocking {
            val uiState = uiStateFlow.value
            dataStore.edit { preferences ->
                uiState.textColor?.let { color ->
                    preferences[TEXT_COLOR_KEY] = color.toLong()
                    println("saved tc $color")
                }
                uiState.textBackgroundColor?.let { color ->
                    preferences[TEXT_BACKGROUND_COLOR_KEY] = color.toLong()
                    println("saved tbc $color")
                }
                uiState.imageBackgroundColor?.let { color ->
                    preferences[IMAGE_BACKGROUND_COLOR_KEY] = color.toLong()
                    println("saved ibc $color")
                }
                uiState.cursorLocation.let { cursorLocation ->
                    preferences[CURSOR_LOCATION] = cursorLocation
                    println("saved cl $cursorLocation")
                }
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

//        private val TEXT_KEY = stringPreferencesKey("text")
        private val CURSOR_LOCATION = intPreferencesKey("cursor_location")
        private val TEXT_COLOR_KEY = longPreferencesKey("text_color")
        private val TEXT_BACKGROUND_COLOR_KEY = longPreferencesKey("text_background_color")
        private val IMAGE_BACKGROUND_COLOR_KEY = longPreferencesKey("image_background_color")
        private const val SAVED_TEXT_FILENAME = "saved-text.txt"
        private const val BACKGROUND_IMAGE_FILENAME = "background-image.jpg"
    }
}