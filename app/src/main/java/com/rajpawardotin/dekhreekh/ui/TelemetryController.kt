package com.rajpawardotin.dekhreekh.ui

import android.Manifest
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.rajpawardotin.dekhreekh.service.TrackingService

@Composable
fun rememberTelemetryLauncher(
    onPermissionsGranted: () -> Unit = {}
): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
    val context = LocalContext.current

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val notificationsGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false

        if (locationGranted && notificationsGranted) {
            onPermissionsGranted()
            val serviceIntent = Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_START_TRACKING
            }
            context.startForegroundService(serviceIntent)
        }
    }
}

val TelemetryPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.POST_NOTIFICATIONS,
    Manifest.permission.BODY_SENSORS,
    Manifest.permission.ACTIVITY_RECOGNITION
)
