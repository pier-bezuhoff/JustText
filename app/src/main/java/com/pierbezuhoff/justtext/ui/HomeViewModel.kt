package com.pierbezuhoff.justtext.ui

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.pierbezuhoff.justtext.JustTextApplication
import com.pierbezuhoff.justtext.data.BackgroundImageRepo
import com.pierbezuhoff.justtext.data.EncryptedData
import com.pierbezuhoff.justtext.data.TaggedUri
import com.pierbezuhoff.justtext.data.TextCloudRepo
import com.pierbezuhoff.justtext.data.TextFileRepo
import com.pierbezuhoff.justtext.dataStore
import com.pierbezuhoff.justtext.encryptedDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * @param[textSourceId] monotonically increasing sequence, increment
 * invalidates the text field and re-initializes it with [tfValue]
 * @param[tfValue] mirrored from the text field, changing it in [UiState]
 * doesn't do anything unless you also increment [textSourceId]
 */
@Immutable
data class UiState(
    val contentStatus: ContentStatus = ContentStatus.LOADING,
    val isLocal: Boolean = true,
    val textSourceId: Int = 0,
    val tfValue: TextFieldValue =
        TextFieldValue(DEFAULT_TEXT, TextRange(0)),
    /** main body text font size in `sp` */
    val fontSize: Int = 18,
    // Color.value: ULong
    val textColor: ULong? = null,
    val textBackgroundColor: ULong? = null,
    val imageBackgroundColor: ULong? = null,
) {
    companion object {
        private const val DEFAULT_TEXT = "Welcome!"
    }
}

