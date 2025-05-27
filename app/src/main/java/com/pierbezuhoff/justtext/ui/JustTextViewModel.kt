package com.pierbezuhoff.justtext.ui

import androidx.compose.animation.core.updateTransition
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class JustTextViewModel(
    private val dataStore: DataStore<Preferences>,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val initialTextFlow = MutableStateFlow("...")
    val uiStateFlow = MutableStateFlow(UiState(""))

    init {
        viewModelScope.launch {
            val dataStoredText = dataStore.data.map { it[TEXT_KEY] }.first()
            if (dataStoredText != null) {
                initialTextFlow.update { dataStoredText }
            }
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

    companion object {
        // reference: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return JustTextViewModel(
                    application.dataStore,
                    savedStateHandle,
                ) as T
            }
        }

        private val TEXT_KEY = stringPreferencesKey("text")
    }
}