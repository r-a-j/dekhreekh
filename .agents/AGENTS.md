# Dekhreekh Project Agent Rules

1. **NO AUTO-COMMITS**: Do not commit or push any code unless explicitly instructed to do so by the user.
2. **LIVING DOCUMENTATION**: Maintain all feature, architecture, and codebase documentation accurately in a dedicated `docs/` folder. Update these docs as part of every feature completion.
3. **TDD MANDATE**: All new features must be developed using strict Test-Driven Development (TDD). Write failing unit/integration tests first, show them to the user, and then write the implementation to make them pass.
4. **NO MOCKS IN PROD**: Never fake a required library integration or UI component just to pass a test. If a specific third-party library is requested, you must import and use the actual classes from that library. Do not use generic SDK components with mocked test tags as placeholders in production code.
5. **THE SYSTEM REALITY CHECK:** You must never fake OS-level state (permissions, network, hardware sensors) to pass a test. Every time you implement a feature requiring Android OS access, you MUST proactively update `AndroidManifest.xml` and use actual platform APIs (e.g., `ActivityResultContracts`).
