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

## UI/UX Design System: Liquid Glass & macOS Dock

The application combines a high-performance MapLibre base map with a custom Liquid Glass design system, using a highly tailored dark mode aesthetic.

### 1. The macOS-Style Bottom Dock
Navigation and key map operations are handled by the official library-provided `LiquidGlassBottomNav` floating glass dock situated at the bottom of the screen inside `AppNavigation.kt`.
- **Dock Items**: Hosts "Map" (Dashboard tab), "Vault" (History list overlay tab), and "Locate" (instantly centers the map viewport on the user's current location and switches screen focus back to the active Map).
- **Aesthetic**: Implemented using the official `LiquidGlassBottomNav` configured with the exact customized physics and glass parameters from the `Scopecomp` app (e.g., `stretchIntensity = 0.48f`, `squashIntensity = 0.52f`, `headStiffness = 617.66f`, `tailStiffness = 200f`, `blobSize = 1.07f`, `metaballIntensity = 0.09f`, along with custom `navContainer` and `navIndicator` glass refraction/contrast tokens). This generates the exact highly-tuned fluid blob stretching and metaball immersion transitions.
- **Positioning**: Bound to `width(280.dp)` and centered at the bottom, raised slightly above the navigation bar bounds (requiring the dashboard control panels and lists to adjust their bottom offsets).

### 2. DashboardScreen Structure
The `DashboardScreen` is clean and immersive:
- **Permission Guard**: If location permissions are denied, a custom-tinted glass prompt requests them.
- **Standby & Tracking Controls**: The bottom controls card is shifted up by 96dp to prevent overlapping the bottom dock. A radial neon glow sits directly behind it.
- **Map & Scrims**: MapLibre is rendered fullscreen behind all content. Heavy gradient scrims (black face) are painted at the top and bottom bounds of the map to ensure system status bar and card readability.

### 3. VaultScreen Structure
The `VaultScreen` is displayed as a transparent overlay directly on top of the dashboard map, showing the map beneath:
- **Session List**: Rendered inside a `LazyColumn`.
- **Obsidian Cards**: Each session item is rendered using a custom-scoped `LiquidGlassCard` with a stable `GlassComponentTokens` instance configured with a 70% dark obsidian tint (`tintAlpha = 0.70f` and `frost = 16.dp`) to make text highly readable over map tiles under any style (like Liberty).
- **Empty Vault State**: When no sessions are recorded, displays a floating obsidian glass card containing a clean, minimalist outlined history icon container and simplified typography prompting the user to record runs or import GPX workouts.
- **Global Theme Tinting**: Inside `Theme.kt`, `LiquidGlassTheme` is configured with a global obsidian color (`Color(0xFF040406)`) as its `glassTint` color.

### 4. Navigation & Routes
We utilize Jetpack Compose Navigation's Type-Safe routing:
- **Active Navigation**: Handled reactively using a root-level `showVault` state toggled by the bottom dock.
- **Detail Route**: Direct detailed session lookups use type-safe arguments (`VaultDetailRoute(sessionId)`).
- **VaultDetailScreen UI**: Extends the map edge-to-edge under a transparent TopAppBar, and layers the Timeline Scrubber and Metrics cards over bottom and top scrims.

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
- **`ImportEngine`:** Self-contained XML parsing engine that parses standard GPX version 1.1 coordinates and elevations. Leverages JDK `DocumentBuilderFactory` DOM parsing so it runs cleanly on standard JVM unit tests without Android OS mocking dependencies. Computes distance via Haversine logic, detects if distance is < 5m, and auto-tags low-activity sessions as `glitch`.
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