// NOTE: VM survives config changes but not OOM-related process kill,
//  but we call VM.persistState in MainActivity.onPause,
//  so the important elements of UiState are saved via dataStore
class HomeViewModel(
    application: JustTextApplication,
    private val dataStore: DataStore<Preferences>,
    private val encryptedDataStore: DataStore<EncryptedData>,
    private val textFileRepo: TextFileRepo,
    private val backgroundImageRepo: BackgroundImageRepo,
) : AndroidViewModel(application) {
    val textCloudRepo: StateFlow<TextCloudRepo?>
        field = MutableStateFlow<TextCloudRepo?>(null)

    // MAYBE: pipe dataStore updates directly into uiStateFlow, and modify dataStore data directly
    val uiState: StateFlow<UiState>
        field = MutableStateFlow(UiState(
            contentStatus = ContentStatus.LOADING,
        ))

    val backgroundImageUri: StateFlow<TaggedUri?>
        field = MutableStateFlow<TaggedUri?>(null)

    val encryptedDataFlow: Flow<EncryptedData> = encryptedDataStore.data

    init {
        viewModelScope.launch {
            loadBackgroundImageFromFile()
            loadDataStoreData() // sets isLocal
            loadEncryptedDataStoreData()
            if (uiState.value.isLocal)
                loadTextFromFile()
            else
                loadTextFromCloud()
            println("ViewModel loaded persistent data")
        }
    }

    private fun loadTextFromFile() {
        textFileRepo.load()
            .onSuccess { text ->
                uiState.update { it.copy(
                    contentStatus = ContentStatus.SYNCED,
                    isLocal = true,
                    textSourceId = it.textSourceId + 1,
                    tfValue = TextFieldValue(text, it.tfValue.selection),
                ) }
            }.onFailure {
                uiState.update { it.copy(
                    contentStatus = ContentStatus.LOADING_FAILED
                ) }
            }
    }

    private suspend fun loadTextFromCloud() {
        textCloudRepo.value?.pull()
            ?.onSuccess { text ->
                uiState.update { it.copy(
                    contentStatus = ContentStatus.SYNCED,
                    isLocal = false,
                    textSourceId = it.textSourceId + 1,
                    tfValue = TextFieldValue(text, it.tfValue.selection),
                ) }
            }.also { savingResult ->
                if (savingResult?.isSuccess != true) {
                    uiState.update { it.copy(
                        contentStatus = ContentStatus.LOADING_FAILED
                    ) }
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
            val isLocal = data[IS_LOCAL_KEY]
            uiState.update { it.copy(
                isLocal = isLocal ?: true,
                tfValue = if (cursorLocation == null) {
                    it.tfValue
                } else {
                    it.tfValue.copy(
                        selection = TextRange(cursorLocation)
                    )
                },
                fontSize = fontSize ?: it.fontSize,
                textColor = textColor ?: it.textColor,
                textBackgroundColor = textBackgroundColor ?: it.textBackgroundColor,
                imageBackgroundColor = imageBackgroundColor ?: it.imageBackgroundColor,
            ) }
        }
    }

    private suspend fun loadEncryptedDataStoreData() {
        encryptedDataStore.data.firstOrNull()?.let { data ->
            val (endpoint, password) = data
            if (endpoint != null && password != null) {
                textCloudRepo.update {
                    TextCloudRepo(endpoint, password)
                }
            }
        }
    }

    private fun loadBackgroundImageFromFile() {
        backgroundImageRepo.load().getOrNull()?.let { newImage ->
            backgroundImageUri.update { newImage }
        }
    }

    private fun markSaved() {
        uiState.update { it.copy(
            contentStatus = ContentStatus.SYNCED
        ) }
    }

    private fun markUnsaved() {
        uiState.update { it.copy(
            contentStatus = ContentStatus.UNSAVED
        ) }
    }

    fun setFontSize(fontSize: Int) {
        uiState.update { it.copy(
            fontSize = fontSize
        ) }
    }

    fun setTextColor(color: Color) {
        uiState.update { it.copy(
            textColor = color.value
        ) }
    }

    fun setTextBackgroundColor(color: Color) {
        uiState.update { it.copy(
            textBackgroundColor = color.value
        ) }
    }

    fun setImageBackgroundColor(color: Color) {
        uiState.update { it.copy(
            imageBackgroundColor = color.value
        ) }
    }

    fun setTFValue(newTFValue: TextFieldValue) {
        if (newTFValue.text != uiState.value.tfValue.text) {
            markUnsaved()
        }
        uiState.update { it.copy(
            tfValue = newTFValue
        ) }
    }

    fun setBackgroundImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            backgroundImageRepo.loadAndOverwrite(uri)
                .onSuccess { newTaggedUri ->
                    backgroundImageUri.update { newTaggedUri }
                }
        }
    }

    fun setCloudRepoProperties(properties: TextCloudRepo.Properties) {
        textCloudRepo.update { TextCloudRepo(properties) }
        viewModelScope.launch {
            encryptedDataStore.updateData { it.copy(
                noteEndpoint = properties.endpoint,
                notePassword = properties.password,
            ) }
        }
    }

    private fun switchTextSourceToCloud() {
        if (textCloudRepo.value != null) {
            viewModelScope.launch {
                if (uiState.value.contentStatus == ContentStatus.UNSAVED) {
                    uiState.update { it.copy(
                        contentStatus = ContentStatus.SAVING,
                    ) }
                    saveTextToFile()
                }
                uiState.update { it.copy(
                    contentStatus = ContentStatus.LOADING,
                ) }
                loadTextFromCloud()
            }
        } else {
            println("no cloud repo")
        }
    }

    private fun switchTextSourceToLocal() {
        // we have to snapshot current text, otherwise it can save text loaded from file...
        val text = uiState.value.tfValue.text
        viewModelScope.launch {
            if (uiState.value.contentStatus == ContentStatus.UNSAVED) {
                uiState.update { it.copy(
                    contentStatus = ContentStatus.SAVING,
                ) }
                launch(Dispatchers.Default) {
                    saveTextToCloud(text)
                }
            }
        }
        loadTextFromFile()
    }

    fun switchTextSource() {
        if (uiState.value.isLocal) {
            switchTextSourceToCloud()
        } else {
            switchTextSourceToLocal()
        }
    }

    fun save() {
        val uiState0 = uiState.value
        when (uiState0.contentStatus) {
            ContentStatus.SYNCED,
            ContentStatus.LOADING, ContentStatus.LOADING_FAILED,
            ContentStatus.SAVING -> {
                println("saving skipped")
            }
            // unsaved, saving failed
            else -> viewModelScope.launch {
                uiState.update { it.copy(
                    contentStatus = ContentStatus.SAVING
                ) }
                saveDatastoreData()
                val saveResult =
                    if (uiState0.isLocal) {
                        saveTextToFile()
                    } else {
                        saveTextToCloud()
                    }
                saveResult.fold(
                    onSuccess = {
                        markSaved()
                    },
                    onFailure = { e ->
                        uiState.update { it.copy(
                            contentStatus = ContentStatus.SAVING_FAILED
                        ) }
                        e.printStackTrace()
                        launch {
                            delay(3.seconds)
                            uiState.update { it.copy(
                                contentStatus = ContentStatus.UNSAVED
                            ) }
                        }
                    }
                )
            }
        }
    }

    /** same as [save] but uses runBlocking for coroutines */
    fun persistState() {
        println("persist state")
        val saveResult = when (uiState.value.contentStatus ) {
            ContentStatus.SYNCED ->
                Result.success(Unit)
            ContentStatus.LOADING, ContentStatus.LOADING_FAILED, ContentStatus.SAVING ->
                Result.failure(Error("Bad state for persisting text"))
            // unsaved, saving failed
            else -> when {
                uiState.value.isLocal ->
                    saveTextToFile()
                else -> runBlocking {
                    saveTextToCloud()
                }
            }
        }
        runBlocking {
            saveDatastoreData()
        }
        saveResult.onSuccess {
            markSaved()
        }
    }

    private suspend fun saveDatastoreData() {
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
            uiState.isLocal.let { isLocal ->
                preferences[IS_LOCAL_KEY] = isLocal
            }
        }
    }

    private fun saveTextToFile(
        text: String = uiState.value.tfValue.text,
    ): Result<Unit> {
        return textFileRepo.save(text)
    }

    private suspend fun saveTextToCloud(
        text: String = uiState.value.tfValue.text,
    ): Result<Unit> {
        val repo = textCloudRepo.value
            ?: return Result.failure(Error("No cloud repo"))
        return repo.push(text)
    }

    fun freeResources() {
        textCloudRepo.value?.client?.close()
    }

    override fun onCleared() {
        println("VM.onCleared")
        freeResources()
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
                return HomeViewModel(
                    application = application as JustTextApplication,
                    dataStore = application.dataStore,
                    encryptedDataStore = application.encryptedDataStore,
                    textFileRepo = TextFileRepo(applicationContext),
                    backgroundImageRepo = BackgroundImageRepo(applicationContext),
                ) as T
            }
        }

        val PERIODIC_SAVE_DELAY = 3.minutes
        private const val SKIP_TO_THE_END_OF_NEW_TEXT = false

        private val CURSOR_LOCATION_KEY = intPreferencesKey("cursor_location")
        private val FONT_SIZE_KEY = intPreferencesKey("font_size")
        private val TEXT_COLOR_KEY = longPreferencesKey("text_color")
        private val TEXT_BACKGROUND_COLOR_KEY = longPreferencesKey("text_background_color")
        private val IMAGE_BACKGROUND_COLOR_KEY = longPreferencesKey("image_background_color")
        private val IS_LOCAL_KEY = booleanPreferencesKey("is_local")
    }
}