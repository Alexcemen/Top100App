# Web Version (wasmJs) Design Spec

## Goal

Add a browser-based version of the CryptoPortfolio app. Users open a link (GitHub Pages), enter their API keys once, and use the app on their phone's browser. Keys and portfolio data persist in `localStorage` per user, per device. No server involved.

## Hosting

GitHub Pages at `alexcemen.github.io/Top100App`. Static files only (HTML + JS + WASM). Deployed via GitHub Actions on push to `main`.

## Architecture

### New target: `wasmJs` in `:shared`

The existing `shared/build.gradle.kts` gets a `wasmJs { browser() }` target. All `commonMain` code (UI, MVI, domain, navigation, Koin, Ktor, Coil) compiles to WASM without changes.

### New module: `:webApp`

A thin entry point module that:
- Calls `ComposeViewport` to mount `App()`
- Starts Koin with `commonModule + webModule`
- Provides the `index.html` shell

### New source set: `wasmJsMain` in `:shared`

Actual implementations for all expect declarations:

| expect | wasmJs actual |
|--------|---------------|
| `SecureStorage` | Wrapper around `localStorage` |
| `getDatabaseBuilder()` | Not applicable, see Repository section below |
| `PlatformContext` | Empty object |
| `Logger` | `println()` (maps to console.log in wasmJs) |
| `signMexcQuery()` (HMAC-SHA256) | Pure Kotlin implementation |
| `currentTimeMillis()` | `Date.now().toLong()` |
| `formatNumber()` | Already pure Kotlin in commonMain, no actual needed |

### Room replacement for web

Room does not support `wasmJs`. The solution:

1. Keep `PortfolioRepository` interface in `commonMain` unchanged.
2. Create `WebPortfolioRepositoryImpl` in `wasmJsMain` that stores coins serialized as JSON in `localStorage`.
3. Bind it in `webModule` (Koin) instead of the Room-backed implementation.
4. Move Room-specific Koin bindings (PortfolioDao, PortfolioRepositoryImpl) from `commonModule` to `androidModule`/`iosModule`.

### Ktor engine for web

Add `ktor-client-js` dependency in `wasmJsMain`.

### Koin web module

`webModule` provides: SecureStorage, WebPortfolioRepositoryImpl as PortfolioRepository.

### CORS consideration

MEXC and CMC APIs may not allow browser CORS requests. If so, a minimal Cloudflare Worker proxy (free, serverless) can forward requests. Needs testing during implementation.

## Security

- API keys stored in `localStorage`, scoped to origin
- No server, no telemetry, no third-party scripts
- Keys never leave the browser (only sent directly to MEXC/CMC APIs via fetch)
- Each user on their own device has isolated storage (same-origin policy)

## Deployment flow

1. Push to `main`
2. GitHub Actions runs `./gradlew :webApp:wasmJsBrowserDistribution`
3. Output published to GitHub Pages
4. Static files served at `alexcemen.github.io/Top100App`
