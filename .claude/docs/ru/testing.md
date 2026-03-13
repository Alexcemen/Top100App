# Стратегия тестирования

## Расположение тестов
- `app/src/test/` — JVM unit-тесты (без зависимостей от Android)
- `app/src/androidTest/` — инструментированные тесты (требуют устройство или эмулятор)

## Приоритеты
1. **Reducers** — чистые функции, быстрые, высокая ценность
2. **Use case-ы** — бизнес-логика с фейковыми репозиториями/сервисами
3. **DAO тесты** — Room с базой данных в памяти (инструментированные)

## TDD процесс
1. Написать падающий тест
2. Запустить — ожидать ошибку компиляции или падение
3. Реализовать минимальный код для прохождения
4. Запустить — ожидать успех
5. Зафиксировать в коммите

## Наименование тестов
Формат: `[сценарий]_[ожидаемоеПоведение]`
Примеры: `allKeysPresent_returnsTrue`, `emptyPortfolio_mapsCorrectly`

## Фейковые реализации
Использовать анонимные объекты-реализации интерфейсов репозиториев/сервисов — без фреймворка для моков:
```kotlin
private val fakeRepo = object : SettingsRepository {
    override suspend fun getSettings() = SettingsData(cmcApiKey = "key")
    override suspend fun saveSettings(settings: SettingsData) {}
}
```

## Ключевые зависимости
- `kotlinx-coroutines-test` для `runTest` в `testImplementation` и `androidTestImplementation`
- `junit:4.13.2` для unit-тестов
- Room in-memory builder для DAO тестов
