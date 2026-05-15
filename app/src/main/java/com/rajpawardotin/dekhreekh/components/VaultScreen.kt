package com.rajpawardotin.dekhreekh.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rajpawardotin.dekhreekh.data.DekhreekhDatabase
import com.rajpawardotin.dekhreekh.utils.ExportEngine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun VaultScreen() {
    val context = LocalContext.current
    val db = remember { DekhreekhDatabase.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Reactively observe the total row count
    val rowCount by db.telemetryDao().getPingCount().collectAsState(initial = 0)

    // Rough estimation: ~64 bytes per row
    val estimatedSizeKb = (rowCount * 64) / 1024

    // 1. The SAF File Creation Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/gpx+xml")
    ) { uri ->
        if (uri != null) {
            // The user selected a folder and saved the file. Now we write the data to it.
            coroutineScope.launch {
                val success = ExportEngine.exportDatabaseToGpx(context, uri, db)
                if (success) {
                    // 2. The Purge: Only delete if the export actually worked!
                    db.telemetryDao().wipeDatabase()
                }
            }
        }
    }

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

        // Update the button to trigger the export pipeline instead of just deleting
        Button(
            onClick = {
                // Generate a smart default filename with a unique short-timestamp identifier
                // Format: dekhreekh_export_2026-05-14_1530.gpx
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"))
                exportLauncher.launch("dekhreekh_export_$timestamp.gpx")
            },
            enabled = rowCount > 0, // Disable if there's nothing to export
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("EXPORT GPX & PURGE VAULT", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
