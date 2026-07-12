package com.rajpawardotin.dekhreekh.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.rajpawardotin.dekhreekh.presentation.dashboard.DashboardScreen
import androidx.compose.material3.Text

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.androidx.compose.koinViewModel
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = DashboardRoute) {
        composable<DashboardRoute> {
            val viewModel: TrackingViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val livePath by viewModel.livePath.collectAsState()
            
            val context = androidx.compose.ui.platform.LocalContext.current
            
            // Runtime permissions logic
            val requiredPermissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }

            var hasPermission by remember { 
                mutableStateOf(
                    requiredPermissions.all {
                        androidx.core.content.ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    }
                ) 
            }
            
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                hasPermission = permissions.entries.all { it.value }
            }

            DashboardScreen(
                uiState = uiState,
                livePath = livePath,
                hasLocationPermission = hasPermission,
                onRequestPermission = { permissionLauncher.launch(requiredPermissions) },
                onIntent = { intent -> 
                    viewModel.onIntent(intent)
                    if (intent == com.rajpawardotin.dekhreekh.presentation.tracking.TrackingIntent.IgniteEngine) {
                        if (hasPermission) {
                            val serviceIntent = android.content.Intent(context, com.rajpawardotin.dekhreekh.service.TrackingService::class.java).apply {
                                action = com.rajpawardotin.dekhreekh.service.TrackingService.ACTION_START_TRACKING
                            }
                            context.startForegroundService(serviceIntent)
                        }
                    } else if (intent == com.rajpawardotin.dekhreekh.presentation.tracking.TrackingIntent.HaltEngine) {
                        val stopIntent = android.content.Intent(context, com.rajpawardotin.dekhreekh.service.TrackingService::class.java).apply {
                            action = com.rajpawardotin.dekhreekh.service.TrackingService.ACTION_STOP_TRACKING
                        }
                        context.startService(stopIntent)
                    }
                },
                onNavigateToVault = { navController.navigate(VaultRoute) }
            )
        }
        composable<VaultRoute> {
            val vaultViewModel: com.rajpawardotin.dekhreekh.presentation.vault.VaultViewModel = koinViewModel()
            val vaultState by vaultViewModel.uiState.collectAsState()
            com.rajpawardotin.dekhreekh.presentation.vault.VaultScreen(uiState = vaultState)
        }
    }
}
