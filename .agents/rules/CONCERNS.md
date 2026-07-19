# Codebase Concerns

**Analysis Date:** 2026-07-17

## Test Coverage Gaps

**Sensor Fusion & Shaders:**
- What's not tested: Sensor fusion algorithms and the Heart Pulse Path AGSL shader.
- Risk: Inaccuracies in sensor fusion could lead to bad telemetry maps, and shader compiling issues on specific GPUs could crash the app.
- Priority: Medium
- Difficulty to test: Requires emulator sensor injection tests and AGSL shader compilation verification tools.

---

*Concerns audit: 2026-07-17*
*Update as issues are fixed or new scaling limitations are identified*
