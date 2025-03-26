package com.example.tubesmobdev.ui.LibraryScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.tubesmobdev.ui.components.BottomNavigationBar
import com.example.tubesmobdev.ui.components.ScreenHeader

@Composable
fun LibraryScreen(navController: NavController) {

    val tabs = listOf("All", "Liked")
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    Scaffold (
        topBar = { ScreenHeader("Library", actions = {
            IconButton (onClick = { /* action */ }) {
                Icon(Icons.Default.Add, "Add")
            }
        }) },

        bottomBar = { BottomNavigationBar(navController) },
    ) {
            paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),

        ) {
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier
                    .padding(bottom = 10.dp, start = 10.dp, end = 10.dp, top = 10.dp)
                    .width(150.dp)

            ){
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    divider = {},
                    indicator = {}

                ) {
                    tabs.forEachIndexed{index, title ->
                        Tab(
                            onClick = { selectedTabIndex = index },
                            selected = selectedTabIndex == index,
                            modifier = Modifier
                                .background(Color.Transparent)
                                .padding(end = 10.dp)
                                .clip(shape = RoundedCornerShape(30.dp))
                                .background(if (selectedTabIndex != index) Color(0xff212121) else MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex != index) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            HorizontalDivider()


            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,

            )
        }
    }
}
