package com.alexcemen.cryptoportfolio.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class ScreenViewModel<
        S : MviState,
        I : MviEvent,
        E : MviSideEffect,
        EF : MviEffect,
        UiState : MviUiState,
        >(
    private val reducer: Reducer<S, UiState>,
) : ViewModel() {
    private val log = Timber.tag(this::class.java.name)
    abstract fun createState(): S
    open val startIntent: List<I> = emptyList()

    private val _state = MutableStateFlow(createState())
    protected val state: StateFlow<S> = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<E>()
    val sideEffects: SharedFlow<E> = _sideEffects.asSharedFlow()

    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow<UiState>(
            reducer.reduce(state.value)
        )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val effects = MutableSharedFlow<EF>()

    init {
        viewModelScope.launch {
            state.collect { emitToUi(it) }
        }
        viewModelScope.launch {
            effects.collect {
                collectEffect(_state.value, it)
            }
        }
    }

    abstract fun handleEvent(currentState: S, intent: I): Flow<EF>
    abstract fun handleEffect(currentState: S, effect: EF): S

    fun onEvent(intent: I) {
        log.i("onEvent:: $intent")
        viewModelScope.launch {
            handleEvent(currentState = _state.value, intent).collect {
                effects.emit(it)
            }
        }
    }

    fun forceEffect(effect: EF) {
        log.i("forceEffect:: $effect")
        viewModelScope.launch {
            effects.emit(effect)
        }
    }

    protected suspend fun sendSideEffect(sideEffect: E) {
        log.i("sendSideEffect:: $sideEffect")
        _sideEffects.emit(sideEffect)
    }

    private suspend fun emitToUi(state: S) {
        log.i("emitToUi:: $state")
        _uiState.emit(
            reducer.reduce(state)
        )
    }

    private suspend fun collectEffect(state: S, effect: EF) {
        _state.emit(handleEffect(state, effect))
    }
}
