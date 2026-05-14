package com.rajpawardotin.dekhreekh.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TrackingService : Service() {

    companion object {
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startForegroundService()
            ACTION_STOP_TRACKING -> stopTrackingService()
        }
        // START_STICKY ensures the OS restarts the service if it crashes under heavy load
        return START_STICKY 
    }

    private fun startForegroundService() {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // We'll replace this with your custom logo later
            .setContentTitle("Dekhreekh Active")
            .setContentText("Telemetry Engine Running")
            .build()

        // Android 14+ requires explicit declaration of the service type at runtime
        startForeground(
            NOTIFICATION_ID, 
            notification, 
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        )
        
        // TODO: Initialize Dual-Band GNSS and IMU sensor listeners here
    }

    private fun stopTrackingService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Active Tracking",
            NotificationManager.IMPORTANCE_LOW // Low prevents constant buzzing, but keeps it in the status bar
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}