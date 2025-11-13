package com.pierbezuhoff.justtext.ui

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.pierbezuhoff.justtext.data.TaggedUri
import com.pierbezuhoff.justtext.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import kotlin.time.Duration.Companion.minutes

// NOTE: VM survives config changes but not OOM-related process kill,
//  but we call VM.persistState in MainActivity.onPause,
//  so the important elements of UiState are saved via dataStore
class JustTextViewModel(
    private val applicationContext: Context,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {
    // alternatively we could fuse textFlow, datastore.data flow and transientUIStateFlow into uiStateFlow
    private val _uiStateFlow = MutableStateFlow(UiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    private val _backgroundImageUri = MutableStateFlow<TaggedUri?>(null)
    val backgroundImageUri: StateFlow<TaggedUri?> = _backgroundImageUri.asStateFlow()

    private val backgroundImageFile =
        File(applicationContext.filesDir, BACKGROUND_IMAGE_FILENAME)

    private val periodicSaveIsOn = MutableStateFlow(false)
    private var periodicSaveJob: Job? = null

    fun startLoadingData() {
        println("ViewModel.startLoadingData")
        viewModelScope.launch {
            loadInitialTextFromFile()
            loadBackgroundImageFromFile()
            loadDataStoreData()
            _uiStateFlow.update { it.copy(loadedFromDisk = true) }
            println("ViewModel loaded persistent data")
            startPeriodicSave()
        }
    }

    private fun loadInitialTextFromFile() {
        try {
            applicationContext.openFileInput(SAVED_TEXT_FILENAME)
                .bufferedReader()
                .useLines { lines ->
                    val text = lines.joinToString("\n")
                    _uiStateFlow.update {
                        it.copy(
                            tfValue = TextFieldValue(text, TextRange(text.length))
                        )
                    }
                }
        } catch (_: FileNotFoundException) {
            // triggers on first install
            println("No $SAVED_TEXT_FILENAME found")
        }
    }

    // assumption: dataStore has just been loaded
    private suspend fun loadDataStoreData() {
        // .firstOrNull assumes that data flow has 0 or 1 entries
        // otherwise we are getting the oldest one which might be undesirable
        dataStore.data.firstOrNull()?.let { data ->
            val imageBackgroundColor = data[IMAGE_BACKGROUND_COLOR_KEY]?.toULong()
            val textBackgroundColor = data[TEXT_BACKGROUND_COLOR_KEY]?.toULong()
            val textColor = data[TEXT_COLOR_KEY]?.toULong()
            val cursorLocation = data[CURSOR_LOCATION]
            val fontSize = data[FONT_SIZE]
            _uiStateFlow.update { state ->
                state.copy(
                    tfValue = if (cursorLocation == null) {
                        state.tfValue
                    } else {
                        state.tfValue.copy(
                            selection = TextRange(cursorLocation)
                        )
                    },
                    fontSize = fontSize ?: state.fontSize,
                    textColor = textColor ?: state.textColor,
                    textBackgroundColor = textBackgroundColor ?: state.textBackgroundColor,
                    imageBackgroundColor = imageBackgroundColor ?: state.imageBackgroundColor,
                )
            }
        }
    }

    private fun loadBackgroundImageFromFile() {
        if (backgroundImageFile.exists()) {
            _backgroundImageUri.update { getTaggedUri() }
        }
    }

    private fun markSaved() {
        _uiStateFlow.update { it.copy(syncedToDisk = true) }
    }

    private fun markUnsaved() {
        _uiStateFlow.update { it.copy(syncedToDisk = false) }
    }

    fun save() {
        viewModelScope.launch {
            saveDatastoreData()
            withContext(Dispatchers.IO) {
                saveTextToFile()
            }
            markSaved()
            println("saved.")
        }
    }

    private fun startPeriodicSave() {
        if (!periodicSaveIsOn.value) {
            periodicSaveIsOn.update { true }
            periodicSaveJob = viewModelScope.launch(Dispatchers.Default) {
                flow {
                    while (true) {
                        emit(Unit)
                        delay(PERIODIC_SAVE_DELAY)
                    }
                }
                    .collect {
                        println("periodic save")
                        save()
                    }
            }
        }
    }

    fun stopPeriodicSave() {
        periodicSaveJob?.cancel()
        periodicSaveIsOn.update { false }
    }

    private fun getTaggedUri(): TaggedUri =
        TaggedUri(
            Uri.fromFile(backgroundImageFile)
        )

    fun setFontSize(fontSize: Int) {
        _uiStateFlow.update {
            it.copy(fontSize = fontSize)
        }
    }

    fun setTextColor(color: Color) {
        _uiStateFlow.update {
            it.copy(textColor = color.value)
        }
    }

    fun setTextBackgroundColor(color: Color) {
        _uiStateFlow.update {
            it.copy(textBackgroundColor = color.value)
        }
    }

    fun setImageBackgroundColor(color: Color) {
        _uiStateFlow.update {
            it.copy(imageBackgroundColor = color.value)
        }
    }

    fun setTFValue(newTFValue: TextFieldValue) {
        if (newTFValue.text != uiStateFlow.value.tfValue.text) {
            markUnsaved()
        }
        _uiStateFlow.update { it.copy(tfValue = newTFValue) }
    }

    fun setBackgroundImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                    backgroundImageFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                    val newTaggedUri = getTaggedUri()
                    println("finished copying new bg image $uri -> $newTaggedUri")
                    _backgroundImageUri.update { newTaggedUri }
                } ?: println("cannot copy new bg image")
            } catch (e: Exception) {
                println("failed to copy new bg image")
                e.printStackTrace()
            }
        }
    }

    fun persistState() {
        saveTextToFile()
        runBlocking {
            saveDatastoreData()
        }
        markSaved()
    }

    suspend fun saveDatastoreData() {
        val uiState = _uiStateFlow.value
        dataStore.edit { preferences ->
            uiState.textColor?.let { color ->
                preferences[TEXT_COLOR_KEY] = color.toLong()
            }
            uiState.textBackgroundColor?.let { color ->
                preferences[TEXT_BACKGROUND_COLOR_KEY] = color.toLong()
            }
            uiState.imageBackgroundColor?.let { color ->
                preferences[IMAGE_BACKGROUND_COLOR_KEY] = color.toLong()
            }
            uiState.tfValue.selection.start.let { cursorLocation ->
                preferences[CURSOR_LOCATION] = cursorLocation
            }
            uiState.fontSize.let { fontSize ->
                preferences[FONT_SIZE] = fontSize
            }
        }
    }

    fun saveTextToFile() {
        try {
            val text = _uiStateFlow.value.tfValue.text
            applicationContext.openFileOutput(SAVED_TEXT_FILENAME, Context.MODE_PRIVATE).use {
                it.write(text.toByteArray())
            }
            println("text saved (${text.length} characters)")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPeriodicSave()
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

        private val PERIODIC_SAVE_DELAY = 3.minutes

//        private val TEXT_KEY = stringPreferencesKey("text")
        private val CURSOR_LOCATION = intPreferencesKey("cursor_location")
        private val FONT_SIZE = intPreferencesKey("font_size")
        private val TEXT_COLOR_KEY = longPreferencesKey("text_color")
        private val TEXT_BACKGROUND_COLOR_KEY = longPreferencesKey("text_background_color")
        private val IMAGE_BACKGROUND_COLOR_KEY = longPreferencesKey("image_background_color")
        private const val SAVED_TEXT_FILENAME = "saved-text.txt"
        private const val BACKGROUND_IMAGE_FILENAME = "background-image.jpg"
    }
}