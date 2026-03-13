# Design Spec: Crypto Portfolio Android App

**Date:** 2026-03-13
**Status:** Approved
**Based on:** Tutor (template), ANDROID_REFERENCE.md (backend bot logic)

---

## What We Are Building

A personal Android app that:
- Shows the user's MEXC cryptocurrency portfolio (coin name, price, quantity, position size in USDT)
- Displays the total portfolio value in USDT
- Allows manual portfolio update (fetches live data from MEXC + saves to Room)
- Triggers the 4-step rebalancing algorithm from the backend bot
- Sells a specified USDT amount across all held coins proportionally
- Stores API keys securely in `EncryptedSharedPreferences`; stores portfolio data in Room DB

---

## App Configuration

| Property | Value |
|----------|-------|
| Application ID | `com.alexcemen.cryptoportfolio` |
| `minSdk` | 26 |
| `compileSdk` | 35 |
| `targetSdk` | 35 |
| No product flavors | single variant: `debug` / `release` |

---

## Architecture

### Module Structure

Single `:app` Gradle module. Three clean architecture package layers inside:

```
app/
├── ui/           — screens, ViewModels, MVI, Composables, theme, navigation
├── domain/       — use cases, repository interfaces, domain models
├── data/         — repository implementations, Room entities/DAOs, Retrofit services, DTOs
└── di/           — Hilt modules
```

### MVI Pattern

Identical to Tutor. Every screen has:
```
ui/screen/<screen_name>/
├── <ScreenName>Store.kt        — State, UiState, Event, Effect, SideEffect
├── <ScreenName>ViewModel.kt    — extends ScreenViewModel, handleEvent/handleEffect
├── <ScreenName>Reducer.kt      — State → UiState (pure function)
└── composable/
    ├── <ScreenName>Screen.kt   — collects uiState, handles sideEffects
    └── <ScreenName>Content.kt  — stateless content composable
```

### Navigation

AndroidX Navigation3. All destinations are `@Serializable data class` implementing `AppNavKey`. Back stack type: `NavBackStack<AppNavKey>`.

| Destination | Parameters | Purpose |
|-------------|------------|---------|
| `PortfolioScreen` | — | Main screen (root) |
| `SettingsScreen` | — | API keys + config |

---

## Storage

### EncryptedSharedPreferences (settings)

API keys and config are stored in `EncryptedSharedPreferences` (file name: `crypto_secure_prefs`). All 5 settings fields live here — no settings in Room.

| Key | Type | Default |
|-----|------|---------|
| `cmc_api_key` | String | `""` |
| `mexc_api_key` | String | `""` |
| `mexc_api_secret` | String | `""` |
| `top_coins_limit` | Int | `20` |
| `excluded_coins` | String | `"USDT,USDC,BUSD"` (comma-separated) |

`EncryptedSharedPreferences` encrypts both keys and values using AES256-GCM / AES256-SIV, backed by Android Keystore.

## Database Schema

**Class:** `AppDatabase`, **Version:** 1, **Entities:** `[CoinEntity::class]`
No migrations needed at version 1.

### `portfolio_table` (one row per coin)

| Column | Type | Notes |
|--------|------|-------|
| `symbol` | String (PK) | e.g. "ETH", "SOL" |
| `priceUsdt` | Double | Price per coin in USDT |
| `quantity` | Double | Number of coins held |

---

## Domain Models

| Model | Fields | Notes |
|-------|--------|-------|
| `CoinData` | `symbol, priceUsdt, quantity` | Computed property: `totalPositionUsdt = priceUsdt * quantity` |
| `PortfolioData` | `coins: List<CoinData>, totalUsdt: Double` | `totalUsdt` computed in the repository as `sum of all totalPositionUsdt` |
| `SettingsData` | `cmcApiKey, mexcApiKey, mexcApiSecret, topCoinsLimit, excludedCoins: List<String>` | Backed by EncryptedSharedPreferences, not Room |

### Naming Conventions (identical to Tutor)

