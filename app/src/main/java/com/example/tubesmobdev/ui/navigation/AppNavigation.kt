package com.example.tubesmobdev.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubesmobdev.ui.auth.login.LoginScreen
import com.example.tubesmobdev.ui.home.HomeScreen
import com.example.tubesmobdev.ui.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
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