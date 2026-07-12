package com.rajpawardotin.dekhreekh.presentation.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ElevatedCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun VaultScreen(
    uiState: VaultState,
    onSessionClick: (String) -> Unit = {}
) {
    // Stealth Data Aesthetic Colors
    val oledBlack = Color(0xFF000000)
    val matrixGreen = Color(0xFF00FF41)
    val neonCyan = Color(0xFF00FFFF)

    Scaffold(
        containerColor = oledBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(oledBlack),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is VaultState.Empty -> {
                    Text(
                        text = "No telemetry data found",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is VaultState.HistoryLoaded -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "VAULT HISTORY",
                                color = matrixGreen,
                                fontSize = 24.sp,
                                letterSpacing = 4.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        items(uiState.sessions) { session ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("SessionCard_${session.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    val dateStr = Instant.ofEpochMilli(session.startTime)
                                        .atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))

                                    Text(
                                        text = dateStr,
                                        color = matrixGreen,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "${session.totalDistanceMeters} m", color = neonCyan, fontSize = 24.sp)
                                        Text(text = "${session.averagePace} s/km", color = Color.LightGray, fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Duration: ${session.totalDurationSeconds}s",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
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
