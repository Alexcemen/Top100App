# Testing Strategy

## Test Locations
- `app/src/test/` — JVM unit tests (no Android dependencies)
- `app/src/androidTest/` — Instrumented tests (require device/emulator)

## Priority Order
1. **Reducers** — pure functions, fast, high value
2. **Use cases** — business logic with fake repos/services
3. **DAO tests** — Room in-memory database (instrumented)

## TDD Workflow
1. Write failing test
2. Run — expect compile error or failure
3. Implement minimum code to pass
4. Run — expect pass
5. Commit

## Test Naming
Format: `[scenario]_[expectedBehavior]`
Examples: `allKeysPresent_returnsTrue`, `emptyPortfolio_mapsCorrectly`

## Fake Implementations
Use anonymous object implementations of repository/service interfaces — no mocking framework needed:
```kotlin
private val fakeRepo = object : SettingsRepository {
    override suspend fun getSettings() = SettingsData(cmcApiKey = "key")
    override suspend fun saveSettings(settings: SettingsData) {}
}
```

## Key Dependencies
- `kotlinx-coroutines-test` for `runTest` in both `testImplementation` and `androidTestImplementation`
- `junit:4.13.2` for unit tests
- Room in-memory builder for DAO tests
