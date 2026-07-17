package com.rajpawardotin.dekhreekh.presentation.vaultdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class VaultDetailViewModel(
    val sessionId: String,
    val repository: SessionRepository
) : ViewModel() {

    val telemetryPath: Flow<List<TelemetryData>> = repository.getTelemetryForSession(sessionId)

    val session: StateFlow<WorkoutSession?> = repository.getAllSessions()
        .map { sessions -> sessions.find { it.id == sessionId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
