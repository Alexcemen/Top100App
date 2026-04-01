package com.alexcemen.cryptoportfolio.data.network

private const val PROXY_BASE = "https://alexcementop100app-kappa.vercel.app/api"

actual fun cmcBaseUrl(): String = "$PROXY_BASE/cmc/"
actual fun mexcBaseUrl(): String = "$PROXY_BASE/mexc/"
