package com.rajpawardotin.dekhreekh.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.rajpawardotin.dekhreekh.data.DekhreekhDatabase
import com.rajpawardotin.dekhreekh.data.TelemetryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class TelemetryStatus { IDLE, INITIALIZING, SEARCHING, LOCKED }

class TrackingService : Service() {

    companion object {
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1

        // GREEDY DATA PIPELINE: Emits raw hardware telemetry objects
        private val _locationStream = MutableStateFlow<Location?>(null)
        val locationStream = _locationStream.asStateFlow()

        private val _status = MutableStateFlow(TelemetryStatus.IDLE)
        val status = _status.asStateFlow()
        
        private val _pathPoints = MutableStateFlow<List<org.maplibre.geojson.Point>>(emptyList())
        val pathPoints = _pathPoints.asStateFlow()
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DekhreekhDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var currentSessionId: String = ""

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            _status.value = TelemetryStatus.LOCKED
            for (location in result.locations) {
                // NEVER FILTER HERE: The data science layer needs the "spaghetti" truth
                _locationStream.value = location
                
                // Persist to the SQLite Vault (Unfiltered!)
                serviceScope.launch {
                    val point = TelemetryPoint(
                        sessionId = currentSessionId,
                        timestamp = location.time,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        accuracy = location.accuracy,
                        speed = location.speed
                    )
                    database.telemetryDao().insertPoint(point)
                }

                android.util.Log.d("TrackingService", "Greedy Telemetry: ${location.latitude}, ${location.longitude} (±${location.accuracy}m)")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startForegroundService()
            ACTION_STOP_TRACKING -> stopTrackingService()
        }
        return START_STICKY 
    }

    @SuppressLint("MissingPermission")
    private fun startForegroundService() {
        _status.value = TelemetryStatus.INITIALIZING
        currentSessionId = UUID.randomUUID().toString() 
        database = DekhreekhDatabase.getDatabase(applicationContext)

        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Dekhreekh Active")
            .setContentText("Telemetry Engine Running")
            .build()

        startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Instant Warm-up
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                if (_locationStream.value == null) {
                    _locationStream.value = it
                    _status.value = TelemetryStatus.SEARCHING
                }
            }
        }

        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(0f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            _status.value = TelemetryStatus.IDLE
        }
    }

    private fun stopTrackingService() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _status.value = TelemetryStatus.IDLE
        _locationStream.value = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Active Tracking", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
