package com.alexcemen.cryptoportfolio.data.network

private const val PROXY_BASE = "https://alexcementop100app.vercel.app/api/proxy"

actual fun cmcBaseUrl(): String = "$PROXY_BASE?url=https://pro-api.coinmarketcap.com/"
actual fun mexcBaseUrl(): String = "$PROXY_BASE?url=https://api.mexc.com/"
