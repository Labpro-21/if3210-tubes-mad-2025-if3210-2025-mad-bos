package com.example.tubesmobdev.ui.LibraryScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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

@Composable
fun LibraryScreen(navController: NavController) {
    Scaffold (
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
