package com.alexcemen.cryptoportfolio.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS — iOS uses swipe-to-go-back gesture natively
}
