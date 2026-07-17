# Dekhreekh Architecture & UI Structure

## Core Architecture Pattern: Clean Architecture + MVI

The application strictly adheres to Clean Architecture principles integrated with a Model-View-Intent (MVI) presentation layer.

### 1. Presentation Layer (MVI)
The UI is driven entirely by state emitted from ViewModels. 
- **State (`TrackingState`)**: Represents the Single Source of Truth (SSOT) for what the UI should display. For the Dashboard, this can be `Idle`, `Ready`, or `Tracking(distance, pace)`.
- **Intent (`TrackingIntent`)**: Represents user actions that trigger business logic. Examples include `IgniteEngine` (start tracking) and `HaltEngine` (stop tracking).
- **ViewModel (`TrackingViewModel`)**: Observes the global state from the `TrackingService` (Foreground Service) and maps it into a unified `TrackingState` for the UI. It accepts `TrackingIntent` actions to modify the state.

### 2. Dependency Injection (Koin)
We use Koin to manage dependencies without code-generation overhead.
- ViewModels are injected into Compose destinations using `koinViewModel()`.
- Services use Koin's `inject()` delegated properties.
- Room databases and DAOs are provided as Singletons.

## UI/UX Design System: Material 3 Defaults

The application strictly adheres to the standard Material 3 design system, leveraging `lightColorScheme` and `darkColorScheme` for dynamic adaptation to system settings.

### DashboardScreen Structure
The `DashboardScreen` acts as the main entry point:
1. **Permission Guard**: If location permissions are denied, a strictly styled prompt requests them.
2. **Idle/Ready State**: Displays a "SYSTEM READY" indicator and an "IGNITE ENGINE" button. A Floating Action Button (FAB) navigates to the Vault.
3. **Tracking State**: Displays an "ENGINE ACTIVE" indicator and renders live telemetry using `MetricCard` components. The Vault FAB is hidden during active tracking to prevent accidental navigation.

### VaultScreen Structure
The `VaultScreen` displays the user's historical workout sessions:
1. **Empty State**: Displays "No telemetry data found" when the SQLite database has no sessions.
2. **History State**: Uses a `LazyColumn` to render a list of sessions.
3. **Components**: Each session is rendered using standard Material `ElevatedCard` components displaying distance, pace, and time.
4. **Data Flow**: `VaultViewModel` automatically maps the `SessionRepository`'s internal data flow into the UI state (`VaultState`) using `stateIn(SharingStarted.WhileSubscribed(5000))`.

### Navigation (Type-Safe)
We utilize Jetpack Compose Navigation's new Type-Safe routing (v2.8.0+):
- Routes are defined as `@Serializable data object` classes (e.g., `DashboardRoute`, `VaultRoute`).
- This guarantees compile-time safety when passing arguments and navigating between screens.

## Data Architecture and Persistence

The local persistence layer strictly adheres to Clean Architecture via Room. We segregate the raw database models (`Entity`) from the core app models (`Domain`) to ensure the UI and business logic never interact with database annotations or SQL details.

### Data-to-Domain Mapping (Anti-Corruption Layer)
1. **Separation of Concerns**: 
   - `SessionEntity` and `TelemetryEntity` map 1:1 to SQLite tables.
   - `WorkoutSession` and `TelemetryData` represent the pure domain equivalents.
2. **Mapper Extensions**: Located in `Mappers.kt`, we expose strictly typed Kotlin extension functions (e.g., `SessionEntity.toDomain()`, `WorkoutSession.toEntity()`).
3. **Repository Responsibility**: `SessionRepositoryImpl` acts as the Anti-Corruption Layer. It consumes internal DAOs, observes the Room `Flow`, applies the `toDomain()` mappers, and emits pure Domain flows to the upstream components (`VaultViewModel`, etc.). No `Entity` ever leaks past the Repository layer.

