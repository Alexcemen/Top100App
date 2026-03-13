# Crypto Portfolio App — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a single-module Android app that displays a MEXC crypto portfolio, supports manual update, rebalancing, and partial sell operations.

**Architecture:** Single `:app` module, three clean layers (`ui`, `domain`, `data`), MVI pattern (identical to Tutor), Hilt DI, Room DB, Retrofit network.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation3, Hilt 2.57.2, Room 2.7.1, Retrofit 2.11.0, OkHttp 4.12.0, Gson 2.11.0, Coroutines 1.10.1, AndroidX Security Crypto 1.0.0, Timber 5.0.1

**Spec:** `docs/superpowers/specs/2026-03-13-crypto-portfolio-design.md`
**Tutor reference:** `/Users/alexcemen/AndroidStudioProjects/Tutor/` — use as pattern source for MVI, theme, navigation

---

## File Map

```
app/src/main/kotlin/com/alexcemen/cryptoportfolio/
├── CryptoApp.kt
├── MainActivity.kt
├── ui/
│   ├── mvi/
│   │   ├── MviStore.kt              — MviState/Event/Effect/SideEffect/UiState/Reducer interfaces
│   │   ├── ScreenViewModel.kt       — abstract base ViewModel
│   │   ├── MviExtensions.kt         — sideEffect {} composable extension
│   │   └── Screen.kt                — AppNavKey abstract class
│   ├── theme/
│   │   ├── AppTheme.kt
│   │   ├── AppThemeColors.kt        — LightColor, DarkColor, AppThemeColorsSchemes
│   │   └── AppThemeTypography.kt
│   ├── navigation/
│   │   └── Navigator.kt             — PortfolioScreen, SettingsScreen destinations + RootNavigation
│   └── screen/
│       ├── portfolio/
│       │   ├── PortfolioStore.kt
│       │   ├── PortfolioViewModel.kt
│       │   ├── PortfolioReducer.kt
│       │   └── composable/
│       │       ├── PortfolioScreen.kt
│       │       └── PortfolioContent.kt
│       └── settings/
│           ├── SettingsStore.kt
│           ├── SettingsViewModel.kt
│           ├── SettingsReducer.kt
│           └── composable/
│               ├── SettingsScreen.kt
│               └── SettingsContent.kt
├── domain/
│   ├── model/
│   │   ├── CoinData.kt
│   │   ├── PortfolioData.kt
│   │   └── SettingsData.kt
│   ├── repository/
│   │   ├── PortfolioRepository.kt
│   │   └── SettingsRepository.kt
│   └── usecase/
│       ├── CheckSettingsUseCase.kt
│       ├── GetPortfolioUseCase.kt
│       ├── GetSettingsUseCase.kt
│       ├── SaveSettingsUseCase.kt
│       ├── UpdatePortfolioUseCase.kt
│       ├── SellUseCase.kt
│       └── RebalancerUseCase.kt
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── entity/
│   │   │   └── CoinEntity.kt
│   │   └── dao/
│   │       └── PortfolioDao.kt
│   ├── network/
│   │   ├── CmcApiService.kt
│   │   ├── MexcApiService.kt
│   │   ├── MexcSigningInterceptor.kt
│   │   └── dto/
│   │       ├── CmcListingsResponse.kt
│   │       ├── MexcAccountResponse.kt
│   │       ├── MexcExchangeInfoResponse.kt
│   │       ├── MexcTickerPriceDto.kt
│   │       └── MexcOrderRequest.kt
│   └── repository/
│       ├── PortfolioRepositoryImpl.kt
│       └── SettingsRepositoryImpl.kt
└── di/
    ├── DatabaseModule.kt
    ├── NetworkModule.kt
    └── RepositoryModule.kt

app/src/test/kotlin/com/alexcemen/cryptoportfolio/
├── domain/usecase/
│   ├── CheckSettingsUseCaseTest.kt
│   ├── UpdatePortfolioUseCaseTest.kt
│   ├── SellUseCaseTest.kt
│   └── RebalancerUseCaseTest.kt
└── ui/screen/
    ├── portfolio/PortfolioReducerTest.kt
    └── settings/SettingsReducerTest.kt

app/src/androidTest/kotlin/com/alexcemen/cryptoportfolio/
└── data/db/
    └── PortfolioDaoTest.kt
```

---

## Chunk 1: Project Setup & MVI Foundation

### Task 1: Create Android Studio project

- [ ] In Android Studio: **File → New → New Project → Empty Activity**
  - Name: `CryptoPortfolio`
  - Package: `com.alexcemen.cryptoportfolio`
  - Language: Kotlin
  - Min SDK: 26
- [ ] Delete the generated `MainActivity.kt` and `ui/theme/` — we'll create our own
- [ ] Add `INTERNET` permission to `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  ```
- [ ] Copy `docs/superpowers/specs/2026-03-13-crypto-portfolio-design.md` and `ANDROID_REFERENCE.md` into the new project root; rename design spec to `CLAUDE.md` (or keep both and reference from `CLAUDE.md`)

---

### Task 2: Configure Gradle

- [ ] Replace `gradle/libs.versions.toml` with:

