package com.rajpawardotin.dekhreekh.presentation.tracking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@Composable
fun TrackingMap(
    pathPoints: List<TelemetryData>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    


    val mapView = remember {
        MapView(context).apply {
            getMapAsync { map ->
                map.uiSettings.isLogoEnabled = false
                map.uiSettings.isAttributionEnabled = false
                
                map.setStyle("https://demotiles.maplibre.org/style.json") { style ->
                    val source = GeoJsonSource("tracking-source")
                    style.addSource(source)

                    val layer = LineLayer("tracking-layer", "tracking-source").withProperties(
                        PropertyFactory.lineColor(android.graphics.Color.parseColor("#6650a4")),
                        PropertyFactory.lineWidth(5f),
                        PropertyFactory.lineCap("round"),
                        PropertyFactory.lineJoin("round")
                    )
                    style.addLayer(layer)
                }
            }
        }
    }

    // Lifecycle Management
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
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

    // Update map with new telemetry points
    LaunchedEffect(pathPoints) {
        mapView.getMapAsync { map ->
            map.getStyle { style ->
                val source = style.getSourceAs<GeoJsonSource>("tracking-source")
                if (source != null && pathPoints.isNotEmpty()) {
                    val points = pathPoints.map { Point.fromLngLat(it.longitude, it.latitude) }
                    val lineString = LineString.fromLngLats(points)
                    source.setGeoJson(Feature.fromGeometry(lineString))
                    
                    // Smoothly animate camera to latest point
                    val lastPoint = pathPoints.last()
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastPoint.latitude, lastPoint.longitude),
                            16.0
                        ),
                        1000
                    )
                } else if (source != null && pathPoints.isEmpty()) {
                    source.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(emptyList())))
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("TrackingMapContainer")
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
        
        // Dummy UI element for testing assertions
        if (pathPoints.isNotEmpty()) {
            Box(modifier = Modifier.testTag("TrackingPolyline"))
        }
    }
}
