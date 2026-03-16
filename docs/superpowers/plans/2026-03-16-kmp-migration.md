# KMP Migration Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate CryptoPortfolio from Android-only to Kotlin Multiplatform + Compose Multiplatform, adding iOS support with full feature parity.

**Architecture:** Three-module structure — `:shared` (KMP with commonMain/androidMain/iosMain), `:androidApp` (thin Android shell), `:iosApp` (SwiftUI shell hosting Compose). Domain layer moves as-is, platform-specific code uses expect/actual for SecureStorage and DatabaseBuilder.

**Tech Stack:** Kotlin 2.1.21, Compose Multiplatform, Koin (DI), Ktor (HTTP), Room KMP (DB), kotlinx.serialization (JSON), Coil 3 (images)

**Spec:** `docs/superpowers/specs/2026-03-16-kmp-migration-design.md`

---

## File Structure

### New files to create:
```
shared/
  build.gradle.kts
  src/commonMain/kotlin/com/alexcemen/cryptoportfolio/
    platform/PlatformContext.kt           — expect class
    platform/SecureStorage.kt             — expect class
    platform/DatabaseBuilder.kt           — expect fun getDatabaseBuilder()
    platform/Logger.kt                    — expect object
    platform/currentTimeMillis.kt         — expect fun (replaces System.currentTimeMillis)
    di/CommonModule.kt                    — Koin module with all common bindings
    data/db/AppDatabase.kt                — Room KMP database (moved + adapted)
    data/db/PortfolioDao.kt               — Room DAO (moved as-is)
    data/db/entity/CoinEntity.kt          — Room entity (moved as-is)
    data/network/HttpClients.kt           — Ktor client factory functions
    data/network/CmcApiService.kt         — Ktor-based (rewritten from Retrofit interface)
    data/network/MexcApiService.kt        — Ktor-based (rewritten from Retrofit interface)
    data/network/MexcSigningPlugin.kt     — Ktor plugin (replaces OkHttp interceptor)
    data/network/OrderSide.kt             — moved as-is
    data/network/dto/CmcListingsResponse.kt  — @Serializable added
    data/network/dto/MexcAccountResponse.kt  — @Serializable added
    data/network/dto/MexcExchangeInfoResponse.kt — @Serializable added
    data/network/dto/MexcTickerPriceDto.kt   — @Serializable added
    data/repository/CmcRepositoryImpl.kt     — moved, remove @Inject
    data/repository/MexcRepositoryImpl.kt    — moved, Timber→Logger, HttpException→Ktor
    data/repository/PortfolioRepositoryImpl.kt — moved, remove @Inject
    data/repository/SettingsRepositoryImpl.kt  — moved, rewrite to use SecureStorage
    domain/model/CoinData.kt              — moved as-is
    domain/model/PortfolioData.kt         — moved as-is
    domain/model/SettingsData.kt          — moved as-is
    domain/model/AssetBalance.kt          — moved as-is
    domain/model/TradeSide.kt             — moved as-is
    domain/repository/PortfolioRepository.kt  — moved as-is
    domain/repository/SettingsRepository.kt   — moved as-is
    domain/repository/CmcRepository.kt        — moved as-is
    domain/repository/MexcRepository.kt       — moved as-is
    domain/usecase/CheckSettingsUseCase.kt    — moved, remove @Inject
    domain/usecase/GetPortfolioUseCase.kt     — moved, remove @Inject
    domain/usecase/GetSettingsUseCase.kt      — moved, remove @Inject
    domain/usecase/SaveSettingsUseCase.kt     — moved, remove @Inject
    domain/usecase/UpdatePortfolioUseCase.kt  — moved, remove @Inject
    domain/usecase/SellUseCase.kt             — moved, remove @Inject
    domain/usecase/RebalancerUseCase.kt       — moved, remove @Inject
    domain/usecase/RebalancerHelpers.kt       — moved as-is
    ui/mvi/MviStore.kt                    — moved as-is
    ui/mvi/ScreenModel.kt                 — new, replaces ScreenViewModel
    ui/mvi/MviExtensions.kt               — moved, adapt imports
    ui/navigation/Navigator.kt            — new, custom stack-based
    ui/screen/portfolio/PortfolioStore.kt — moved as-is
    ui/screen/portfolio/PortfolioReducer.kt — moved, remove @Inject
    ui/screen/portfolio/PortfolioScreenModel.kt — new, replaces PortfolioViewModel
    ui/screen/portfolio/composable/PortfolioScreen.kt — moved, koinInject + collectAsState
    ui/screen/portfolio/composable/PortfolioContent.kt — moved, CMP resources
    ui/screen/portfolio/composable/AppHeader.kt        — moved, CMP resources
    ui/screen/portfolio/composable/BalanceCard.kt      — moved, CMP resources
    ui/screen/portfolio/composable/ActionButtonsRow.kt — moved, CMP resources
    ui/screen/portfolio/composable/CoinListCard.kt     — moved as-is
    ui/screen/portfolio/composable/CoinAvatar.kt       — moved, Coil 3
    ui/screen/portfolio/composable/SellSheet.kt        — moved, CMP resources
    ui/screen/settings/SettingsStore.kt    — moved as-is
    ui/screen/settings/SettingsReducer.kt  — moved, remove @Inject
    ui/screen/settings/SettingsScreenModel.kt — new, replaces SettingsViewModel
    ui/screen/settings/composable/SettingsScreen.kt   — moved, koinInject + collectAsState
    ui/screen/settings/composable/SettingsContent.kt  — moved, CMP resources
    ui/screen/settings/composable/SettingRow.kt       — moved, CMP resources
    ui/screen/settings/composable/ExcludedCoinsSection.kt — moved, CMP resources
    ui/theme/AppTheme.kt                   — moved as-is
    ui/theme/AppThemeColors.kt             — moved as-is
    ui/theme/AppThemeTypography.kt         — moved as-is
    ui/App.kt                              — new, root composable with Navigator
  src/commonMain/composeResources/
    values/strings.xml                     — moved from Android res
    drawable/ic_launcher_foreground.xml    — moved from Android res
  src/androidMain/kotlin/com/alexcemen/cryptoportfolio/
    platform/PlatformContext.android.kt    — actual typealias to Context
    platform/SecureStorage.android.kt      — actual wrapping EncryptedSharedPreferences
    platform/DatabaseBuilder.android.kt    — actual Room.databaseBuilder(context)
    platform/Logger.android.kt            — actual using android.util.Log
    platform/currentTimeMillis.android.kt — actual using System.currentTimeMillis()
    di/PlatformModule.android.kt          — Koin module with Android-specific bindings
  src/iosMain/kotlin/com/alexcemen/cryptoportfolio/
    platform/PlatformContext.ios.kt        — actual empty class
    platform/SecureStorage.ios.kt          — actual wrapping iOS Keychain
    platform/DatabaseBuilder.ios.kt        — actual Room.databaseBuilder(dbFilePath)
    platform/Logger.ios.kt                — actual using NSLog
    platform/currentTimeMillis.ios.kt     — actual using NSDate
    di/PlatformModule.ios.kt              — Koin module with iOS-specific bindings
    MainViewController.kt                 — ComposeUIViewController entry point
androidApp/
  build.gradle.kts                        — thin Android application module
  src/main/kotlin/com/alexcemen/cryptoportfolio/
    CryptoApp.kt                          — Application + startKoin
    MainActivity.kt                       — setContent { App() }
  src/main/res/                           — launcher icons, themes.xml, AndroidManifest.xml
  src/main/AndroidManifest.xml
iosApp/
  iosApp.xcodeproj/                       — Xcode project (created by KMP wizard)
  iosApp/
    iOSApp.swift                          — SwiftUI @main entry
    ContentView.swift                     — hosts ComposeUIViewController
    Assets.xcassets/                      — app icon
    Info.plist
```

### Files to delete (after migration):
- Entire `app/` module (replaced by `shared/` + `androidApp/`)
- All Hilt DI modules: `di/DatabaseModule.kt`, `di/NetworkModule.kt`, `di/RepositoryModule.kt`, `di/PreferencesModule.kt`

### Root files to modify:
- `settings.gradle.kts` — add `:shared`, rename `:app` to `:androidApp`, add `:iosApp`
- `build.gradle.kts` — add KMP and Compose Multiplatform plugins
- `gradle/libs.versions.toml` — add Koin, Ktor, Coil 3, CMP versions; remove Hilt, Retrofit, OkHttp, Timber
- `gradle.properties` — add KMP flags

### Existing test files to move:
- `app/src/test/` unit tests → `shared/src/commonTest/`
- `app/src/androidTest/` → `shared/src/androidInstrumentedTest/` (Room DAO tests stay Android)

