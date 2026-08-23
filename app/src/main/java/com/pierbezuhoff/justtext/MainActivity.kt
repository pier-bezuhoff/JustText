package com.pierbezuhoff.justtext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.pierbezuhoff.justtext.ui.HomeScreenRoot
import com.pierbezuhoff.justtext.ui.JustTextViewModel
import com.pierbezuhoff.justtext.ui.theme.ColorTheme
import com.pierbezuhoff.justtext.ui.theme.JustTextTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JustTextViewModel by viewModels { JustTextViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JustTextTheme(
                colorTheme = ColorTheme.Auto
            ) {
                HomeScreenRoot(
                    viewModel = viewModel,
                    quitApp = {
                        finishAndRemoveTask()
                    },
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
