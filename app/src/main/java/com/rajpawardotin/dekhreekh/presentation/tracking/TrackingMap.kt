package com.rajpawardotin.dekhreekh.presentation.tracking

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import io.github.raj.liquid.LiquidState
import io.github.raj.liquid.molecules.LiquidGlassCard
import androidx.compose.foundation.clickable

@Composable
fun TrackingMap(
    pathPoints: List<TelemetryData>,
    modifier: Modifier = Modifier,
    isStaticHistory: Boolean = false,
    selectedPoint: TelemetryData? = null,
    liquidState: LiquidState? = null,
    isOverlayOpen: Boolean = false,
    locateTrigger: Int = 0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        val options = MapLibreMapOptions.createFromAttributes(context, null).textureMode(true)
        MapView(context, options).apply {
            onCreate(null)
        }
    }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }

    val lastLocationState = remember { mutableStateOf<Location?>(null) }
    val initialLocationSent = remember { mutableStateOf(false) }

    // Remote locate trigger animation handler
    LaunchedEffect(locateTrigger) {
        if (locateTrigger > 0) {
            val map = mapLibreMap ?: return@LaunchedEffect
            
            // Fall back to LocationManager query if live state is null
            var loc = lastLocationState.value
            if (loc == null) {
                val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                loc = try {
                    locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                } catch (e: SecurityException) { null }
            }
            
            val targetLoc = loc ?: return@LaunchedEffect
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(targetLoc.latitude, targetLoc.longitude))
                        .zoom(16.5)
                        .tilt(55.0)
                        .build()
                ),
                1000
            )

            // Rapid color pulse animation on the location dot layers!
            val style = map.style ?: return@LaunchedEffect
            val pulseLayer = style.getLayerAs<CircleLayer>("user-location-pulse")
            val glowLayer = style.getLayerAs<CircleLayer>("user-location-glow")
            val coreLayer = style.getLayerAs<CircleLayer>("user-location-layer")
            
            if (pulseLayer != null && glowLayer != null && coreLayer != null) {
                val neonColors = listOf(
                    "#007AFF", // Cyan
                    "#0055FF", // Purple
                    "#007AFF", // Coral
                    "#0055FF", // Green
                    "#007AFF", // Yellow
                    "#0055FF"  // Back to Electric Blue
                )
                
                for (colorStr in neonColors) {
                    val parsedColor = AndroidColor.parseColor(colorStr)
                    coreLayer.setProperties(circleColor(parsedColor))
                    glowLayer.setProperties(circleColor(parsedColor), circleRadius(22f), circleOpacity(0.40f))
                    pulseLayer.setProperties(circleColor(parsedColor), circleRadius(38f), circleOpacity(0.20f))
                    
                    kotlinx.coroutines.delay(100) // 100ms
                    
                    // overshoot back
                    glowLayer.setProperties(circleRadius(16f), circleOpacity(0.25f))
                    pulseLayer.setProperties(circleRadius(28f), circleOpacity(0.10f))
                    
                    kotlinx.coroutines.delay(100)
                }
                
                // Reset to default Electric Blue
                val defaultColor = AndroidColor.parseColor("#0055FF")
                coreLayer.setProperties(circleColor(defaultColor))
                glowLayer.setProperties(circleColor(defaultColor), circleRadius(16f), circleOpacity(0.25f))
                pulseLayer.setProperties(circleColor(defaultColor), circleRadius(28f), circleOpacity(0.10f))
            }
        }
    }

    // Initialize MapLibre
    LaunchedEffect(mapView) {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        val lastKnown = if (!isStaticHistory) {
            try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) { null }
        } else null

        mapView.getMapAsync { map ->
            mapLibreMap = map
            // "fiord" style: dark-based but richly colorful — deep ocean blues, vivid road accents,
            // terrain relief. Best of both worlds: white text stays readable, map stays vibrant.
            map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                // ── Path source ───────────────────────────────────────────────
                val source = GeoJsonSource("tracking-path-source")
                style.addSource(source)

                // Layer 1: Wide outer bloom (biggest, most transparent) — the glow aura
                val lineGlowOuter = LineLayer("tracking-path-glow-outer", "tracking-path-source")
                .withProperties(
                    lineColor(AndroidColor.parseColor("#220077FF")), // Deep Electric Blue bloom
                    lineWidth(28f),
                    lineCap("round"),
                    lineJoin("round"),
                    lineOpacity(0.18f),
                    lineBlur(8f)
                )
                style.addLayer(lineGlowOuter)

                // Layer 2: Mid glow halo
                val lineGlowMid = LineLayer("tracking-path-glow-mid", "tracking-path-source")
                .withProperties(
                    lineColor(AndroidColor.parseColor("#5500AFFF")), // Vibrant Cyan-blue halo
                    lineWidth(14f),
                    lineCap("round"),
                    lineJoin("round"),
                    lineOpacity(0.35f),
                    lineBlur(4f)
                )
                style.addLayer(lineGlowMid)

                // Layer 3: Sharp bright core — the actual neon trail line
                val lineCore = LineLayer("tracking-path-layer", "tracking-path-source")
                .withProperties(
                    lineColor(AndroidColor.parseColor("#0055FF")), // Electric Blue core
                    lineWidth(5f),
                    lineCap("round"),
                    lineJoin("round"),
                    lineOpacity(1.0f)
                )
                style.addLayer(lineCore)

                // ── Location dot source ───────────────────────────────────────
                val circleSource = GeoJsonSource("user-location-source")
                style.addSource(circleSource)

                // Outer pulse ring — large, very transparent
                val circlePulse = CircleLayer("user-location-pulse", "user-location-source")
                .withProperties(
                    circleColor(AndroidColor.parseColor("#0055FF")),
                    circleRadius(28f),
                    circleOpacity(0.10f),
                    circleBlur(1f)
                )
                style.addLayer(circlePulse)

                // Inner glow ring
                val circleGlow = CircleLayer("user-location-glow", "user-location-source")
                .withProperties(
                    circleColor(AndroidColor.parseColor("#0055FF")),
                    circleRadius(16f),
                    circleOpacity(0.25f),
                    circleBlur(0.5f)
                )
                style.addLayer(circleGlow)

                // Core dot — sharp, bright
                val circleLayer = CircleLayer("user-location-layer", "user-location-source")
                .withProperties(
                    circleColor(AndroidColor.parseColor("#0055FF")),
                    circleRadius(8f),
                    circleStrokeColor(AndroidColor.WHITE),
                    circleStrokeWidth(2.5f),
                    circleOpacity(1.0f)
                )
                style.addLayer(circleLayer)

                if (!isStaticHistory) {
                    lastKnown?.let { loc ->
                        lastLocationState.value = loc
                        circleSource.setGeoJson(Feature.fromGeometry(Point.fromLngLat(loc.longitude, loc.latitude)))
                    }
                }

                isStyleLoaded = true
            }

            if (!isStaticHistory) {
                val initialLatLng = lastKnown?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(37.7749, -122.4194)
                map.moveCamera(CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(initialLatLng)
                        .zoom(16.5)   // slightly closer
                        .tilt(55.0)   // dramatic 3D perspective tilt
                        .bearing(0.0)
                        .build()
                ))
            }
        }
    }

    // Live GPS listener (only in non-history mode)
    LaunchedEffect(isStyleLoaded, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!isStyleLoaded) return@LaunchedEffect
        if (isStaticHistory) return@LaunchedEffect

        try {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
            val locationListener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    lastLocationState.value = loc
                    map.getStyle { style ->
                        val source = style.getSourceAs<GeoJsonSource>("user-location-source")
                        source?.setGeoJson(Feature.fromGeometry(Point.fromLngLat(loc.longitude, loc.latitude)))
                    }
                    if (!initialLocationSent.value) {
                        initialLocationSent.value = true
                        map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(loc.latitude, loc.longitude))
                                    .zoom(16.0)
                                    .tilt(60.0)
                                    .build()
                            ),
                            1000
                        )
                    }
                }
                @Deprecated("Required for older APIs")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, locationListener)
        } catch (e: SecurityException) {}
    }

    // Path line updates
    LaunchedEffect(pathPoints, mapLibreMap, isStyleLoaded) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!isStyleLoaded) return@LaunchedEffect

        map.getStyle { style ->
            val source = style.getSourceAs<GeoJsonSource>("tracking-path-source")
            if (pathPoints.isNotEmpty()) {
                val pointsList = pathPoints.map { Point.fromLngLat(it.longitude, it.latitude) }
                source?.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(pointsList)))
            } else {
                source?.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(emptyList())))
            }
        }

        // Auto-fit camera to track bounds in history mode
        if (isStaticHistory && pathPoints.size >= 2) {
            mapView.post {
                try {
                    val boundsBuilder = LatLngBounds.Builder()
                    pathPoints.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
                    val bounds = boundsBuilder.build()
                    mapLibreMap?.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, 80)
                    )
                } catch (e: Exception) {
                    // bounds build can fail if all points are identical
                    pathPoints.firstOrNull()?.let { pt ->
                        mapLibreMap?.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(pt.latitude, pt.longitude))
                                    .zoom(16.0)
                                    .build()
                            )
                        )
                    }
                }
            }
        }
    }

    // Selected point marker updates (history scrubbing)
    LaunchedEffect(selectedPoint, mapLibreMap, isStyleLoaded) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!isStyleLoaded) return@LaunchedEffect
        if (!isStaticHistory) return@LaunchedEffect

        val point = selectedPoint ?: pathPoints.firstOrNull() ?: return@LaunchedEffect
        map.getStyle { style ->
            val source = style.getSourceAs<GeoJsonSource>("user-location-source")
            source?.setGeoJson(Feature.fromGeometry(Point.fromLngLat(point.longitude, point.latitude)))
        }
    }

    // Lifecycle binding for Native MapView
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("TrackingMapContainer")
    ) {
        AndroidView(
            factory = {
                mapView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )



        if (pathPoints.isNotEmpty()) {
            Box(modifier = Modifier.testTag("TrackingPolyline"))
        }
    }
}