```toml
[versions]
agp = "8.10.1"
kotlin = "2.1.21"
coreKtx = "1.16.0"
activityCompose = "1.10.1"
lifecycleRuntimeKtx = "2.9.1"
composeBom = "2025.06.00"
navigation3 = "1.0.0"
hilt = "2.57.2"
hiltNavigation = "1.3.0"
room = "2.7.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
gson = "2.11.0"
coroutines = "1.10.1"
kotlinxSerialization = "1.8.1"
securityCrypto = "1.0.0"
timber = "5.0.1"
junit = "4.13.2"
androidxTestJunit = "1.2.1"
espresso = "3.6.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation3 = { group = "androidx.navigation3", name = "navigation3-ui", version.ref = "navigation3" }
androidx-navigation3-runtime = { group = "androidx.navigation3", name = "navigation3-runtime", version.ref = "navigation3" }
androidx-viewmodel-navigation3 = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-navigation3", version.ref = "lifecycleRuntimeKtx" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigation" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core", version.ref = "kotlinxSerialization" }
security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "securityCrypto" }
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-test-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxTestJunit" }
androidx-espresso = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.1.21-2.0.1" }
```

- [ ] Update root `build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

- [ ] Update `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.alexcemen.cryptoportfolio"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.alexcemen.cryptoportfolio"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.viewmodel.navigation3)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.security.crypto)
    implementation(libs.timber)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
```

- [ ] Sync Gradle — verify it builds with `./gradlew assembleDebug`
- [ ] Commit: `git commit -m "chore: configure gradle dependencies"`

---

### Task 3: MVI base classes

Port directly from Tutor. These files have no business logic — they are infrastructure.

- [ ] Create `ui/mvi/MviStore.kt`:
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

- [ ] Create `ui/mvi/Screen.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.ui.mvi

import androidx.navigation3.runtime.NavKey

abstract class AppNavKey : NavKey {
    open val type: String = ""
}
```

- [ ] Create `ui/mvi/ScreenViewModel.kt` — copy exactly from Tutor's `ScreenViewModel.kt`, change only the package to `com.alexcemen.cryptoportfolio.ui.mvi`

- [ ] Create `ui/mvi/MviExtensions.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
inline fun <E : MviSideEffect> ScreenViewModel<*, *, E, *, *>.sideEffect(
    crossinline body: (effect: E) -> Unit,
) {
    LaunchedEffect(Unit) {
        this@sideEffect.sideEffects.collect { effect ->
            body(effect)
        }
    }
}
```

- [ ] Commit: `git commit -m "feat: add MVI base classes"`

---

### Task 4: Theme

Port from Tutor's `common_ui/composable/theme/`. Reference files:
- `AppTheme.kt` — `compositionLocalOf` providers, `AppTheme` object with `colors` and `textStyle`
- `AppThemeColors.kt` — color tokens, `LightColor`, `DarkColor`
- `AppThemeTypography.kt` — all text styles

- [ ] Create `ui/theme/AppThemeColors.kt` — copy color tokens from Tutor's `AppThemeColors.kt`, change package
- [ ] Create `ui/theme/AppThemeTypography.kt` — copy typography from Tutor, change package
- [ ] Create `ui/theme/AppTheme.kt` — copy from Tutor's `AppTheme.kt`, change package
- [ ] Commit: `git commit -m "feat: add app theme"`

---

### Task 5: Hilt setup + Navigation + MainActivity

- [ ] Create `CryptoApp.kt`:
```kotlin
package com.alexcemen.cryptoportfolio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CryptoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
```

- [ ] Register in `AndroidManifest.xml`: `android:name=".CryptoApp"`

- [ ] Create `ui/navigation/Navigator.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.ui.navigation

import kotlinx.serialization.Serializable
import com.alexcemen.cryptoportfolio.ui.mvi.AppNavKey

@Serializable
data class PortfolioScreen(
    override val type: String = PortfolioScreen::class.simpleName.toString()
) : AppNavKey()

@Serializable
data class SettingsScreen(
    override val type: String = SettingsScreen::class.simpleName.toString()
) : AppNavKey()
```

- [ ] Create `MainActivity.kt`:
```kotlin
package com.alexcemen.cryptoportfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alexcemen.cryptoportfolio.ui.mvi.AppNavKey
import com.alexcemen.cryptoportfolio.ui.navigation.PortfolioScreen
import com.alexcemen.cryptoportfolio.ui.navigation.SettingsScreen
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

val RootNavigation = compositionLocalOf<NavBackStack<AppNavKey>?> { null }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val backStack = rememberNavBackStack(PortfolioScreen()) as NavBackStack<AppNavKey>
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<PortfolioScreen> {
                            // PortfolioScreenContent() — added in Task 21
                        }
                        entry<SettingsScreen> {
                            // SettingsScreenContent() — added in Task 13
                        }
                    }
                )
            }
        }
    }
}
```

- [ ] Build: `./gradlew assembleDebug` — must succeed
- [ ] Commit: `git commit -m "feat: hilt setup, navigation, main activity"`

---

## Chunk 2: Data Foundation

### Task 6: Domain models

- [ ] Create `domain/model/CoinData.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.model

data class CoinData(
    val symbol: String,
    val priceUsdt: Double,
    val quantity: Double,
) {
    val totalPositionUsdt: Double get() = priceUsdt * quantity
}
```

- [ ] Create `domain/model/PortfolioData.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.model

data class PortfolioData(
    val coins: List<CoinData>,
    val totalUsdt: Double,
)
```

- [ ] Create `domain/model/SettingsData.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.model

data class SettingsData(
    val cmcApiKey: String = "",
    val mexcApiKey: String = "",
    val mexcApiSecret: String = "",
    val topCoinsLimit: Int = 20,
    val excludedCoins: List<String> = listOf("USDT", "USDC", "BUSD"),
)
```

- [ ] Create `domain/repository/PortfolioRepository.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.repository

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import kotlinx.coroutines.flow.Flow

