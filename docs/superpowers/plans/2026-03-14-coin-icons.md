# Coin Icons Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Display real coin icons (from CoinMarketCap CDN) in portfolio coin list, with colored-letter avatar as fallback.

**Architecture:** `UpdatePortfolioUseCase` already calls MEXC for balances; we add a best-effort CMC `listings/latest` call to get symbol→id mappings, store `cmcId` in Room (new nullable column, migration v1→v2), derive the icon URL at repository boundary, and thread it through to a Coil `AsyncImage` in `CoinAvatar`. The CMC call never breaks the update — it falls back to `emptyMap()` silently.

**Tech Stack:** Coil 2.7.0 (coil-compose), Room migration, CoinMarketCap CDN `https://s2.coinmarketcap.com/static/img/coins/64x64/{id}.png`

---

## File Map

| Action | File | What changes |
|---|---|---|
| Modify | `gradle/libs.versions.toml` | Add `coil = "2.7.0"` + library alias |
| Modify | `app/build.gradle.kts` | Add `implementation(libs.coil.compose)` |
| Modify | `data/network/dto/CmcListingsResponse.kt` | Add `val id: Int` to `CmcCoinDto` |
| Modify | `domain/repository/CmcRepository.kt` | Add `getCoinIds(apiKey, limit): Map<String, Int>` |
| Modify | `data/repository/CmcRepositoryImpl.kt` | Implement `getCoinIds` |
| Modify | `domain/model/CoinData.kt` | Add `val logoUrl: String? = null` |
| Modify | `data/db/entity/CoinEntity.kt` | Add `val cmcId: Int? = null` |
| Modify | `data/db/AppDatabase.kt` | Bump version 1 → 2 |
| Modify | `di/DatabaseModule.kt` | Add `MIGRATION_1_2`, wire into builder |
| Modify | `data/repository/PortfolioRepositoryImpl.kt` | Map `cmcId` in `toDomain()`/`toEntity()` |
| Modify | `domain/usecase/UpdatePortfolioUseCase.kt` | Inject `CmcRepository`, best-effort icon lookup |
| Modify | `ui/screen/portfolio/PortfolioStore.kt` | Add `val logoUrl: String?` to `CoinUi` |
| Modify | `ui/screen/portfolio/PortfolioReducer.kt` | Map `logoUrl` from `CoinData` |
| Modify | `ui/screen/portfolio/composable/PortfolioContent.kt` | `CoinAvatar` uses Coil `AsyncImage` |
| Modify | `test/.../UpdatePortfolioUseCaseTest.kt` | Add fake `CmcRepository`, assert `logoUrl` set |
| Modify | `test/.../PortfolioReducerTest.kt` | Assert `logoUrl` mapped through |

---

## Chunk 1: Dependencies + CMC data layer

### Task 1: Add Coil dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add Coil to version catalog**

In `gradle/libs.versions.toml`, under `[versions]`:
```toml
coil = "2.7.0"
```
Under `[libraries]`:
```toml
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

- [ ] **Step 2: Add dependency to app module**

In `app/build.gradle.kts`, after `implementation(libs.androidx.material.icons.extended)`:
```kotlin
implementation(libs.coil.compose)
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**
```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build: add Coil 2.7.0 for async coin icon loading"
```

---

### Task 2: Extend CMC DTO and repository

**Files:**
- Modify: `data/network/dto/CmcListingsResponse.kt`
- Modify: `domain/repository/CmcRepository.kt`
- Modify: `data/repository/CmcRepositoryImpl.kt`

**Context:** CMC `listings/latest` already returns `id` (integer) per coin in the JSON response — we just weren't mapping it. `CmcRepositoryImpl` can reuse the existing `cmcService.getListings()` call.

- [ ] **Step 1: Add `id` to DTO**

Replace `data/network/dto/CmcListingsResponse.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.network.dto

data class CmcListingsResponse(val data: List<CmcCoinDto>)
data class CmcCoinDto(val id: Int, val symbol: String)
```

- [ ] **Step 2: Add interface method**

Replace `domain/repository/CmcRepository.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.repository

interface CmcRepository {
    /** Returns top coin symbols ordered by market cap. */
    suspend fun getTopCoins(apiKey: String, limit: Int): List<String>

    /** Returns symbol → CMC id map for the top [limit] coins by market cap. */
    suspend fun getCoinIds(apiKey: String, limit: Int): Map<String, Int>
}
```

- [ ] **Step 3: Implement in CmcRepositoryImpl**

