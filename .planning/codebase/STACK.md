# Technology Stack

**Analysis Date:** 2026-07-17

## Languages

**Primary:**
- Kotlin 2.1.10 - All application code

**Secondary:**
- Kotlin DSL (Gradle) - Build scripts and configuration files
- XML - Android manifests, vector assets, and system configurations

## Runtime

**Environment:**
- Android Runtime (JVM 21)
- Target SDK: 36 (Android 16 Baklava)
- Min SDK: 34 (Android 14 Upside Down Cake)

**Build Tooling & Package Manager:**
- Gradle with Kotlin DSL
- Android Gradle Plugin (AGP) 9.2.1
- Version Catalog (`gradle/libs.versions.toml`) for dependency management

## Frameworks

**Core:**
- Jetpack Compose (BOM 2026.02.01) - UI framework for modern native layouts
- Android Jetpack Lifecycle (Runtime 2.10.0) - Lifecycle management
- Navigation Compose 2.8.8 - Navigation mapping
- Koin 4.0.0 - Dependency Injection

**Testing:**
- JUnit 4.13.2 - Unit testing
- kotlinx-coroutines-test 1.10.1 - Coroutines testing
- Koin Test 4.0.0 - Dependency injection testing and verification

**Build/Dev:**
- Kotlin Symbol Processing (KSP) 2.1.10-1.0.29 - Annotation processing (primarily for Room)

## Key Dependencies

**Critical:**
- `org.maplibre.gl:android-sdk:13.3.1` (MapLibre SDK) - High-performance offline vector maps
- `androidx.room:room-runtime:2.8.4` (Room DB) - SQLite abstraction for local data warehousing
- `com.google.android.gms:play-services-location:21.3.0` - Real-time location and telemetry gathering
- `io.insert-koin:koin-android:4.0.0` - Application lifecycle DI and ViewModel provision
- `androidx.webkit:webkit:1.11.0` - Web view component support (if required for custom maps/tile logic)

**Infrastructure:**
- Kotlinx Serialization JSON 1.8.0 - Offline JSON parsing and serialization

## Configuration

**Environment:**
- Configured via Android standard resource files (`strings.xml`, `colors.xml`) and runtime system services.
- `local.properties` (gitignored) - Local SDK paths.

**Build:**
- `build.gradle.kts` (project root) - Root configuration and plugins.
- `app/build.gradle.kts` - Android compile/target SDK setup, dependencies.
- `gradle/libs.versions.toml` - Centralized versions catalog.

## Platform Requirements

**Development:**
- Android Studio Ladybug or later, supporting JDK 21+ and Gradle 9.x.
- Android SDK platforms for API 34-36.

**Production:**
- Android devices running Android 14+ (API 34+), with particular optimization for flagship devices with robust sensors and NPUs.

---

*Stack analysis: 2026-07-17*
*Update after major dependency changes*
