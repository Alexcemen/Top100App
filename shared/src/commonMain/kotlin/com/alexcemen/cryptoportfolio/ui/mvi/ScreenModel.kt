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
