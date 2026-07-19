# Codebase Concerns

**Analysis Date:** 2026-07-17

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
