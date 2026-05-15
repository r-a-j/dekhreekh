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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class TelemetryStatus { IDLE, INITIALIZING, SEARCHING, LOCKED }

class TrackingService : Service() {

    companion object {
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1

        // 1. The Raw Location Stream (For the Map)
        private val _locationStream = MutableStateFlow<Location?>(null)
        val locationStream = _locationStream.asStateFlow()

        private val _status = MutableStateFlow(TelemetryStatus.IDLE)
        val status = _status.asStateFlow()

        // 2. The Calculated Metric Streams (For the HUD)
        val elapsedSeconds = MutableStateFlow(0L)
        val distanceMeters = MutableStateFlow(0f)
        val currentPace = MutableStateFlow(0L) // Stored as Seconds per Kilometer
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DekhreekhDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var currentSessionId: String = ""
    
    private var isTracking = false
    private var timerJob: Job? = null
    private var lastValidLocation: Location? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            _status.value = TelemetryStatus.LOCKED
            val location = result.lastLocation ?: return

            // 1. Pipe to Map (UI applies its own visual filter)
            _locationStream.value = location
            
            // 2. Pipe to SQLite Vault (Save the raw, unfiltered truth)
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

            // 3. Telemetry Math: Calculate Distance
            lastValidLocation?.let { lastLoc ->
                val distanceDelta = lastLoc.distanceTo(location)
                
                // DRIFT FILTER: Only add distance if accuracy is good AND you moved > 2 meters
                if (location.accuracy < 15f && distanceDelta > 2.0f) {
                    distanceMeters.value += distanceDelta
                    lastValidLocation = location
                    recalculatePace()
                }
            } ?: run {
                // Lock the very first coordinate as the starting point
                if (location.accuracy < 15f) {
                    lastValidLocation = location
                }
            }

            android.util.Log.d("TrackingService", "Greedy Telemetry: ${location.latitude}, ${location.longitude} (±${location.accuracy}m)")
        }
    }

    private fun startTimer() {
        isTracking = true
        timerJob = serviceScope.launch {
            while (isTracking) {
                delay(1000L)
                elapsedSeconds.value += 1
                recalculatePace()
            }
        }
    }

    private fun stopTimer() {
        isTracking = false
        timerJob?.cancel()
    }

    private fun recalculatePace() {
        val distKm = distanceMeters.value / 1000f
        
        // Prevent division by zero and wildly fluctuating pace during the first few seconds
        if (distKm > 0.05f) { 
            // Average Pace: Total Time / Total Distance
            val paceSecondsPerKm = (elapsedSeconds.value / distKm).toLong()
            currentPace.value = paceSecondsPerKm
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

        // Reset metrics
        elapsedSeconds.value = 0L
        distanceMeters.value = 0f
        currentPace.value = 0L
        lastValidLocation = null

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

        startTimer()
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
        // 1. Kill the Hardware Listeners
        stopTimer()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        
        // 2. Tear down the Foreground Notification
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        // 3. Purge the Live State (Ready for the next session)
        _status.value = TelemetryStatus.IDLE
        _locationStream.value = null
        distanceMeters.value = 0f
        currentPace.value = 0L
        elapsedSeconds.value = 0L
        lastValidLocation = null
        isTracking = false
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Active Tracking", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        stopTrackingService()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
