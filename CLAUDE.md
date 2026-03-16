# CryptoPortfolio — Project Memory

## Architecture
- Kotlin Multiplatform (KMP) with three modules: `:shared` (KMP), `:androidApp`, `:iosApp`
- Three clean layers in `shared/src/commonMain/`: `ui`, `domain`, `data`
- MVI pattern: Store (State/UiState/Event/Effect/SideEffect) + ScreenModel + Reducer
- Navigation: Custom stack-based Navigator with CompositionLocal
- DI: Koin 4.0.4 — commonModule + platform modules (androidModule, iosModule)
- Platform abstractions via expect/actual: SecureStorage, DatabaseBuilder, PlatformContext, Logger, BackHandler

## Package: `com.alexcemen.cryptoportfolio`
## Min SDK: 26 | Compile SDK: 36 | Target SDK: 35

## Key Files
- `androidApp/.../CryptoApp.kt` — Application class, startKoin
- `androidApp/.../MainActivity.kt` — setContent { App() }
- `shared/.../ui/App.kt` — Root composable, Navigator + screen routing + BackHandler
- `shared/.../ui/mvi/` — MviStore.kt, ScreenModel.kt, MviExtensions.kt
- `shared/.../ui/theme/` — AppTheme.kt, AppThemeColors.kt, AppThemeTypography.kt
- `shared/.../ui/navigation/Navigator.kt` — Screen sealed interface, stack Navigator, LocalNavigator
- `shared/.../ui/screen/portfolio/` — PortfolioStore, PortfolioReducer, PortfolioScreenModel + composables
- `shared/.../ui/screen/settings/` — SettingsStore, SettingsReducer, SettingsScreenModel + composables
- `shared/.../domain/model/` — CoinData, PortfolioData, SettingsData
- `shared/.../domain/repository/` — PortfolioRepository, SettingsRepository interfaces
- `shared/.../domain/usecase/` — CheckSettings, GetSettings, SaveSettings, GetPortfolio, UpdatePortfolio, Sell, Rebalancer
- `shared/.../data/db/` — AppDatabase (Room KMP + @ConstructedBy), PortfolioDao, CoinEntity
- `shared/.../data/network/` — CmcApiService, MexcApiService (Ktor), MexcSigning (expect/actual HMAC-SHA256)
- `shared/.../data/repository/` — PortfolioRepositoryImpl (Room), SettingsRepositoryImpl (SecureStorage)
- `shared/.../di/CommonModule.kt` — All common Koin bindings
- `shared/.../platform/` — SecureStorage, DatabaseBuilder, PlatformContext, Logger, FormatUtil
- `iosApp/` — SwiftUI shell (iOSApp.swift, ContentView.swift)

## Permissions
- All code changes in this repo (edits, builds, tests, commits, file writes) — proceed without asking
- Only ask before: network requests to external services, accessing files outside this repo, destructive git operations that can't be rolled back (force-push to remote, etc.)

## Rules
1. API keys stored ONLY in SecureStorage (EncryptedSharedPreferences on Android, Keychain on iOS) — never in plain storage
2. New screens: create Store → Reducer (with unit test) → ScreenModel → Composables
3. Use cases are plain classes with constructor injection, operator fun invoke()
4. Koin: use cases as `factoryOf`, repositories as `singleOf`, ScreenModels as `factoryOf`
5. Network: two separate Ktor HttpClient instances with `named("mexc")` and `named("cmc")` qualifiers
6. MEXC API calls require HMAC-SHA256 signature on query string (expect/actual: javax.crypto on Android, CoreCrypto on iOS)
7. All formatting of doubles uses `formatNumber()` from platform/FormatUtil.kt (locale-independent, always uses `.` decimal separator)

## Testing Strategy
- Unit tests in `shared/src/commonTest/` — use cases, reducers (kotlin.test)
- TDD: write test first, then implement

## Dependencies (key versions)
- AGP 8.10.1, Kotlin 2.2.21, KSP 2.3.6
- Koin 4.0.4, Room KMP 2.8.4 (schema version: 2) + sqlite-bundled + BundledSQLiteDriver
- Ktor 3.1.1, kotlinx.serialization 1.8.1
- Compose Multiplatform 1.8.0
- Security Crypto 1.0.0 (Android), iOS Keychain (Security framework)
- Coil 3.1.0 (coil-compose + coil-network-ktor3) — async coin icon loading
- material-icons-extended — SwapVert, AccountBalance, CallMade icons
