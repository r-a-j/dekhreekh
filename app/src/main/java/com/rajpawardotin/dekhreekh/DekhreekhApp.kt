package com.rajpawardotin.dekhreekh

import android.app.Application
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

import com.rajpawardotin.dekhreekh.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class DekhreekhApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@DekhreekhApp)
            modules(appModule)
        }
        
        // Initialize MapLibre SDK before any MapView is created
        // Since we use OpenFreeMap, we don't need a Mapbox/MapTiler API key
        MapLibre.getInstance(this, null, WellKnownTileServer.MapLibre)
    }
}
