# Dekhreekh (देखरेख) :family:
#### :heart: *Built for family, friends, and the love of clean data.*

Most fitness apps (like Strava) treat your phone like a dumb terminal. They sample your GPS sparingly to save battery on low-end devices, aggressively smooth out the data to hide inaccuracies, and then send it to a server so they can charge you a subscription to see your own stats.

I am a software engineer and data scientist, and I wanted to see what happens when you treat a modern flagship phone (like the Galaxy S25 Ultra) like the supercomputer it actually is. 

**Dekhreekh** is my personal fitness tracking engine. It is local-first, obsessively accurate, and designed to look like a high-end telemetry instrument rather than a social network. 

---

### The Core Philosophy

1. **Unconstrained Hardware:** I don't support 5-year-old budget phones. By dropping legacy support, this app can run 100Hz IMU polling, raw dual-band GPS, and real-time Kalman filtering without worrying about memory limits.
2. **Local Everything:** Your data shouldn't be held hostage. Dekhreekh uses Android Health Connect to pull wearable data, stores gigabytes of raw telemetry in a local Room database, and backs up directly to your personal Google Drive. Zero backend servers. Zero subscription costs.
3. **Standard Material 3 UI:** The app uses standard Android UI components with dynamic theming to adhere to Android's native design guidelines while keeping the map prominent.

---

### What's Inside?

* **Raw Sensor Fusion:** We don't just trust the GPS. The app fuses L1/L5 satellite data with the phone's accelerometer and gyroscope to maintain sub-meter accuracy even when you're running under a bridge or in dense forests.
* **On-Device Data Science:** Instead of pinging an API, the app runs LiteRT (TensorFlow Lite) models directly on the phone's NPU to calculate real-time metrics like Aerobic Decoupling (Pw:HR) and gait anomalies.
* **Bespoke Nutrition:** Includes a custom offline nutrition manager specifically tailored for custom dietary preferences, calculating actual recovery windows based on workout intensity.
* **Offline Vector Maps:** Powered by MapLibre and OpenFreeMap. The app caches vector tiles for your local region, so the map renders at 60fps even if you have zero cell service.
* **Heart Pulse Path Shaders:** Instead of a boring blue line on a map, your route is rendered with a custom shader that physically "pulses" to the rhythm of your live heart rate.

---

### The Tech Stack

Built for Android 16 (API 36 Baklava). 

* **Language:** Kotlin 2.1+
* **UI:** Jetpack Compose + AGSL (RuntimeShaders)
* **Architecture:** Clean Architecture, MVI, Coroutines/Flow, Dagger Hilt
* **Database:** Room (SQLite) structured for heavy data warehousing.
