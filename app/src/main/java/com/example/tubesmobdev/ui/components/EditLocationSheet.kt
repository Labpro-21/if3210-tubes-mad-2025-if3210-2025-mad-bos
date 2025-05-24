package com.example.tubesmobdev.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.graphics.Color
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.ui.Alignment
import com.google.accompanist.permissions.isGranted
import java.lang.SecurityException
import java.util.Locale

enum class LocationStatus {
    Loading, GpsEnabled, Failed
}

enum class SelectionMode {
    AUTO, SELECT, LINK
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditLocationSheet(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val gpsResolutionStatus = remember { mutableStateOf(LocationStatus.Loading) }
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val countryCode = remember { mutableStateOf<String?>(null) }
    val selectionMode = remember { mutableStateOf(SelectionMode.AUTO) }
    val selectCountryCode = remember { mutableStateOf<String?>(null) }
    val linkInput = remember { mutableStateOf("") }
    val linkCountryCode = remember { mutableStateOf<String?>(null) }

    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        gpsResolutionStatus.value = if (result.resultCode == android.app.Activity.RESULT_OK)
            LocationStatus.GpsEnabled else LocationStatus.Failed
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            val locationRequest = com.google.android.gms.location.LocationRequest.create()
                .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)

            val builder = com.google.android.gms.location.LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())
                .addOnSuccessListener { gpsResolutionStatus.value = LocationStatus.GpsEnabled }
                .addOnFailureListener {
                    if (it is com.google.android.gms.common.api.ResolvableApiException) {
                        try {
                            val intentSenderRequest = IntentSenderRequest.Builder(it.resolution).build()
                            gpsLauncher.launch(intentSenderRequest)
                        } catch (e: Exception) {
                            gpsResolutionStatus.value = LocationStatus.Failed
                        }
                    } else {
                        gpsResolutionStatus.value = LocationStatus.Failed
                    }
                }
        } else {
            locationPermission.launchPermissionRequest()
            gpsResolutionStatus.value = LocationStatus.Failed
        }
    }

    LaunchedEffect(gpsResolutionStatus.value) {
        if (gpsResolutionStatus.value == LocationStatus.GpsEnabled &&
            selectionMode.value == SelectionMode.AUTO) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                try {
                    fusedClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
                    ).addOnSuccessListener { location ->
                        if (location != null) {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            countryCode.value = addresses?.firstOrNull()?.countryCode
                        } else {
                            gpsResolutionStatus.value = LocationStatus.Failed
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("EditLocation", "SecurityException: ${e.localizedMessage}")
                    gpsResolutionStatus.value = LocationStatus.Failed
                }
            } else {
                gpsResolutionStatus.value = LocationStatus.Failed
            }
        }
    }

    LaunchedEffect(linkInput.value) {
        if (selectionMode.value == SelectionMode.LINK && linkInput.value.isNotBlank()) {
            delay(1500)
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(linkInput.value, 1)
                linkCountryCode.value = addresses?.firstOrNull()?.countryCode ?: "Not found"
            } catch (e: Exception) {
                linkCountryCode.value = "Not found"
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(Modifier.fillMaxWidth(0.99f)) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xff212121),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        listOf(
                            Triple(SelectionMode.AUTO, Icons.Default.LocationOn, "Auto"),
                            Triple(SelectionMode.SELECT, Icons.Default.Check, "Select"),
                            Triple(SelectionMode.LINK, Icons.Default.Link, "Link")
                        ).forEach { (mode, icon, description) ->
                            IconButton(
                                onClick = { selectionMode.value = mode },
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(8.dp)
                                    .background(
                                        if (selectionMode.value == mode) Color(0xFF41D18D) else Color.Gray,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(icon, contentDescription = description, tint = Color.White)
                            }
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    if (selectionMode.value == SelectionMode.LINK) {
                        OutlinedTextField(
                            value = linkInput.value,
                            onValueChange = {
                                linkInput.value = it
                                linkCountryCode.value = null
                            },
                            label = { Text("Enter location or Google Maps link") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp),
                            singleLine = false,
                            maxLines = 3,
                            textStyle = LocalTextStyle.current.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
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
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Result: ${linkCountryCode.value ?: "Searching..."}",
                            color = when (linkCountryCode.value) {
                                null -> Color.LightGray
                                "Not found" -> Color.Red
                                else -> Color(0xFF41D18D)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                    Text("Detected Country Code:", color = Color.White)
                    Spacer(Modifier.height(8.dp))

                    val locationDisplay = when (selectionMode.value) {
                        SelectionMode.LINK -> linkCountryCode.value ?: "Searching..."
                        SelectionMode.SELECT -> selectCountryCode.value ?: "No country selected"
                        SelectionMode.AUTO -> when {
                            countryCode.value != null ->countryCode.value!!
                            gpsResolutionStatus.value == LocationStatus.Failed -> "Failed to retrieve location"
                            else -> "Loading..."
                        }
                    }
                    Text(
                        text = locationDisplay,
                        color = when {
                            locationDisplay == "Not found" -> Color.Red
                            gpsResolutionStatus.value == LocationStatus.Failed -> Color.Red
                            else -> Color(0xFF41D18D)
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (selectionMode.value == SelectionMode.AUTO) {
                        Spacer(Modifier.height(16.dp))
                        AnimatedContent(
                            targetState = gpsResolutionStatus.value,
                            label = "LocationStatusIndicator"
                        ) { status ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (status) {
                                    LocationStatus.GpsEnabled -> {
                                        if (countryCode.value != null) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Location Detected",
                                                tint = Color(0xFF41D18D),
                                                modifier = Modifier.size(64.dp)
                                            )
                                        } else {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                strokeWidth = 3.dp,
                                                modifier = Modifier.size(64.dp)
                                            )
                                        }
                                    }

                                    LocationStatus.Failed -> {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Location Failed",
                                            tint = Color.Red,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }

                                    else -> {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (selectionMode.value == SelectionMode.SELECT) {
                        CountrySelector { selectedIso ->
                            selectCountryCode.value = selectedIso
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff535353)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                when (selectionMode.value) {
                                    SelectionMode.LINK -> {
                                        linkCountryCode.value?.takeIf { it != "Not found" }?.let { onSave(it) }
                                    }
                                    SelectionMode.SELECT -> {
                                        selectCountryCode.value?.let { onSave(it) }
                                    }
                                    SelectionMode.AUTO -> {
                                        countryCode.value?.let { onSave(it) }
                                    }
                                }
                            },
                            enabled = when (selectionMode.value) {
                                SelectionMode.LINK -> linkCountryCode.value != null && linkCountryCode.value != "Not found"
                                else -> countryCode.value != null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}