# Testing Patterns

**Analysis Date:** 2026-07-17

## Test Framework

**Runner:**
- JUnit 4
- Android JUnit Runner (for instrumented device tests)

**Testing Utilities & Libraries:**
- `kotlinx-coroutines-test` - Coroutine dispatchers overrides for ViewModels.
- `koin-test` + `koin-test-junit4` - DI verification.
- `androidx.compose.ui:ui-test-junit4` - UI Compose testing (instrumented).
- `androidx.test.espresso:espresso-core` - UI integration tests.

**Run Commands:**
```bash
./gradlew test                        # Run all local JVM unit tests
./gradlew connectedAndroidTest        # Run all instrumented tests on an emulator/device
./gradlew :app:testDebugUnitTest      # Run local debug unit tests specifically
```

## Test File Organization

**Location:**
- Local unit tests: `app/src/test/java/com/rajpawardotin/dekhreekh/` collocated under package subfolders mirroring source code files (e.g., ViewModels and Services).
- Instrumented tests: `app/src/androidTest/java/com/rajpawardotin/dekhreekh/` (standard Android device tests folder).

**Structure:**
```
app/src/test/java/com/rajpawardotin/dekhreekh/
├── ExampleUnitTest.kt
├── service/
│   └── SessionRecorderTest.kt
└── presentation/
    ├── tracking/
    │   └── TrackingViewModelTest.kt
    ├── vault/
    │   └── VaultViewModelTest.kt
    └── vaultdetail/
        └── VaultDetailViewModelTest.kt
```

## Test Structure

**Typical ViewModel Test Suite:**
- Uses a `TestDispatcher` (e.g. `StandardTestDispatcher` or `UnconfinedTestDispatcher`) to control coroutine execution.
- Configures mock repository actions using testing frameworks or fake repository implementations.
- Arranges state parameters, acts by executing ViewModel intents, and asserts output state variables.

## Mocking & Fakes

**What to Mock/Fake:**
- `SessionRepository` interface implementations are faked or mocked to control database returns during local ViewModel tests.
- Hardware APIs (location, Bluetooth, body sensors) are mocked or abstracted through interfaces to avoid OS coupling.

**What NOT to Mock:**
- Clean architecture domain models (`TelemetryData`, `WorkoutSession`).
- Kotlin flow transformers and functional mapping logic.

---

*Testing analysis: 2026-07-17*
*Update when adding test coverage checkers, changing assertion frameworks, or migrating to JUnit 5*
