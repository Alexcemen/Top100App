# Clean Architecture: Domain/Data Boundary Fix

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove all `data` layer imports from the `domain` layer by introducing `MexcRepository` and `CmcRepository` domain interfaces implemented in the `data` layer.

**Architecture:** Domain use cases currently inject `MexcApiService` and `CmcApiService` directly (data layer), and work with data DTOs and signing utilities. We introduce two new domain repository interfaces (`MexcRepository`, `CmcRepository`) that express what the domain needs in domain terms. The data layer provides implementations that handle all network details, DTOs, signing and constants internally.

**Tech Stack:** Kotlin, Hilt (ActivityRetainedComponent), Retrofit, Room (unchanged), Coroutines

---

## File Map

### Create
- `domain/model/AssetBalance.kt` — domain model: one coin/asset with quantity + price in USDT
- `domain/model/TradeSide.kt` — domain enum: BUY / SELL (replaces `data.network.OrderSide` in domain)
- `domain/repository/MexcRepository.kt` — domain interface: balances, tradable symbols, precisions, order placement
- `domain/repository/CmcRepository.kt` — domain interface: top coins by market cap
- `data/repository/MexcRepositoryImpl.kt` — implements MexcRepository, handles all signing/DTOs/constants internally
- `data/repository/CmcRepositoryImpl.kt` — implements CmcRepository

### Modify
- `di/RepositoryModule.kt` — bind two new implementations
- `domain/usecase/UpdatePortfolioUseCase.kt` — inject `MexcRepository` instead of `MexcApiService`
- `domain/usecase/SellUseCase.kt` — inject `MexcRepository` instead of `MexcApiService`; remove `SettingsRepository` (secret moved to impl)
- `domain/usecase/RebalancerUseCase.kt` — inject `MexcRepository` + `CmcRepository` instead of services; remove all data imports

### Unchanged
- `domain/usecase/RebalancerHelpers.kt` — no data imports, no changes needed
- `data/network/*` — services, DTOs, signing interceptor all stay as-is

---

## Task 1: Domain models — AssetBalance and TradeSide

**Files:**
- Create: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/domain/model/AssetBalance.kt`
- Create: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/domain/model/TradeSide.kt`

- [ ] **Step 1: Create AssetBalance.kt**

```kotlin
package com.alexcemen.cryptoportfolio.domain.model

data class AssetBalance(
    val symbol: String,
    val quantity: Double,
    val priceUsdt: Double,
) {
    val valueUsdt: Double get() = quantity * priceUsdt
}
```

- [ ] **Step 2: Create TradeSide.kt**

```kotlin
package com.alexcemen.cryptoportfolio.domain.model

enum class TradeSide { BUY, SELL }
```

- [ ] **Step 3: Build to verify no compile errors**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

---

## Task 2: Domain repository interfaces

**Files:**
- Create: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/domain/repository/MexcRepository.kt`
- Create: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/domain/repository/CmcRepository.kt`

- [ ] **Step 1: Create MexcRepository.kt**

```kotlin
package com.alexcemen.cryptoportfolio.domain.repository

import com.alexcemen.cryptoportfolio.domain.model.AssetBalance
import com.alexcemen.cryptoportfolio.domain.model.TradeSide

interface MexcRepository {
    /** All held assets (including USDT with priceUsdt = 1.0), free quantity only. */
    suspend fun getBalances(): List<AssetBalance>

    /** Base asset symbols that have a USDT trading pair on MEXC. */
    suspend fun getTradableSymbols(): Set<String>

    /** Map of baseAsset → decimal precision for quantity. */
    suspend fun getAssetPrecisions(): Map<String, Int>

    /** Place a market order (buy/sell) for a given USDT amount. Logs and swallows errors. */
    suspend fun placeMarketOrderByUsdt(symbol: String, side: TradeSide, usdtAmount: Double)

    /** Place a market sell order for an exact asset quantity. Logs and swallows errors. */
    suspend fun placeMarketSellByQty(symbol: String, qty: String)
}
```

- [ ] **Step 2: Create CmcRepository.kt**

```kotlin
package com.alexcemen.cryptoportfolio.domain.repository

interface CmcRepository {
    /** Returns top coin symbols ordered by market cap. */
    suspend fun getTopCoins(apiKey: String, limit: Int): List<String>
}
```

- [ ] **Step 3: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

---

## Task 3: MexcRepositoryImpl

