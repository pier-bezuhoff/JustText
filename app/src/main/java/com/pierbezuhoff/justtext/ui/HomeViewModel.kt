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
import com.pierbezuhoff.justtext.stateInWhileSubscribed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val DEFAULT_TEXT = "Welcome!"
private const val DEFAULT_FONT_SIZE = 18
private const val DEFAULT_IS_LOCAL = true
val PERIODIC_SAVE_DELAY = 3.minutes

/**
 * @param[fontSize] main body text font size in `sp`
 */
@Immutable
data class UiState(
    val contentStatus: ContentStatus = ContentStatus.LOADING,
    val isLocal: Boolean = DEFAULT_IS_LOCAL,
    val initialText: String = DEFAULT_TEXT,
    val textSelection: TextRange = TextRange.Zero,
    val fontSize: Int = DEFAULT_FONT_SIZE,
    // Color.value: ULong
    val textColor: ULong? = null,
    val textBackgroundColor: ULong? = null,
    val imageBackgroundColor: ULong? = null,
)

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

    /** initial texts, update each load */
    private val initialText = MutableStateFlow(DEFAULT_TEXT)
    /** TFV from the text field state, used for saving */
    private val currentTextField = MutableStateFlow(TextFieldValue(DEFAULT_TEXT))

    private val contentStatus = MutableStateFlow(ContentStatus.LOADING)

    val encryptedData: Flow<EncryptedData> = encryptedDataStore.data

    val uiState = combine(
        dataStore.data, initialText, contentStatus
    ) { preferences, initialText, contentStatus ->
        val imageBackgroundColor = preferences[IMAGE_BACKGROUND_COLOR_KEY]?.toULong()
        val textBackgroundColor = preferences[TEXT_BACKGROUND_COLOR_KEY]?.toULong()
        val textColor = preferences[TEXT_COLOR_KEY]?.toULong()
        val cursorLocation = preferences[CURSOR_LOCATION_KEY]
        val fontSize = preferences[FONT_SIZE_KEY]
        val isLocal = preferences[IS_LOCAL_KEY]
        val selection =
            if (cursorLocation == null)
                TextRange.Zero
            else
                TextRange(cursorLocation)
        UiState(
            contentStatus = contentStatus,
            isLocal = isLocal ?: DEFAULT_IS_LOCAL,
            initialText = initialText,
            textSelection = selection,
            fontSize = fontSize ?: DEFAULT_FONT_SIZE,
            textColor = textColor,
            textBackgroundColor = textBackgroundColor,
            imageBackgroundColor = imageBackgroundColor,
        )
    }.stateInWhileSubscribed(UiState())

    val backgroundImageUri: StateFlow<TaggedUri?>
        field = MutableStateFlow<TaggedUri?>(null)

    init {
        viewModelScope.launch {
            loadBackgroundImageFromFile()
            loadEncryptedDataStoreData()
            if (uiState.value.isLocal)
                loadTextFromFile()
            else
                loadTextFromCloud()
            println("ViewModel loaded persistent data")
        }
    }

    private suspend fun loadTextFromFile() {
        textFileRepo.load()
            .onSuccess { text ->
                initialText.update { text }
                // we upd current TFV here so that it won't be marked as Unsaved/changed
                // when the text field receives it
                currentTextField.update { TextFieldValue(text) }
                contentStatus.update { ContentStatus.SYNCED }
//                println("text file loaded: $text")
            }.onFailure {
                contentStatus.update { ContentStatus.LOADING_FAILED }
            }
    }

    private suspend fun loadTextFromCloud() {
        textCloudRepo.value?.pull()
            ?.onSuccess { text ->
                initialText.update { text }
                currentTextField.update { TextFieldValue(text) }
                contentStatus.update { ContentStatus.SYNCED }
//                println("cloud text loaded: $text")
            }.also { pullResult ->
                if (pullResult?.isSuccess != true) {
                    contentStatus.update { ContentStatus.LOADING_FAILED }
                }
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

    fun setFontSize(fontSize: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[FONT_SIZE_KEY] = fontSize
            }
        }
    }

    fun setTextColor(color: Color) {
        viewModelScope.launch {
            dataStore.edit {
                it[TEXT_COLOR_KEY] = color.value.toLong()
            }
        }
    }

    fun setTextBackgroundColor(color: Color) {
        viewModelScope.launch {
            dataStore.edit {
                it[TEXT_BACKGROUND_COLOR_KEY] = color.value.toLong()
            }
        }
    }

    fun setImageBackgroundColor(color: Color) {
        viewModelScope.launch {
            dataStore.edit {
                it[IMAGE_BACKGROUND_COLOR_KEY] = color.value.toLong()
            }
        }
    }

    fun onNewTFValue(newTFValue: TextFieldValue) {
        if (newTFValue.text != currentTextField.value.text) {
            contentStatus.update { ContentStatus.UNSAVED }
        }
        currentTextField.update { newTFValue }
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
            val currentText = currentTextField.value.text
            viewModelScope.launch {
                if (contentStatus.value == ContentStatus.UNSAVED) {
                    contentStatus.update { ContentStatus.SAVING }
                    saveTextToFile(currentText)
                }
                contentStatus.update { ContentStatus.LOADING }
                loadTextFromCloud()
            }
        } else {
            println("no cloud repo")
        }
    }

    private fun switchTextSourceToLocal() {
        // we have to snapshot current text, otherwise it can save text loaded from file...
        val currentText = currentTextField.value.text
        viewModelScope.launch {
            if (contentStatus.value == ContentStatus.UNSAVED) {
                contentStatus.update { ContentStatus.SAVING }
                launch(Dispatchers.Default) {
                    saveTextToCloud(currentText)
                }
            }
            loadTextFromFile()
        }
    }

    fun switchTextSource() {
        if (uiState.value.isLocal) {
            switchTextSourceToCloud()
        } else {
            switchTextSourceToLocal()
        }
    }

    fun save() {
        when (contentStatus.value) {
            ContentStatus.SYNCED,
            ContentStatus.LOADING, ContentStatus.LOADING_FAILED,
            ContentStatus.SAVING -> {
                println("saving skipped")
            }
            // unsaved, saving failed
            else -> viewModelScope.launch {
                contentStatus.update { ContentStatus.SAVING }
                val saveResult =
                    if (uiState.value.isLocal) {
                        saveTextToFile()
                    } else {
                        saveTextToCloud()
                    }
                saveResult.fold(
                    onSuccess = {
                        contentStatus.update { ContentStatus.SYNCED }
                    },
                    onFailure = { e ->
                        contentStatus.update { ContentStatus.SAVING_FAILED }
                        e.printStackTrace()
                        launch {
                            delay(3.seconds)
                            contentStatus.update { ContentStatus.UNSAVED }
                        }
                    }
                )
            }
        }
    }

    /** same as [save] but uses runBlocking for coroutines */
    fun persistState() {
        println("persist state")
        val saveResult = when (contentStatus.value) {
            ContentStatus.SYNCED ->
                Result.success(Unit)
            ContentStatus.LOADING, ContentStatus.LOADING_FAILED, ContentStatus.SAVING ->
                Result.failure(Error("Bad state for persisting text"))
            // unsaved, saving failed
            else -> runBlocking {
                if (uiState.value.isLocal)
                    saveTextToFile()
                else
                    saveTextToCloud()
            }
        }
        saveResult.onSuccess {
            contentStatus.update { ContentStatus.SYNCED }
        }
    }

    private suspend fun saveTextToFile(
        text: String = currentTextField.value.text,
    ): Result<Unit> {
        return textFileRepo.save(text)
    }

    private suspend fun saveTextToCloud(
        text: String = currentTextField.value.text,
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

        private val CURSOR_LOCATION_KEY = intPreferencesKey("cursor_location")
        private val FONT_SIZE_KEY = intPreferencesKey("font_size")
        private val TEXT_COLOR_KEY = longPreferencesKey("text_color")
        private val TEXT_BACKGROUND_COLOR_KEY = longPreferencesKey("text_background_color")
        private val IMAGE_BACKGROUND_COLOR_KEY = longPreferencesKey("image_background_color")
        private val IS_LOCAL_KEY = booleanPreferencesKey("is_local")
    }
}