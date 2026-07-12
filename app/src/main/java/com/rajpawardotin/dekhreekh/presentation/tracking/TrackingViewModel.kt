package com.rajpawardotin.dekhreekh.presentation.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajpawardotin.dekhreekh.service.TelemetryStatus
import com.rajpawardotin.dekhreekh.service.TrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import com.rajpawardotin.dekhreekh.service.SessionRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TrackingState {
    data object Idle : TrackingState
    data object Ready : TrackingState
    data class Tracking(val distance: Float, val pace: Long) : TrackingState
}

sealed interface TrackingIntent {
    data object IgniteEngine : TrackingIntent
    data object HaltEngine : TrackingIntent
}

class TrackingViewModel(
    private val sessionRecorder: SessionRecorder,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    // Internal state representing user intent
    private val _engineActive = MutableStateFlow(false)

    // SSOT from the foreground service
    private val serviceStatus = TrackingService.status
    private val distanceMeters = TrackingService.distanceMeters
    private val currentPace = TrackingService.currentPace

    // Combined UI State
    val uiState: StateFlow<TrackingState> = combine(
        _engineActive,
        serviceStatus,
        distanceMeters,
        currentPace
    ) { active, status, distance, pace ->
        when {
            active || status == TelemetryStatus.LOCKED || status == TelemetryStatus.SEARCHING -> TrackingState.Tracking(distance, pace)
            status == TelemetryStatus.INITIALIZING -> TrackingState.Ready
            else -> TrackingState.Idle
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrackingState.Idle
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val livePath: StateFlow<List<TelemetryData>> = sessionRecorder.activeSessionId
        .flatMapLatest { sessionId ->
            if (sessionId.isNotEmpty()) {
                sessionRepository.getTelemetryForSession(sessionId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onIntent(intent: TrackingIntent) {
        when (intent) {
            TrackingIntent.IgniteEngine -> {
                _engineActive.value = true
                // In a real implementation, this would start the foreground service
            }
            TrackingIntent.HaltEngine -> {
                _engineActive.value = false
                // In a real implementation, this would stop the foreground service
            }
        }
    }
}
