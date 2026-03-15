# KMP Migration Design — CryptoPortfolio

## Goal
Migrate the Android-only CryptoPortfolio app to Kotlin Multiplatform + Compose Multiplatform, adding iOS support while keeping clean architecture and full feature parity.

## Project Structure

```
Top100App/
├── shared/                              # KMP shared module (~90% of code)
│   └── src/
│       ├── commonMain/kotlin/.../
│       │   ├── domain/                  # Models, interfaces, use cases (moved as-is)
│       │   ├── data/
│       │   │   ├── network/             # Ktor services, DTOs, signing plugin
│       │   │   └── repository/          # Repository implementations
│       │   ├── ui/
│       │   │   ├── mvi/                 # MviStore, Reducer, ScreenModel
│       │   │   ├── screen/              # All composables (portfolio/, settings/)
│       │   │   ├── theme/               # AppTheme, colors, typography
│       │   │   └── navigation/          # Stack-based Navigator
│       │   ├── di/                      # Koin common modules
│       │   └── platform/               # expect declarations
│       ├── androidMain/kotlin/.../
│       │   ├── platform/               # actual: EncryptedSharedPrefs, Room builder
│       │   └── di/                     # actual: platform Koin bindings
│       └── iosMain/kotlin/.../
│           ├── platform/               # actual: iOS Keychain, Room builder
│           └── di/                     # actual: platform Koin bindings
├── androidApp/                          # Thin Android shell
│   └── src/main/
│       ├── kotlin/.../ CryptoApp.kt, MainActivity.kt
│       ├── res/                         # Launcher icons, themes.xml
│       └── AndroidManifest.xml
├── iosApp/                              # Xcode project
│   └── iosApp/ iOSApp.swift, ContentView.swift
└── gradle/libs.versions.toml
```

## Platform Abstractions (expect/actual)

Only 3 expect/actual declarations needed:

### 1. SecureStorage
- **commonMain**: expect class with getString/putString/remove/clear
- **androidMain**: wraps EncryptedSharedPreferences + MasterKeys
- **iosMain**: wraps iOS Keychain (Security framework)

### 2. getDatabaseBuilder
- **commonMain**: expect function returning RoomDatabase.Builder<AppDatabase>
- **androidMain**: Room.databaseBuilder(context, ...)
- **iosMain**: Room.databaseBuilder(NSDocumentDirectory path)

### 3. PlatformContext
- **commonMain**: expect class
- **androidMain**: typealias to android.content.Context
- **iosMain**: empty class (iOS doesn't need context)

## Dependency Injection: Hilt -> Koin

### Common module
```kotlin
val commonModule = module {
    // Use cases
    factory { CheckSettingsUseCase(get()) }
    factory { GetSettingsUseCase(get()) }
    factory { SaveSettingsUseCase(get()) }
    factory { GetPortfolioUseCase(get()) }
    factory { UpdatePortfolioUseCase(get(), get(), get()) }
    factory { SellUseCase(get()) }
    factory { RebalancerUseCase(get(), get()) }

    // Repositories
    single<PortfolioRepository> { PortfolioRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<CmcRepository> { CmcRepositoryImpl(get()) }
    single<MexcRepository> { MexcRepositoryImpl(get()) }

    // Network
    single { createCmcHttpClient() }
    single { createMexcHttpClient(get()) }
    single { CmcApiService(get()) }
    single { MexcApiService(get()) }

    // Database
    single { get<AppDatabase>().portfolioDao() }

    // ScreenModels
    factory { PortfolioScreenModel(get(), get(), get(), get(), get()) }
    factory { SettingsScreenModel(get(), get(), get()) }
}
```

### Platform modules provide SecureStorage + Database builder
- androidModule: Context-based implementations
- iosModule: Keychain + file-path-based implementations

### Initialization
- Android: startKoin in CryptoApp.onCreate()
- iOS: initKoin() called from Swift before UI

## Networking: Retrofit/OkHttp -> Ktor

### HTTP Clients
- createCmcHttpClient(): ContentNegotiation (JSON), Logging, base URL
- createMexcHttpClient(secureStorage): same + MexcSigningPlugin (HMAC-SHA256)

### API Services become classes
- CmcApiService(client): suspend fun getListings(apiKey, limit, sort)
- MexcApiService(client): suspend fun getAccount(), getExchangeInfo(), getTickerPrice(), placeOrder()

### DTOs
- Gson annotations replaced with @Serializable (kotlinx.serialization)
- Field names already match JSON keys, minimal changes

### HMAC-SHA256 Signing
- MexcSigningPlugin (Ktor HttpClientPlugin) replaces MexcSigningInterceptor
- signMexcQuery() is already pure Kotlin (javax.crypto.Mac), works on both platforms

## ViewModel Replacement: ScreenModel

```kotlin
abstract class ScreenModel<S, I, E, EF, UiState>(...) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    // StateFlow-based state management (same as current ScreenViewModel)
    // launchIn {} replaces viewModelScope.launch {}
    // onCleared() cancels scope — called via DisposableEffect
}
```

Changes per ViewModel:
- Rename to ScreenModel, extend ScreenModel instead of ScreenViewModel
- Remove @HiltViewModel, @Inject constructor
- Replace viewModelScope.launch with launchIn
- All business logic stays identical

## Navigation: Custom Stack

```kotlin
sealed interface Screen {
    data object Portfolio : Screen
    data object Settings : Screen
}

class Navigator {
    private val _stack = mutableStateListOf<Screen>(Screen.Portfolio)
    fun navigate(screen: Screen) { _stack.add(screen) }
    fun back(): Boolean { ... }
}
```

- App() composable uses `when (navigator.current)` to show screens
- CompositionLocal provides Navigator to all composables
- BackHandler on Android for system back button

## Compose UI Migration

Most composables move to commonMain unchanged. Small changes:
- Coil 2 -> Coil 3 (KMP) — same AsyncImage API
- collectAsStateWithLifecycle -> collectAsState
- hiltViewModel() -> koinInject()
- R.string.* -> Compose Multiplatform stringResource() from commonMain/composeResources/
- Material Icons (SwapVert, AccountBalance, etc.) available in Compose Multiplatform

## Logging: Timber -> expect/actual Logger

```kotlin
expect object Logger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
```
- androidMain: android.util.Log
- iosMain: NSLog

## Library Replacements Summary

| Concern | Current | KMP Replacement |
|---|---|---|
| DI | Hilt 2.57.2 | Koin |
| Networking | Retrofit 2.11 + OkHttp 4.12 | Ktor |
| Database | Room 2.7.1 | Room KMP 2.7.1 |
| Secure storage | EncryptedSharedPreferences | expect/actual |
| ViewModel | AndroidX ViewModel | Custom ScreenModel |
| Navigation | Navigation3 | Custom stack Navigator |
| UI | Jetpack Compose | Compose Multiplatform |
| Image loading | Coil 2.7 | Coil 3 (KMP) |
| Strings | Android R.string | Compose Multiplatform resources |
| Logging | Timber | expect/actual Logger |
| Serialization | Gson | kotlinx.serialization |
