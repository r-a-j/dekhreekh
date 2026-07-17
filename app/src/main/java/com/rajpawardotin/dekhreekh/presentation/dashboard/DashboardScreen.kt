package com.rajpawardotin.dekhreekh.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import io.github.raj.liquid.rememberLiquidState
import io.github.raj.liquid.liquefiable
import io.github.raj.liquid.molecules.LiquidGlassCard
import io.github.raj.liquid.molecules.LiquidButton

@Composable
fun DashboardScreen(
    uiState: TrackingState,
    livePath: List<TelemetryData> = emptyList(),
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onIntent: (TrackingIntent) -> Unit,
    onNavigateToVault: () -> Unit,
    liquidState: io.github.raj.liquid.LiquidState,
    isOverlayOpen: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cyanAccent = MaterialTheme.colorScheme.primary
    val darkBg = MaterialTheme.colorScheme.background
    val glassBg = Color(0xF2181824)
    val purpleAccent = MaterialTheme.colorScheme.secondary // Maps to Steel Gray
    val activeRed = Color(0xFFFF3B30)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    // liquidState is now passed from parent

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
                            .liquefiable(liquidState)
                            .drawWithContent {
                                drawContent() // Draw map first
                                
                                if (!isOverlayOpen) {
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

                                    // Draw radial glow under bottom control panel card for liquid glass refraction
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(cyanAccent.copy(alpha = 0.22f), Color.Transparent),
                                            center = Offset(size.width / 2f, size.height - 120.dp.toPx()),
                                            radius = 220.dp.toPx()
                                        ),
                                        center = Offset(size.width / 2f, size.height - 120.dp.toPx()),
                                        radius = 220.dp.toPx()
                                    )
                                }
                            }
                    ) {
                        TrackingMap(
                            pathPoints = livePath,
                            modifier = Modifier.fillMaxSize(),
                            liquidState = liquidState,
                            isOverlayOpen = isOverlayOpen
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .liquefiable(liquidState)
                            .background(darkBg)
                            .drawBehind {
                                // Draw radial glow under permission prompt card for liquid glass refraction
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(cyanAccent.copy(alpha = 0.2f), Color.Transparent),
                                        center = Offset(size.width / 2f, size.height - 150.dp.toPx()),
                                        radius = 220.dp.toPx()
                                    ),
                                    center = Offset(size.width / 2f, size.height - 150.dp.toPx()),
                                    radius = 220.dp.toPx()
                                )
                            }
                    )
                }

                // 2. Floating Hamburger Menu Icon (Top-Left)
                if (hasLocationPermission && !isOverlayOpen) {
                    LiquidGlassCard(
                        liquidState = liquidState,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(top = 16.dp, start = 16.dp)
                            .size(48.dp)
                            .clickable {
                                scope.launch { drawerState.open() }
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = cyanAccent
                            )
                        }
                    }
                }

                // 3. Unified Dashboard Card Panel (Bottom Area)
                if (!isOverlayOpen) {
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
                        LiquidGlassCard(
                            liquidState = liquidState,
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(24.dp),
                            tokens = io.github.raj.liquid.tokens.GlassComponentTokens(
                                refraction = 0.18f,
                                curve = 1.00f,
                                frost = 9.94.dp,
                                dispersion = 0.16f,
                                edge = 0.0f,
                                tintAlpha = 0.00f,
                                saturation = 1.65f,
                                contrast = 1.65f
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                        ) {
                            Column(
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
                                LiquidButton(
                                    onClick = onRequestPermission,
                                    liquidState = liquidState,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("GRANT ACCESS", fontWeight = FontWeight.Bold, color = cyanAccent)
                                }
                            }
                        }
                    } else {
                        // Main Bottom Glassmorphism Control Panel
                        LiquidGlassCard(
                            liquidState = liquidState,
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(20.dp),
                            tokens = io.github.raj.liquid.tokens.GlassComponentTokens(
                                refraction = 0.18f,
                                curve = 1.00f,
                                frost = 9.94.dp,
                                dispersion = 0.16f,
                                edge = 0.0f,
                                tintAlpha = 0.00f,
                                saturation = 1.65f,
                                contrast = 1.65f
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
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
                                        LiquidButton(
                                            onClick = { onIntent(TrackingIntent.IgniteEngine) },
                                            liquidState = liquidState,
                                            shape = RoundedCornerShape(28.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                        ) {
                                            Text(
                                                text = "START", 
                                                fontWeight = FontWeight.Black, 
                                                letterSpacing = 2.sp, 
                                                fontSize = 16.sp,
                                                color = cyanAccent
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
                                        LiquidButton(
                                            onClick = { onIntent(TrackingIntent.HaltEngine) },
                                            liquidState = liquidState,
                                            shape = RoundedCornerShape(28.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                        ) {
                                            Text(
                                                text = "STOP", 
                                                fontWeight = FontWeight.Black, 
                                                letterSpacing = 2.sp, 
                                                fontSize = 16.sp,
                                                color = activeRed
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
}

