package com.pierbezuhoff.justtext

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JustTextTheme(darkTheme = true) {
                HomeScreen(
                    viewModel = viewModel,
                    quitApp = { finishAndRemoveTask() },
                )
            }
        }
    }

    // onDestroy doesn't proc, idk why (maybe no compat)
    override fun onPause() {
        super.onPause()
        viewModel.persistState()
    }
}