interface PortfolioRepository {
    fun getPortfolio(): Flow<PortfolioData>
    suspend fun savePortfolio(coins: List<CoinData>)
}
```

- [ ] Create `domain/repository/SettingsRepository.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.repository

import com.alexcemen.cryptoportfolio.domain.model.SettingsData

interface SettingsRepository {
    suspend fun getSettings(): SettingsData
    suspend fun saveSettings(settings: SettingsData)
}
```

- [ ] Commit: `git commit -m "feat: domain models and repository interfaces"`

---

### Task 7: Room entities, DAOs, Database

Only portfolio data goes in Room. Settings go in `EncryptedSharedPreferences` (Task 8).

- [ ] Write failing instrumented test `PortfolioDaoTest.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PortfolioDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: PortfolioDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).build()
        dao = db.portfolioDao()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insertAndGetCoins() = runTest {
        val coins = listOf(
            CoinEntity("ETH", 3000.0, 0.5),
            CoinEntity("SOL", 150.0, 10.0),
        )
        dao.replaceAll(coins)
        val result = dao.getAll().first()
        assertEquals(2, result.size)
        assertEquals("ETH", result.find { it.symbol == "ETH" }?.symbol)
    }

    @Test
    fun replaceAllClearsPrevious() = runTest {
        dao.replaceAll(listOf(CoinEntity("BTC", 60000.0, 0.1)))
        dao.replaceAll(listOf(CoinEntity("ETH", 3000.0, 1.0)))
        val result = dao.getAll().first()
        assertEquals(1, result.size)
        assertEquals("ETH", result[0].symbol)
    }
}
```

- [ ] Run test — expect compile error (classes don't exist yet)

- [ ] Create `data/db/entity/CoinEntity.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_table")
data class CoinEntity(
    @PrimaryKey val symbol: String,
    val priceUsdt: Double,
    val quantity: Double,
)
```

- [ ] Create `data/db/dao/PortfolioDao.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_table")
    fun getAll(): Flow<List<CoinEntity>>

    @Query("DELETE FROM portfolio_table")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coins: List<CoinEntity>)

    @Transaction
    suspend fun replaceAll(coins: List<CoinEntity>) {
        deleteAll()
        insertAll(coins)
    }
}
```

- [ ] Create `data/db/AppDatabase.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity

@Database(
    entities = [CoinEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
}
```

- [ ] Run instrumented test: `./gradlew connectedAndroidTest` — must pass
- [ ] Commit: `git commit -m "feat: room entity, dao, database"`

---

### Task 8: DI modules (Database + Repository stubs)

- [ ] Create `di/DatabaseModule.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.di

import android.content.Context
import androidx.room.Room
import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import com.alexcemen.cryptoportfolio.data.db.PortfolioDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object DatabaseModule {

    @Provides
    @ActivityRetainedScoped
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "crypto_portfolio.db").build()

    @Provides
    fun providePortfolioDao(db: AppDatabase): PortfolioDao = db.portfolioDao()
}
```

- [ ] Create `data/repository/SettingsRepositoryImpl.kt` — backed by `EncryptedSharedPreferences`:
```kotlin
package com.alexcemen.cryptoportfolio.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val PREFS_FILE = "crypto_secure_prefs"
private const val KEY_CMC = "cmc_api_key"
private const val KEY_MEXC_KEY = "mexc_api_key"
private const val KEY_MEXC_SECRET = "mexc_api_secret"
private const val KEY_TOP_LIMIT = "top_coins_limit"
private const val KEY_EXCLUDED = "excluded_coins"

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun getSettings(): SettingsData = withContext(Dispatchers.IO) {
        SettingsData(
            cmcApiKey = prefs.getString(KEY_CMC, "") ?: "",
            mexcApiKey = prefs.getString(KEY_MEXC_KEY, "") ?: "",
            mexcApiSecret = prefs.getString(KEY_MEXC_SECRET, "") ?: "",
            topCoinsLimit = prefs.getInt(KEY_TOP_LIMIT, 20),
            excludedCoins = prefs.getString(KEY_EXCLUDED, "USDT,USDC,BUSD")
                ?.split(",")?.filter { it.isNotBlank() } ?: listOf("USDT", "USDC", "BUSD"),
        )
    }

    override suspend fun saveSettings(settings: SettingsData) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putString(KEY_CMC, settings.cmcApiKey)
            .putString(KEY_MEXC_KEY, settings.mexcApiKey)
            .putString(KEY_MEXC_SECRET, settings.mexcApiSecret)
            .putInt(KEY_TOP_LIMIT, settings.topCoinsLimit)
            .putString(KEY_EXCLUDED, settings.excludedCoins.joinToString(","))
            .apply()
    }
}
```

`data/repository/PortfolioRepositoryImpl.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.data.db.PortfolioDao
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity
import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PortfolioRepositoryImpl @Inject constructor(
    private val dao: PortfolioDao
) : PortfolioRepository {

    override fun getPortfolio(): Flow<PortfolioData> =
        dao.getAll().map { entities ->
            val coins = entities.map { it.toDomain() }
            PortfolioData(
                coins = coins,
                totalUsdt = coins.sumOf { it.totalPositionUsdt }
            )
        }

    override suspend fun savePortfolio(coins: List<CoinData>) {
        dao.replaceAll(coins.map { it.toEntity() })
    }

    private fun CoinEntity.toDomain() = CoinData(symbol, priceUsdt, quantity)
    private fun CoinData.toEntity() = CoinEntity(symbol, priceUsdt, quantity)
}
```

- [ ] Create `di/RepositoryModule.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.repository.PortfolioRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.SettingsRepositoryImpl
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindPortfolioRepository(impl: PortfolioRepositoryImpl): PortfolioRepository
    @Binds abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
