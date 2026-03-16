package com.alexcemen.cryptoportfolio.ui.mvi

interface MviState
interface MviEvent
interface MviEffect
interface MviSideEffect
interface MviUiState

interface Reducer<S : MviState, UiState : MviUiState> {
    fun reduce(state: S): UiState
}
