package com.rajpawardotin.dekhreekh.di

import com.rajpawardotin.dekhreekh.data.local.DekhreekhDatabase
import com.rajpawardotin.dekhreekh.data.repository.SessionRepositoryImpl
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingViewModel
import com.rajpawardotin.dekhreekh.presentation.vault.VaultViewModel
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
        .fallbackToDestructiveMigration()
        .build()
    }
    single { get<DekhreekhDatabase>().sessionDao() }
    single { get<DekhreekhDatabase>().telemetryDao() }

    // Repositories
    single<SessionRepository> { SessionRepositoryImpl(get(), get()) }

    // ViewModels
    viewModel { TrackingViewModel() }
    viewModel { VaultViewModel(get()) }
}
