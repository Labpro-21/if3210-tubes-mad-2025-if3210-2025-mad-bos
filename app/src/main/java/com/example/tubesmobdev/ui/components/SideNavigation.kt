package com.example.tubesmobdev.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tubesmobdev.ui.navigation.BottomNavigationItem

@Composable
fun SideNavigation(navController: NavController) {
    Column (
        modifier = Modifier
//            .fillMaxHeight()
            .width(240.dp)
            .padding(top = 16.dp, bottom = 16.dp, start = 12.dp, end = 0.dp)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = Color(0xFF535353),
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            },
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BottomNavigationItem.allScreens.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(item.route) }
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,

            ) {
                Icon(
                    imageVector = if (item.route == navController.currentDestination?.route) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.title,
                    tint = if (item.route == navController.currentDestination?.route) Color.White else Color(0xFFB3B3B3)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.title,
                    color = if (item.route == navController.currentDestination?.route) Color.White else Color(0xFFB3B3B3),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

