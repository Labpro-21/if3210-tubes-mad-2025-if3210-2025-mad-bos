package com.example.tubesmobdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tubesmobdev.ui.navigation.AppNavigation
import com.example.tubesmobdev.ui.theme.TubesMobdevTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TubesMobdevTheme {

                AppNavigation()
            }
        }
    }
}

