# Architecture

## Module Structure
Single `:app` module with three clean layers:

```
app/src/main/kotlin/com/alexcemen/cryptoportfolio/
├── ui/          — Presentation layer (MVI, screens, theme, navigation)
├── domain/      — Business logic (models, repositories interfaces, use cases)
└── data/        — Data layer (Room DB, Retrofit network, repository impls)
```

## MVI Pattern
Each screen follows: **Store → Reducer → ViewModel → Composables**

- `Store` object: defines `State`, `UiState`, `Event`, `Effect`, `SideEffect` sealed types
- `Reducer` class: pure function `State → UiState`, unit-testable
- `ViewModel` extends `ScreenViewModel`: handles events, emits effects, sends side effects
- Composables: collect `uiState`, call `onEvent`, handle `sideEffect {}`

## Base MVI Classes (ui/mvi/)
- `MviStore.kt` — marker interfaces
- `ScreenViewModel.kt` — abstract base with state/effect/sideEffect flows
- `MviExtensions.kt` — `sideEffect {}` composable helper
- `Screen.kt` — `AppNavKey` abstract class for navigation

## Navigation
Navigation3 with `NavDisplay`. Back stack typed as `NavBackStack<NavKey>`.
`RootNavigation` compositionLocal provides back stack to all composables.
Screen destinations: `PortfolioScreen`, `SettingsScreen` in `ui/navigation/Navigator.kt`.

## Dependency Injection
Hilt with `ActivityRetainedComponent` scope. Modules:
- `DatabaseModule` — Room database + DAO
- `NetworkModule` — OkHttpClient (@Named), Retrofit, API services
- `RepositoryModule` — binds impl to interface

## Naming Conventions
- Store: `[Screen]Store` (e.g. `PortfolioStore`)
- ViewModel: `[Screen]ViewModel`
- Reducer: `[Screen]Reducer`
- Screen composable entry point: `[Screen]ScreenContent()`
- Content composable: `[Screen]Content()`