### Room Implementations
1. **One-to-Many Relational Queries:** Instead of doing N+1 queries, we use Room's `@Embedded` and `@Relation` capabilities via `SessionWithTelemetry` to perform highly efficient multi-table read queries natively mapping a session to all its telemetry points.
2. **Database Version 3 Migration (Metadata Persistence):**
   - Migrated database schema from `v2` to `v3` using Room's `@AutoMigration` with schema JSON exports.
   - Added metadata columns `name` (nullable String), `tags` (comma-separated String mapped to lists via `TagsConverter`), and `isLowActivity` (Boolean flag indicating whether distance is < 5 meters).

### 3. Foreground Service & Recording Lifecycle
- **`TrackingService`:** Android Foreground Service responsible for orchestrating hardware sensors (GPS via FusedLocationProviderClient) and pushing raw location streams to the UI. It reset and finalized metrics via `SessionRecorder.stopRecording()` safely upon shutdown.
- **`SessionRecorder`:** A pure Kotlin component that isolates the business logic of recording a session to the repository from the volatile Android Service lifecycle. It manages `currentSessionId`, processes raw pings to SQLite, and finalizes session metrics upon termination. This promotes high testability and cleanly bridges the Foreground Service to the Data layer.

### 4. Tracking Map, Real-time Pathing & Interactive History detail
- **`TrackingMap` Integration:** Uses MapLibre Native (`org.maplibre.android.maps.MapView`) styled via standard open-source vector tiles (`demotiles.maplibre.org`). The MapLibre watermark and attributions are disabled.
- **Real-time Data Flow:** `TrackingViewModel` directly exposes a `livePath` `StateFlow` containing the active session's telemetry points (queried from `SessionRepository` using `SessionRecorder`'s `activeSessionId`). This stream powers the `Polyline` composable drawn in `TrackingMap`.
- **Interactive History detail (Scrubbing timeline):**
  - Displays historical track details in a scrollable list format fitting the screen bounds.
  - Features a timeline seekbar/scrubbing indicator that matches the exact timestamp of telemetry coordinates.
  - The map marker updates its coordinates in real-time as the user seek-scrolls the timeline, illustrating exactly where the user was at that specific timestamp.

### 5. GPX Data Import/Export Engine
- **`ImportEngine`:** Self-contained XML parsing engine that parses standard GPX version 1.1 coordinates and elevations. Leverages JDK `DocumentBuilderFactory` DOM parsing so it runs cleanly on standard JVM unit tests without Android OS mocking dependencies. Computes distance via Haversine logic, detects if distance is < 5m, and auto-tags low-activity sessions as `glitch` and `bogus`.
- **`ExportEngine`:** Generates standard GPX XML outputs with track point coordinate timestamps, formatting them cleanly for external platforms.

### 6. Global Tag Management Architecture
- **In-Memory Reactive Sorting & Filtering:** `VaultViewModel` uses a declarative `combine` stream to join database sessions with active filters (tags list, low activity filter) and sort metrics (by date, duration, or distance).
- **Global Tag Actions:** Exposes database mutations `renameTagGlobally(old, new)` and `deleteTagGlobally(tag)` running inside single Room transactions to allow global bulk updating and removing tags from all session records simultaneously.
- **Tag Usage Counts:** Exposes active usage counts mapped globally for UI consumption in a dedicated Tag Manager Dialog inside the Vault screen.

### 7. Liquid Glass Design System Integration
- **Dependency Source:** Pulled online via JitPack coordinates `com.github.r-a-j:custom-liquid-glass:v1.1.0`. The temporary local folder `temp_aar/` has been removed.
- **Theme Wrapper:** App themes inside `Theme.kt` wrap composables with `LiquidGlassTheme` alongside the standard `MaterialTheme`.
- **Custom Molecules:**
  - **`LiquidGlassCard`:** Applied to status cards, permission boxes, metrics layout containers (`MetricCard.kt`), and historical workout session items.
  - **`LiquidButton`:** Replaced Material 3 buttons on the Dashboard (Start, Stop, and Grant Access) and HUD layout (Extinguish Engine).
  - **`LiquidGlassChip`:** Replaced the filtering pills on the Vault Screen and session item tag displays.