| Layer | Convention | Example |
|-------|------------|---------|
| Domain model | `<Entity>Data` | `CoinData`, `SettingsData` |
| UI model | `<Entity>Ui` | `CoinUi`, `PortfolioUi` |
| Room entity | `<Entity>Entity` | `CoinEntity`, `SettingsEntity` |
| Repository interface | `<Entity>Repository` | `PortfolioRepository` |
| Repository impl | `<Entity>RepositoryImpl` | `PortfolioRepositoryImpl` |
| Use case | `<Verb><Entity>UseCase` | `UpdatePortfolioUseCase` |
| ViewModel | `<Screen>ViewModel` | `PortfolioViewModel` |
| Store | `<Screen>Store` | `PortfolioStore`, `SettingsStore` |
| Reducer | `<Screen>Reducer` | `PortfolioReducer`, `SettingsReducer` |

---

## Repository Interfaces

**`PortfolioRepository`**
- `fun getPortfolio(): Flow<PortfolioData>` — live stream from Room; `totalUsdt` computed here
- `suspend fun savePortfolio(coins: List<CoinData>)` — replaces all rows in portfolio_table

**`SettingsRepository`**
- `suspend fun getSettings(): SettingsData` — runs on `Dispatchers.IO`
- `suspend fun saveSettings(settings: SettingsData)` — runs on `Dispatchers.IO`

All `suspend` calls in repositories are dispatched on `Dispatchers.IO` inside the implementation. Use cases and ViewModels do not set dispatchers themselves.

---

## Use Cases

| Use Case | Return type | Description |
|----------|-------------|-------------|
| `GetPortfolioUseCase` | `Flow<PortfolioData>` | Returns live Room stream |
| `UpdatePortfolioUseCase` | `Result<Unit>` | Fetches MEXC balances + prices → saves to DB |
| `RebalancerUseCase` | `Result<Unit>` | Full 4-step algorithm (ported from `Rebalancer.kt`) |
| `SellUseCase` | `Result<Unit>` | Sells specified USDT amount proportionally across held coins |
| `GetSettingsUseCase` | `SettingsData` | Returns current settings from DB |
| `SaveSettingsUseCase` | `Unit` | Saves settings to DB |
| `CheckSettingsUseCase` | `Boolean` | Returns true if all 3 API keys are non-empty |

### Error handling

`UpdatePortfolioUseCase`, `RebalancerUseCase`, and `SellUseCase` each internally call `CheckSettingsUseCase` first. If keys are missing they return `Result.failure(MissingApiKeysException)`. Otherwise they proceed with network calls and return `Result.failure(...)` on any API or network error. ViewModels check `result.isFailure` and emit a `SideEffect.ShowSnackbar(message)`.

### HMAC Signing Interceptor

The OkHttp interceptor for MEXC signing needs the API key and secret at request time. Solution: inject a `SettingsRepository` (or a dedicated `ApiKeyProvider`) into the interceptor. Since interceptors are called on a background thread, the repository's `suspend fun getSettings()` is called via `runBlocking` inside the interceptor. The interceptor is only attached to the MEXC `OkHttpClient`, not the CMC client.

### Rebalancer Algorithm (4 steps, ported from Rebalancer.kt)

```
Step 1 — Build coin lists (parallel fetch):
  topCmc       = CoinMarketCap top-N symbols
  tradableMexc = MEXC tradable base assets
  available    = topCmc ∩ tradableMexc − excludedCoins
  mine         = my MEXC balances with value > 0 (excluding USDT)
  toSell       = mine − available − excludedCoins

Step 2 — Sell unlisted:
  For each coin in toSell where USDT value > $1: place SELL market order

Step 3 — Buy missing:
  missing      = available − mine
  averageValue = mean(my coin USDT balances)
  For each missing coin: buy min(averageValue, remainingUSDT)

Step 4 — Rebalance weights:
  target = totalPortfolioValue / numberOfCoins
  Sell excess from over-weight coins (value − target > $1)
  Buy deficit for under-weight coins (target − value > $1)
```

---

## Network Layer

### Retrofit Services

**`CmcApiService`**
- `GET /v1/cryptocurrency/listings/latest?limit={n}&sort=market_cap`
- Auth: `X-CMC_PRO_API_KEY` header

