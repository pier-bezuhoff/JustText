package com.pierbezuhoff.justtext

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.pierbezuhoff.justtext.ui.HomeScreen
import com.pierbezuhoff.justtext.ui.JustTextViewModel
import com.pierbezuhoff.justtext.ui.theme.JustTextTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "save")

class MainActivity : ComponentActivity() {
    private val viewModel: JustTextViewModel by viewModels { JustTextViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JustTextTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    // onDestroy doesn't proc, idk why (maybe no compat)
    override fun onPause() {
        super.onPause()
        viewModel.saveToDatastore()
    }
}
