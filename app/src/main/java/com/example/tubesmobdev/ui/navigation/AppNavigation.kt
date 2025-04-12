package com.example.tubesmobdev.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubesmobdev.service.TokenRefreshService
import com.example.tubesmobdev.ui.auth.login.LoginScreen
import com.example.tubesmobdev.ui.layout.MainLayout
import com.example.tubesmobdev.ui.splash.SplashScreen
import com.example.tubesmobdev.ui.viewmodel.NavigationViewModel
import com.example.tubesmobdev.ui.viewmodel.TokenRefreshViewModel
import com.example.tubesmobdev.util.ServiceUtil

@Composable
fun AppNavigation(authViewModel: NavigationViewModel = hiltViewModel(), tokenRefreshViewModel: TokenRefreshViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isInitialized by authViewModel.isInitialized.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(isLoggedIn) {
        authViewModel.setShouldRestartService(isLoggedIn)

        if (isLoggedIn) {
            if (ServiceUtil.isServiceRunning(context, TokenRefreshService::class.java)) {
                Log.d("ServiceUtil", "Service is running → Trigger restart only")
                ServiceUtil.triggerRestart(context)
            } else {
                Log.d("ServiceUtil", "Service not running → Start service")
                ServiceUtil.startService(context)
            }
        } else {
            Log.d("ServiceUtil", "User logged out → Stop service")
            ServiceUtil.stopService(context)
        }
    }

//    LaunchedEffect(isLoggedIn) {
//        if (isLoggedIn) {
//            tokenRefreshViewModel.startTokenRefreshLoop()
//        } else {
//            tokenRefreshViewModel.stopTokenRefreshLoop()
//        }
//    }

    if (!isInitialized) {
        SplashScreen()
        return
    }
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("main") {
            MainLayout(navController)
        }
    }

//    when (isLoggedIn.value) {
//        null -> {
//            LoginScreen(navController = navController)
//        }
//        true -> {
//            NavHost(navController = navController, startDestination = "home") {
//
//                composable("login") {
//                    LoginScreen(navController = navController)
//                }
//
//                composable("home") {
//                    HomeScreen(navController = navController)
//                }
//
//                composable("library") {
//                    LibraryScreen(navController = navController, onSongClick = {})
//                }
//
//                composable("profile") {
//                    ProfileScreen(navController = navController)
//                }
//            }
//        }
//        false -> {
//            NavHost(navController = navController, startDestination = "login") {
//
//                composable("login") {
//                    LoginScreen(navController = navController)
//                }
//
//                composable("home") {
//                    HomeScreen(navController = navController)
//                }
//
//                composable("library") {
//                    LibraryScreen(navController = navController, onSongClick = {})
//                }
//
//                composable("profile") {
//                    ProfileScreen(navController = navController)
//                }
//            }
//
//        }
//    }


}