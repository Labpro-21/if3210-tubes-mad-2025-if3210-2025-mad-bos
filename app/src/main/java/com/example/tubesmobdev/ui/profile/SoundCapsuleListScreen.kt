package com.example.tubesmobdev.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tubesmobdev.ui.components.ScreenHeader

data class TopListItemData(
    val id       : String,
    val title    : String,
    val subtitle : String?    = null,
    val coverUrl : String?    = null,
    val count    : Int        = 0
)

@Composable
fun TopListScreen(
    title        : String,
    bulanTahun   : String,
    summaryText  : String,
    items        : List<TopListItemData>,
    onItemClick  : (TopListItemData) -> Unit,
    onBack       : () -> Unit
) {
    Scaffold(
        topBar = {
            ScreenHeader(
                title       = title,
                isMainMenu  = false,
                onBack      = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = bulanTahun,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summaryText,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(items) { index, data ->
                    TopListItem(
                        rank    = index + 1,
                        data    = data,
                        onClick = { onItemClick(data) }
                    )
                }
            }
        }
    }
}

@Composable
fun TopListItem(
    rank    : Int,
    data    : TopListItemData,
    onClick : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = rank.toString().padStart(2, '0'),
            style    = MaterialTheme.typography.bodyLarge,
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(32.dp)
        )
        data.coverUrl?.let { url ->
            Spacer(modifier = Modifier.width(12.dp))
            AsyncImage(
                model               = url,
                contentDescription  = null,
                modifier            = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text  = data.title,
                style = MaterialTheme.typography.bodyLarge
            )
            data.subtitle?.let { sub ->
                Text(
                    text  = sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text  = data.count.toString(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}