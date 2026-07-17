# Architecture

**Analysis Date:** 2026-07-17

## Pattern Overview

**Overall:** Clean Architecture + MVI (Model-View-Intent) local-first Android Application.

**Key Characteristics:**
- **Local-First / Serverless:** All workouts and telemetry are stored inside an on-device Room database. No server backend.
- **Unconstrained Hardware Focus:** Custom location tracking fuses dual-band GPS + sensor data.
- **Unidirectional Data Flow (MVI):** ViewModels expose state flows to Jetpack Compose screens, receiving intents from UI components.

## Layers

**Presentation Layer:**
- Purpose: Render telemetry dashboards and handle user navigation.
- Contains: ViewModels, Compose Screens, UI components, custom graphics shaders.
- Location: `app/src/main/java/com/rajpawardotin/dekhreekh/presentation/`
- Depends on: Domain layer (models, repository interfaces).
- Used by: Android Activity lifecycle (`MainActivity.kt`).

**Domain Layer:**
- Purpose: Enforce business models and repository abstractions.
- Contains: Domain models (`TelemetryData`, `WorkoutSession`), and the `SessionRepository` interface.
- Location: `app/src/main/java/com/rajpawardotin/dekhreekh/domain/`
- Depends on: Coroutine libraries, standard library (independent of framework/database details).
- Used by: Presentation layer ViewModels, Service layer.

**Data Layer:**
- Purpose: Persistence in local SQLite database and mapping database entities to domain models.
- Contains: Room Database definition, Entities, DAOs, and repository implementation.
- Location: `app/src/main/java/com/rajpawardotin/dekhreekh/data/`
- Depends on: Domain layer (implements repository), Room DB libraries.
- Used by: DI registration.

**Service Layer:**
- Purpose: Enforce persistent tracking session lifecycle independent of UI states.
- Contains: Android Foreground Service (`TrackingService`), lifecycle-bound recorders (`SessionRecorder`).
- Location: `app/src/main/java/com/rajpawardotin/dekhreekh/service/`
- Depends on: Domain repository, standard Android Location APIs.
- Used by: UI/ViewModel commands to start/stop tracking.

**DI Layer (Koin Modules):**
- Purpose: Declare dependency injections and ViewModel mapping.
- Location: `app/src/main/java/com/rajpawardotin/dekhreekh/di/`
- Depends on: All layers (constructs concrete instances).

## Data Flow

**Telemetry Session Flow:**

1. User clicks "Start Tracking" in the UI -> triggers Intent to `TrackingService`.
2. `TrackingService` starts in the foreground (showing location tracking notification).
3. Service polls FFusedLocationProviderClient -> receives coordinates + body sensors.
4. Telemetry data is pushed to `SessionRecorder` -> saves into `SessionRepository` sequentially.
5. Room database updates flow down to `SessionRepository` -> triggers state change in `TrackingViewModel`.
6. UI renders real-time coordinates, distance, and custom pulse shader on `TrackingMap`.
7. User clicks "Stop" -> service terminates, database session is finalized, and data goes to local storage.

**State Management:**
- Kotlin `StateFlow` and standard `Flow` expose database streams directly to UI composables.
- In-memory service state manages running tracker state.

## Key Abstractions

**SessionRepository:**
- Purpose: Abstraction boundary for sessions and telemetry.
- Implementation: `SessionRepositoryImpl` (Room DB mapping).
- Pattern: Repository pattern.

**SessionRecorder:**
- Purpose: Direct service recorder helper managing current active tracking sessions.
- Location: `app/src/main/java/com/rajpawardotin/dekhreekh/service/SessionRecorder.kt`

## Entry Points

**Main Activity:**
- Location: `app/src/main/java/com/rajpawardotin/dekhreekh/MainActivity.kt`
- Triggers: User launches the Android application.
- Responsibilities: Initialize Koin, load navigation map, draw standard Material 3 scaffold.

**Tracking Service:**
- Location: `app/src/main/java/com/rajpawardotin/dekhreekh/service/TrackingService.kt`
- Triggers: Start command from tracking view model.
- Responsibilities: Core foreground tracking loop, location permission management.

## Error Handling

**Strategy:** Bubbles database errors through flows, catches permission issues before launching services, uses local defaults if sensors fail.

---

*Architecture analysis: 2026-07-17*
*Update when core patterns or layer divisions change*
