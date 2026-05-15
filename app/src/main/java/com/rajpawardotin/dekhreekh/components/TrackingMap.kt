package com.rajpawardotin.dekhreekh.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@Composable
fun TrackingMap(
    modifier: Modifier = Modifier,
    pathCoordinates: List<Point>,
    focusTrigger: Int
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // We MUST keep the MapView instance stable across re-compositions
    val mapView = remember { 
        MapView(context).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
        }
    }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }
    var hasInitialFocus by remember { mutableStateOf(false) }

    // Initialize the map ONLY once
    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            mapLibreMap = map
            map.setStyle("https://tiles.openfreemap.org/styles/dark") { style ->
                // Data Source for the Path
                val source = GeoJsonSource("tracking-path-source")
                style.addSource(source)

                // Visual Layer
                val lineLayer = LineLayer("tracking-path-layer", "tracking-path-source")
                    .withProperties(
                        lineColor(AndroidColor.parseColor("#00FF41")),
                        lineWidth(6f),
                        lineCap("round"),
                        lineJoin("round"),
                        lineOpacity(0.9f)
                    )
                style.addLayer(lineLayer)

                // User Location Dot Layer
                val circleSource = GeoJsonSource("user-location-source")
                style.addSource(circleSource)
                val circleLayer = org.maplibre.android.style.layers.CircleLayer("user-location-layer", "user-location-source")
                    .withProperties(
                        circleColor(AndroidColor.parseColor("#00FF41")),
                        circleRadius(8f),
                        circleStrokeColor(AndroidColor.WHITE),
                        circleStrokeWidth(2f)
                    )
                style.addLayer(circleLayer)
                
                isStyleLoaded = true
            }

            // Initial Camera Settings: Start at user location if already known to avoid pan from (0,0)
            val initialLatLng = if (pathCoordinates.isNotEmpty()) {
                val p = pathCoordinates.last()
                LatLng(p.latitude(), p.longitude())
            } else {
                LatLng(0.0, 0.0)
            }

            map.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(initialLatLng)
                    .zoom(16.0)
                    .tilt(60.0) // Aggressive 3D angle
                    .build()
            ))

            if (pathCoordinates.isNotEmpty()) {
                hasInitialFocus = true
            }
        }
    }

    // Reactive Update: ONLY update the data, don't touch the style/layers
    LaunchedEffect(pathCoordinates, mapLibreMap, isStyleLoaded) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!isStyleLoaded) return@LaunchedEffect

        if (pathCoordinates.isNotEmpty()) {
            val latestPoint = pathCoordinates.last()

            // Update the Path Line
            map.getStyle { style ->
                if (pathCoordinates.size > 1) {
                    val source = style.getSourceAs<GeoJsonSource>("tracking-path-source")
                    source?.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(pathCoordinates)))
                }
                
                val dotSource = style.getSourceAs<GeoJsonSource>("user-location-source")
                dotSource?.setGeoJson(Feature.fromGeometry(latestPoint))
            }

            // AUTO-PAN ON FIRST LOCK
            if (!hasInitialFocus) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLng(LatLng(latestPoint.latitude(), latestPoint.longitude())),
                    1000
                )
                hasInitialFocus = true
            }
        }
    }

    // Handle the "Locate Myself" Camera Snap
    LaunchedEffect(focusTrigger) {
        if (focusTrigger > 0 && pathCoordinates.isNotEmpty()) {
            val latestPoint = pathCoordinates.last()
            mapLibreMap?.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(latestPoint.latitude(), latestPoint.longitude()))
                        .zoom(17.0) // Zoom in tight
                        .tilt(60.0) // Aggressive flagship 3D tilt
                        .build()
                ),
                1200 // 1.2 second cinematic sweep
            )
        }
    }

    // Lifecycle Binding
    DisposableEffect(lifecycleOwner) {
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

    AndroidView(
        factory = { mapView },
        modifier = modifier
            .fillMaxSize()
            .background(ComposeColor.Black),
        update = { /* Updates are handled via LaunchedEffect for granular control */ }
    )
}
