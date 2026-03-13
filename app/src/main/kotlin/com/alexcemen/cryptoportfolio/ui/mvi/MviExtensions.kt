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