```

- [ ] Build: `./gradlew assembleDebug` — must succeed
- [ ] Commit: `git commit -m "feat: DI modules, repository implementations"`

---

## Chunk 3: Settings Feature

### Task 9: Settings use cases (TDD)

- [ ] Write `CheckSettingsUseCaseTest.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckSettingsUseCaseTest {
    private fun makeRepo(settings: SettingsData): SettingsRepository = object : SettingsRepository {
        override suspend fun getSettings() = settings
        override suspend fun saveSettings(settings: SettingsData) {}
    }

    @Test
    fun allKeysPresent_returnsTrue() = runTest {
        val useCase = CheckSettingsUseCase(makeRepo(SettingsData(
            cmcApiKey = "key1", mexcApiKey = "key2", mexcApiSecret = "secret"
        )))
        assertTrue(useCase())
    }

    @Test
    fun cmcKeyEmpty_returnsFalse() = runTest {
        val useCase = CheckSettingsUseCase(makeRepo(SettingsData(
            cmcApiKey = "", mexcApiKey = "key2", mexcApiSecret = "secret"
        )))
        assertFalse(useCase())
    }

    @Test
    fun mexcKeyEmpty_returnsFalse() = runTest {
        val useCase = CheckSettingsUseCase(makeRepo(SettingsData(
            cmcApiKey = "key1", mexcApiKey = "", mexcApiSecret = "secret"
        )))
        assertFalse(useCase())
    }
}
```

- [ ] Run — expect compile error
- [ ] Create `domain/usecase/CheckSettingsUseCase.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import javax.inject.Inject

