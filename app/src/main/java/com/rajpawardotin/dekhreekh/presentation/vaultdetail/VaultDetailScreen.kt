package com.rajpawardotin.dekhreekh.presentation.vaultdetail

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingMap
import io.github.raj.liquid.liquefiable
import io.github.raj.liquid.molecules.LiquidGlassCard
import io.github.raj.liquid.tokens.GlassComponentTokens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDetailScreen(
    telemetryPath: List<TelemetryData>,
    session: WorkoutSession? = null,
    liquidState: io.github.raj.liquid.LiquidState,
    onBackClick: () -> Unit = {}
) {
    val darkBg = Color(0xFF0D0D14)
    val glassBg = Color(0xEA12121E)
    val voltGreen = Color(0xFFD4FF00)
    val dimText = Color(0xFF6B6B80)

    // Timeline scrubbing state (0f = start, 1f = end)
    var sliderPosition by remember { mutableStateOf(0f) }
    val selectedIndex = if (telemetryPath.isEmpty()) 0
        else (sliderPosition * (telemetryPath.size - 1)).roundToInt().coerceIn(0, telemetryPath.size - 1)
    val selectedPoint = telemetryPath.getOrNull(selectedIndex)

    // Animated slider alpha for subtle pulse
    val sliderAlpha by animateFloatAsState(
        targetValue = if (telemetryPath.size > 1) 1f else 0.3f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "sliderAlpha"
    )

    // Compute cumulative distance up to selectedIndex
    val cumulativeDistanceM = remember(selectedIndex, telemetryPath) {
        if (telemetryPath.size < 2) 0f
        else {
            var dist = 0f
            for (i in 0 until minOf(selectedIndex, telemetryPath.size - 1)) {
                val p1 = telemetryPath[i]
                val p2 = telemetryPath[i + 1]
                dist += haversine(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
            }
            dist
        }
    }

    val timestampStr = selectedPoint?.let {
        Instant.ofEpochMilli(it.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
    } ?: "--"

    val dateStr = (selectedPoint?.let { Instant.ofEpochMilli(it.timestamp) }
        ?: session?.let { Instant.ofEpochMilli(it.startTime) })
        ?.atZone(ZoneId.systemDefault())
        ?.format(DateTimeFormatter.ofPattern("EEE, MMM dd yyyy")) ?: "--"

    val speedKmh = selectedPoint?.let { String.format("%.1f", it.speed * 3.6f) } ?: "0.0"
    val elevationM = selectedPoint?.let { String.format("%.0f", it.altitude) } ?: "0"
    val distKmStr = String.format("%.2f", cumulativeDistanceM / 1000f)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = dateStr,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = timestampStr,
                            fontSize = 11.sp,
                            color = voltGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // Let the top scrim show through
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // --- Map Layer with top + bottom gradient scrims ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent() // Draw map first
                        // Top status bar dark fade gradient (black face)
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFA0A0A0F), Color.Transparent),
                                startY = 0f,
                                endY = 160.dp.toPx()
                            )
                        )
                        // Bottom navigation bar dark fade gradient (black face)
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFA0A0A0F)),
                                startY = this.size.height - 380.dp.toPx(),
                                endY = this.size.height
                            )
                        )
                    }
            ) {
                // Map background layer for history path playback
                if (telemetryPath.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().liquefiable(liquidState)) {
                        TrackingMap(
                            pathPoints = telemetryPath,
                            modifier = Modifier.fillMaxSize(),
                            isStaticHistory = true,
                            selectedPoint = selectedPoint,
                            liquidState = liquidState
                        )
                    }
                }
            }

            // --- Bottom Panel ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding() // Protect against bottom navigation bar overlap
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {

                // --- Timeline Scrubber Card ---
                if (telemetryPath.size > 1) {
                    LiquidGlassCard(
                        liquidState = liquidState,
                        shape = RoundedCornerShape(20.dp),
                        tokens = GlassComponentTokens(
                            refraction = 0.18f,
                            curve = 1.00f,
                            frost = 16.dp,
                            dispersion = 0.16f,
                            edge = 0.0f,
                            tintAlpha = 0.70f, // Increased obsidian backing opacity for light map style contrast
                            saturation = 1.65f,
                            contrast = 1.65f
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = voltGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TIMELINE",
                                    color = voltGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${selectedIndex + 1} / ${telemetryPath.size}",
                                    color = dimText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = sliderPosition,
                                onValueChange = { sliderPosition = it },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = voltGreen,
                                    activeTrackColor = voltGreen,
                                    inactiveTrackColor = Color(0xFF2A2A3A)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("TimelineSlider")
                            )
                            // Start / End time labels
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = telemetryPath.firstOrNull()?.let {
                                        Instant.ofEpochMilli(it.timestamp)
                                            .atZone(ZoneId.systemDefault())
                                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                                    } ?: "",
                                    color = dimText,
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = telemetryPath.lastOrNull()?.let {
                                        Instant.ofEpochMilli(it.timestamp)
                                            .atZone(ZoneId.systemDefault())
                                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                                    } ?: "",
                                    color = dimText,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // --- Stat Metrics Card ---
                LiquidGlassCard(
                    liquidState = liquidState,
                    shape = RoundedCornerShape(20.dp),
                    tokens = GlassComponentTokens(
                        refraction = 0.18f,
                        curve = 1.00f,
                        frost = 16.dp,
                        dispersion = 0.16f,
                        edge = 0.0f,
                        tintAlpha = 0.70f, // Increased obsidian backing opacity for light map style contrast
                        saturation = 1.65f,
                        contrast = 1.65f
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("DetailMetricsCard")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            icon = { Icon(Icons.Default.DirectionsRun, null, tint = voltGreen, modifier = Modifier.size(20.dp)) },
                            value = distKmStr,
                            unit = "km",
                            label = "DISTANCE"
                        )
                        StatDivider()
                        StatItem(
                            icon = { Icon(Icons.Default.Speed, null, tint = Color(0xFFB06CFF), modifier = Modifier.size(20.dp)) },
                            value = speedKmh,
                            unit = "km/h",
                            label = "SPEED"
                        )
                        StatDivider()
                        StatItem(
                            icon = { Icon(Icons.Default.Height, null, tint = Color(0xFF6CCDFF), modifier = Modifier.size(20.dp)) },
                            value = elevationM,
                            unit = "m",
                            label = "ELEVATION"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: @Composable () -> Unit,
    value: String,
    unit: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                color = Color(0xFF6B6B80),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Text(
            text = label,
            color = Color(0xFF6B6B80),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(Color(0x1AFFFFFF))
    )
}

/**
 * Haversine distance in meters between two coordinate pairs.
 */
private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2).let { it * it } +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2).let { it * it }
    return (R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))).toFloat()
}
