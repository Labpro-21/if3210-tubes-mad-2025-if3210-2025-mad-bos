package com.example.tubesmobdev.ui.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.tubesmobdev.service.CountryData

@Composable
fun CountrySelector(
    onCountrySelected: (String) -> Unit
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var suggestions by remember { mutableStateOf(emptyList<Pair<String, String>>()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                suggestions = if (query.text.isBlank()) {
                    emptyList()
                } else {
                    CountryData.countryList.filter {
                        it.first.contains(query.text, ignoreCase = true)
                    }.take(5)
                }
            },
            label = { Text("Search country") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(suggestions) { (country, code) ->
                Text(
                    text = "$country ($code)",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCountrySelected(code) }
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        }
    }
}