class CheckSettingsUseCase @Inject constructor(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(): Boolean {
        val s = repo.getSettings()
        return s.cmcApiKey.isNotBlank() && s.mexcApiKey.isNotBlank() && s.mexcApiSecret.isNotBlank()
    }
}
```

- [ ] Create `domain/usecase/GetSettingsUseCase.kt`:
```kotlin
class GetSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke() = repo.getSettings()
}
```

- [ ] Create `domain/usecase/SaveSettingsUseCase.kt`:
```kotlin
class SaveSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(settings: SettingsData) = repo.saveSettings(settings)
}
```

- [ ] Create `domain/usecase/GetPortfolioUseCase.kt`:
```kotlin
class GetPortfolioUseCase @Inject constructor(private val repo: PortfolioRepository) {
    operator fun invoke() = repo.getPortfolio()
}
```

- [ ] Run `CheckSettingsUseCaseTest` — all 3 tests must pass: `./gradlew :app:testDebugUnitTest --tests "*.CheckSettingsUseCaseTest"`
- [ ] Commit: `git commit -m "feat: settings and portfolio use cases"`

---

### Task 10: SettingsScreen MVI

- [ ] Write `SettingsReducerTest.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsReducerTest {
    private val reducer = SettingsReducer()

    @Test
    fun loadedState_mapsToUiState() {
        val settings = SettingsData(cmcApiKey = "abc", topCoinsLimit = 30)
        val state = SettingsStore.State(settings = settings, isLoading = false)
        val uiState = reducer.reduce(state)
        assertEquals("abc", uiState.cmcApiKey)
        assertEquals(30, uiState.topCoinsLimit)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun loadingState_isReflected() {
        val state = SettingsStore.State(settings = SettingsData(), isLoading = true)
        val uiState = reducer.reduce(state)
        assertTrue(uiState.isLoading)
    }
}
```

- [ ] Run — expect compile error

- [ ] Create `ui/screen/settings/SettingsStore.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.ui.mvi.*

object SettingsStore {
    data class State(
        val settings: SettingsData = SettingsData(),
        val editingField: EditingField? = null,
        val isLoading: Boolean = false,
    ) : MviState

    data class UiState(
        val cmcApiKey: String,
        val mexcApiKey: String,
        val mexcApiSecret: String,
        val topCoinsLimit: Int,
        val excludedCoins: List<String>,
        val editingField: EditingField?,
        val isLoading: Boolean,
    ) : MviUiState

    enum class EditingField { CMC_KEY, MEXC_KEY, MEXC_SECRET, TOP_COINS_LIMIT, EXCLUDED_COINS }

    sealed interface Event : MviEvent {
        data object Load : Event
        data class StartEdit(val field: EditingField) : Event
        data object CancelEdit : Event
        data class SaveField(val field: EditingField, val value: String) : Event
        data class AddExcludedCoin(val coin: String) : Event
        data class RemoveExcludedCoin(val coin: String) : Event
    }

    sealed interface Effect : MviEffect {
        data class SetSettings(val settings: SettingsData) : Effect
        data class SetEditingField(val field: EditingField?) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }

    sealed interface SideEffect : MviSideEffect {
        data class ShowSnackbar(val message: String) : SideEffect
    }
}
```

- [ ] Create `ui/screen/settings/SettingsReducer.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.ui.mvi.Reducer
import javax.inject.Inject

class SettingsReducer @Inject constructor() :
    Reducer<SettingsStore.State, SettingsStore.UiState> {
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

- [ ] Run `SettingsReducerTest` — must pass
- [ ] Create `ui/screen/settings/SettingsViewModel.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.usecase.GetSettingsUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.SaveSettingsUseCase
import com.alexcemen.cryptoportfolio.ui.mvi.ScreenViewModel
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    reducer: SettingsReducer,
) : ScreenViewModel<State, Event, SideEffect, Effect, UiState>(reducer) {

    override fun createState() = State()
    override val startIntent = listOf(Event.Load)

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
        is Effect.ShowSnackbar -> currentState // handled via SideEffect
    }

    private fun applyFieldUpdate(settings: SettingsData, field: EditingField, value: String) =
        when (field) {
            EditingField.CMC_KEY -> settings.copy(cmcApiKey = value)
            EditingField.MEXC_KEY -> settings.copy(mexcApiKey = value)
            EditingField.MEXC_SECRET -> settings.copy(mexcApiSecret = value)
            EditingField.TOP_COINS_LIMIT -> settings.copy(topCoinsLimit = value.toIntOrNull() ?: settings.topCoinsLimit)
            EditingField.EXCLUDED_COINS -> settings // handled via Add/Remove events
        }
}
```

- [ ] Commit: `git commit -m "feat: settings screen MVI"`

---

### Task 11: SettingsScreen composables

- [ ] Create `ui/screen/settings/composable/SettingsScreen.kt` — collects `uiState`, handles `sideEffects`, renders `SettingsContent`
- [ ] Create `ui/screen/settings/composable/SettingsContent.kt`:
  - Top bar with back button and "Settings" title
  - 5 rows, each with label + value + pencil icon
  - Rows 1–4: tapping pencil shows `TextField` in place of value text; confirm button saves
  - Row 5 (Excluded Coins): horizontally scrollable `LazyRow` of chips; edit mode adds ✕ to each chip and shows add input at end
- [ ] Wire `SettingsScreen` into `MainActivity` `entry<SettingsScreen>` block
- [ ] Build and run on emulator — manually test all field edits
- [ ] Commit: `git commit -m "feat: settings screen UI"`

---

## Chunk 4: Network Layer

### Task 12: DTOs

- [ ] Create `data/network/dto/CmcListingsResponse.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network.dto

data class CmcListingsResponse(val data: List<CmcCoinDto>)
data class CmcCoinDto(val symbol: String)
```

- [ ] Create `data/network/dto/MexcAccountResponse.kt`:
```kotlin
data class MexcAccountResponse(val balances: List<MexcBalanceDto>)
data class MexcBalanceDto(val asset: String, val free: String, val locked: String)
```

- [ ] Create `data/network/dto/MexcExchangeInfoResponse.kt`:
```kotlin
data class MexcExchangeInfoResponse(val symbols: List<MexcSymbolDto>)
data class MexcSymbolDto(val baseAsset: String, val quoteAsset: String, val status: String)
```

- [ ] Create `data/network/dto/MexcTickerPriceDto.kt`:
```kotlin
data class MexcTickerPriceDto(val symbol: String, val price: String)
```

- [ ] Create `data/network/dto/MexcOrderRequest.kt`:
```kotlin
data class MexcOrderRequest(
    val symbol: String,   // e.g. "ETHUSDT"
    val side: String,     // "BUY" or "SELL"
    val type: String = "MARKET",
    val quoteOrderQty: String? = null,  // for BUY by USDT amount
    val quantity: String? = null,       // for SELL by coin amount
)
```

- [ ] Commit: `git commit -m "feat: network DTOs"`

---

### Task 13: Retrofit services

- [ ] Create `data/network/CmcApiService.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network

import com.alexcemen.cryptoportfolio.data.network.dto.CmcListingsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CmcApiService {
    @GET("v1/cryptocurrency/listings/latest")
    suspend fun getListings(
        @Header("X-CMC_PRO_API_KEY") apiKey: String,
        @Query("limit") limit: Int,
        @Query("sort") sort: String = "market_cap",
    ): CmcListingsResponse
}
```

- [ ] Create `data/network/MexcApiService.kt`:
```kotlin
interface MexcApiService {
    @GET("api/v3/account")
    suspend fun getAccount(@Query("timestamp") timestamp: Long, @Query("signature") signature: String): MexcAccountResponse

    @GET("api/v3/exchangeInfo")
    suspend fun getExchangeInfo(): MexcExchangeInfoResponse

    @GET("api/v3/ticker/price")
    suspend fun getAllPrices(): List<MexcTickerPriceDto>

    @POST("api/v3/order")
    suspend fun placeOrder(
        @Body order: MexcOrderRequest,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String,
    ): Any
}
```

---

### Task 14: HMAC signing interceptor

- [ ] Create `data/network/MexcSigningInterceptor.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network

import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class MexcSigningInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val settings = runBlocking { settingsRepository.getSettings() }
        val original = chain.request()

        // Add API key header
        val request = original.newBuilder()
            .addHeader("X-MEXC-APIKEY", settings.mexcApiKey)
            .build()

        return chain.proceed(request)
    }

    fun sign(queryString: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(queryString.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
```

Note: The full MEXC v3 API uses HMAC-SHA256 on the query string. The `sign()` function is used inside `PortfolioRepositoryImpl` when building signed request parameters.

---

### Task 15: NetworkModule

- [ ] Create `di/NetworkModule.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.MexcSigningInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(ActivityRetainedComponent::class)
object NetworkModule {

    @Provides
    @ActivityRetainedScoped
    fun provideMexcOkHttpClient(signingInterceptor: MexcSigningInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(signingInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @ActivityRetainedScoped
    fun provideCmcOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @ActivityRetainedScoped
    fun provideMexcApiService(client: OkHttpClient): MexcApiService =
        Retrofit.Builder()
            .baseUrl("https://api.mexc.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MexcApiService::class.java)

    @Provides
    @ActivityRetainedScoped
    fun provideCmcApiService(client: OkHttpClient): CmcApiService =
        Retrofit.Builder()
            .baseUrl("https://pro-api.coinmarketcap.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CmcApiService::class.java)
}
```

Note: The two `OkHttpClient` provides will conflict unless qualified. Use `@Named("mexc")` and `@Named("cmc")` qualifiers on both `@Provides` and all injection sites.

- [ ] Add `@Named("mexc")` / `@Named("cmc")` qualifiers to both `OkHttpClient` providers and the two `Retrofit` providers
- [ ] Build: `./gradlew assembleDebug`
- [ ] Commit: `git commit -m "feat: network layer — retrofit services, signing interceptor, DI"`

---

## Chunk 5: Portfolio Update Feature

### Task 16: UpdatePortfolioUseCase (TDD)

- [ ] Write `UpdatePortfolioUseCaseTest.kt`:
```kotlin
class UpdatePortfolioUseCaseTest {
    // Fake implementations
    private val fakeSettings = SettingsData(cmcApiKey = "k", mexcApiKey = "m", mexcApiSecret = "s", topCoinsLimit = 3)
    private val fakeSettingsRepo = object : SettingsRepository {
        override suspend fun getSettings() = fakeSettings
        override suspend fun saveSettings(settings: SettingsData) {}
    }
    private var savedCoins: List<CoinData>? = null
    private val fakePortfolioRepo = object : PortfolioRepository {
        override fun getPortfolio() = flow { emit(PortfolioData(emptyList(), 0.0)) }
        override suspend fun savePortfolio(coins: List<CoinData>) { savedCoins = coins }
    }

    @Test
    fun missingKeys_returnsFailure() = runTest {
        val emptySettingsRepo = object : SettingsRepository {
            override suspend fun getSettings() = SettingsData()
            override suspend fun saveSettings(settings: SettingsData) {}
        }
        val useCase = UpdatePortfolioUseCase(
            CheckSettingsUseCase(emptySettingsRepo),
            emptySettingsRepo, fakePortfolioRepo,
            fakeMexcService, fakeCmcService  // injected fakes
        )
        val result = useCase()
        assertTrue(result.isFailure)
    }
}
```

The full `UpdatePortfolioUseCase` test is harder to unit-test without mocking Retrofit services — write fakes for `MexcApiService` and `CmcApiService` interfaces. Focus on: missing keys returns failure, happy path saves coins.

- [ ] Create `domain/usecase/UpdatePortfolioUseCase.kt`:
```kotlin
class UpdatePortfolioUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepo: SettingsRepository,
    private val portfolioRepo: PortfolioRepository,
    private val mexcService: MexcApiService,
    private val cmcService: CmcApiService,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val settings = settingsRepo.getSettings()

        // Fetch all prices
        val prices = mexcService.getAllPrices()
            .associate { it.symbol to it.price.toDoubleOrNull() }

        // Fetch MEXC balances
        val timestamp = System.currentTimeMillis()
        // Note: build query string, sign it, pass timestamp + signature
        val account = mexcService.getAccount(timestamp, signQuery("timestamp=$timestamp", settings.mexcApiSecret))

        val coins = account.balances
            .filter { it.asset != "USDT" }
            .mapNotNull { balance ->
                val qty = balance.free.toDoubleOrNull() ?: 0.0
                val price = prices["${balance.asset}USDT"] ?: return@mapNotNull null
                if (qty * price < 0.01) return@mapNotNull null
                CoinData(symbol = balance.asset, priceUsdt = price, quantity = qty)
            }

        portfolioRepo.savePortfolio(coins)
    }

    private fun signQuery(query: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(query.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
```

- [ ] Commit: `git commit -m "feat: UpdatePortfolioUseCase"`

---

### Task 17: PortfolioScreen MVI

- [ ] Write `PortfolioReducerTest.kt`:
```kotlin
class PortfolioReducerTest {
    private val reducer = PortfolioReducer()

    @Test
    fun emptyPortfolio_mapsCorrectly() {
        val state = PortfolioStore.State(portfolio = PortfolioData(emptyList(), 0.0), isLoading = false)
        val ui = reducer.reduce(state)
        assertTrue(ui.coins.isEmpty())
        assertEquals(0.0, ui.totalUsdt, 0.001)
        assertFalse(ui.isLoading)
    }

    @Test
    fun loadingState_reflected() {
        val state = PortfolioStore.State(isLoading = true)
        assertTrue(reducer.reduce(state).isLoading)
    }
}
```

- [ ] Run — expect compile error

- [ ] Create `ui/screen/portfolio/PortfolioStore.kt`:
```kotlin
object PortfolioStore {
    data class State(
        val portfolio: PortfolioData = PortfolioData(emptyList(), 0.0),
        val isLoading: Boolean = false,
        val showSellSheet: Boolean = false,
        val sellAmountInput: String = "",
    ) : MviState

    data class UiState(
        val coins: List<CoinUi>,
        val totalUsdt: Double,
        val isLoading: Boolean,
        val showSellSheet: Boolean,
        val sellAmountInput: String,
    ) : MviUiState

    data class CoinUi(
        val symbol: String,
        val priceUsdt: String,
        val quantity: String,
        val totalPositionUsdt: String,
    )

    sealed interface Event : MviEvent {
        data object Update : Event
        data object Rebalance : Event
        data object OpenSellSheet : Event
        data object CloseSellSheet : Event
        data class SetSellAmount(val amount: String) : Event
        data class SetSellPercent(val percent: Float) : Event  // 0.25, 0.5, 0.75, 1.0
        data object Sell : Event
        data object NavigateToSettings : Event
    }

    sealed interface Effect : MviEffect {
        data class SetPortfolio(val portfolio: PortfolioData) : Effect
        data class SetLoading(val isLoading: Boolean) : Effect
        data class SetShowSellSheet(val show: Boolean) : Effect
        data class SetSellAmount(val amount: String) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }

    sealed interface SideEffect : MviSideEffect {
        data class ShowSnackbar(val message: String) : SideEffect
        data object NavigateToSettings : SideEffect
    }
}
```

- [ ] Create `ui/screen/portfolio/PortfolioReducer.kt`:
```kotlin
class PortfolioReducer @Inject constructor() : Reducer<PortfolioStore.State, PortfolioStore.UiState> {
    override fun reduce(state: PortfolioStore.State) = PortfolioStore.UiState(
        coins = state.portfolio.coins.map { coin ->
            PortfolioStore.CoinUi(
                symbol = coin.symbol,
                priceUsdt = "$%.4f".format(coin.priceUsdt),
                quantity = "%.6f".format(coin.quantity),
                totalPositionUsdt = "$%.2f".format(coin.totalPositionUsdt),
            )
        },
        totalUsdt = state.portfolio.totalUsdt,
        isLoading = state.isLoading,
        showSellSheet = state.showSellSheet,
        sellAmountInput = state.sellAmountInput,
    )
}
```

- [ ] Run `PortfolioReducerTest` — must pass

- [ ] Create `ui/screen/portfolio/PortfolioViewModel.kt`:
```kotlin
@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val getPortfolio: GetPortfolioUseCase,
    private val updatePortfolio: UpdatePortfolioUseCase,
    private val sell: SellUseCase,
    private val rebalancer: RebalancerUseCase,
    reducer: PortfolioReducer,
) : ScreenViewModel<State, Event, SideEffect, Effect, UiState>(reducer) {

    override fun createState() = State()

    init {
        // Collect live portfolio from Room
        viewModelScope.launch {
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
                if (result.isFailure) emit(Effect.ShowSnackbar(result.exceptionOrNull()?.message ?: "Update failed"))
            }
            Event.Rebalance -> {
                emit(Effect.SetLoading(true))
                val result = rebalancer()
                emit(Effect.SetLoading(false))
                if (result.isFailure) emit(Effect.ShowSnackbar(result.exceptionOrNull()?.message ?: "Rebalance failed"))
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
            Event.NavigateToSettings -> { /* handled via SideEffect below */ }
        }
    }

    override fun handleEffect(currentState: State, effect: Effect): State = when (effect) {
        is Effect.SetPortfolio -> currentState.copy(portfolio = effect.portfolio)
        is Effect.SetLoading -> currentState.copy(isLoading = effect.isLoading)
        is Effect.SetShowSellSheet -> currentState.copy(showSellSheet = effect.show)
        is Effect.SetSellAmount -> currentState.copy(sellAmountInput = effect.amount)
        is Effect.ShowSnackbar -> currentState.also {
            viewModelScope.launch { sendSideEffect(SideEffect.ShowSnackbar(effect.message)) }
        }
    }
}
```

- [ ] Commit: `git commit -m "feat: portfolio screen MVI"`

---

### Task 18: PortfolioScreen composables

- [ ] Create `ui/screen/portfolio/composable/PortfolioContent.kt`:
  - Top bar: title "Portfolio" + `IconButton` with gear icon → dispatches `Event.NavigateToSettings`
  - Header card: total portfolio value formatted as `$X,XXX.XX`
  - `LazyColumn` of coin cards — each shows symbol, price, quantity, total position
  - Bottom bar: 3 `Button`s — Update, Rebalance, Sell (all disabled when `isLoading`)
  - `LoadingOverlay` composable: full-screen semi-transparent box with `CircularProgressIndicator`, shown when `isLoading`
  - Sell `ModalBottomSheet`: USDT text field, row of 4 percentage buttons (25/50/75/100%), Sell button
  - Empty state text when `coins` is empty

- [ ] Create `ui/screen/portfolio/composable/PortfolioScreen.kt`:
```kotlin
@Composable
fun PortfolioScreen(viewModel: PortfolioViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nav = RootNavigation.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.sideEffect { effect ->
        when (effect) {
            is PortfolioStore.SideEffect.ShowSnackbar ->
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            PortfolioStore.SideEffect.NavigateToSettings ->
                nav?.add(SettingsScreen())
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        PortfolioContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(padding)
        )
    }
}
```

- [ ] Wire into `MainActivity` `entry<PortfolioScreen>` block
- [ ] Build and run — manually verify portfolio list, loading overlay, sell sheet, navigation to settings
- [ ] Commit: `git commit -m "feat: portfolio screen UI"`

---

## Chunk 6: Trading Operations

### Task 19: SellUseCase (TDD)

- [ ] Write `SellUseCaseTest.kt`:
```kotlin
class SellUseCaseTest {
    @Test
    fun missingKeys_returnsFailure() = runTest {
        val useCase = SellUseCase(CheckSettingsUseCase(fakeEmptySettingsRepo), ...)
        assertTrue(useCase(500.0).isFailure)
    }

    @Test
    fun zeroAmount_returnsFailure() = runTest {
        val useCase = makeSellUseCase(validSettings)
        assertTrue(useCase(0.0).isFailure)
    }
}
```

- [ ] Create `domain/usecase/SellUseCase.kt`:
```kotlin
class SellUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepo: SettingsRepository,
    private val portfolioRepo: PortfolioRepository,
    private val mexcService: MexcApiService,
) {
    suspend operator fun invoke(usdtAmount: Double): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")
        if (usdtAmount <= 0) throw IllegalArgumentException("Amount must be > 0")

        val settings = settingsRepo.getSettings()
        val portfolio = portfolioRepo.getPortfolio().first()

        if (portfolio.coins.isEmpty()) throw IllegalStateException("Portfolio is empty")

        // Sell proportionally: each coin's share = its weight in total portfolio
        portfolio.coins.forEach { coin ->
            val coinShare = coin.totalPositionUsdt / portfolio.totalUsdt
            val coinSellUsdt = usdtAmount * coinShare
            if (coinSellUsdt < 1.0) return@forEach  // skip tiny amounts

            val qty = coinSellUsdt / coin.priceUsdt
            val timestamp = System.currentTimeMillis()
            val queryString = "symbol=${coin.symbol}USDT&side=SELL&type=MARKET&quantity=${"%.6f".format(qty)}&timestamp=$timestamp"
            val signature = signQuery(queryString, settings.mexcApiSecret)
            mexcService.placeOrder(
                MexcOrderRequest(symbol = "${coin.symbol}USDT", side = "SELL", quantity = "%.6f".format(qty)),
                timestamp, signature
            )
        }
    }

    private fun signQuery(query: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(query.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
```

- [ ] Commit: `git commit -m "feat: SellUseCase"`

---

### Task 20: RebalancerUseCase (TDD)

The 4-step algorithm from `ANDROID_REFERENCE.md` / `Rebalancer.kt`.

- [ ] Write `RebalancerUseCaseTest.kt` — test the pure helper functions:
```kotlin
class RebalancerUseCaseTest {
    @Test
    fun buildAvailableCoins_excludesStablecoins() {
        val top = setOf("BTC", "ETH", "USDT", "SOL")
        val tradable = setOf("BTC", "ETH", "USDT", "SOL")
        val excluded = setOf("USDT", "USDC")
        val result = buildAvailableCoins(top, tradable, excluded)
        assertFalse("USDT" in result)
        assertTrue("BTC" in result)
    }

    @Test
    fun buildCoinsToSell_returnsCoinsNotInAvailable() {
        val mine = setOf("BTC", "ETH", "DOGE")
        val available = setOf("BTC", "ETH", "SOL")
        val excluded = emptySet<String>()
        val result = buildCoinsToSell(mine, available, excluded)
        assertTrue("DOGE" in result)
        assertFalse("BTC" in result)
    }
}
```

- [ ] Create `domain/usecase/RebalancerUseCase.kt` — port directly from `Rebalancer.kt` in `/Users/alexcemen/TradingBotTop100/src/main/kotlin/rebalancer/Rebalancer.kt`, adapting to Android (use Retrofit services instead of OkHttp, `suspend` coroutines instead of `runBlocking`, inject dependencies via Hilt)
- [ ] Key helper functions to extract as internal (testable) functions:
  - `buildAvailableCoins(topCmc: Set<String>, tradable: Set<String>, excluded: Set<String>): Set<String>`
  - `buildCoinsToSell(mine: Set<String>, available: Set<String>, excluded: Set<String>): Set<String>`
- [ ] Run `RebalancerUseCaseTest` — must pass
- [ ] Commit: `git commit -m "feat: RebalancerUseCase — 4-step rebalancing algorithm"`

---

## Chunk 7: Documentation

### Task 21: Write project documentation

Create `.claude/docs/en/` and `.claude/docs/ru/` with the following files. Base content on this spec and the patterns in this plan. English files are the source of truth; Russian files are translations.

- [ ] Create `CLAUDE.md` at project root (based on mandatory rules in spec)
- [ ] Create `.claude/docs/en/architecture.md` — module structure, MVI pattern, navigation, naming conventions
- [ ] Create `.claude/docs/en/features.md` — Update, Rebalance, Sell, Settings, key storage rules
- [ ] Create `.claude/docs/en/screens.md` — PortfolioScreen, SettingsScreen, composable breakdown
- [ ] Create `.claude/docs/en/di.md` — Hilt modules, scopes, all bindings
- [ ] Create `.claude/docs/en/data.md` — Room tables, domain models, repository interfaces, use cases
- [ ] Create `.claude/docs/en/testing.md` — test strategy, naming convention, priority order
- [ ] Create `.claude/docs/en/dependencies.md` — all libraries with exact versions
- [ ] Create `.claude/docs/en/ui-guidelines.md` — AppTheme, color tokens, typography tokens, design rules
- [ ] Create Russian copies in `.claude/docs/ru/`
- [ ] Move `ANDROID_REFERENCE.md` to `.claude/ANDROID_REFERENCE.md`
- [ ] Commit: `git commit -m "docs: add project knowledge base"`

---

## Final Verification

- [ ] Run all unit tests: `./gradlew :app:testDebugUnitTest` — all pass
- [ ] Run instrumented tests: `./gradlew connectedAndroidTest` — all pass
- [ ] Full manual smoke test on emulator:
  - [ ] Open app — portfolio screen shows (empty)
  - [ ] Open settings — all fields show, edit each field, verify persists after restart
  - [ ] Add and remove excluded coin chips
  - [ ] Tap Update without keys — snackbar "API keys not configured"
  - [ ] Enter valid test keys in settings, tap Update — loading overlay appears, coins load
  - [ ] Tap Sell — sheet opens, tap 50% — correct amount fills input, tap Sell
  - [ ] Tap Rebalance — loading overlay appears
  - [ ] Dark mode — all screens verified
- [ ] `./gradlew assembleDebug` — clean build
- [ ] Commit: `git commit -m "feat: crypto portfolio app complete"`
