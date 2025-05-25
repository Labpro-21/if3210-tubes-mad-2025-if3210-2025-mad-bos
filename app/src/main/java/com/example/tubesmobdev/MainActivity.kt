// MainActivity.kt
package com.example.tubesmobdev

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.tubesmobdev.service.MusicService
import com.example.tubesmobdev.ui.navigation.AppNavigation
import com.example.tubesmobdev.ui.theme.TubesMobdevTheme
import com.example.tubesmobdev.ui.viewmodel.NavigationViewModel
import com.example.tubesmobdev.util.SongEvent
import com.example.tubesmobdev.util.SongEventBus
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navigationViewModel: NavigationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationViewModel = ViewModelProvider(this)[NavigationViewModel::class.java]
        installSplashScreen()
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {

            TubesMobdevTheme {
                AppNavigation(navigationViewModel = navigationViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXIT_AND_REMOVE", false) == true) {
            finishAndRemoveTask()
            return
        }
        val navigateToFullPlayer = intent?.getBooleanExtra("NAVIGATE_TO_FULL_PLAYER", false) == true
        val deepLink = intent?.data

        when {
            navigateToFullPlayer -> {
                navigationViewModel.triggerNavigateToFullPlayer()
            }
            deepLink != null && deepLink.scheme == "purrytify" && deepLink.host == "song" -> {
                val songId = deepLink.lastPathSegment
                if (songId != null) {
                    Log.d("DeepLink", "Received song ID: $songId")
                    navigationViewModel.triggerNavigateToSongId(songId)
                }
            }
        }
    }
}
