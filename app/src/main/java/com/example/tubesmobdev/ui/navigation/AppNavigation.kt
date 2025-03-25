package com.example.tubesmobdev.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubesmobdev.ui.auth.login.LoginScreen
import com.example.tubesmobdev.ui.home.HomeScreen
import com.example.tubesmobdev.ui.splash.SplashScreen
import com.example.tubesmobdev.ui.viewmodel.NavigationViewModel

@Composable
fun AppNavigation(authViewModel: NavigationViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val isLoggedIn = authViewModel.isLoggedIn.collectAsState()


    when (isLoggedIn.value) {
        null -> {
            SplashScreen(navController = navController)
        }
        true -> {
            NavHost(navController = navController, startDestination = "home") {
                composable("splash") {
                    SplashScreen(navController = navController)
                }
                composable("login") {
                    LoginScreen(navController = navController)
                }

                composable("home") {
                    HomeScreen(navController = navController)
                }
            }
        }
        false -> {
            NavHost(navController = navController, startDestination = "login") {
                composable("splash") {
                    SplashScreen(navController = navController)
                }
                composable("login") {
                    LoginScreen(navController = navController)
                }

                composable("home") {
                    HomeScreen(navController = navController)
                }
            }
        }
    }
}