package com.rajpawardotin.dekhreekh.presentation.tracking

import android.location.Location
import androidx.lifecycle.ViewModel
import com.rajpawardotin.dekhreekh.service.TelemetryStatus
import com.rajpawardotin.dekhreekh.service.TrackingService
import kotlinx.coroutines.flow.StateFlow

class TrackingViewModel() : ViewModel() {
    
    // We observe the static StateFlows from the foreground service directly
    // This maintains the SSOT (Single Source of Truth) in the service
    
    val locationStream: StateFlow<Location?> = TrackingService.locationStream
    val telemetryStatus: StateFlow<TelemetryStatus> = TrackingService.status
    val elapsedSeconds: StateFlow<Long> = TrackingService.elapsedSeconds
    val distanceMeters: StateFlow<Float> = TrackingService.distanceMeters
    val currentPace: StateFlow<Long> = TrackingService.currentPace
}
