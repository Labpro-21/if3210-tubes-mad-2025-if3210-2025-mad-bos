package com.example.tubesmobdev.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubesmobdev.ui.auth.login.LoginScreen
import com.example.tubesmobdev.ui.layout.MainLayout
import com.example.tubesmobdev.ui.splash.SplashScreen
import com.example.tubesmobdev.ui.viewmodel.NavigationViewModel

@Composable
fun AppNavigation(authViewModel: NavigationViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isInitialized by authViewModel.isInitialized.collectAsState()

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