**Files:**
- Create: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/data/repository/MexcRepositoryImpl.kt`

All network-specific logic lives here: `QUOTE_ASSET`, `signMexcQuery`, `OrderSide`, `ORDER_TYPE_MARKET`, DTO mapping.

- [ ] **Step 1: Create MexcRepositoryImpl.kt**

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
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class MexcRepositoryImpl @Inject constructor(
    private val mexcService: MexcApiService,
    private val settingsRepository: SettingsRepository,
) : MexcRepository {

    override suspend fun getBalances(): List<AssetBalance> {
        val prices = mexcService.getAllPrices()
            .associate { it.symbol to (it.price.toDoubleOrNull() ?: 0.0) }
        val secret = settingsRepository.getSettings().mexcApiSecret
        val timestamp = System.currentTimeMillis()
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
            val timestamp = System.currentTimeMillis()
            val quoteQty = usdtAmount.toString()
            val mexcSide = if (side == TradeSide.BUY) OrderSide.BUY else OrderSide.SELL
            val signature = signMexcQuery(
                query = "symbol=${symbol}$QUOTE_ASSET&side=${mexcSide.name}&type=$ORDER_TYPE_MARKET&quoteOrderQty=$quoteQty&timestamp=$timestamp",
                secret = secret,
            )
            Timber.d("placeMarketOrderByUsdt: ${symbol}$QUOTE_ASSET side=$side quoteOrderQty=$quoteQty")
            mexcService.placeOrder(
                symbol = "${symbol}$QUOTE_ASSET",
                side = mexcSide,
                type = ORDER_TYPE_MARKET,
                quoteOrderQty = quoteQty,
                timestamp = timestamp,
                signature = signature,
            )
        }.onFailure {
            val body = (it as? HttpException)?.response()?.errorBody()?.string()
            Timber.e("placeMarketOrderByUsdt failed: ${symbol}$QUOTE_ASSET side=$side error=${it.message} body=$body")
        }
    }

    override suspend fun placeMarketSellByQty(symbol: String, qty: String) {
        runCatching {
            val secret = settingsRepository.getSettings().mexcApiSecret
            val timestamp = System.currentTimeMillis()
            val signature = signMexcQuery(
                query = "symbol=${symbol}$QUOTE_ASSET&side=SELL&type=$ORDER_TYPE_MARKET&quantity=$qty&timestamp=$timestamp",
                secret = secret,
            )
            Timber.d("placeMarketSellByQty: ${symbol}$QUOTE_ASSET quantity=$qty")
            mexcService.placeOrderByQty(
                symbol = "${symbol}$QUOTE_ASSET",
                side = OrderSide.SELL,
                type = ORDER_TYPE_MARKET,
                quantity = qty,
                timestamp = timestamp,
                signature = signature,
            )
        }.onFailure {
            val body = (it as? HttpException)?.response()?.errorBody()?.string()
            Timber.e("placeMarketSellByQty failed: ${symbol}$QUOTE_ASSET error=${it.message} body=$body")
        }
    }
}
```

- [ ] **Step 2: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

---

## Task 4: CmcRepositoryImpl

**Files:**
- Create: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/data/repository/CmcRepositoryImpl.kt`

- [ ] **Step 1: Create CmcRepositoryImpl.kt**

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
}
```

- [ ] **Step 2: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

---

## Task 5: Bind new repositories in DI

**Files:**
- Modify: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/di/RepositoryModule.kt`

- [ ] **Step 1: Add bindings for MexcRepository and CmcRepository**

Replace the entire file:

```kotlin
package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.repository.CmcRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.MexcRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.PortfolioRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.SettingsRepositoryImpl
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
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
    @Binds abstract fun bindMexcRepository(impl: MexcRepositoryImpl): MexcRepository
    @Binds abstract fun bindCmcRepository(impl: CmcRepositoryImpl): CmcRepository
}
```

- [ ] **Step 2: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

---

## Task 6: Update UpdatePortfolioUseCase

**Files:**
- Modify: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/domain/usecase/UpdatePortfolioUseCase.kt`

Inject `MexcRepository` instead of `MexcApiService`. Remove all `data` imports.

- [ ] **Step 1: Rewrite UpdatePortfolioUseCase.kt**

```kotlin
package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdatePortfolioUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val mexcRepository: MexcRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val excludedCoins = settingsRepository.getSettings().excludedCoins
        val coins = mexcRepository.getBalances()
            .filter { it.symbol !in excludedCoins }
            .filter { it.valueUsdt >= 0.01 }
            .map { CoinData(symbol = it.symbol, priceUsdt = it.priceUsdt, quantity = it.quantity) }

        portfolioRepository.savePortfolio(coins)
    }
}
```

- [ ] **Step 2: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (no `data` imports remaining in this file)

---

## Task 7: Update SellUseCase

