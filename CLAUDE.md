# CryptoPortfolio — Project Memory

## Architecture
- Single `:app` module, three clean layers: `ui`, `domain`, `data`
- MVI pattern: Store (State/UiState/Event/Effect/SideEffect) + ScreenViewModel + Reducer
- Navigation: Navigation3 (NavDisplay, NavBackStack), screen keys in `ui/navigation/Navigator.kt`
- DI: Hilt 2.57.2, `ActivityRetainedComponent` scope for all modules

## Package: `com.alexcemen.cryptoportfolio`
## Min SDK: 26 | Compile SDK: 36 | Target SDK: 35

## Key Files
- `CryptoApp.kt` — Application class, Timber init
- `MainActivity.kt` — NavDisplay host, RootNavigation compositionLocal
- `ui/mvi/` — MviStore.kt, ScreenViewModel.kt, Screen.kt, MviExtensions.kt
- `ui/theme/` — AppTheme.kt, AppThemeColors.kt, AppThemeTypography.kt
- `ui/navigation/Navigator.kt` — PortfolioScreen, SettingsScreen nav keys
- `ui/screen/portfolio/` — PortfolioStore, PortfolioReducer, PortfolioViewModel + composables
- `ui/screen/settings/` — SettingsStore, SettingsReducer, SettingsViewModel + composables
- `domain/model/` — CoinData, PortfolioData, SettingsData
- `domain/repository/` — PortfolioRepository, SettingsRepository interfaces
- `domain/usecase/` — CheckSettings, GetSettings, SaveSettings, GetPortfolio, UpdatePortfolio, Sell, Rebalancer
- `data/db/` — AppDatabase, PortfolioDao, CoinEntity (Room, portfolio_table)
- `data/network/` — CmcApiService, MexcApiService, MexcSigningInterceptor, DTOs
- `data/repository/` — PortfolioRepositoryImpl (Room), SettingsRepositoryImpl (EncryptedSharedPreferences)
- `di/` — DatabaseModule, NetworkModule, RepositoryModule

## Permissions
- All code changes in this repo (edits, builds, tests, commits, file writes) — proceed without asking
- Only ask before: network requests to external services, accessing files outside this repo, destructive git operations that can't be rolled back (force-push to remote, etc.)

## Rules
1. API keys stored ONLY in EncryptedSharedPreferences — never in plain SharedPreferences or files
2. New screens: create Store → Reducer (with unit test) → ViewModel → Composables
3. Use cases are plain classes with `@Inject constructor`, operator fun invoke()
4. Hilt modules installed in `ActivityRetainedComponent` (not Singleton) — matches ViewModel lifecycle
5. Network: two separate OkHttpClient instances with `@Named("mexc")` and `@Named("cmc")` qualifiers
6. MEXC API calls require HMAC-SHA256 signature on query string
7. All formatting of doubles uses `Locale.US` to avoid locale-dependent decimal separators

## Testing Strategy
- Unit tests in `app/src/test/` — use cases, reducers (no Android dependencies)
- Instrumented tests in `app/src/androidTest/` — Room DAO tests
- TDD: write test first, then implement

## Dependencies (key versions)
- AGP 8.10.1, Kotlin 2.1.21, KSP 2.1.21-2.0.1
- Hilt 2.57.2, Room 2.7.1 (current schema version: 2), Retrofit 2.11.0, OkHttp 4.12.0
- Navigation3 1.0.0, Compose BOM 2025.06.00
- Security Crypto 1.0.0 (uses MasterKeys.getOrCreate API, not MasterKey.Builder)
- Coil 2.7.0 (coil-compose) — async coin icon loading
- material-icons-extended (BOM-managed) — SwapVert, AccountBalance, CallMade icons
