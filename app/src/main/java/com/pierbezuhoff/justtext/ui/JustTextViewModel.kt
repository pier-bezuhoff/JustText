package com.pierbezuhoff.justtext.ui

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
import com.pierbezuhoff.justtext.data.BackgroundImageRepo
import com.pierbezuhoff.justtext.data.TaggedUri
import com.pierbezuhoff.justtext.data.TextRepo
import com.pierbezuhoff.justtext.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes

// NOTE: VM survives config changes but not OOM-related process kill,
//  but we call VM.persistState in MainActivity.onPause,
//  so the important elements of UiState are saved via dataStore
class JustTextViewModel(
    private val dataStore: DataStore<Preferences>,
    private val textRepo: TextRepo,
    private val backgroundImageRepo: BackgroundImageRepo,
) : ViewModel() {
    // alternatively we could fuse textFlow, datastore.data flow and transientUIStateFlow into uiStateFlow
    val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState())

    val backgroundImageUri: StateFlow<TaggedUri?>
        field = MutableStateFlow<TaggedUri?>(null)

    private val periodicSaveIsOn = MutableStateFlow(false)
    private var periodicSaveJob: Job? = null

    fun startLoadingData() {
        viewModelScope.launch {
            loadInitialTextFromFile()
            loadBackgroundImageFromFile()
            loadDataStoreData()
            markSaved()
            uiState.update { it.copy(loadedFromDisk = true) }
            println("ViewModel loaded persistent data")
            startPeriodicSave()
        }
    }

    private fun loadInitialTextFromFile() {
        textRepo.load()
            .onSuccess { text ->
                uiState.update {
                    it.copy(
                        tfValue = TextFieldValue(text, TextRange(text.length))
                    )
                }
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
            val cursorLocation = data[CURSOR_LOCATION_KEY]
            val fontSize = data[FONT_SIZE_KEY]
            uiState.update { state ->
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
        backgroundImageRepo.load().getOrNull()?.let { newImage ->
            backgroundImageUri.update { newImage }
        }
    }

    private fun markSaved() {
        uiState.update { it.copy(syncedToDisk = true) }
    }

    private fun markUnsaved() {
        uiState.update { it.copy(syncedToDisk = false) }
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

    fun setFontSize(fontSize: Int) {
        uiState.update {
            it.copy(fontSize = fontSize)
        }
    }

    fun setTextColor(color: Color) {
        uiState.update {
            it.copy(textColor = color.value)
        }
    }

    fun setTextBackgroundColor(color: Color) {
        uiState.update {
            it.copy(textBackgroundColor = color.value)
        }
    }

    fun setImageBackgroundColor(color: Color) {
        uiState.update {
            it.copy(imageBackgroundColor = color.value)
        }
    }

    fun setTFValue(newTFValue: TextFieldValue) {
        if (newTFValue.text != uiState.value.tfValue.text) {
            markUnsaved()
        }
        uiState.update { it.copy(tfValue = newTFValue) }
    }

    fun setBackgroundImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            backgroundImageRepo.loadAndOverwrite(uri)
                .onSuccess { newTaggedUri ->
                    backgroundImageUri.update { newTaggedUri }
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
        val uiState = uiState.value
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
                preferences[CURSOR_LOCATION_KEY] = cursorLocation
            }
            uiState.fontSize.let { fontSize ->
                preferences[FONT_SIZE_KEY] = fontSize
            }
        }
    }

    fun saveTextToFile() {
        textRepo.save(uiState.value.tfValue.text)
    }

    override fun onCleared() {
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
                val applicationContext = application.applicationContext
                return JustTextViewModel(
                    dataStore = application.dataStore,
                    textRepo = TextRepo(applicationContext),
                    backgroundImageRepo = BackgroundImageRepo(applicationContext),
                ) as T
            }
        }

        private val PERIODIC_SAVE_DELAY = 3.minutes

        private val CURSOR_LOCATION_KEY = intPreferencesKey("cursor_location")
        private val FONT_SIZE_KEY = intPreferencesKey("font_size")
        private val TEXT_COLOR_KEY = longPreferencesKey("text_color")
        private val TEXT_BACKGROUND_COLOR_KEY = longPreferencesKey("text_background_color")
        private val IMAGE_BACKGROUND_COLOR_KEY = longPreferencesKey("image_background_color")
    }
}