package com.rajpawardotin.dekhreekh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.rajpawardotin.dekhreekh.presentation.navigation.AppNavigation
import com.rajpawardotin.dekhreekh.ui.theme.DekhreekhTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DekhreekhTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