Replace `data/repository/CmcRepositoryImpl.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import javax.inject.Inject

class CmcRepositoryImpl @Inject constructor(
    private val cmcService: CmcApiService,
) : CmcRepository {
    override suspend fun getTopCoins(apiKey: String, limit: Int): List<String> =
        cmcService.getListings(apiKey, limit).data.map { it.symbol }

    override suspend fun getCoinIds(apiKey: String, limit: Int): Map<String, Int> =
        cmcService.getListings(apiKey, limit).data.associate { it.symbol to it.id }
}
```

- [ ] **Step 4: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**
```bash
git add data/network/dto/CmcListingsResponse.kt \
        domain/repository/CmcRepository.kt \
        data/repository/CmcRepositoryImpl.kt
git commit -m "feat: add CMC symbol→id mapping to CmcRepository"
```
*(paths are relative to `app/src/main/kotlin/com/alexcemen/cryptoportfolio/`)*

---

## Chunk 2: Domain model + Room migration

### Task 3: Extend CoinData domain model

**Files:**
- Modify: `domain/model/CoinData.kt`

- [ ] **Step 1: Add `logoUrl` field**

Replace `domain/model/CoinData.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.model

data class CoinData(
    val symbol: String,
    val priceUsdt: Double,
    val quantity: Double,
    val logoUrl: String? = null,
) {
    val totalPositionUsdt: Double get() = priceUsdt * quantity
}
```

- [ ] **Step 2: Verify all existing tests still compile and pass**

Run: `./gradlew :app:test`
Expected: `BUILD SUCCESSFUL`, all tests green. (`logoUrl` has a default value so no existing call sites break.)

- [ ] **Step 3: Commit**
```bash
git add domain/model/CoinData.kt
git commit -m "feat: add logoUrl to CoinData domain model"
```

---

### Task 4: Add cmcId column to Room + migration

**Files:**
- Modify: `data/db/entity/CoinEntity.kt`
- Modify: `data/db/AppDatabase.kt`
- Modify: `di/DatabaseModule.kt`
- Modify: `data/repository/PortfolioRepositoryImpl.kt`

**Context:** Room requires an explicit migration when the schema changes. We add a nullable `cmcId INTEGER` column. SQLite's `ALTER TABLE ... ADD COLUMN` sets NULL for all existing rows — that's fine, since `cmcId: Int?` is nullable. The migration must be added to the Room builder or Room will crash on open.

- [ ] **Step 1: Add `cmcId` to CoinEntity**

Replace `data/db/entity/CoinEntity.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_table")
data class CoinEntity(
    @PrimaryKey val symbol: String,
    val priceUsdt: Double,
    val quantity: Double,
    val cmcId: Int? = null,
)
```

- [ ] **Step 2: Bump database version to 2**

Replace `data/db/AppDatabase.kt`:
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

- [ ] **Step 3: Add migration + wire it into the builder**

Replace `di/DatabaseModule.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import com.alexcemen.cryptoportfolio.data.db.PortfolioDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE portfolio_table ADD COLUMN cmcId INTEGER")
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object DatabaseModule {

    @Provides
    @ActivityRetainedScoped
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "crypto_portfolio.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun providePortfolioDao(db: AppDatabase): PortfolioDao = db.portfolioDao()
}
```

- [ ] **Step 4: Map cmcId through PortfolioRepositoryImpl**

Replace `data/repository/PortfolioRepositoryImpl.kt`:
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

- [ ] **Step 5: Verify compilation and tests**

Run: `./gradlew :app:compileDebugKotlin :app:test`
Expected: `BUILD SUCCESSFUL`, all tests green.

- [ ] **Step 6: Commit**
```bash
git add data/db/entity/CoinEntity.kt \
        data/db/AppDatabase.kt \
        di/DatabaseModule.kt \
        data/repository/PortfolioRepositoryImpl.kt
git commit -m "feat: add cmcId to Room schema with v1→v2 migration"
```

---

## Chunk 3: Use case + UI

### Task 5: UpdatePortfolioUseCase — best-effort CMC icon lookup (TDD)

**Files:**
- Modify: `domain/usecase/UpdatePortfolioUseCase.kt`
- Modify: `test/.../UpdatePortfolioUseCaseTest.kt`

**Context:** We inject `CmcRepository` and call `getCoinIds(apiKey, limit=500)`. We wrap this in `runCatching` so a failed/missing CMC key never breaks the portfolio update — coins just get `logoUrl = null`. The limit 500 ensures we cover virtually any coin in a real portfolio.

- [ ] **Step 1: Write failing tests**

Add to `UpdatePortfolioUseCaseTest.kt` — add `fakeCmcRepo` field and two new test methods:

