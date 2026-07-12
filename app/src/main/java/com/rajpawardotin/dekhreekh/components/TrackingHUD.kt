package com.rajpawardotin.dekhreekh.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.rajpawardotin.dekhreekh.service.TrackingService
import com.rajpawardotin.dekhreekh.utils.formatPace
import com.rajpawardotin.dekhreekh.utils.formatTime

@Composable
fun TrackingHUD(
    distanceMeters: Float,
    paceSecondsPerKm: Long,
    elapsedSeconds: Long,
    onLocateClicked: () -> Unit,
    onStopClicked: () -> Unit
) {
    val context = LocalContext.current

    // Format the math for the UI
    val distanceKm = String.format(java.util.Locale.getDefault(), "%.2f", distanceMeters / 1000f)
    val formattedTime = formatTime(elapsedSeconds) // e.g., 01:24:03
    val formattedPace = formatPace(paceSecondsPerKm) // e.g., 5:30

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. The Tactical "Locate" FAB (Top Right)
        FloatingActionButton(
            onClick = onLocateClicked,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.primary, // Matrix Green
            shape = CircleShape
        ) {
            Text("📍", modifier = Modifier.padding(16.dp)) 
        }

        // 2. The HUD Layer: Liquid Glass Metrics
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricCard(
                    label = "DISTANCE",
                    value = distanceKm,
                    unit = "KM",
                    modifier = Modifier.weight(1f).testTag("MetricCard_Distance")
                )
                Spacer(modifier = Modifier.width(16.dp))
                MetricCard(
                    label = "PACE",
                    value = formattedPace,
                    unit = "/KM",
                    modifier = Modifier.weight(1f).testTag("MetricCard_Pace")
                )
            }
            
            // A wider central card for the primary timer
            MetricCard(
                label = "DURATION",
                value = formattedTime,
                unit = "",
                modifier = Modifier.fillMaxWidth().testTag("MetricCard_Duration")
            )

            // The EXTINGUISH Button
            Button(
                onClick = {
                    // 1. Fire the Kill Intent to the Service
                    val stopIntent = Intent(context, TrackingService::class.java).apply {
                        action = TrackingService.ACTION_STOP_TRACKING
                    }
                    context.startService(stopIntent)
                    
                    // 2. Notify the parent Activity to swap screens
                    onStopClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "EXTINGUISH ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
