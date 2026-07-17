package com.rajpawardotin.dekhreekh.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingIntent
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingState
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingMap
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    uiState: TrackingState,
    livePath: List<TelemetryData> = emptyList(),
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onIntent: (TrackingIntent) -> Unit,
    onNavigateToVault: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val darkBg = MaterialTheme.colorScheme.background
    val glassBg = Color(0xF2181824)
    val cyanAccent = MaterialTheme.colorScheme.primary // Maps to Volt Green
    val purpleAccent = MaterialTheme.colorScheme.secondary // Maps to Steel Gray
    val activeRed = Color(0xFFFF3B30)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false, // Disable edge swipe to prevent conflicts with map panning/zooming
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = darkBg,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = "DEKHREEKH",
                    color = cyanAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(16.dp))
                // 1. Map Dashboard (Home)
                NavigationDrawerItem(
                    label = { Text("Map Dashboard", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                    selected = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0x1AD4FF00), // Muted Volt Green
                        selectedTextColor = cyanAccent,
                        selectedIconColor = cyanAccent
                    ),
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. History Vault
                NavigationDrawerItem(
                    label = { Text("History Vault", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToVault()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = Color.LightGray,
                        unselectedIconColor = cyanAccent
                    ),
                    icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Vault") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Exit Application
                NavigationDrawerItem(
                    label = { Text("Exit Application", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        // Stop telemetry service if active before exiting
                        if (uiState is TrackingState.Tracking) {
                            val stopIntent = android.content.Intent(context, com.rajpawardotin.dekhreekh.service.TrackingService::class.java).apply {
                                action = com.rajpawardotin.dekhreekh.service.TrackingService.ACTION_STOP_TRACKING
                            }
                            context.startService(stopIntent)
                        } else {
                            val serviceIntent = android.content.Intent(context, com.rajpawardotin.dekhreekh.service.TrackingService::class.java)
                            context.stopService(serviceIntent)
                        }
                        (context as? android.app.Activity)?.finish()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = Color(0xFFFF5252),
                        unselectedIconColor = Color(0xFFFF5252)
                    ),
                    icon = { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Exit") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Background Map Layer (fully fullscreen with click-through gradient overlays)
                if (hasLocationPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent() // Draw map first
                                
                                // Draw top status bar dark fade gradient
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xE608080C), Color.Transparent),
                                        startY = 0f,
                                        endY = 140.dp.toPx()
                                    )
                                )

                                // Draw bottom navigation bar dark fade gradient
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xE608080C)),
                                        startY = size.height - 260.dp.toPx(),
                                        endY = size.height
                                    )
                                )
                            }
                    ) {
                        TrackingMap(
                            pathPoints = livePath,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(darkBg)
                    )
                }

                // 2. Floating Hamburger Menu Icon (Top-Left)
                if (hasLocationPermission) {
                    IconButton(
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(top = 16.dp, start = 16.dp)
                            .background(glassBg, shape = CircleShape)
                            .border(1.dp, Color(0x1AFFFFFF), shape = CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = cyanAccent
                        )
                    }
                }

                // 3. Unified Dashboard Card Panel (Bottom Area)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (!hasLocationPermission) {
                        // Permission Prompt Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = glassBg),
                            border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "GPS Access Required",
                                    color = cyanAccent,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Please enable GPS permission to track runs and compute edge telemetry.",
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onRequestPermission,
                                    colors = ButtonDefaults.buttonColors(containerColor = cyanAccent, contentColor = Color.Black),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("GRANT ACCESS", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Main Bottom Glassmorphism Control Panel
                        Card(
                            colors = CardDefaults.cardColors(containerColor = glassBg),
                            border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when (uiState) {
                                    is TrackingState.Idle, is TrackingState.Ready -> {
                                        // Standby Layout: Clean, minimal title and single START button
                                        Text(
                                            text = "STANDBY MODE",
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { onIntent(TrackingIntent.IgniteEngine) },
                                            colors = ButtonDefaults.buttonColors(containerColor = cyanAccent, contentColor = Color.Black),
                                            shape = RoundedCornerShape(28.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(28.dp))
                                        ) {
                                            Text(
                                                text = "START", 
                                                fontWeight = FontWeight.Black, 
                                                letterSpacing = 2.sp, 
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                    is TrackingState.Tracking -> {
                                        // Active Tracking Layout: Clean, side-by-side metrics with dividers
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Distance Section
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f).testTag("MetricCard_Distance"),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsRun,
                                                    contentDescription = "Distance",
                                                    tint = cyanAccent,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "${uiState.distance}",
                                                        color = Color.White,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Text(
                                                        text = "METERS",
                                                        color = Color.Gray,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }

                                            // Vertical Divider
                                            VerticalDivider(
                                                color = Color(0x1AFFFFFF),
                                                modifier = Modifier.height(40.dp)
                                            )

                                            // Pace Section
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f).testTag("MetricCard_Pace"),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Speed,
                                                    contentDescription = "Pace",
                                                    tint = purpleAccent,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "${uiState.pace}",
                                                        color = Color.White,
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Text(
                                                        text = "PACE (s/km)",
                                                        color = Color.Gray,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Stop Button: Solid red with bold white text
                                        Button(
                                            onClick = { onIntent(TrackingIntent.HaltEngine) },
                                            colors = ButtonDefaults.buttonColors(containerColor = activeRed, contentColor = Color.White),
                                            shape = RoundedCornerShape(28.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(28.dp))
                                        ) {
                                            Text(
                                                text = "STOP", 
                                                fontWeight = FontWeight.Black, 
                                                letterSpacing = 2.sp, 
                                                fontSize = 16.sp
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
    }
}
