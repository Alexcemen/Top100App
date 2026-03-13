# Архитектура

## Структура модулей
Единый модуль `:app` с тремя чистыми слоями:

```
app/src/main/kotlin/com/alexcemen/cryptoportfolio/
├── ui/          — Слой представления (MVI, экраны, тема, навигация)
├── domain/      — Бизнес-логика (модели, интерфейсы репозиториев, use case-ы)
└── data/        — Слой данных (Room БД, Retrofit сеть, реализации репозиториев)
```

## Паттерн MVI
Каждый экран следует схеме: **Store → Reducer → ViewModel → Composables**

- `Store` объект: определяет закрытые типы `State`, `UiState`, `Event`, `Effect`, `SideEffect`
- `Reducer` класс: чистая функция `State → UiState`, покрывается unit-тестами
- `ViewModel` расширяет `ScreenViewModel`: обрабатывает события, эмитирует эффекты, отправляет side effect-ы
- Composables: собирают `uiState`, вызывают `onEvent`, обрабатывают `sideEffect {}`

## Базовые MVI классы (ui/mvi/)
- `MviStore.kt` — маркерные интерфейсы
- `ScreenViewModel.kt` — абстрактная база с потоками state/effect/sideEffect
- `MviExtensions.kt` — composable-хелпер `sideEffect {}`
- `Screen.kt` — абстрактный класс `AppNavKey` для навигации

## Навигация
Navigation3 с `NavDisplay`. Стек навигации типизирован как `NavBackStack<NavKey>`.
`RootNavigation` compositionLocal предоставляет стек навигации всем composable-компонентам.
Пункты назначения: `PortfolioScreen`, `SettingsScreen` в `ui/navigation/Navigator.kt`.

## Внедрение зависимостей
Hilt со скоупом `ActivityRetainedComponent`. Модули:
- `DatabaseModule` — Room база данных + DAO
- `NetworkModule` — OkHttpClient (@Named), Retrofit, API сервисы
- `RepositoryModule` — привязывает реализацию к интерфейсу

## Соглашения по именованию
- Store: `[Screen]Store` (например, `PortfolioStore`)
- ViewModel: `[Screen]ViewModel`
- Reducer: `[Screen]Reducer`
- Точка входа экрана: `[Screen]ScreenContent()`
- Composable содержимого: `[Screen]Content()`
