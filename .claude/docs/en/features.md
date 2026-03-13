# Features

## Update Portfolio
**Trigger**: User taps "Update" button on PortfolioScreen
**UseCase**: `UpdatePortfolioUseCase`
**Steps**:
1. Validate API keys (CheckSettingsUseCase)
2. Fetch all MEXC ticker prices
3. Fetch MEXC account balances (HMAC-signed request)
4. Filter out excluded coins and zero-value positions
5. Save to Room via PortfolioRepository

## Rebalance
**Trigger**: User taps "Rebalance" button
**UseCase**: `RebalancerUseCase` (4-step algorithm)
**Steps**:
1. Fetch CMC top N coins + MEXC tradable pairs in parallel
2. Build available coins (CMC ∩ MEXC − excluded)
3. Sell coins not in available list
4. Buy missing available coins (at average position size)
5. Rebalance existing: sell over-weight, buy under-weight

## Sell (Partial)
**Trigger**: User opens sell sheet, enters USDT amount, taps Sell
**UseCase**: `SellUseCase`
**Logic**: Proportional sell — each coin's share = its weight in total portfolio value
**Minimum**: Skip coins where sell amount < $1

## Settings
**Screen**: SettingsScreen with 5 editable fields
**Storage**: `EncryptedSharedPreferences` (AES256-GCM) — API keys NEVER stored in plain text
**Fields**: CMC API Key, MEXC API Key, MEXC API Secret, Top Coins Limit, Excluded Coins

## Key Storage Rules
- API keys → `EncryptedSharedPreferences` ONLY
- Uses `MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)` (security-crypto 1.0.0 API)
- File name: `crypto_secure_prefs`
