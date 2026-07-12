package com.rajpawardotin.dekhreekh.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface VaultState {
    data object Empty : VaultState
    data class HistoryLoaded(val sessions: List<WorkoutSession>) : VaultState
}

class VaultViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val uiState: StateFlow<VaultState> = sessionRepository.getAllSessions()
        .map { sessions ->
            if (sessions.isEmpty()) {
                VaultState.Empty
            } else {
                VaultState.HistoryLoaded(sessions)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VaultState.Empty
        )
}
