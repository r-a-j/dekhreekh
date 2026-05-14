package com.rajpawardotin.dekhreekh.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    pathCoordinates: List<Point>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // We MUST keep the MapView instance stable across re-compositions
    val mapView = remember { MapView(context) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }

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

            // Initial Camera Settings
            map.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .zoom(16.0)
                    .tilt(60.0) // Aggressive 3D angle
                    .build()
            ))
        }
    }

    // Reactive Update: ONLY update the data, don't touch the style/layers
    LaunchedEffect(pathCoordinates, mapLibreMap, isStyleLoaded) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!isStyleLoaded) return@LaunchedEffect

        if (pathCoordinates.isNotEmpty()) {
            val latestPoint = pathCoordinates.last()
            val latestLatLng = LatLng(latestPoint.latitude(), latestPoint.longitude())

            // Update the Path Line
            if (pathCoordinates.size > 1) {
                map.getStyle { style ->
                    val source = style.getSourceAs<GeoJsonSource>("tracking-path-source")
                    source?.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(pathCoordinates)))
                    
                    val dotSource = style.getSourceAs<GeoJsonSource>("user-location-source")
                    dotSource?.setGeoJson(Feature.fromGeometry(latestPoint))
                }
            } else if (pathCoordinates.size == 1) {
                map.getStyle { style ->
                    val dotSource = style.getSourceAs<GeoJsonSource>("user-location-source")
                    dotSource?.setGeoJson(Feature.fromGeometry(latestPoint))
                }
            }

            // SMOOTH PAN: 1000ms animation matches the 1Hz update interval for fluid motion
            map.animateCamera(CameraUpdateFactory.newLatLng(latestLatLng), 1000)
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
        modifier = modifier.fillMaxSize(),
        update = { /* Updates are handled via LaunchedEffect for granular control */ }
    )
}