**`MexcApiService`**
- `GET /api/v3/account` — balances (HMAC-SHA256 signed)
- `GET /api/v3/exchangeInfo` — tradable symbols
- `GET /api/v3/ticker/price` — all current prices (bulk)
- `POST /api/v3/order` — place market buy/sell order

HMAC-SHA256 signing lives in an OkHttp `Interceptor` in the data layer (see above). ViewModels and use cases never touch signing.

---

## Screens

### PortfolioScreen (main screen)

**On init:** `PortfolioViewModel` collects `GetPortfolioUseCase()` via `startIntent` or `forceEffect` on creation — immediately loads cached portfolio from Room. Screen shows the last saved state without the user needing to tap Update.

**Top bar:** App title + gear icon → navigates to `SettingsScreen`

**Header:** Total portfolio value in USDT (e.g. `$1,247.83`). Empty state shown when Room has no data yet.

**Body:** Lazy list of coins. Each item shows:
1. Symbol (e.g. ETH)
2. Price per coin
3. Quantity of coins held
4. Total position size in USDT

**Bottom bar — 3 buttons (disabled during any loading operation):**
- `Update` — fetches live MEXC data, saves to DB, loading overlay
- `Rebalance` — runs 4-step algorithm, loading overlay
- `Sell` — opens sell bottom sheet

**Loading state:** A single `isLoading: Boolean` flag in `State`. While `isLoading = true`, all three bottom buttons are disabled and a full-screen loading overlay is shown. The coroutine is tied to `viewModelScope` — navigating away cancels it automatically.

**Sell bottom sheet:**
- USDT amount input field
- Row of 4 quick-fill buttons: 25% / 50% / 75% / 100% (fills input with `totalUsdt × percentage`; 100% = sell full portfolio value)
- `Sell` button — dismisses sheet, starts sell process with loading overlay
- Errors surfaced as Snackbar

---

### SettingsScreen

Each row has **read mode** (default) and **edit mode** (tap pencil to activate). Only one row editable at a time — switching rows auto-cancels the current edit without saving.

**Read mode:**
```
CoinMarketCap API Key    abc123xyz...    [✏️]
```
**Edit mode:**
```
CoinMarketCap API Key    [_abc123xyz___]    [✓]
```

Tap ✓ saves that field individually to DB. Tap away/back cancels without saving.

**Rows 1–4** (standard inline edit):
1. CoinMarketCap API Key
2. MEXC API Key
3. MEXC API Secret
4. Top Coins Limit (number input)

**Row 5 — Excluded Coins** (chip list):

*Read mode:*
```
Excluded Coins   [USDT] [USDC] [BUSD] →scrollable→   [✏️]
```

*Edit mode:*
```
Excluded Coins   [USDT ✕] [USDC ✕] [BUSD ✕]  [________] [+]   [✓]
```
- Each chip shows ✕ — tap to remove instantly
- Text input at end of scrollable row — type symbol, tap + to add chip
- Tap ✓ to save full list to DB
- Stored internally as comma-separated string in `settings_table`; displayed as chip list in UI

---

## Dependency Injection

### Hilt Setup

- Application class: `CryptoApp` — annotated with `@HiltAndroidApp`
- Main activity: `MainActivity` — annotated with `@AndroidEntryPoint`
- All ViewModels: `@HiltViewModel`

### Hilt Modules

**`NetworkModule`** — `@InstallIn(ActivityRetainedComponent::class)`
- `OkHttpClient` (MEXC) with HMAC signing interceptor
- `OkHttpClient` (CMC) without signing interceptor
- `CmcApiService` (Retrofit → `api.coinmarketcap.com`)
- `MexcApiService` (Retrofit → `api.mexc.com`)
- Gson converter factory

**`DatabaseModule`** — `@InstallIn(ActivityRetainedComponent::class)`
- `AppDatabase` — Room singleton
- `PortfolioDao`

**`RepositoryModule`** — `@InstallIn(ActivityRetainedComponent::class)`
- `SettingsRepository` → `SettingsRepositoryImpl` (backed by `EncryptedSharedPreferences`)
- `PortfolioRepository` → `PortfolioRepositoryImpl` (backed by Room)