```kotlin
// Add import at top
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository

// Add field alongside fakeMexcRepository
private val fakeCmcRepo = object : CmcRepository {
    override suspend fun getTopCoins(apiKey: String, limit: Int) = listOf("ETH")
    override suspend fun getCoinIds(apiKey: String, limit: Int) = mapOf("ETH" to 1027)
}

private val emptyCmcRepo = object : CmcRepository {
    override suspend fun getTopCoins(apiKey: String, limit: Int) = emptyList<String>()
    override suspend fun getCoinIds(apiKey: String, limit: Int) = emptyMap<String, Int>()
}
```

New test methods:
```kotlin
@Test
fun happyPath_logoUrlIsSet() = runTest {
    val useCase = UpdatePortfolioUseCase(
        checkSettings = CheckSettingsUseCase(validSettingsRepo),
        settingsRepository = validSettingsRepo,
        portfolioRepository = fakePortfolioRepo,
        mexcRepository = fakeMexcRepository,
        cmcRepository = fakeCmcRepo,
    )
    useCase()
    val eth = savedCoins!!.first { it.symbol == "ETH" }
    assertEquals(
        "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
        eth.logoUrl
    )
}

@Test
fun cmcReturnsNoIds_logoUrlIsNull() = runTest {
    val useCase = UpdatePortfolioUseCase(
        checkSettings = CheckSettingsUseCase(validSettingsRepo),
        settingsRepository = validSettingsRepo,
        portfolioRepository = fakePortfolioRepo,
        mexcRepository = fakeMexcRepository,
        cmcRepository = emptyCmcRepo,
    )
    useCase()
    assertTrue(savedCoins!!.all { it.logoUrl == null })
}
```

Also update the existing `happyPath_savesCoins` and `missingKeys_returnsFailure` tests to pass `cmcRepository = fakeCmcRepo` (new constructor parameter).

- [ ] **Step 2: Run tests — verify they fail**

Run: `./gradlew :app:test --tests "*.UpdatePortfolioUseCaseTest"`
Expected: compile error — `UpdatePortfolioUseCase` doesn't have `cmcRepository` param yet.

- [ ] **Step 3: Implement**

Replace `domain/usecase/UpdatePortfolioUseCase.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import javax.inject.Inject

private const val CMC_ICON_FETCH_LIMIT = 500

class UpdatePortfolioUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val mexcRepository: MexcRepository,
    private val cmcRepository: CmcRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val settings = settingsRepository.getSettings()
        val excludedCoins = settings.excludedCoins

        val cmcIds: Map<String, Int> = runCatching {
            cmcRepository.getCoinIds(settings.cmcApiKey, CMC_ICON_FETCH_LIMIT)
        }.getOrDefault(emptyMap())

        val coins = mexcRepository.getBalances()
            .filter { it.symbol !in excludedCoins }
            .filter { it.valueUsdt >= 0.01 }
            .map { balance ->
                val cmcId = cmcIds[balance.symbol]
                CoinData(
                    symbol = balance.symbol,
                    priceUsdt = balance.priceUsdt,
                    quantity = balance.quantity,
                    logoUrl = cmcId?.let {
                        "https://s2.coinmarketcap.com/static/img/coins/64x64/$it.png"
                    },
                )
            }

        portfolioRepository.savePortfolio(coins)
    }
}
```

- [ ] **Step 4: Run tests — verify they pass**

Run: `./gradlew :app:test --tests "*.UpdatePortfolioUseCaseTest"`
Expected: all 4 tests GREEN.

- [ ] **Step 5: Commit**
```bash
git add domain/usecase/UpdatePortfolioUseCase.kt \
        test/.../UpdatePortfolioUseCaseTest.kt
git commit -m "feat: fetch CMC icon IDs during portfolio update (best-effort)"
```

---

### Task 6: PortfolioStore + Reducer — thread logoUrl to UI (TDD)

**Files:**
- Modify: `ui/screen/portfolio/PortfolioStore.kt`
- Modify: `ui/screen/portfolio/PortfolioReducer.kt`
- Modify: `test/.../PortfolioReducerTest.kt`

- [ ] **Step 1: Write failing test**

Add to `PortfolioReducerTest.kt`:
```kotlin
@Test
fun logoUrl_mappedThroughToCoinUi() {
    val coin = CoinData(
        symbol = "ETH",
        priceUsdt = 2000.0,
        quantity = 1.0,
        logoUrl = "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
    )
    val state = PortfolioStore.State(
        portfolio = PortfolioData(listOf(coin), coin.totalPositionUsdt),
    )
    val ui = reducer.reduce(state)
    assertEquals(
        "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
        ui.coins[0].logoUrl
    )
}

@Test
fun logoUrl_nullWhenNotSet() {
    val coin = CoinData(symbol = "BTC", priceUsdt = 60000.0, quantity = 0.1)
    val state = PortfolioStore.State(
        portfolio = PortfolioData(listOf(coin), coin.totalPositionUsdt),
    )
    val ui = reducer.reduce(state)
    assertNull(ui.coins[0].logoUrl)
}
```

