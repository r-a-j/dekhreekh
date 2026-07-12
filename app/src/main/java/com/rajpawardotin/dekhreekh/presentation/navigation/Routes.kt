package com.rajpawardotin.dekhreekh.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

@Serializable
data object VaultRoute

@Serializable
data class VaultDetailRoute(val sessionId: String)
