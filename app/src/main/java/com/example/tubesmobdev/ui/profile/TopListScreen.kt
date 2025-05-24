package com.example.tubesmobdev.ui.common

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tubesmobdev.data.model.TopListItemData
import com.example.tubesmobdev.ui.components.ScreenHeader
import com.example.tubesmobdev.ui.viewmodel.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.model.TopListType
import kotlinx.coroutines.launch

@Composable
fun TopListScreen(
    title       : String,
    bulanTahun  : String,
    summaryText : String,
    type        : TopListType,
    onItemClick : (Song) -> Unit,
    onBack      : () -> Unit,
    viewModel   : ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(type, bulanTahun) {
        viewModel.fetchMonthlyTopList(bulanTahun, type)
    }

    val items by viewModel.monthlyTopList.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Log.d("TopListScreen", "Collected items: $items")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(text = bulanTahun, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = summaryText, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            itemsIndexed(items) { index, data ->
                if (index == 0) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (type == TopListType.Song) {
                                coroutineScope.launch {
                                    val song = viewModel.getSongById(data.id.toInt())
                                    if (song != null) {
                                        onItemClick(song)
                                    } else {
                                        Log.e("TopListScreen", "Failed to fetch song from id=${data.id}")
                                    }
                                }
                            }
                        },
                    shape  = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    TopListItem(
                        rank    = index + 1,
                        data    = data,
                        type = type,
                        onClick = {
                            if (type == TopListType.Song) {
                                coroutineScope.launch {
                                    val song = viewModel.getSongById(data.id.toInt())
                                    if (song != null) {
                                        onItemClick(song)
                                    } else {
                                        Log.e("TopListScreen", "Failed to fetch song from id=${data.id}")
                                    }
                                }
                            }
                        }
                    )
                }

                Divider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )

            }
        }
    }
}
@Composable
private fun TopListItem(
    rank: Int,
    data: TopListItemData,
    type: TopListType,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(
            text = rank.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleMedium,
            color = if (type == TopListType.Song) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Info Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.bodyLarge
            )

            data.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (type == TopListType.Song) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${data.count} plays",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Cover Image
        data.coverUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        }
    }
}