package com.example.tubesmobdev.ui.LibraryScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.tubesmobdev.ui.components.BottomNavigationBar
import com.example.tubesmobdev.ui.components.ScreenHeader

@Composable
fun LibraryScreen(navController: NavController) {
    Scaffold (
        topBar = { ScreenHeader("Library", actions = {
            IconButton (onClick = { /* action */ }) {
                Icon(Icons.Default.Add, "Add")
            }
        }) },

        bottomBar = { BottomNavigationBar(navController) }
    ) {
            paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
