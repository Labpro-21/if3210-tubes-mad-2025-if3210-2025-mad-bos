package com.example.tubesmobdev.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.tubesmobdev.service.CountryData

@Composable
fun CountrySelector(
    onCountrySelected: (String) -> Unit
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var suggestions by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    var isPositionCalculated by remember { mutableStateOf(false) }
    var shouldShowAbove by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    val dropdownHeight = 200.dp
    val threshold = 0.6f
    LaunchedEffect(query.text) {
        suggestions = if (query.text.isBlank()) {
            emptyList()
        } else {
            CountryData.countryList.filter {
                it.first.contains(query.text, ignoreCase = true)
            }.take(5)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val parentWidth = maxWidth

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search country") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .onGloballyPositioned { coordinates ->
                        if (!isPositionCalculated) {
                            val textFieldY = coordinates.localToWindow(
                                androidx.compose.ui.geometry.Offset.Zero
                            ).y
                            val textFieldHeight = coordinates.size.height
                            val dropdownHeightPx = with(density) { dropdownHeight.toPx() }

                            val availableSpaceBelow = screenHeight - (textFieldY + textFieldHeight)
                            val availableSpaceAbove = textFieldY

                            shouldShowAbove = (availableSpaceBelow < dropdownHeightPx &&
                                    availableSpaceAbove > availableSpaceBelow) ||
                                    (textFieldY > screenHeight * threshold)

                            isPositionCalculated = true
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White
                )
            )

            if (suggestions.isNotEmpty() && isPositionCalculated) {
                Popup(
                    offset = IntOffset(
                        0,
                        if (shouldShowAbove) {
                            -with(density) { dropdownHeight.toPx().toInt() } - 8
                        } else {
                            8
                        }
                    ),
                    properties = PopupProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .width(parentWidth)
                            .heightIn(max = dropdownHeight),
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(4.dp)
                        ) {
                            items(suggestions) { (country, code) ->
                                Text(
                                    text = "$country ($code)",
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCountrySelected(code)
                                            query = TextFieldValue("")
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                )
                                if (suggestions.indexOf(Pair(country, code)) < suggestions.size - 1) {
                                    HorizontalDivider(
                                        color = Color.Gray,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
