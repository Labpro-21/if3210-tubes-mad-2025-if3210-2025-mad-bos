package com.example.tubesmobdev.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
) {
    object Home : BottomNavigationItem(
        title = "Home",
        route = "home",
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Default.Home,
        hasNews = false
    )

    object Search : BottomNavigationItem(
        route = "library",
        title = "Your Library",
        selectedIcon = Icons.Default.LibraryMusic,
        unselectedIcon = Icons.Default.LibraryMusic,
        hasNews = false

    )

    object Profile : BottomNavigationItem(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Default.Person,
        unselectedIcon = Icons.Default.Person,
        hasNews = false

    )

    companion object {
        val allScreens = listOf(Home, Search, Profile)
    }
}