---

## Chunk 1: Project Structure & Gradle Setup

### Task 1: Update version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Update libs.versions.toml with KMP dependencies**

Replace the entire file. Add Compose Multiplatform, Koin, Ktor, Coil 3. Remove Hilt, Retrofit, OkHttp, Gson, Timber, Navigation3.

```toml
[versions]
agp = "8.10.1"
kotlin = "2.1.21"
ksp = "2.1.21-2.0.1"
composeMultiplatform = "1.8.0"
coreKtx = "1.16.0"
activityCompose = "1.10.1"
lifecycleRuntimeKtx = "2.9.1"
room = "2.7.1"
coroutines = "1.10.1"
kotlinxSerialization = "1.8.1"
securityCrypto = "1.0.0"
coil = "3.1.0"
koin = "4.0.4"
ktor = "3.1.1"
junit = "4.13.2"
androidxTestJunit = "1.2.1"
espresso = "3.6.1"

[libraries]
# Android-specific
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "securityCrypto" }

# Room KMP
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Koin
koin-core = { group = "io.insert-koin", name = "koin-core", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-compose = { group = "io.insert-koin", name = "koin-compose", version.ref = "koin" }

# Ktor
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { group = "io.ktor", name = "ktor-client-darwin", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { group = "io.ktor", name = "ktor-client-logging", version.ref = "ktor" }

# Coil 3 (multiplatform)
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
coil-network-ktor = { group = "io.coil-kt.coil3", name = "coil-network-ktor3", version.ref = "coil" }

# Coroutines
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-test-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxTestJunit" }
androidx-espresso = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
```

- [ ] **Step 2: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore: update version catalog for KMP migration

Add Koin, Ktor, Coil 3, Compose Multiplatform, Room KMP plugin.
Remove Hilt, Retrofit, OkHttp, Gson, Timber, Navigation3."
```

---

### Task 2: Update root build.gradle.kts

**Files:**
- Modify: `build.gradle.kts`

- [ ] **Step 1: Replace root build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}
```

- [ ] **Step 2: Commit**

```bash
git add build.gradle.kts
git commit -m "chore: update root build.gradle for KMP plugins"
```

---

### Task 3: Update settings.gradle.kts

**Files:**
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Replace settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Top100App"
include(":shared")
include(":androidApp")
include(":iosApp")
```

- [ ] **Step 2: Commit**

```bash
git add settings.gradle.kts
git commit -m "chore: update settings.gradle for multi-module KMP layout"
```

---

### Task 4: Update gradle.properties

**Files:**
- Modify: `gradle.properties`

- [ ] **Step 1: Add KMP flags to gradle.properties**

Append these lines:

```properties
# KMP
kotlin.mpp.androidSourceSetLayoutVersion=2
kotlin.mpp.enableCInteropCommonization=true
```

- [ ] **Step 2: Commit**

```bash
git add gradle.properties
git commit -m "chore: add KMP gradle properties"
```

---

### Task 5: Create shared module build.gradle.kts

**Files:**
- Create: `shared/build.gradle.kts`

- [ ] **Step 1: Create shared/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "11" }
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)

            // Room KMP
            implementation(libs.room.runtime)

            // Coil 3
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.security.crypto)
            implementation(libs.kotlinx.coroutines.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.alexcemen.cryptoportfolio.shared"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    listOf("kspAndroid", "kspIosX64", "kspIosArm64", "kspIosSimulatorArm64").forEach {
        add(it, libs.room.compiler)
    }
}
```

- [ ] **Step 2: Create directory structure**

```bash
mkdir -p shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio
mkdir -p shared/src/commonMain/composeResources/values
mkdir -p shared/src/commonMain/composeResources/drawable
mkdir -p shared/src/commonTest/kotlin/com/alexcemen/cryptoportfolio
mkdir -p shared/src/androidMain/kotlin/com/alexcemen/cryptoportfolio
mkdir -p shared/src/androidMain/AndroidManifest.xml
mkdir -p shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio
```

- [ ] **Step 3: Commit**

```bash
git add shared/
git commit -m "feat: create shared KMP module with Gradle config"
```

---

### Task 6: Create androidApp module

**Files:**
- Create: `androidApp/build.gradle.kts`
- Move: Android resources and manifest from `app/` to `androidApp/`

- [ ] **Step 1: Create androidApp/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    org.jetbrains.kotlin.android
}

android {
    namespace = "com.alexcemen.cryptoportfolio"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.alexcemen.cryptoportfolio"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true; buildConfig = true }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.koin.android)
}
```

- [ ] **Step 2: Copy Android resources**

```bash
mkdir -p androidApp/src/main
cp -r app/src/main/res androidApp/src/main/
cp app/src/main/AndroidManifest.xml androidApp/src/main/
mkdir -p androidApp/src/main/kotlin/com/alexcemen/cryptoportfolio
```

Note: Remove `<string>` entries from `androidApp/src/main/res/values/strings.xml` that will be in Compose Multiplatform resources. Keep only `app_name` for the Android manifest label.

- [ ] **Step 3: Commit**

```bash
git add androidApp/
git commit -m "feat: create androidApp thin shell module"
```

---

### Task 7: Create iosApp module

**Files:**
- Create: `iosApp/build.gradle.kts` (empty, just for Gradle inclusion)
- Create: `iosApp/iosApp/iOSApp.swift`
- Create: `iosApp/iosApp/ContentView.swift`
- Create: `iosApp/iosApp/Info.plist`

- [ ] **Step 1: Create iosApp Gradle stub**

```kotlin
// iosApp/build.gradle.kts — empty, iOS build is handled by Xcode
// This file exists so Gradle doesn't fail on include(":iosApp")
```

Actually, the iosApp doesn't need a build.gradle.kts if we use Xcode directly. Remove `:iosApp` from `settings.gradle.kts` to avoid Gradle errors. Update settings:

```kotlin
rootProject.name = "Top100App"
include(":shared")
include(":androidApp")
```

- [ ] **Step 2: Create Swift files**

Create `iosApp/iosApp/iOSApp.swift`:
```swift
import SwiftUI

@main
struct iOSApp: App {
    init() {
        KoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

Create `iosApp/iosApp/ContentView.swift`:
```swift
import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
```

- [ ] **Step 3: Create Info.plist**

Standard iOS Info.plist with bundle identifier `com.alexcemen.cryptoportfolio`.

- [ ] **Step 4: Commit**

```bash
git add iosApp/ settings.gradle.kts
git commit -m "feat: create iosApp SwiftUI shell"
```

---

## Chunk 2: Platform Abstractions & Domain Layer

### Task 8: Create platform expect declarations

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/platform/PlatformContext.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/platform/SecureStorage.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/platform/DatabaseBuilder.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/platform/Logger.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/platform/currentTimeMillis.kt`

- [ ] **Step 1: Create PlatformContext.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

expect class PlatformContext
```

- [ ] **Step 2: Create SecureStorage.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

expect class SecureStorage {
    fun getString(key: String, default: String): String
    fun getInt(key: String, default: Int): Int
    fun putString(key: String, value: String)
    fun putInt(key: String, value: Int)
}
```

- [ ] **Step 3: Create DatabaseBuilder.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase

expect fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase>
```

- [ ] **Step 4: Create Logger.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

expect object Logger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun i(tag: String, message: String)
}
```

- [ ] **Step 5: Create currentTimeMillis.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

expect fun currentTimeMillis(): Long
```

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/platform/
git commit -m "feat: add expect declarations for platform abstractions"
```

---

### Task 9: Create Android actual implementations

**Files:**
- Create: `shared/src/androidMain/kotlin/com/alexcemen/cryptoportfolio/platform/PlatformContext.android.kt`
- Create: `shared/src/androidMain/kotlin/com/alexcemen/cryptoportfolio/platform/SecureStorage.android.kt`
- Create: `shared/src/androidMain/kotlin/com/alexcemen/cryptoportfolio/platform/DatabaseBuilder.android.kt`
- Create: `shared/src/androidMain/kotlin/com/alexcemen/cryptoportfolio/platform/Logger.android.kt`
- Create: `shared/src/androidMain/kotlin/com/alexcemen/cryptoportfolio/platform/currentTimeMillis.android.kt`

- [ ] **Step 1: PlatformContext.android.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

import android.content.Context

actual typealias PlatformContext = Context
```

- [ ] **Step 2: SecureStorage.android.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

actual class SecureStorage(context: Context) {
    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "crypto_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    actual fun getString(key: String, default: String): String =
        prefs.getString(key, default) ?: default

    actual fun getInt(key: String, default: Int): Int =
        prefs.getInt(key, default)

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}
```

- [ ] **Step 3: DatabaseBuilder.android.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase

actual fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder(context, AppDatabase::class.java, "crypto_portfolio.db")
```

- [ ] **Step 4: Logger.android.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

import android.util.Log

actual object Logger {
    actual fun d(tag: String, message: String) { Log.d(tag, message) }
    actual fun e(tag: String, message: String, throwable: Throwable?) { Log.e(tag, message, throwable) }
    actual fun i(tag: String, message: String) { Log.i(tag, message) }
}
```

- [ ] **Step 5: currentTimeMillis.android.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
```

- [ ] **Step 6: Commit**

```bash
git add shared/src/androidMain/
git commit -m "feat: add Android actual implementations for platform abstractions"
```

---

### Task 10: Create iOS actual implementations

**Files:**
- Create: `shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/platform/PlatformContext.ios.kt`
- Create: `shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/platform/SecureStorage.ios.kt`
- Create: `shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/platform/DatabaseBuilder.ios.kt`
- Create: `shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/platform/Logger.ios.kt`
- Create: `shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/platform/currentTimeMillis.ios.kt`

- [ ] **Step 1: PlatformContext.ios.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

actual class PlatformContext
```

- [ ] **Step 2: SecureStorage.ios.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.*

actual class SecureStorage {
    private val serviceName = "com.alexcemen.cryptoportfolio"

    actual fun getString(key: String, default: String): String {
        return keychainGet(key) ?: default
    }

    actual fun getInt(key: String, default: Int): Int {
        return keychainGet(key)?.toIntOrNull() ?: default
    }

    actual fun putString(key: String, value: String) {
        keychainSet(key, value)
    }

    actual fun putInt(key: String, value: Int) {
        keychainSet(key, value.toString())
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keychainSet(key: String, value: String) {
        keychainDelete(key)
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            kSecValueData to data,
        )
        SecItemAdd(query as CFDictionaryRef, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keychainGet(key: String): String? {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            kSecReturnData to true,
            kSecMatchLimit to kSecMatchLimitOne,
        )
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)
            if (status != errSecSuccess) return null
            val data = CFBridgingRelease(result.value) as? NSData ?: return null
            return NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keychainDelete(key: String) {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
        )
        SecItemDelete(query as CFDictionaryRef)
    }
}
```

Note: The iOS Keychain implementation above is a starting point. It may need minor adjustments during compilation — the cinterop types for Security framework can be finicky. The core logic is correct.

- [ ] **Step 3: DatabaseBuilder.ios.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/crypto_portfolio.db"
    return Room.databaseBuilder<AppDatabase>(name = dbFilePath)
}

