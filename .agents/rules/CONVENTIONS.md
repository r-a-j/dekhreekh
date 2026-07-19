# Coding Conventions

**Analysis Date:** 2026-07-17

## Naming Patterns

**Files & Classes:**
- PascalCase for all Kotlin source files, classes, and interfaces (`MainActivity.kt`, `SessionRepository.kt`).
- Views and Screens: `[Name]Screen.kt` (`DashboardScreen.kt`, `VaultScreen.kt`).
- ViewModels: `[Name]ViewModel.kt` (`TrackingViewModel.kt`).
- Repositories: Interface `[Name]Repository` (`SessionRepository.kt`), Implementation `[Name]RepositoryImpl.kt` (`SessionRepositoryImpl.kt`).
- Database Components: DAOs are `[Name]Dao` (`SessionDao.kt`), Entities are `[Name]Entity` (`SessionEntity.kt`).
- DI Modules: `[Name]Modules.kt` (`KoinModules.kt`).
- Services: `[Name]Service.kt` (`TrackingService.kt`).

**Functions & Variables:**
- camelCase for functions (`startSession()`, `insertTelemetry()`).
- camelCase for variables (`sessionId`, `totalDistanceMeters`).
- UPPER_SNAKE_CASE for constants (`TAG`, `NOTIFICATION_ID`).

## Code Style

**Kotlin Formatting:**
- Use standard Kotlin style guidelines.
- 4-space indentation for regular Kotlin files.
- Destructure objects in parameters when useful.
- Prefer early returns with guard clauses.

**Jetpack Compose Style:**
- Material 3 styling components.
- Composable functions must be PascalCase and start with a noun (`MetricCard`, `TrackingHUD`).
- Composables should accept a `modifier: Modifier = Modifier` parameter as the first optional argument.
- Read states with `.collectAsStateWithLifecycle()` or `.collectAsState()` inside composables to prevent resource leaks.

## Dependency Injection (Koin)

**Structure:**
- Registrations are centralized under `di/KoinModules.kt` inside Koin `module { ... }` block.
- Singletons use `single { ... }`.
- ViewModels use `viewModel { ... }` (from Koin Android libraries).

**Order & Scope:**
- Database builder and Dao definitions first.
- Repositories and foreground service instances second.
- UI ViewModels last.

## Error Handling

**Data Layer:**
- SQLite database transactions must complete inside transaction blocks or use Room's `@Transaction` annotation for atomicity.
- In-memory streams and Flow flows should swallow transient location issues, defaulting to last known values if required.

**Service Layer:**
- Android Foreground Services must verify permission grants (`ACCESS_FINE_LOCATION`, etc.) before launching. If missing, fail gracefully by notifying the user or requesting action.

---

*Convention analysis: 2026-07-17*
*Update when language upgrades or style guidelines are updated*
