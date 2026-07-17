# Codebase Structure

**Analysis Date:** 2026-07-17

## Directory Layout

```
[dekhreekh-v2]/
├── .agents/                    # Agent-specific guidelines and rules
├── app/                        # Main Android application module
│   ├── src/
│   │   ├── main/               # Application source code
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── assets/         # Resource assets (maps styles, vector assets)
│   │   │   ├── java/com/rajpawardotin/dekhreekh/
│   │   │   │   ├── components/  # Shared Compose components
│   │   │   │   ├── data/       # Room database, repositories, mappers
│   │   │   │   ├── di/         # Koin Dependency Injection modules
│   │   │   │   ├── domain/     # Domain models & repository interfaces
│   │   │   │   ├── presentation/ # ViewModels, Screen states, navigation
│   │   │   │   ├── service/    # Foreground telemetry recording services
│   │   │   │   ├── ui/         # Theme setup and global view controllers
│   │   │   │   └── utils/      # Standard utility helper tools
│   │   │   └── res/            # Static layout/string assets
│   │   └── test/               # Local unit tests
│   └── build.gradle.kts        # App Gradle script config
├── gradle/                     # Gradle wrapper and versions configuration
│   └── libs.versions.toml      # Gradle central versions catalog
├── build.gradle.kts            # Project root Gradle configuration
├── settings.gradle.kts         # Multi-module registration configuration
└── README.md                   # Project documentation
```

## Directory Purposes

**app/src/main/java/com/rajpawardotin/dekhreekh/presentation/**
- Purpose: MVVM / MVI screen state definitions and Composable containers.
- Subdirectories:
  - `dashboard/` - Main dashboard listing stats and recent sessions.
  - `navigation/` - Standard application bottom navigation and routes.
  - `tracking/` - Location maps, real-time coordinate buffers, tracking ViewModels.
  - `vault/` - Telemetry details store screens.

**app/src/main/java/com/rajpawardotin/dekhreekh/data/**
- Purpose: SQLite database entities, DAOs, and repository implementations.
- Subdirectories:
  - `local/` - Room DB implementation and entities (`local/dao/`, `local/entity/`).
  - `mappers/` - Mappers converting entity states to domain model instances.
  - `repository/` - Implementation of domain repository interfaces.

**app/src/main/java/com/rajpawardotin/dekhreekh/domain/**
- Purpose: Business entities and repository interface rules.
- Subdirectories:
  - `models/` - Standard telemetry structures (`TelemetryData`, `WorkoutSession`).
  - `repository/` - Database access interfaces (`SessionRepository`).

**app/src/main/java/com/rajpawardotin/dekhreekh/service/**
- Purpose: Foreground location gathering and lifecycle management.
- Key Files:
  - `TrackingService.kt` - Location polling foreground loop.
  - `SessionRecorder.kt` - Database telemetry insertion router.

## Key File Locations

**Entry Points:**
- `app/src/main/java/com/rajpawardotin/dekhreekh/MainActivity.kt` - Main Activity launching navigation host.
- `app/src/main/java/com/rajpawardotin/dekhreekh/DekhreekhApp.kt` - Extends `Application`, boots Koin DI.

**Configuration:**
- `app/src/main/AndroidManifest.xml` - System hardware permissions and component declarations.
- `gradle/libs.versions.toml` - Global dependencies and version pointers.
- `app/build.gradle.kts` - App compilation targets and dependency implementations.

**Core Logic:**
- `app/src/main/java/com/rajpawardotin/dekhreekh/service/TrackingService.kt` - Fused location polling logic.
- `app/src/main/java/com/rajpawardotin/dekhreekh/data/repository/SessionRepositoryImpl.kt` - Database operations router.

**Testing:**
- `app/src/test/java/com/rajpawardotin/dekhreekh/` - Unit tests for ViewModels (`presentation/`) and service helpers (`service/`).

## Naming Conventions

**Files:**
- PascalCase for Kotlin files and classes (`MainActivity.kt`, `TrackingViewModel.kt`, `SessionRepository.kt`).
- kebab-case or snake_case for resources and configurations (`build.gradle.kts`, `activity_main.xml`).

**Methods & Variables:**
- camelCase for Kotlin methods and variables (`startSession()`, `totalDistanceMeters`).
- UPPER_SNAKE_CASE for constants.

## Where to Add New Code

**New Telemetry Analysis or Model:**
- Implementation: `app/src/main/java/com/rajpawardotin/dekhreekh/domain/models/`
- Mapper: `app/src/main/java/com/rajpawardotin/dekhreekh/data/mappers/`

**New View Model or Screen:**
- View Model & Composable: `app/src/main/java/com/rajpawardotin/dekhreekh/presentation/[feature_name]/`
- Route declaration: `app/src/main/java/com/rajpawardotin/dekhreekh/presentation/navigation/Routes.kt`

**Database Entities:**
- Entity class: `app/src/main/java/com/rajpawardotin/dekhreekh/data/local/entity/`
- DAO: `app/src/main/java/com/rajpawardotin/dekhreekh/data/local/dao/`
- Remember to register entities in `DekhreekhDatabase.kt` and update DI bindings in `KoinModules.kt`.

---

*Structure analysis: 2026-07-17*
*Update when directory structure or conventions change*