private fun documentDirectory(): String {
    val paths = NSFileManager.defaultManager.URLsForDirectory(
        NSDocumentDirectory, NSUserDomainMask
    )
    return paths.first().path!!
}
```

- [ ] **Step 4: Logger.ios.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

import platform.Foundation.NSLog

actual object Logger {
    actual fun d(tag: String, message: String) { NSLog("D/$tag: $message") }
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        NSLog("E/$tag: $message ${throwable?.message ?: ""}")
    }
    actual fun i(tag: String, message: String) { NSLog("I/$tag: $message") }
}
```

- [ ] **Step 5: currentTimeMillis.ios.kt**

```kotlin
package com.alexcemen.cryptoportfolio.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()
```

- [ ] **Step 6: Commit**

```bash
git add shared/src/iosMain/
git commit -m "feat: add iOS actual implementations (Keychain, Room, Logger)"
```

---

### Task 11: Move domain layer to commonMain

**Files:**
- Create: all files under `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/domain/`
- Source: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/domain/`

- [ ] **Step 1: Copy domain models (as-is, pure Kotlin)**

Copy these files unchanged:
- `domain/model/CoinData.kt`
- `domain/model/PortfolioData.kt`
- `domain/model/SettingsData.kt`
- `domain/model/AssetBalance.kt`
- `domain/model/TradeSide.kt`

- [ ] **Step 2: Copy domain repository interfaces (as-is, pure Kotlin)**

Copy these files unchanged:
- `domain/repository/PortfolioRepository.kt`
- `domain/repository/SettingsRepository.kt`
- `domain/repository/CmcRepository.kt`
- `domain/repository/MexcRepository.kt`

- [ ] **Step 3: Copy use cases — remove `@Inject` and `javax.inject.Inject` import**

For each use case file, remove `import javax.inject.Inject` and change `@Inject constructor(` to just `constructor(`:

- `domain/usecase/CheckSettingsUseCase.kt`
- `domain/usecase/GetPortfolioUseCase.kt`
- `domain/usecase/GetSettingsUseCase.kt`
- `domain/usecase/SaveSettingsUseCase.kt`
- `domain/usecase/UpdatePortfolioUseCase.kt`
- `domain/usecase/SellUseCase.kt`
- `domain/usecase/RebalancerUseCase.kt`
- `domain/usecase/RebalancerHelpers.kt` (unchanged, pure Kotlin)

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/domain/
git commit -m "feat: move domain layer to commonMain (models, repos, use cases)"
```

---

## Chunk 3: Data Layer Migration

### Task 12: Move Room database to commonMain

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/db/AppDatabase.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/db/PortfolioDao.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/db/entity/CoinEntity.kt`

- [ ] **Step 1: Move CoinEntity.kt — unchanged**

Same content as original `app/src/main/kotlin/.../data/db/entity/CoinEntity.kt`. Room annotations work in KMP.

- [ ] **Step 2: Move PortfolioDao.kt — unchanged**

Same content as original. Room DAO works in KMP.

- [ ] **Step 3: Move AppDatabase.kt — add RoomDatabase.Builder constructor**

For Room KMP, the database class needs a slight change:

```kotlin
package com.alexcemen.cryptoportfolio.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity

private const val DATABASE_VERSION = 2

@Database(
    entities = [CoinEntity::class],
    version = DATABASE_VERSION
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
}
```

Note: Room KMP requires the `@ConstructedBy` annotation and a `RoomDatabaseConstructor` for cross-platform instantiation. Add:

```kotlin
package com.alexcemen.cryptoportfolio.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity

private const val DATABASE_VERSION = 2

@Database(
    entities = [CoinEntity::class],
    version = DATABASE_VERSION
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
```

The KSP Room compiler generates the `actual` implementation automatically.

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/db/
git commit -m "feat: move Room database to commonMain (KMP compatible)"
```

---

### Task 13: Create Ktor network layer

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/network/HttpClients.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/network/CmcApiService.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/network/MexcApiService.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/network/MexcSigningPlugin.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/network/OrderSide.kt`
- Create: DTOs with `@Serializable`

- [ ] **Step 1: Move OrderSide.kt — unchanged**

```kotlin
package com.alexcemen.cryptoportfolio.data.network

const val ORDER_TYPE_MARKET = "MARKET"
const val QUOTE_ASSET = "USDT"

enum class OrderSide {
    BUY,
    SELL,
}
```

- [ ] **Step 2: Create DTOs with @Serializable**

`data/network/dto/CmcListingsResponse.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CmcListingsResponse(val data: List<CmcCoinDto>)

@Serializable
data class CmcCoinDto(val id: Int, val symbol: String)
```

`data/network/dto/MexcAccountResponse.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MexcAccountResponse(val balances: List<MexcBalanceDto>)

@Serializable
data class MexcBalanceDto(val asset: String, val free: String, val locked: String)
```

`data/network/dto/MexcExchangeInfoResponse.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MexcExchangeInfoResponse(val symbols: List<MexcSymbolDto>)

@Serializable
data class MexcSymbolDto(
    val baseAsset: String,
    val quoteAsset: String,
    val status: String,
    val baseAssetPrecision: Int = 8,
)
```

`data/network/dto/MexcTickerPriceDto.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MexcTickerPriceDto(val symbol: String, val price: String)
```

- [ ] **Step 3: Create HMAC signing function**

`data/network/MexcSigning.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun signMexcQuery(query: String, secret: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
    return mac.doFinal(query.toByteArray()).joinToString("") { "%02x".format(it) }
}
```

IMPORTANT: `javax.crypto.Mac` is JVM-only. For KMP, this needs to be an expect/actual. Create:

`shared/src/commonMain/.../data/network/MexcSigning.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network

expect fun signMexcQuery(query: String, secret: String): String
```

`shared/src/androidMain/.../data/network/MexcSigning.android.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

actual fun signMexcQuery(query: String, secret: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
    return mac.doFinal(query.toByteArray()).joinToString("") { "%02x".format(it) }
}
```

`shared/src/iosMain/.../data/network/MexcSigning.ios.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA256

@OptIn(ExperimentalForeignApi::class)
actual fun signMexcQuery(query: String, secret: String): String {
    val keyBytes = secret.encodeToByteArray()
    val dataBytes = query.encodeToByteArray()
    val digestLen = CC_SHA256_DIGEST_LENGTH

    return memScoped {
        val result = allocArray<platform.posix.uint8_tVar>(digestLen)
        keyBytes.usePinned { keyPinned ->
            dataBytes.usePinned { dataPinned ->
                CCHmac(
                    kCCHmacAlgSHA256,
                    keyPinned.addressOf(0),
                    keyBytes.size.convert(),
                    dataPinned.addressOf(0),
                    dataBytes.size.convert(),
                    result,
                )
            }
        }
        (0 until digestLen).joinToString("") {
            result[it].toInt().and(0xFF).toString(16).padStart(2, '0')
        }
    }
}
```

- [ ] **Step 4: Create HttpClients.kt**

```kotlin
package com.alexcemen.cryptoportfolio.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createCmcHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) { level = LogLevel.BODY }
    defaultRequest { url("https://pro-api.coinmarketcap.com/") }
}

fun createMexcHttpClient(settingsProvider: suspend () -> Pair<String, String>): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) { level = LogLevel.BODY }
    defaultRequest { url("https://api.mexc.com/") }
}
```

Note: The MEXC signing is done inline in `MexcApiService` calls (not as an interceptor), since Ktor plugins for per-request signing are more complex than needed here. The API key header is added directly in each request.

- [ ] **Step 5: Create CmcApiService.kt (Ktor)**

```kotlin
package com.alexcemen.cryptoportfolio.data.network

