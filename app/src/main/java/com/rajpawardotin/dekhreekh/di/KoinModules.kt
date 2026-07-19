package com.rajpawardotin.dekhreekh.di

import com.rajpawardotin.dekhreekh.data.local.DekhreekhDatabase
import com.rajpawardotin.dekhreekh.data.repository.SessionRepositoryImpl
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import com.rajpawardotin.dekhreekh.service.SessionRecorder
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingViewModel
import com.rajpawardotin.dekhreekh.presentation.vault.VaultViewModel
import com.rajpawardotin.dekhreekh.presentation.vaultdetail.VaultDetailViewModel
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { 
        Room.databaseBuilder(
            androidContext(),
            DekhreekhDatabase::class.java,
            "dekhreekh_database"
        )
        .build()
    }
    single { get<DekhreekhDatabase>().sessionDao() }
    single { get<DekhreekhDatabase>().telemetryDao() }

    // Repositories & Use Cases
    single<SessionRepository> { SessionRepositoryImpl(get(), get()) }
    single { SessionRecorder(get()) }

    // ViewModels
    viewModel { TrackingViewModel(get(), get()) }
    viewModel { VaultViewModel(get()) }
    viewModel { params -> VaultDetailViewModel(sessionId = params.get(), repository = get()) }
}
