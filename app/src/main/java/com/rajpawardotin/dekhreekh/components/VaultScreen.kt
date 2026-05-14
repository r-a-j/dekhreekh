package com.rajpawardotin.dekhreekh.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rajpawardotin.dekhreekh.data.DekhreekhDatabase
import kotlinx.coroutines.launch

@Composable
fun VaultScreen() {
    val context = LocalContext.current
    val db = remember { DekhreekhDatabase.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Reactively observe the total row count
    val rowCount by db.telemetryDao().getPingCount().collectAsState(initial = 0)

    // Rough estimation: ~64 bytes per row
    val estimatedSizeKb = (rowCount * 64) / 1024

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DATA VAULT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary // Neon Cyan
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // You can wrap this in your LiquidGlassCard!
        Text(
            text = "$rowCount",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary // Matrix Green
        )
        Text(
            text = "RAW TELEMETRY PINGS",
            style = MaterialTheme.typography.labelSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "ESTIMATED SIZE: $estimatedSizeKb KB",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    db.telemetryDao().wipeDatabase()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) // Alert Red
        ) {
            Text("PURGE DATABASE", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
