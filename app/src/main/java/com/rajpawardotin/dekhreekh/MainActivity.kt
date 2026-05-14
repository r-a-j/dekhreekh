package com.rajpawardotin.dekhreekh

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rajpawardotin.dekhreekh.components.TrackingMap
import com.rajpawardotin.dekhreekh.components.VaultScreen
import com.rajpawardotin.dekhreekh.service.TelemetryStatus
import com.rajpawardotin.dekhreekh.service.TrackingService
import com.rajpawardotin.dekhreekh.ui.TelemetryPermissions
import com.rajpawardotin.dekhreekh.ui.rememberTelemetryLauncher
import com.rajpawardotin.dekhreekh.ui.theme.DekhreekhTheme
import org.maplibre.geojson.Point

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DekhreekhTheme {
                val realPath = remember { mutableStateListOf<Point>() }
                val rawLocation by TrackingService.locationStream.collectAsState()
                val telemetryStatus by TrackingService.status.collectAsState()
                val launcher = rememberTelemetryLauncher()

                var lastValidLocation by remember { mutableStateOf<Location?>(null) }
                var showVault by remember { mutableStateOf(false) }

                // UI/Presentation Filter
                LaunchedEffect(rawLocation) {
                    rawLocation?.let { newLoc ->
                        val isAccurate = newLoc.accuracy < 15f
                        val isMoving = lastValidLocation?.let { lastLoc ->
                            newLoc.distanceTo(lastLoc) > 2.0f
                        } ?: true

                        if (isAccurate && isMoving) {
                            realPath.add(Point.fromLngLat(newLoc.longitude, newLoc.latitude))
                            lastValidLocation = newLoc
                        }
                    }
                }

                // Status Animation
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "alpha"
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showVault = !showVault },
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            contentColor = Color.Black
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = "Vault")
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        
                        if (showVault) {
                            VaultScreen()
                        } else {
                            if (realPath.isNotEmpty()) {
                                TrackingMap(pathCoordinates = realPath)
                            }

                            Column(
                                modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when (telemetryStatus) {
                                    TelemetryStatus.IDLE -> {
                                        Button(
                                            onClick = { launcher.launch(TelemetryPermissions) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Text("IGNITE TELEMETRY ENGINE", style = MaterialTheme.typography.labelLarge)
                                        }
                                    }
                                    TelemetryStatus.INITIALIZING -> {
                                        Text("BOOTING HARDWARE...", color = MaterialTheme.colorScheme.primary, modifier = Modifier.alpha(pulseAlpha))
                                    }
                                    TelemetryStatus.SEARCHING -> {
                                        Text("S25 ULTRA: SCANNING SATELLITES...", color = MaterialTheme.colorScheme.primary, modifier = Modifier.alpha(pulseAlpha))
                                    }
                                    TelemetryStatus.LOCKED -> {}
                                }
                            }

                            if (telemetryStatus == TelemetryStatus.LOCKED) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        "GNSS: LOCKED (1Hz)",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