Add `import org.junit.Assert.assertNull` at the top.

- [ ] **Step 2: Run tests — verify they fail**

Run: `./gradlew :app:test --tests "*.PortfolioReducerTest"`
Expected: compile error — `CoinUi` doesn't have `logoUrl` yet.

- [ ] **Step 3: Add `logoUrl` to CoinUi**

In `ui/screen/portfolio/PortfolioStore.kt`, update `CoinUi`:
```kotlin
data class CoinUi(
    val symbol: String,
    val priceUsdt: String,
    val quantity: String,
    val totalPositionUsdt: String,
    val logoUrl: String? = null,
)
```

- [ ] **Step 4: Map logoUrl in Reducer**

Replace `ui/screen/portfolio/PortfolioReducer.kt`:
```kotlin
package com.alexcemen.cryptoportfolio.ui.screen.portfolio

import com.alexcemen.cryptoportfolio.ui.mvi.Reducer
import java.util.Locale
import javax.inject.Inject

class PortfolioReducer @Inject constructor() : Reducer<PortfolioStore.State, PortfolioStore.UiState> {
    override fun reduce(state: PortfolioStore.State) = PortfolioStore.UiState(
        coins = state.portfolio.coins.sortedByDescending { it.totalPositionUsdt }.map { coin ->
            PortfolioStore.CoinUi(
                symbol = coin.symbol,
                priceUsdt = "$%.4f".format(Locale.US, coin.priceUsdt),
                quantity = "%.6f".format(Locale.US, coin.quantity),
                totalPositionUsdt = "$%.2f".format(Locale.US, coin.totalPositionUsdt),
                logoUrl = coin.logoUrl,
            )
        },
        totalUsdt = state.portfolio.totalUsdt,
        isLoading = state.isLoading,
        showSellSheet = state.showSellSheet,
        sellAmountInput = state.sellAmountInput,
    )
}
```

- [ ] **Step 5: Run all tests**

Run: `./gradlew :app:test`
Expected: all tests GREEN.

- [ ] **Step 6: Commit**
```bash
git add ui/screen/portfolio/PortfolioStore.kt \
        ui/screen/portfolio/PortfolioReducer.kt \
        test/.../PortfolioReducerTest.kt
git commit -m "feat: thread logoUrl from CoinData to CoinUi via reducer"
```

---

### Task 7: CoinAvatar — Coil AsyncImage with letter fallback

**Files:**
- Modify: `ui/screen/portfolio/composable/PortfolioContent.kt`

**Design:** The colored circle with first letter is always rendered. When `logoUrl != null`, a `32.dp` `AsyncImage` is overlaid on top (centered). CMC icons are PNGs — most have transparent backgrounds, so the colored circle shows at the edges. If the image fails to load, the letter remains visible.

- [ ] **Step 1: Update `CoinAvatar` signature and implementation**

In `PortfolioContent.kt`:

1. Add imports:
```kotlin
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.size   // already present
import androidx.compose.ui.draw.clip             // already present via foundation
```

2. Update `CoinListItem` to pass `logoUrl`:
```kotlin
@Composable
private fun CoinListItem(coin: PortfolioStore.CoinUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinAvatar(coin.symbol, coin.logoUrl)   // <-- add logoUrl
        // ... rest unchanged
    }
}
```

3. Replace `CoinAvatar`:
```kotlin
@Composable
private fun CoinAvatar(symbol: String, logoUrl: String?) {
    val color = avatarColors[abs(symbol.hashCode()) % avatarColors.size]
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            symbol.first().uppercaseChar().toString(),
            style = AppTheme.textStyle.subtitleOne,
            color = Color.White,
        )
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = symbol,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
```

Also update preview `CoinUi` instances to include `logoUrl = null` (no change needed — default is null).

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`, no warnings.

- [ ] **Step 3: Commit**
```bash
git add ui/screen/portfolio/composable/PortfolioContent.kt
git commit -m "feat: show real coin icons in CoinAvatar via Coil AsyncImage"
```

---

## Verification Checklist

After all tasks complete:

- [ ] `./gradlew :app:test` — all unit tests pass
- [ ] Build and install on device/emulator
- [ ] Tap **Update** — portfolio loads, coins show colored circles initially, then icons fade in
- [ ] Icons appear for common coins (BTC, ETH, SOL, etc.)
- [ ] Unknown/new coins fall back gracefully to colored letter avatar
- [ ] Rotate device / kill process and reopen — icons still appear (served from Room + Coil disk cache)
- [ ] Settings screen unaffected (no visual regression)
- [ ] Downgrade test: install v1 DB app → update to v2 → app opens without crash (Room migration runs)
