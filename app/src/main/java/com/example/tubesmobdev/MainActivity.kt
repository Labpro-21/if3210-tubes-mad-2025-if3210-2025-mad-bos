// MainActivity.kt
package com.example.tubesmobdev

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.tubesmobdev.ui.navigation.AppNavigation
import com.example.tubesmobdev.ui.theme.TubesMobdevTheme
import com.example.tubesmobdev.ui.viewmodel.NavigationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navigationViewModel: NavigationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigationViewModel = ViewModelProvider(this)[NavigationViewModel::class.java]
        val navigateToFullPlayer = intent?.getBooleanExtra("NAVIGATE_TO_FULL_PLAYER", false) == true

        Log.d("Intent", navigateToFullPlayer.toString())
        installSplashScreen()
        enableEdgeToEdge()

        if (navigateToFullPlayer) {
            navigationViewModel.triggerNavigateToFullPlayer()
        }

        setContent {
            TubesMobdevTheme {
                AppNavigation(navigationViewModel = navigationViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val navigateToFullPlayer = intent?.getBooleanExtra("NAVIGATE_TO_FULL_PLAYER", false) == true
        Log.d("MainLayout", navigateToFullPlayer.toString())


        if (navigateToFullPlayer) {
            navigationViewModel.triggerNavigateToFullPlayer()
        }
    }
}
