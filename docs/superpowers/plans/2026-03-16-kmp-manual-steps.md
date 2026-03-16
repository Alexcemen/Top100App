# KMP Migration — Manual Steps

> Эти шаги нужно выполнить вручную после завершения автоматической миграции на ветке `feature/kmp-migration`.

---

## Шаг 1: Создать Xcode проект для iOS

### 1.1 Создать проект
- Открой Xcode → File → New → Project
- Выбери: App → SwiftUI → Language: Swift
- Product Name: `iosApp`
- Bundle Identifier: `com.alexcemen.cryptoportfolio`
- Location: выбери папку `Top100App/` (корень репозитория)
- **Убери галочку** "Create Git repository" (репозиторий уже есть)

### 1.2 Удалить сгенерированные Swift-файлы
- Xcode создаст свои `ContentView.swift` и `iosAppApp.swift`
- Удали их из проекта (Delete → Move to Trash)
- Вместо них добавь существующие файлы:
  - `iosApp/iosApp/iOSApp.swift`
  - `iosApp/iosApp/ContentView.swift`

### 1.3 Подключить shared.framework

1. Сначала собери фреймворк:
   ```bash
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```

2. В Xcode → выбери таргет `iosApp` → General → "Frameworks, Libraries, and Embedded Content"
   - Нажми "+" → "Add Other..." → "Add Files..."
   - Перейди в `shared/build/bin/iosSimulatorArm64/debugFramework/`
   - Выбери `shared.framework`
   - Embed: **Do Not Embed** (фреймворк статический)

3. Build Settings → Search Paths → Framework Search Paths:
   ```
   $(SRCROOT)/../shared/build/bin/iosSimulatorArm64/debugFramework
   ```

### 1.4 Добавить Build Phase для автосборки фреймворка

1. Xcode → таргет `iosApp` → Build Phases
2. Нажми "+" → "New Run Script Phase"
3. Перетащи скрипт **выше** "Compile Sources"
4. Вставь:
   ```bash
   cd "$SRCROOT/.."
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```
5. Input Files (для кэширования):
   ```
   $(SRCROOT)/../shared/src/commonMain
   $(SRCROOT)/../shared/src/iosMain
   ```

### 1.5 Собрать и запустить
- Выбери симулятор (iPhone 15 или любой)
- Cmd+R → приложение должно запуститься и показать Portfolio экран

### Возможные ошибки:
- **"No such module 'shared'"** → проверь Framework Search Paths
- **"Symbol not found"** → убедись что Embed = Do Not Embed (static framework)
- **Koin crash** → `doInitKoin()` должен вызываться в `iOSApp.swift` до UI

---

## Шаг 2: Проверить Android приложение

1. Открой проект в Android Studio
2. Выбери конфигурацию `androidApp`
3. Запусти на эмуляторе или устройстве
4. Проверь:
   - [ ] Portfolio экран загружается
   - [ ] Нажатие "Update" обновляет портфель (нужны API ключи в Settings)
   - [ ] Навигация в Settings работает
   - [ ] Системная кнопка "Назад" возвращает из Settings в Portfolio
   - [ ] Settings сохраняются между запусками
   - [ ] Sell sheet открывается и закрывается

---

## Шаг 3: Проверить iOS приложение

После успешной сборки в Xcode, проверь то же самое:
- [ ] Portfolio экран загружается
- [ ] Навигация в Settings работает
- [ ] Settings сохраняются (Keychain)
- [ ] API вызовы работают (нужны ключи)

---

## Шаг 4: Merge в main

Когда всё проверено:
```bash
git checkout main
git merge feature/kmp-migration
git push origin main
```

---

## Контекст для Claude

При обращении к Claude по поводу этих шагов, скажи:
> "Я работаю над KMP миграцией CryptoPortfolio. Ветка `feature/kmp-migration` готова. Мне нужна помощь с [конкретный шаг]. Прочитай `docs/superpowers/plans/2026-03-16-kmp-manual-steps.md` для контекста."
