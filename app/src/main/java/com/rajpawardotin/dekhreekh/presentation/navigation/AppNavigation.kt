package com.rajpawardotin.dekhreekh.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

import com.rajpawardotin.dekhreekh.presentation.dashboard.DashboardScreen
import androidx.compose.material3.Text

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.androidx.compose.koinViewModel
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import com.rajpawardotin.dekhreekh.presentation.vault.VaultScreen
import org.koin.core.parameter.parametersOf

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.widget.Toast
import kotlinx.coroutines.launch
import com.rajpawardotin.dekhreekh.utils.ImportEngine

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

            var showVault by remember { mutableStateOf(false) }

            // Handle system back gesture when Vault overlay is open
            BackHandler(enabled = showVault) {
                showVault = false
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Keep the Dashboard/Map layout alive in composition underneath
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
                    onNavigateToVault = { showVault = true }
                )

                // Render Vault Screen as an instantaneous overlay on top of Dashboard
                if (showVault) {
                    val vaultViewModel: com.rajpawardotin.dekhreekh.presentation.vault.VaultViewModel = koinViewModel()
                    val vaultState by vaultViewModel.uiState.collectAsState()
                    val sortOrder by vaultViewModel.sortOrder.collectAsState()
                    val activeTagFilter by vaultViewModel.activeTagFilter.collectAsState()
                    val hideLowActivity by vaultViewModel.hideLowActivity.collectAsState()
                    val tagUsageCounts by vaultViewModel.tagUsageCounts.collectAsState()
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()

                    // Pending import URI (held until user confirms name)
                    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
                    var importNameInput by remember { mutableStateOf("") }

                    // Session export state
                    var exportSessionId by remember { mutableStateOf<String?>(null) }
                    var exportSessionName by remember { mutableStateOf<String?>(null) }

                    val importLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: android.net.Uri? ->
                        uri?.let { pendingImportUri = it; importNameInput = "" }
                    }

                    val exportLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument("application/gpx+xml")
                    ) { uri: android.net.Uri? ->
                        uri?.let { destUri ->
                            val sid = exportSessionId ?: return@let
                            val sname = exportSessionName
                            scope.launch {
                                val success = com.rajpawardotin.dekhreekh.utils.ExportEngine.exportSessionToGpx(
                                    context = context,
                                    uri = destUri,
                                    sessionId = sid,
                                    sessionName = sname,
                                    sessionRepository = vaultViewModel.sessionRepository
                                )
                                if (success) Toast.makeText(context, "Session exported!", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    // Import name confirmation dialog
                    if (pendingImportUri != null) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { pendingImportUri = null },
                            containerColor = androidx.compose.ui.graphics.Color(0xFF13131F),
                            title = {
                                androidx.compose.material3.Text(
                                    "Name This Session",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            },
                            text = {
                                androidx.compose.material3.OutlinedTextField(
                                    value = importNameInput,
                                    onValueChange = { importNameInput = it },
                                    label = { androidx.compose.material3.Text("Session Name (optional)") },
                                    singleLine = true,
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFFD4FF00),
                                        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFFD4FF00),
                                        cursorColor = androidx.compose.ui.graphics.Color(0xFFD4FF00),
                                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0x33FFFFFF),
                                        focusedTextColor = androidx.compose.ui.graphics.Color.White,
                                        unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                                        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF6B6B80)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ImportNameField")
                                )
                            },
                            confirmButton = {
                                androidx.compose.material3.TextButton(onClick = {
                                    val uri = pendingImportUri!!
                                    val name = importNameInput.takeIf { it.isNotBlank() }
                                    pendingImportUri = null
                                    scope.launch {
                                        val success = ImportEngine.importGpxToDatabase(
                                            context = context,
                                            uri = uri,
                                            repository = vaultViewModel.sessionRepository,
                                            customName = name
                                        )
                                        if (success) Toast.makeText(context, "Session imported!", Toast.LENGTH_SHORT).show()
                                        else Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    androidx.compose.material3.Text("Import", color = androidx.compose.ui.graphics.Color(0xFFD4FF00), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(onClick = { pendingImportUri = null }) {
                                    androidx.compose.material3.Text("Cancel", color = androidx.compose.ui.graphics.Color(0xFF6B6B80))
                                }
                            }
                        )
                    }

                    VaultScreen(
                        uiState = vaultState,
                        sortOrder = sortOrder,
                        activeTagFilter = activeTagFilter,
                        hideLowActivity = hideLowActivity,
                        tagUsageCounts = tagUsageCounts,
                        onBackClick = { showVault = false },
                        onSessionClick = { sessionId ->
                            navController.navigate(VaultDetailRoute(sessionId))
                        },
                        onImportClick = { importLauncher.launch("*/*") },
                        onExportClick = { sid, sname ->
                            exportSessionId = sid
                            exportSessionName = sname
                            exportLauncher.launch("${sname ?: "session"}.gpx")
                        },
                        onSortChange = vaultViewModel::setSortOrder,
                        onTagToggle = vaultViewModel::toggleTagFilter,
                        onClearTagFilter = vaultViewModel::clearTagFilter,
                        onHideLowActivityToggle = vaultViewModel::setHideLowActivity,
                        onRenameSession = { id, name, tags -> vaultViewModel.renameSession(id, name, tags) },
                        onDeleteSession = vaultViewModel::deleteSession,
                        onRenameTagGlobally = vaultViewModel::renameTagGlobally,
                        onDeleteTagGlobally = vaultViewModel::deleteTagGlobally
                    )
                }

            }
        }
        composable<VaultDetailRoute> { backStackEntry ->
            val route: VaultDetailRoute = backStackEntry.toRoute()
            val vaultDetailViewModel: com.rajpawardotin.dekhreekh.presentation.vaultdetail.VaultDetailViewModel = koinViewModel {
                parametersOf(route.sessionId)
            }
            val telemetryPath by vaultDetailViewModel.telemetryPath.collectAsState(initial = emptyList())
            val session by vaultDetailViewModel.session.collectAsState(initial = null)
            
            com.rajpawardotin.dekhreekh.presentation.vaultdetail.VaultDetailScreen(
                telemetryPath = telemetryPath,
                session = session,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
