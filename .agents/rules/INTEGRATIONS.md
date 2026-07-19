# External Integrations

**Analysis Date:** 2026-07-17

## APIs & External Services

**Location Services:**
- Android Google Play Services Location - Core location telemetry provider
  - SDK/Client: `com.google.android.gms:play-services-location:21.3.0`
  - Auth: Android System Runtime Permissions (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`)
  - Endpoints/APIs used: `FusedLocationProviderClient`, `LocationRequest`, `LocationCallback`

**Offline Map Tiles:**
- OpenFreeMap & MapLibre - Vector map tile rendering
  - SDK/Client: `org.maplibre.gl:android-sdk:13.3.1`
  - Endpoints used: OpenFreeMap vector style URLs and cached vector tile schemas.
  - Auth: Public endpoints, no api key required (or configured locally via asset file).

**Cloud Backup & Storage:**
- Google Drive (Backup) - Direct database uploads for user data persistence
  - Integration method: Google Drive Android API / REST API
  - Auth: User Google Account sign-in/OAuth2.

**Wearables & Health:**
- Android Health Connect - Wearable data sharing engine (heart rate, workouts)
  - SDK/Client: Android Health Connect Client SDK.
  - Auth: Health Connect runtime permission consent.

## Data Storage

**Databases:**
- SQLite (via Room) - Primary local telemetry data warehouse
  - Connection: Local database instance (`dekhreekh_database`) built in `KoinModules.kt`
  - Client: Room DB compiler and runtime `2.8.4`
  - Migrations: Destructive migrations configured in development (`.fallbackToDestructiveMigration()`)

**Map Cache Storage:**
- MapLibre cache - Local device folder caching vector map tiles for offline 60fps rendering.

## Monitoring & Observability

**Logs:**
- Android Logcat - System stdout and debug logs via `android.util.Log` or custom logger wrappers.

---

*Integration audit: 2026-07-17*
*Update when adding/removing external services or system permissions*
