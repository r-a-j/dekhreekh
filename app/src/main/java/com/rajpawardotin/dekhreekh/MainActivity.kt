package com.rajpawardotin.dekhreekh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rajpawardotin.dekhreekh.ui.theme.DekhreekhTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Forces the app to draw under the status and navigation bars
        enableEdgeToEdge()

        setContent {
            DekhreekhTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background // Forces OLED Black
                ) { innerPadding ->
                    // Temporary placeholder until we build the real UI
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DEKHREEKH CORE ONLINE",
                            color = MaterialTheme.colorScheme.primary, // Matrix Green
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}