Use cases injected directly into ViewModels (no module needed — Hilt resolves them automatically).

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose BOM | 2025.06.00 | All Compose UI libraries |
| Material3 | (BOM) | UI components |
| AndroidX Core KTX | 1.16.0 | Kotlin extensions |
| AndroidX Activity Compose | 1.10.1 | `setContent {}` |
| AndroidX Lifecycle Runtime KTX | 2.9.1 | `viewModelScope` |
| AndroidX Navigation3 | 1.0.0 | Type-safe navigation |
| Hilt | 2.57.2 | Dependency injection |
| Room | 2.7.1 | Local database |
| Retrofit | 2.11.0 | HTTP client + API interfaces |
| OkHttp | 4.12.0 | Underlying HTTP + interceptors |
| AndroidX Security Crypto | 1.0.0 | `EncryptedSharedPreferences` for API keys |
| Gson | 2.11.0 | JSON parsing (Retrofit converter) |
| Kotlinx Coroutines Android | 1.10.1 | Async/coroutines |
| Kotlinx Serialization Core | 1.8.1 | Navigation3 destinations |
| Timber | 5.0.1 | Logging |
| JUnit 4 | 4.13.2 | Unit tests |
| AndroidX Test JUnit | 1.2.1 | Instrumented test runner |
| AndroidX Espresso | 3.6.1 | UI tests |

*No Firebase, WorkManager, ads SDKs, in-app review, or media libraries needed.*

---

## UI Standards

Mirrors Tutor's design system:
- Theme accessed via `AppTheme.colors.*` and `AppTheme.textStyle.*` — never hardcode colors or text styles
- Dark mode first-class — every screen verified in both themes
- Consistent corner radius: buttons 12dp, cards follow `AppCard` defaults
- Reusable composables: `PrimaryButton`, `AppCard`, `ContainerContent`, `MainToolbar`, `TextField`
- Text must never overflow — test with long values before marking any UI task complete

---

## Testing

### Testing approach

Unit tests use **hand-written fakes** for repositories and use cases — no Mockito or MockK needed.

### Naming Convention
```
methodUnderTest_scenario_expectedBehavior

// Examples:
updatePortfolio_validKeys_savesCoinsToDb
rebalancer_missingKeys_returnsFailureResult
reduce_loadingState_showsLoadingIndicator
checkSettings_emptyKey_returnsFalse
```

### Unit Test Priority
1. Use cases (fake repositories)
2. Reducers (pure functions: given State → assert UiState)
3. ViewModels (fake use cases)

### Instrumented Test Priority
1. Room DAOs (in-memory DB)
2. Repository implementations (in-memory DB)
3. Navigation flows

---

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test
./gradlew :app:test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run a specific test class
./gradlew :app:testDebugUnitTest --tests "com.alexcemen.cryptoportfolio.domain.usecase.RebalancerUseCaseTest"
```

---

## Project Documentation Structure

```
.claude/
├── docs/
│   ├── en/
│   │   ├── architecture.md
│   │   ├── features.md
│   │   ├── screens.md
│   │   ├── di.md
│   │   ├── data.md
│   │   ├── testing.md
│   │   ├── dependencies.md
│   │   └── ui-guidelines.md
│   └── ru/
│       ├── CLAUDE.md       ← Russian copy of root CLAUDE.md
│       ├── architecture.md
│       ├── features.md
│       ├── screens.md
│       ├── di.md
│       ├── data.md
│       ├── testing.md
│       ├── dependencies.md
│       └── ui-guidelines.md
└── ANDROID_REFERENCE.md
CLAUDE.md
```

### CLAUDE.md Mandatory Rules

1. **Approval before any change** — explain plan → wait for "yes" → implement → review via `git diff`
2. **Documentation is the source of truth** — update both `en/` and `ru/` docs after every task. Outdated docs = bug. Keep `ru/CLAUDE.md` in sync with root `CLAUDE.md`.
3. **Read all docs before every task** — before any feature, refactor, or bug fix, read all files in `.claude/docs/en/`
4. **Follow existing patterns strictly** — MVI, naming, DI, navigation. No new patterns without approval.
5. **Use context7 MCP** — always fetch up-to-date docs for any library via `mcp__context7__resolve-library-id` + `mcp__context7__query-docs`
