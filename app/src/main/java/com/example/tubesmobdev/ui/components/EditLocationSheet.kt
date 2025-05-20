package com.example.tubesmobdev.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.graphics.Color
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import java.lang.SecurityException
import java.util.Locale

enum class LocationStatus {
    Loading,
    GpsEnabled,
    Failed
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

    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            gpsResolutionStatus.value = LocationStatus.GpsEnabled
        } else {
            gpsResolutionStatus.value = LocationStatus.Failed
        }
    }

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            val locationRequest = com.google.android.gms.location.LocationRequest.create()
                .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)

            val builder = com.google.android.gms.location.LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val client = LocationServices.getSettingsClient(context)

            client.checkLocationSettings(builder.build())
                .addOnSuccessListener {
                    gpsResolutionStatus.value = LocationStatus.GpsEnabled
                }
                .addOnFailureListener { exception ->
                    if (exception is com.google.android.gms.common.api.ResolvableApiException) {
                        try {
                            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
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
        if (gpsResolutionStatus.value == LocationStatus.GpsEnabled) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                try {
                    fusedClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xff212121),
        tonalElevation = 8.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                "Edit Location",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
            )

            Spacer(Modifier.height(16.dp))

            Text("Detected Country Code:", color = Color.White)
            val locationDisplay = when {
                countryCode.value != null -> countryCode.value!!
                gpsResolutionStatus.value == LocationStatus.Failed -> "Failed to retrieve location"
                else -> "Loading..."
            }

            Text(
                text = locationDisplay,
                color = if (gpsResolutionStatus.value == LocationStatus.Failed) Color.Red else Color(0xFF41D18D),
                style = MaterialTheme.typography.bodyLarge
            )

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
                        countryCode.value?.let { onSave(it) }
                    },
                    enabled = countryCode.value != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }
    }
}
