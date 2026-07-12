package com.rajpawardotin.dekhreekh.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingIntent
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingState
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingMap
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData

@Composable
fun DashboardScreen(
    uiState: TrackingState,
    livePath: List<TelemetryData> = emptyList(),
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onIntent: (TrackingIntent) -> Unit,
    onNavigateToVault: () -> Unit
) {
    // Stealth Data Aesthetic Colors
    val oledBlack = Color(0xFF000000)
    val matrixGreen = Color(0xFF00FF41)
    val neonCyan = Color(0xFF00FFFF)

    Scaffold(
        containerColor = oledBlack,
        floatingActionButton = {
            if (uiState is TrackingState.Idle || uiState is TrackingState.Ready) {
                FloatingActionButton(
                    onClick = onNavigateToVault,
                    containerColor = neonCyan,
                    contentColor = oledBlack,
                    modifier = Modifier.semantics { contentDescription = "Vault" }
                ) {
                    Text("Vault")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(oledBlack),
            contentAlignment = Alignment.Center
        ) {
            // Background Map Layer
            if (hasLocationPermission) {
                TrackingMap(
                    pathPoints = livePath,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Foreground UI Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
            if (!hasLocationPermission) {
                // Permission Prompt
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Location Permission Required",
                        color = matrixGreen,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please grant location permission to start tracking your runs.",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = matrixGreen, contentColor = oledBlack)
                    ) {
                        Text("Grant Permission")
                    }
                }
            } else {
                // Main Dashboard Content
                when (uiState) {
                    is TrackingState.Idle, is TrackingState.Ready -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SYSTEM READY",
                                color = neonCyan,
                                fontSize = 24.sp,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = { onIntent(TrackingIntent.IgniteEngine) },
                                colors = ButtonDefaults.buttonColors(containerColor = matrixGreen, contentColor = oledBlack)
                            ) {
                                Text("IGNITE ENGINE")
                            }
                        }
                    }
                    is TrackingState.Tracking -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ENGINE ACTIVE",
                                color = matrixGreen,
                                fontSize = 24.sp,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            ElevatedCard(
                                modifier = Modifier.testTag("MetricCard_Distance").padding(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "${uiState.distance}", color = neonCyan, fontSize = 32.sp)
                                    Text(text = "Meters", color = Color.Gray)
                                }
                            }
                            
                            ElevatedCard(
                                modifier = Modifier.testTag("MetricCard_Pace").padding(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "${uiState.pace}", color = neonCyan, fontSize = 32.sp)
                                    Text(text = "Pace (s/km)", color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = { onIntent(TrackingIntent.HaltEngine) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                            ) {
                                Text("HALT ENGINE")
                            }
                        }
                    }
                }
            }
            }
        }
    }
}