import com.alexcemen.cryptoportfolio.data.network.dto.CmcListingsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class CmcApiService(private val client: HttpClient) {
    suspend fun getListings(apiKey: String, limit: Int, sort: String = "market_cap"): CmcListingsResponse {
        return client.get("v1/cryptocurrency/listings/latest") {
            header("X-CMC_PRO_API_KEY", apiKey)
            parameter("limit", limit)
            parameter("sort", sort)
        }.body()
    }
}
```

- [ ] **Step 6: Create MexcApiService.kt (Ktor)**

```kotlin
package com.alexcemen.cryptoportfolio.data.network

import com.alexcemen.cryptoportfolio.data.network.dto.MexcAccountResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcExchangeInfoResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcTickerPriceDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.serialization.json.JsonObject

class MexcApiService(
    private val client: HttpClient,
    private val apiKeyProvider: suspend () -> String,
) {
    suspend fun getAccount(timestamp: Long, signature: String): MexcAccountResponse {
        return client.get("api/v3/account") {
            header("X-MEXC-APIKEY", apiKeyProvider())
            parameter("timestamp", timestamp)
            parameter("signature", signature)
        }.body()
    }

    suspend fun getExchangeInfo(): MexcExchangeInfoResponse {
        return client.get("api/v3/exchangeInfo").body()
    }

    suspend fun getAllPrices(): List<MexcTickerPriceDto> {
        return client.get("api/v3/ticker/price").body()
    }

    suspend fun placeOrder(
        symbol: String, side: OrderSide, type: String,
        quoteOrderQty: String, timestamp: Long, signature: String,
    ): JsonObject {
        return client.post("api/v3/order") {
            header("X-MEXC-APIKEY", apiKeyProvider())
            parameter("symbol", symbol)
            parameter("side", side.name)
            parameter("type", type)
            parameter("quoteOrderQty", quoteOrderQty)
            parameter("timestamp", timestamp)
            parameter("signature", signature)
        }.body()
    }

    suspend fun placeOrderByQty(
        symbol: String, side: OrderSide, type: String,
        quantity: String, timestamp: Long, signature: String,
    ): JsonObject {
        return client.post("api/v3/order") {
            header("X-MEXC-APIKEY", apiKeyProvider())
            parameter("symbol", symbol)
            parameter("side", side.name)
            parameter("type", type)
            parameter("quantity", quantity)
            parameter("timestamp", timestamp)
            parameter("signature", signature)
        }.body()
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/network/
git add shared/src/androidMain/kotlin/com/alexcemen/cryptoportfolio/data/network/
git add shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/data/network/
git commit -m "feat: create Ktor network layer (replaces Retrofit/OkHttp)"
```

---

### Task 14: Move repository implementations to commonMain

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/repository/`

- [ ] **Step 1: Create SettingsRepositoryImpl.kt (rewritten for SecureStorage)**

```kotlin
package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import com.alexcemen.cryptoportfolio.platform.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

private const val KEY_CMC = "cmc_api_key"
private const val KEY_MEXC_KEY = "mexc_api_key"
private const val KEY_MEXC_SECRET = "mexc_api_secret"
private const val KEY_TOP_LIMIT = "top_coins_limit"
private const val KEY_EXCLUDED = "excluded_coins"

class SettingsRepositoryImpl(
    private val secureStorage: SecureStorage,
) : SettingsRepository {

    override suspend fun getSettings(): SettingsData = withContext(Dispatchers.IO) {
        SettingsData(
            cmcApiKey = secureStorage.getString(KEY_CMC, ""),
            mexcApiKey = secureStorage.getString(KEY_MEXC_KEY, ""),
            mexcApiSecret = secureStorage.getString(KEY_MEXC_SECRET, ""),
            topCoinsLimit = secureStorage.getInt(KEY_TOP_LIMIT, 100),
            excludedCoins = secureStorage.getString(KEY_EXCLUDED, "FDUSD,USD1,PYUSD,USDC,DAI,USDe")
                .split(",").filter { it.isNotBlank() },
        )
    }

    override suspend fun saveSettings(settings: SettingsData) = withContext(Dispatchers.IO) {
        secureStorage.putString(KEY_CMC, settings.cmcApiKey)
        secureStorage.putString(KEY_MEXC_KEY, settings.mexcApiKey)
        secureStorage.putString(KEY_MEXC_SECRET, settings.mexcApiSecret)
        secureStorage.putInt(KEY_TOP_LIMIT, settings.topCoinsLimit)
        secureStorage.putString(KEY_EXCLUDED, settings.excludedCoins.joinToString(","))
    }
}
```

- [ ] **Step 2: Create PortfolioRepositoryImpl.kt (remove @Inject)**

```kotlin
package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.data.db.PortfolioDao
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity
import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PortfolioRepositoryImpl(
    private val dao: PortfolioDao,
) : PortfolioRepository {

    override fun getPortfolio(): Flow<PortfolioData> =
        dao.getAll().map { entities ->
            val coins = entities.map { it.toDomain() }
            PortfolioData(coins = coins, totalUsdt = coins.sumOf { it.totalPositionUsdt })
        }

    override suspend fun savePortfolio(coins: List<CoinData>) {
        dao.replaceAll(coins.map { it.toEntity() })
    }

    private fun CoinEntity.toDomain() = CoinData(
        symbol = symbol,
        priceUsdt = priceUsdt,
        quantity = quantity,
        logoUrl = cmcId?.let { "https://s2.coinmarketcap.com/static/img/coins/64x64/$it.png" },
    )

    private fun CoinData.toEntity() = CoinEntity(
        symbol = symbol,
        priceUsdt = priceUsdt,
        quantity = quantity,
        cmcId = logoUrl
            ?.removePrefix("https://s2.coinmarketcap.com/static/img/coins/64x64/")
            ?.removeSuffix(".png")
            ?.toIntOrNull(),
    )
}
```

- [ ] **Step 3: Create CmcRepositoryImpl.kt (remove @Inject)**

```kotlin
package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository

class CmcRepositoryImpl(
    private val cmcService: CmcApiService,
) : CmcRepository {
    override suspend fun getTopCoins(apiKey: String, limit: Int): List<String> =
        cmcService.getListings(apiKey, limit).data.map { it.symbol }

    override suspend fun getCoinIds(apiKey: String, limit: Int): Map<String, Int> =
        cmcService.getListings(apiKey, limit).data.associate { it.symbol to it.id }
}
```

- [ ] **Step 4: Create MexcRepositoryImpl.kt (replace Timber→Logger, HttpException→Ktor, System.currentTimeMillis→platform)**

```kotlin
package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.data.network.ORDER_TYPE_MARKET
import com.alexcemen.cryptoportfolio.data.network.QUOTE_ASSET
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.OrderSide
import com.alexcemen.cryptoportfolio.data.network.signMexcQuery
import com.alexcemen.cryptoportfolio.domain.model.AssetBalance
import com.alexcemen.cryptoportfolio.domain.model.TradeSide
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import com.alexcemen.cryptoportfolio.platform.Logger
import com.alexcemen.cryptoportfolio.platform.currentTimeMillis

class MexcRepositoryImpl(
    private val mexcService: MexcApiService,
    private val settingsRepository: SettingsRepository,
) : MexcRepository {

    override suspend fun getBalances(): List<AssetBalance> {
        val prices = mexcService.getAllPrices()
            .associate { it.symbol to (it.price.toDoubleOrNull() ?: 0.0) }
        val secret = settingsRepository.getSettings().mexcApiSecret
        val timestamp = currentTimeMillis()
        val account = mexcService.getAccount(timestamp, signMexcQuery("timestamp=$timestamp", secret))
        return account.balances.mapNotNull { balance ->
            val quantity = balance.free.toDoubleOrNull() ?: 0.0
            val price = if (balance.asset == QUOTE_ASSET) 1.0
            else prices["${balance.asset}$QUOTE_ASSET"] ?: return@mapNotNull null
            AssetBalance(symbol = balance.asset, quantity = quantity, priceUsdt = price)
        }
    }

    override suspend fun getTradableSymbols(): Set<String> =
        mexcService.getExchangeInfo().symbols
            .filter { it.quoteAsset == QUOTE_ASSET }
            .map { it.baseAsset }
            .toSet()

    override suspend fun getAssetPrecisions(): Map<String, Int> =
        mexcService.getExchangeInfo().symbols
            .filter { it.quoteAsset == QUOTE_ASSET }
            .associate { it.baseAsset to it.baseAssetPrecision }

    override suspend fun placeMarketOrderByUsdt(symbol: String, side: TradeSide, usdtAmount: Double) {
        runCatching {
            val secret = settingsRepository.getSettings().mexcApiSecret
            val timestamp = currentTimeMillis()
            val quoteQty = usdtAmount.toString()
            val mexcSide = if (side == TradeSide.BUY) OrderSide.BUY else OrderSide.SELL
            val signature = signMexcQuery(
                query = "symbol=${symbol}$QUOTE_ASSET&side=${mexcSide.name}&type=$ORDER_TYPE_MARKET&quoteOrderQty=$quoteQty&timestamp=$timestamp",
                secret = secret,
            )
            Logger.d("MexcRepo", "placeMarketOrderByUsdt: ${symbol}$QUOTE_ASSET side=$side quoteOrderQty=$quoteQty")
            mexcService.placeOrder(
                symbol = "${symbol}$QUOTE_ASSET", side = mexcSide, type = ORDER_TYPE_MARKET,
                quoteOrderQty = quoteQty, timestamp = timestamp, signature = signature,
            )
        }.onFailure {
            Logger.e("MexcRepo", "placeMarketOrderByUsdt failed: ${symbol}$QUOTE_ASSET side=$side error=${it.message}")
        }
    }

    override suspend fun placeMarketSellByQty(symbol: String, qty: String) {
        runCatching {
            val secret = settingsRepository.getSettings().mexcApiSecret
            val timestamp = currentTimeMillis()
            val signature = signMexcQuery(
                query = "symbol=${symbol}$QUOTE_ASSET&side=SELL&type=$ORDER_TYPE_MARKET&quantity=$qty&timestamp=$timestamp",
                secret = secret,
            )
            Logger.d("MexcRepo", "placeMarketSellByQty: ${symbol}$QUOTE_ASSET quantity=$qty")
            mexcService.placeOrderByQty(
                symbol = "${symbol}$QUOTE_ASSET", side = OrderSide.SELL, type = ORDER_TYPE_MARKET,
                quantity = qty, timestamp = timestamp, signature = signature,
            )
        }.onFailure {
            Logger.e("MexcRepo", "placeMarketSellByQty failed: ${symbol}$QUOTE_ASSET error=${it.message}")
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/data/repository/
git commit -m "feat: move repository implementations to commonMain"
```

---

## Chunk 4: MVI Framework, Navigation & DI

### Task 15: Move MVI framework to commonMain

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/mvi/MviStore.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/mvi/ScreenModel.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/mvi/MviExtensions.kt`

- [ ] **Step 1: Move MviStore.kt — unchanged**

```kotlin
package com.alexcemen.cryptoportfolio.ui.mvi

interface MviState
interface MviEvent
interface MviEffect
interface MviSideEffect
interface MviUiState

interface Reducer<S : MviState, UiState : MviUiState> {
    fun reduce(state: S): UiState
}
```

- [ ] **Step 2: Create ScreenModel.kt — replaces ScreenViewModel**

```kotlin
package com.alexcemen.cryptoportfolio.ui.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.alexcemen.cryptoportfolio.platform.Logger

abstract class ScreenModel<
    S : MviState,
    I : MviEvent,
    E : MviSideEffect,
    EF : MviEffect,
    UiState : MviUiState,
>(
    private val reducer: Reducer<S, UiState>,
) {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val tag = this::class.simpleName ?: "ScreenModel"

    abstract fun createState(): S

    private val _state = MutableStateFlow(createState())
    protected val state: StateFlow<S> = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<E>()
    val sideEffects: SharedFlow<E> = _sideEffects.asSharedFlow()

    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(reducer.reduce(_state.value))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val effects = MutableSharedFlow<EF>()

    init {
        scope.launch {
            state.collect { emitToUi(it) }
        }
        scope.launch {
            effects.collect { collectEffect(_state.value, it) }
        }
    }

    abstract fun handleEvent(currentState: S, intent: I): Flow<EF>
    abstract fun handleEffect(currentState: S, effect: EF): S

    fun onEvent(intent: I) {
        Logger.i(tag, "onEvent:: $intent")
        scope.launch {
            handleEvent(currentState = _state.value, intent).collect {
                effects.emit(it)
            }
        }
    }

    fun forceEffect(effect: EF) {
        Logger.i(tag, "forceEffect:: $effect")
        scope.launch {
            effects.emit(effect)
        }
    }

    protected suspend fun sendSideEffect(sideEffect: E) {
        Logger.i(tag, "sendSideEffect:: $sideEffect")
        _sideEffects.emit(sideEffect)
    }

    private suspend fun emitToUi(state: S) {
        Logger.i(tag, "emitToUi:: $state")
        _uiState.emit(reducer.reduce(state))
    }

    private suspend fun collectEffect(state: S, effect: EF) {
        _state.emit(handleEffect(state, effect))
    }

    fun onCleared() {
        scope.cancel()
    }
}
```

- [ ] **Step 3: Create MviExtensions.kt — adapt for ScreenModel**

```kotlin
package com.alexcemen.cryptoportfolio.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
inline fun <E : MviSideEffect> ScreenModel<*, *, E, *, *>.sideEffect(
    crossinline body: (effect: E) -> Unit,
) {
    LaunchedEffect(Unit) {
        this@sideEffect.sideEffects.collect { effect ->
            body(effect)
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/mvi/
git commit -m "feat: create ScreenModel MVI framework for KMP (replaces ScreenViewModel)"
```

---

### Task 16: Create navigation

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/navigation/Navigator.kt`

- [ ] **Step 1: Create Navigator.kt**

```kotlin
package com.alexcemen.cryptoportfolio.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf

sealed interface Screen {
    data object Portfolio : Screen
    data object Settings : Screen
}

class Navigator {
    private val _stack = mutableStateListOf<Screen>(Screen.Portfolio)
    val stack: List<Screen> = _stack

    val current: Screen get() = _stack.last()

    fun navigate(screen: Screen) {
        _stack.add(screen)
    }

    fun back(): Boolean {
        if (_stack.size <= 1) return false
        _stack.removeLast()
        return true
    }
}

val LocalNavigator = compositionLocalOf<Navigator> { error("No navigator provided") }
```

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/navigation/
git commit -m "feat: create custom stack-based Navigator for KMP"
```

---

### Task 17: Create Koin DI modules

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/di/CommonModule.kt`
- Create: `shared/src/androidMain/kotlin/com/alexcemen/cryptoportfolio/di/PlatformModule.android.kt`
- Create: `shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/di/PlatformModule.ios.kt`
- Create: `shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/di/KoinHelper.kt`

- [ ] **Step 1: Create CommonModule.kt**

```kotlin
package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.createCmcHttpClient
import com.alexcemen.cryptoportfolio.data.network.createMexcHttpClient
import com.alexcemen.cryptoportfolio.data.repository.CmcRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.MexcRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.PortfolioRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.SettingsRepositoryImpl
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import com.alexcemen.cryptoportfolio.domain.usecase.CheckSettingsUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.GetPortfolioUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.GetSettingsUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.RebalancerUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.SaveSettingsUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.SellUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.UpdatePortfolioUseCase
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioReducer
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioScreenModel
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsReducer
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    // Network
    single { createCmcHttpClient() }
    single { createMexcHttpClient { get<SettingsRepository>().getSettings().let { it.mexcApiKey to it.mexcApiSecret } } }
    single { CmcApiService(get()) }
    single {
        MexcApiService(
            client = get(),
            apiKeyProvider = { get<SettingsRepository>().getSettings().mexcApiKey },
        )
    }

    // Database
    single { get<com.alexcemen.cryptoportfolio.data.db.AppDatabase>().portfolioDao() }

    // Repositories
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    singleOf(::PortfolioRepositoryImpl) bind PortfolioRepository::class
    singleOf(::CmcRepositoryImpl) bind CmcRepository::class
    singleOf(::MexcRepositoryImpl) bind MexcRepository::class

    // Use cases
    factoryOf(::CheckSettingsUseCase)
    factoryOf(::GetPortfolioUseCase)
    factoryOf(::GetSettingsUseCase)
    factoryOf(::SaveSettingsUseCase)
    factoryOf(::UpdatePortfolioUseCase)
    factoryOf(::SellUseCase)
    factoryOf(::RebalancerUseCase)

    // Reducers
    factoryOf(::PortfolioReducer)
    factoryOf(::SettingsReducer)

    // ScreenModels
    factoryOf(::PortfolioScreenModel)
    factoryOf(::SettingsScreenModel)
}
```

- [ ] **Step 2: Create PlatformModule.android.kt**

```kotlin
package com.alexcemen.cryptoportfolio.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import com.alexcemen.cryptoportfolio.platform.SecureStorage
import com.alexcemen.cryptoportfolio.platform.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE portfolio_table ADD COLUMN cmcId INTEGER")
    }
}

val androidModule = module {
    single { SecureStorage(androidContext()) }
    single {
        getDatabaseBuilder(androidContext())
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}
```

- [ ] **Step 3: Create PlatformModule.ios.kt**

```kotlin
package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import com.alexcemen.cryptoportfolio.platform.PlatformContext
import com.alexcemen.cryptoportfolio.platform.SecureStorage
import com.alexcemen.cryptoportfolio.platform.getDatabaseBuilder
import org.koin.dsl.module

val iosModule = module {
    single { SecureStorage() }
    single {
        getDatabaseBuilder(PlatformContext())
            // Note: Room KMP migration support for iOS may require
            // auto-migration or manual SQL. Add migration if needed.
            .build()
    }
}
```

- [ ] **Step 4: Create KoinHelper.kt (called from Swift)**

```kotlin
package com.alexcemen.cryptoportfolio.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(commonModule, iosModule)
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/di/
git add shared/src/androidMain/kotlin/com/alexcemen/cryptoportfolio/di/
git add shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/di/
git commit -m "feat: create Koin DI modules (replaces Hilt)"
```

---

## Chunk 5: UI Layer Migration

### Task 18: Move theme to commonMain

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/theme/`

- [ ] **Step 1: Move all 3 theme files — unchanged**

Copy as-is:
- `ui/theme/AppTheme.kt` — uses `isSystemInDarkTheme()` which exists in Compose Multiplatform
- `ui/theme/AppThemeColors.kt` — pure Compose Color definitions
- `ui/theme/AppThemeTypography.kt` — pure Compose TextStyle definitions

All imports (`androidx.compose.runtime.*`, `androidx.compose.ui.graphics.Color`, `androidx.compose.ui.text.*`) are available in Compose Multiplatform.

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/theme/
git commit -m "feat: move theme to commonMain"
```

---

### Task 19: Move string resources to Compose Multiplatform resources

**Files:**
- Create: `shared/src/commonMain/composeResources/values/strings.xml`

- [ ] **Step 1: Create strings.xml in composeResources**

```xml
<resources>
    <string name="app_name">AltCoins</string>

    <!-- Portfolio -->
    <string name="portfolio_title">Portfolio</string>
    <string name="portfolio_empty">No portfolio data. Tap Update to fetch.</string>
    <string name="action_update">Update</string>
    <string name="action_rebalance">Rebalance</string>
    <string name="action_sell">Sell</string>
    <string name="error_update_failed">Update failed</string>
    <string name="error_rebalance_failed">Rebalance failed</string>
    <string name="error_sell_failed">Sell failed</string>
    <string name="sell_usdt_amount_label">USDT amount</string>
    <string name="cd_settings">Settings</string>

    <!-- Settings -->
    <string name="settings_title">Settings</string>
    <string name="cd_back">Back</string>
    <string name="label_cmc_api_key">CMC API Key</string>
    <string name="label_mexc_api_key">MEXC API Key</string>
    <string name="label_mexc_api_secret">MEXC API Secret</string>
    <string name="label_top_coins_limit">Top Coins Limit</string>
    <string name="value_not_set">(not set)</string>
    <string name="cd_save">Save</string>
    <string name="cd_cancel">Cancel</string>
    <string name="cd_edit">Edit %1$s</string>

    <!-- Excluded Coins -->
    <string name="excluded_coins_title">Excluded Coins</string>
    <string name="excluded_coins_add_placeholder">Add ticker…</string>
    <string name="cd_add_coin">Add coin</string>
    <string name="cd_edit_excluded_coins">Edit excluded coins</string>
    <string name="cd_remove_coin">Remove %1$s</string>
</resources>
```

- [ ] **Step 2: Copy ic_launcher_foreground.xml to composeResources/drawable/**

Copy `app/src/main/res/drawable/ic_launcher_foreground.xml` to `shared/src/commonMain/composeResources/drawable/ic_launcher_foreground.xml`.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/composeResources/
git commit -m "feat: move string resources and drawable to Compose Multiplatform resources"
```

---

### Task 20: Move Stores and Reducers to commonMain

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/portfolio/PortfolioStore.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/portfolio/PortfolioReducer.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/settings/SettingsStore.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/settings/SettingsReducer.kt`

- [ ] **Step 1: Move PortfolioStore.kt — unchanged** (already pure Kotlin)

- [ ] **Step 2: Move PortfolioReducer.kt — remove @Inject, keep Locale.US**

Note: `java.text.NumberFormat` and `java.util.Locale` are JVM-only. For KMP, use a simple format function instead:

```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.portfolio

import com.alexcemen.cryptoportfolio.ui.mvi.Reducer
import kotlin.math.abs

class PortfolioReducer : Reducer<PortfolioStore.State, PortfolioStore.UiState> {
    override fun reduce(state: PortfolioStore.State) = PortfolioStore.UiState(
        coins = state.portfolio.coins.sortedByDescending { it.totalPositionUsdt }.map { coin ->
            PortfolioStore.CoinUi(
                symbol = coin.symbol,
                priceUsdt = "${"$"}%.4f".format(coin.priceUsdt),
                quantity = "%.6f".format(coin.quantity),
                totalPositionUsdt = "${"$"}%.2f".format(coin.totalPositionUsdt),
                logoUrl = coin.logoUrl,
                avatarColorIndex = abs(coin.symbol.hashCode()) % AVATAR_COLORS_COUNT,
            )
        },
        totalUsdt = "${"$"}${"%.2f".format(state.portfolio.totalUsdt)}",
        isLoading = state.isLoading,
        showSellSheet = state.showSellSheet,
        sellAmountInput = state.sellAmountInput,
    )

    private companion object {
        const val AVATAR_COLORS_COUNT = 7
    }
}
```

Note: `String.format()` is available in Kotlin Multiplatform since Kotlin 2.1.0 via `kotlin.text.format`. However, the `Locale` parameter is not available in common code. Since you use `Locale.US` only for decimal separators, and `kotlin.text.format` uses the invariant locale (which uses `.` as decimal separator), this will work correctly without specifying a locale.

- [ ] **Step 3: Move SettingsStore.kt — unchanged** (already pure Kotlin)

- [ ] **Step 4: Move SettingsReducer.kt — remove @Inject**

```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.ui.mvi.Reducer

class SettingsReducer : Reducer<SettingsStore.State, SettingsStore.UiState> {
    override fun reduce(state: SettingsStore.State) = SettingsStore.UiState(
        cmcApiKey = state.settings.cmcApiKey,
        mexcApiKey = state.settings.mexcApiKey,
        mexcApiSecret = state.settings.mexcApiSecret,
        topCoinsLimit = state.settings.topCoinsLimit,
        excludedCoins = state.settings.excludedCoins,
        editingField = state.editingField,
        isLoading = state.isLoading,
    )
}
```

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/
git commit -m "feat: move Stores and Reducers to commonMain"
```

---

### Task 21: Create ScreenModels (replace ViewModels)

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/portfolio/PortfolioScreenModel.kt`
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/settings/SettingsScreenModel.kt`

- [ ] **Step 1: Create PortfolioScreenModel.kt**

Adapt from `PortfolioViewModel.kt`: replace `viewModelScope` with `scope`, remove Hilt, remove `retrofit2.HttpException`, remove `java.util.Locale`.

```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.portfolio

import com.alexcemen.cryptoportfolio.domain.usecase.GetPortfolioUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.RebalancerUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.SellUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.UpdatePortfolioUseCase
import com.alexcemen.cryptoportfolio.platform.Logger
import com.alexcemen.cryptoportfolio.ui.mvi.ScreenModel
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.Effect
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.Event
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.SideEffect
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.State
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class PortfolioScreenModel(
    private val getPortfolio: GetPortfolioUseCase,
    private val updatePortfolio: UpdatePortfolioUseCase,
    private val sell: SellUseCase,
    private val rebalancer: RebalancerUseCase,
    reducer: PortfolioReducer,
) : ScreenModel<State, Event, SideEffect, Effect, UiState>(reducer) {

    override fun createState() = State()

    init {
        scope.launch {
            getPortfolio().collect { portfolio ->
                forceEffect(Effect.SetPortfolio(portfolio))
            }
        }
    }

    override fun handleEvent(currentState: State, intent: Event): Flow<Effect> = flow {
        when (intent) {
            Event.Update -> {
                emit(Effect.SetLoading(true))
                val result = updatePortfolio()
                emit(Effect.SetLoading(false))
                if (result.isFailure) {
                    val e = result.exceptionOrNull()
                    Logger.e("Portfolio", "UPDATE error: ${e?.message}")
                    emit(Effect.ShowSnackbar(e?.message ?: "Update failed"))
                }
            }
            Event.Rebalance -> {
                emit(Effect.SetLoading(true))
                val result = rebalancer()
                emit(Effect.SetLoading(false))
                if (result.isFailure) {
                    val e = result.exceptionOrNull()
                    Logger.e("Portfolio", "REBALANCER error: ${e?.message}")
                    emit(Effect.ShowSnackbar(e?.message ?: "Rebalance failed"))
                }
            }
            Event.OpenSellSheet -> emit(Effect.SetShowSellSheet(true))
            Event.CloseSellSheet -> emit(Effect.SetShowSellSheet(false))
            is Event.SetSellAmount -> emit(Effect.SetSellAmount(intent.amount))
            is Event.SetSellPercent -> {
                val amount = currentState.portfolio.totalUsdt * intent.percent
                emit(Effect.SetSellAmount("%.2f".format(amount)))
            }
            Event.Sell -> {
                emit(Effect.SetShowSellSheet(false))
                emit(Effect.SetLoading(true))
                val amount = currentState.sellAmountInput.toDoubleOrNull() ?: 0.0
                val result = sell(amount)
                emit(Effect.SetLoading(false))
                if (result.isFailure) emit(Effect.ShowSnackbar(result.exceptionOrNull()?.message ?: "Sell failed"))
            }
            Event.NavigateToSettings -> {
                scope.launch { sendSideEffect(SideEffect.NavigateToSettings) }
            }
        }
    }

    override fun handleEffect(currentState: State, effect: Effect): State = when (effect) {
        is Effect.SetPortfolio -> currentState.copy(portfolio = effect.portfolio)
        is Effect.SetLoading -> currentState.copy(isLoading = effect.isLoading)
        is Effect.SetShowSellSheet -> currentState.copy(showSellSheet = effect.show)
        is Effect.SetSellAmount -> currentState.copy(sellAmountInput = effect.amount)
        is Effect.ShowSnackbar -> currentState.also {
            scope.launch { sendSideEffect(SideEffect.ShowSnackbar(effect.message)) }
        }
    }
}
```

- [ ] **Step 2: Create SettingsScreenModel.kt**

```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.usecase.GetSettingsUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.SaveSettingsUseCase
import com.alexcemen.cryptoportfolio.ui.mvi.ScreenModel
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.EditingField
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.Effect
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.Event
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.SideEffect
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.State
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SettingsScreenModel(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    reducer: SettingsReducer,
) : ScreenModel<State, Event, SideEffect, Effect, UiState>(reducer) {

    override fun createState() = State()

    init {
        onEvent(Event.Load)
    }

    override fun handleEvent(currentState: State, intent: Event): Flow<Effect> = flow {
        when (intent) {
            Event.Load -> {
                val settings = getSettings()
                emit(Effect.SetSettings(settings))
            }
            is Event.StartEdit -> emit(Effect.SetEditingField(intent.field))
            Event.CancelEdit -> emit(Effect.SetEditingField(null))
            is Event.SaveField -> {
                val updated = applyFieldUpdate(currentState.settings, intent.field, intent.value)
                saveSettings(updated)
                emit(Effect.SetSettings(updated))
                emit(Effect.SetEditingField(null))
            }
            is Event.AddExcludedCoin -> {
                val coin = intent.coin.uppercase().trim()
                if (coin.isBlank() || coin in currentState.settings.excludedCoins) return@flow
                val updated = currentState.settings.copy(
                    excludedCoins = currentState.settings.excludedCoins + coin
                )
                saveSettings(updated)
                emit(Effect.SetSettings(updated))
            }
            is Event.RemoveExcludedCoin -> {
                val updated = currentState.settings.copy(
                    excludedCoins = currentState.settings.excludedCoins - intent.coin
                )
                saveSettings(updated)
                emit(Effect.SetSettings(updated))
            }
        }
    }

    override fun handleEffect(currentState: State, effect: Effect): State = when (effect) {
        is Effect.SetSettings -> currentState.copy(settings = effect.settings)
        is Effect.SetEditingField -> currentState.copy(editingField = effect.field)
        is Effect.ShowSnackbar -> currentState
    }

    private fun applyFieldUpdate(settings: SettingsData, field: EditingField, value: String) =
        when (field) {
            EditingField.CMC_KEY -> settings.copy(cmcApiKey = value)
            EditingField.MEXC_KEY -> settings.copy(mexcApiKey = value)
            EditingField.MEXC_SECRET -> settings.copy(mexcApiSecret = value)
            EditingField.TOP_COINS_LIMIT -> settings.copy(topCoinsLimit = value.toIntOrNull() ?: settings.topCoinsLimit)
            EditingField.EXCLUDED_COINS -> settings
        }
}
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/portfolio/PortfolioScreenModel.kt
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/settings/SettingsScreenModel.kt
git commit -m "feat: create ScreenModels (replaces Hilt ViewModels)"
```

---

### Task 22: Move composables to commonMain

**Files:**
- Create: all composable files under `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/`

All composables need these changes:
1. Replace `import androidx.compose.ui.res.stringResource` with `import org.jetbrains.compose.resources.stringResource`
2. Replace `import com.alexcemen.cryptoportfolio.R` with `import cryptoportfolio.shared.generated.resources.*` (or the appropriate generated Res class)
3. Replace `R.string.xxx` with `Res.string.xxx`
4. Replace `R.drawable.xxx` with `Res.drawable.xxx`
5. Replace `painterResource(R.drawable.xxx)` with `org.jetbrains.compose.resources.painterResource(Res.drawable.xxx)`
6. Replace `hiltViewModel()` with `koinInject()`
7. Replace `collectAsStateWithLifecycle()` with `collectAsState()`
8. Replace `RootNavigation.current` with `LocalNavigator.current`
9. Replace `nav?.add(SettingsScreen())` with `navigator.navigate(Screen.Settings)`
10. Replace `nav?.removeLastOrNull()` with `navigator.back()`
11. Replace `import coil.compose.AsyncImage` with `import coil3.compose.AsyncImage`
12. Remove `@Preview` annotations and preview functions (not supported in CMP, can add back later for Android-specific previews)
13. Replace `import androidx.compose.ui.tooling.preview.Preview` — remove

- [ ] **Step 1: Move PortfolioScreen.kt (major changes)**

```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.alexcemen.cryptoportfolio.ui.mvi.sideEffect
import com.alexcemen.cryptoportfolio.ui.navigation.LocalNavigator
import com.alexcemen.cryptoportfolio.ui.navigation.Screen
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioScreenModel
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PortfolioScreenContent(screenModel: PortfolioScreenModel = koinInject()) {
    val uiState by screenModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) { onDispose { screenModel.onCleared() } }

    screenModel.sideEffect { effect ->
        when (effect) {
            is PortfolioStore.SideEffect.ShowSnackbar ->
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            PortfolioStore.SideEffect.NavigateToSettings ->
                navigator.navigate(Screen.Settings)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PortfolioContent(
            uiState = uiState,
            onEvent = screenModel::onEvent,
            contentPadding = padding,
        )
    }
}
```

- [ ] **Step 2: Move PortfolioContent.kt**

Replace `stringResource(R.string.xxx)` with `stringResource(Res.string.xxx)`, remove Preview imports and functions, remove `import com.alexcemen.cryptoportfolio.R`.

- [ ] **Step 3: Move AppHeader.kt**

Replace `painterResource(R.drawable.ic_launcher_foreground)` with `painterResource(Res.drawable.ic_launcher_foreground)`. Replace string resources. Remove previews.

- [ ] **Step 4: Move BalanceCard.kt, ActionButtonsRow.kt, CoinListCard.kt, SellSheet.kt**

Same pattern: replace R.string → Res.string, remove previews.

- [ ] **Step 5: Move CoinAvatar.kt**

Replace `import coil.compose.AsyncImage` with `import coil3.compose.AsyncImage`. Remove preview.

- [ ] **Step 6: Move SettingsScreen.kt**

Replace hiltViewModel → koinInject, collectAsStateWithLifecycle → collectAsState, RootNavigation → LocalNavigator, nav?.removeLastOrNull() → navigator.back(), R.string → Res.string. Add DisposableEffect for onCleared.

- [ ] **Step 7: Move SettingsContent.kt, SettingRow.kt, ExcludedCoinsSection.kt**

Replace R.string → Res.string, remove previews.

- [ ] **Step 8: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/screen/
git commit -m "feat: move all composables to commonMain with CMP resource imports"
```

---

### Task 23: Create App.kt root composable and iOS entry point

**Files:**
- Create: `shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/App.kt`
- Create: `shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/MainViewController.kt`

- [ ] **Step 1: Create App.kt**

```kotlin
package com.alexcemen.cryptoportfolio.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.alexcemen.cryptoportfolio.ui.navigation.LocalNavigator
import com.alexcemen.cryptoportfolio.ui.navigation.Navigator
import com.alexcemen.cryptoportfolio.ui.navigation.Screen
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable.PortfolioScreenContent
import com.alexcemen.cryptoportfolio.ui.screen.settings.composable.SettingsScreenContent
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme

@Composable
fun App() {
    val navigator = remember { Navigator() }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        AppTheme {
            when (navigator.current) {
                Screen.Portfolio -> PortfolioScreenContent()
                Screen.Settings -> SettingsScreenContent()
            }
        }
    }
}
```

- [ ] **Step 2: Create MainViewController.kt for iOS**

```kotlin
package com.alexcemen.cryptoportfolio

import androidx.compose.ui.window.ComposeUIViewController
import com.alexcemen.cryptoportfolio.ui.App

fun MainViewController() = ComposeUIViewController { App() }
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/alexcemen/cryptoportfolio/ui/App.kt
git add shared/src/iosMain/kotlin/com/alexcemen/cryptoportfolio/MainViewController.kt
git commit -m "feat: create App root composable and iOS entry point"
```

---

## Chunk 6: Android & iOS Entry Points, Cleanup

### Task 24: Create androidApp entry points

**Files:**
- Create: `androidApp/src/main/kotlin/com/alexcemen/cryptoportfolio/CryptoApp.kt`
- Create: `androidApp/src/main/kotlin/com/alexcemen/cryptoportfolio/MainActivity.kt`

- [ ] **Step 1: Create CryptoApp.kt**

```kotlin
package com.alexcemen.cryptoportfolio

import android.app.Application
import com.alexcemen.cryptoportfolio.di.androidModule
import com.alexcemen.cryptoportfolio.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CryptoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CryptoApp)
            modules(commonModule, androidModule)
        }
    }
}
```

- [ ] **Step 2: Create MainActivity.kt**

```kotlin
package com.alexcemen.cryptoportfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alexcemen.cryptoportfolio.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}
```

- [ ] **Step 3: Update AndroidManifest.xml**

Ensure `androidApp/src/main/AndroidManifest.xml` has:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:name=".CryptoApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Top100App">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Top100App">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 4: Trim androidApp strings.xml**

Keep only `app_name` in `androidApp/src/main/res/values/strings.xml` (all other strings are now in CMP resources):
```xml
<resources>
    <string name="app_name">AltCoins</string>
</resources>
```

- [ ] **Step 5: Commit**

```bash
git add androidApp/
git commit -m "feat: create Android entry points (CryptoApp + MainActivity with Koin)"
```

---

### Task 25: Move unit tests to commonTest

**Files:**
- Create: `shared/src/commonTest/kotlin/com/alexcemen/cryptoportfolio/domain/usecase/`
- Create: `shared/src/commonTest/kotlin/com/alexcemen/cryptoportfolio/ui/screen/`

- [ ] **Step 1: Move use case tests**

Copy from `app/src/test/` to `shared/src/commonTest/`, adapting:
- Remove `import javax.inject.Inject` references in test mocks
- Replace `@Test` from JUnit4 to `kotlin.test.Test`
- Replace `assertEquals` etc. from JUnit4 to `kotlin.test`

Files:
- `CheckSettingsUseCaseTest.kt`
- `RebalancerUseCaseTest.kt`
- `SellUseCaseTest.kt`
- `UpdatePortfolioUseCaseTest.kt`

- [ ] **Step 2: Move reducer tests**

- `PortfolioReducerTest.kt`
- `SettingsReducerTest.kt`

Same adaptations: JUnit4 → kotlin.test.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonTest/
git commit -m "feat: move unit tests to commonTest (kotlin.test)"
```

---

### Task 26: Delete old app module

**Files:**
- Delete: `app/` directory entirely

- [ ] **Step 1: Remove the old app module**

```bash
rm -rf app/
```

- [ ] **Step 2: Verify build**

Run: `./gradlew :shared:compileKotlinAndroid :androidApp:assembleDebug`

Expected: Build succeeds (may need minor fixes for import issues).

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: remove old app module (replaced by shared + androidApp)"
```

---

### Task 27: Create Xcode project for iOS

**Files:**
- Create: Xcode project at `iosApp/`

- [ ] **Step 1: Build shared framework for iOS**

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Expected: produces `shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework`

- [ ] **Step 2: Create Xcode project**

Open Xcode → File → New → Project → App → SwiftUI → Name: "iosApp" → Location: project root.

OR use the KMP Xcode plugin / manual Xcode project setup:
- Add the shared.framework to the project's "Frameworks, Libraries, and Embedded Content"
- Set the framework search path to `$(SRCROOT)/../shared/build/bin/iosSimulatorArm64/debugFramework`
- Add a Run Script build phase to build the framework before compilation

- [ ] **Step 3: Add Swift source files**

Place `iOSApp.swift` and `ContentView.swift` (from Task 7) into the Xcode project.

- [ ] **Step 4: Build and run on iOS Simulator**

Expected: App launches, shows the Portfolio screen.

- [ ] **Step 5: Commit**

```bash
git add iosApp/
git commit -m "feat: create iOS Xcode project with Compose Multiplatform"
```

---

### Task 28: Final verification

- [ ] **Step 1: Build Android**

```bash
./gradlew :androidApp:assembleDebug
```

Expected: APK builds successfully.

- [ ] **Step 2: Run unit tests**

```bash
./gradlew :shared:allTests
```

Expected: All common tests pass.

- [ ] **Step 3: Build iOS framework**

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Expected: Framework builds successfully.

- [ ] **Step 4: Run on Android emulator**

Verify: Portfolio screen loads, settings navigation works, API calls function.

- [ ] **Step 5: Run on iOS Simulator**

Verify: Same functionality as Android.

- [ ] **Step 6: Commit and tag**

```bash
git add -A
git commit -m "feat: complete KMP migration — Android + iOS"
git tag v2.0.0-kmp
```

---

## Implementation Notes

### Known compilation issues to watch for:

1. **Room KMP + KSP**: Room compiler must be added as `ksp` dependency for each target separately. The `@ConstructedBy` pattern is required for KMP.

2. **Compose Multiplatform resources**: The generated `Res` class path depends on the module name. Import will be something like `import cryptoportfolio.shared.generated.resources.Res` or similar — check the actual generated path after first compilation.

3. **iOS Keychain cinterop**: The Security framework bindings may need adjustments. If `CFTypeRefVar` doesn't exist, use `COpaquePointerVar` instead. Test the Keychain implementation on a real device or simulator.

4. **String.format in common code**: Available since Kotlin 2.1.0 via `kotlin.text.format`. Uses invariant locale (`.` decimal separator). This is correct for your use case (USD formatting).

5. **Dispatchers.IO**: Available in common code via `kotlinx.coroutines` 1.10+. No expect/actual needed.

6. **Coil 3 on iOS**: Requires the `coil-network-ktor3` dependency to work with Ktor's HTTP engine on iOS.

7. **androidApp build.gradle.kts**: May need `kotlin("android")` plugin explicitly if the Compose Multiplatform plugin doesn't include it automatically. Adjust based on build errors.