**Files:**
- Modify: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/domain/usecase/SellUseCase.kt`

Inject `MexcRepository` instead of `MexcApiService`. Remove `SettingsRepository` (signing is now in MexcRepositoryImpl). Remove all `data` imports.

- [ ] **Step 1: Rewrite SellUseCase.kt**

```kotlin
package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class SellUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val portfolioRepository: PortfolioRepository,
    private val mexcRepository: MexcRepository,
) {
    suspend operator fun invoke(usdtAmount: Double): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val portfolio = portfolioRepository.getPortfolio().first()
        if (portfolio.coins.isEmpty()) throw IllegalStateException("Portfolio is empty")

        val precisions = mexcRepository.getAssetPrecisions()

        portfolio.coins.forEach { coin ->
            val coinSellUsdt = usdtAmount * (coin.totalPositionUsdt / portfolio.totalUsdt)
            if (coinSellUsdt < 1.0) return@forEach
            val qty = BigDecimal(coinSellUsdt / coin.priceUsdt)
                .setScale(precisions[coin.symbol] ?: 8, RoundingMode.FLOOR)
                .toPlainString()
            mexcRepository.placeMarketSellByQty(coin.symbol, qty)
        }
    }
}
```

- [ ] **Step 2: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (no `data` imports remaining in this file)

---

## Task 8: Update RebalancerUseCase

**Files:**
- Modify: `app/src/main/kotlin/com/alexcemen/cryptoportfolio/domain/usecase/RebalancerUseCase.kt`

Inject `MexcRepository` + `CmcRepository` instead of services. Remove all `data` imports. Use `TradeSide` instead of `OrderSide`.

- [ ] **Step 1: Rewrite RebalancerUseCase.kt**

```kotlin
package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.TradeSide
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

private const val USDT = "USDT"

class RebalancerUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val mexcRepository: MexcRepository,
    private val cmcRepository: CmcRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val settings = settingsRepository.getSettings()

        val (topCmcList, tradableMexc) = coroutineScope {
            val topDeferred = async { cmcRepository.getTopCoins(settings.cmcApiKey, settings.topCoinsLimit) }
            val infoDeferred = async { mexcRepository.getTradableSymbols() }
            topDeferred.await() to infoDeferred.await()
        }
        val availableList = buildAvailableCoins(topCmcList, tradableMexc, settings.excludedCoins.toSet())

        val balances = mexcRepository.getBalances()
        val balancesInUsdt = balances
            .filter { it.symbol != USDT && it.valueUsdt > 1.0 }
            .associate { it.symbol to it.valueUsdt }
        val freeUsdt = balances.find { it.symbol == USDT }?.quantity ?: 0.0

        sellUnlistedCoins(balancesInUsdt, availableList)
        buyMissingCoins(balancesInUsdt, availableList, freeUsdt)
        rebalancePositions(balancesInUsdt, settings.excludedCoins, freeUsdt)
    }

    private suspend fun sellUnlistedCoins(
        balancesInUsdt: Map<String, Double>,
        availableList: List<String>,
    ) {
        val toSell = buildCoinsToSell(balancesInUsdt.keys, availableList)
        for (coin in toSell) {
            val value = balancesInUsdt[coin] ?: continue
            mexcRepository.placeMarketOrderByUsdt(coin, TradeSide.SELL, value)
        }
    }

    private suspend fun buyMissingCoins(
        balancesInUsdt: Map<String, Double>,
        availableList: List<String>,
        freeUsdt: Double,
    ) {
        val missingList = availableList.filter { it !in balancesInUsdt.keys }
        if (missingList.isEmpty()) return

        val averageValue = if (balancesInUsdt.isEmpty()) 0.0
        else balancesInUsdt.values.average().floor2()
        var remaining = freeUsdt

        for (coin in missingList) {
            val toBuy = minOf(averageValue, remaining).floor2()
            if (toBuy < 1.0) break
            mexcRepository.placeMarketOrderByUsdt(coin, TradeSide.BUY, toBuy)
            remaining -= toBuy
        }
    }

    private suspend fun rebalancePositions(
        balancesInUsdt: Map<String, Double>,
        excludedCoins: List<String>,
        freeUsdt: Double,
    ) {
        val eligible = balancesInUsdt.filter { (symbol, _) -> symbol !in excludedCoins }
        if (eligible.isEmpty()) return

        val target = ((eligible.values.sum() + freeUsdt) / eligible.size).floor2()

        for ((coin, value) in eligible) {
            val excess = (value - target).floor2()
            if (excess > 1.0) mexcRepository.placeMarketOrderByUsdt(coin, TradeSide.SELL, excess)
        }
        for ((coin, value) in eligible) {
            val deficit = (target - value).floor2()
            if (deficit > 1.0) mexcRepository.placeMarketOrderByUsdt(coin, TradeSide.BUY, deficit)
        }
    }
}
```

- [ ] **Step 2: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (no `data` imports remaining in this file)

---

## Task 9: Final verification

- [ ] **Step 1: Run all unit tests**

Run: `./gradlew :app:test`
Expected: BUILD SUCCESSFUL, all existing tests pass

- [ ] **Step 2: Verify no domain→data imports remain**

Run: `grep -r "data\.network\|data\.db" app/src/main/kotlin/com/alexcemen/cryptoportfolio/domain/`
Expected: no output

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/com/alexcemen/cryptoportfolio/
git commit -m "refactor: introduce MexcRepository and CmcRepository to fix domain/data boundary"
```
