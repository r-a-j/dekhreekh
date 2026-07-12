package com.rajpawardotin.dekhreekh.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class VaultViewModel(
    val sessionRepository: SessionRepository
) : ViewModel() {
    val totalPingCount: Flow<Int> = sessionRepository.getTotalTelemetryCount()

    fun wipeVault() {
        viewModelScope.launch {
            sessionRepository.wipeAllData()
        }
    }
}
