package com.rajpawardotin.dekhreekh

import android.app.Application
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class DekhreekhApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize MapLibre SDK before any MapView is created
        // Since we use OpenFreeMap, we don't need a Mapbox/MapTiler API key
        MapLibre.getInstance(this, null, WellKnownTileServer.MapLibre)
    }
}
