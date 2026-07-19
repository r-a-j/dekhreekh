# Codebase Concerns

**Analysis Date:** 2026-07-17

## Tech Debt

**Room Database Destructive Migration:**
- Issue: `DekhreekhDatabase` builder uses `.fallbackToDestructiveMigration()`.
- Files: `app/src/main/java/com/rajpawardotin/dekhreekh/di/KoinModules.kt`
- Impact: If the database schema version is bumped (e.g. adding new telemetry columns), all user data (WorkoutSession and TelemetryEntity rows) will be permanently deleted upon app restart unless a migration plan is explicitly written. This is highly dangerous for a local-first application.
- Fix approach: Implement proper schema versioning and write manual migration paths (`Migration` classes) for all future schema alterations.

**Hard Dependency on FusedLocationProviderClient:**
- Issue: GPS updates depend strictly on Google Play Services location package.
- Files: `app/src/main/java/com/rajpawardotin/dekhreekh/service/TrackingService.kt`
- Impact: The app will fail to receive location telemetry on de-Googled Android devices or emulators lacking Google Play Services.
- Fix approach: Implement a fallback to the standard Android system `LocationManager` API if Play Services is unavailable.

## Security Considerations

**Background Location Permission Request:**
- Risk: The app requests `ACCESS_BACKGROUND_LOCATION` which is highly scrutinized by Google Play Console policies and requires explicit user consent disclosures.
- Files: `app/src/main/AndroidManifest.xml`
- Recommendation: Ensure a clear disclosure screen is shown to the user before requesting background location access, or evaluate if foreground service location access is sufficient.

## Performance Bottlenecks

**High-Frequency Telemetry Room Writes:**
- Problem: Fusing dual-band GPS and body sensors at high frequencies could trigger rapid SQL database inserts, causing disk I/O bottlenecks and battery drain.
- Files: `app/src/main/java/com/rajpawardotin/dekhreekh/service/SessionRecorder.kt`
- Cause: Single row Room inserts on every telemetry event.
- Improvement path: Buffer telemetry updates in memory and write them in batches (e.g., every 5-10 seconds) or write on a separate high-priority background thread pool.

## Test Coverage Gaps

**Sensor Fusion & Shaders:**
- What's not tested: Sensor fusion algorithms and the Heart Pulse Path AGSL shader.
- Risk: Inaccuracies in sensor fusion could lead to bad telemetry maps, and shader compiling issues on specific GPUs could crash the app.
- Priority: Medium
- Difficulty to test: Requires emulator sensor injection tests and AGSL shader compilation verification tools.

---

*Concerns audit: 2026-07-17*
*Update as issues are fixed or new scaling limitations are identified*
