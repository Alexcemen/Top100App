package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.usecase.GetSettingsUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.SaveSettingsUseCase
import com.alexcemen.cryptoportfolio.ui.mvi.ScreenViewModel
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.EditingField
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.Effect
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.Event
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.SideEffect
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.State
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    reducer: SettingsReducer,
) : ScreenViewModel<State, Event, SideEffect, Effect, UiState>(reducer) {

    override fun createState() = State()
    override val startIntent = listOf(Event.Load)

    init {
        startIntent.forEach { onEvent(it) }
    }

    override fun handleEvent(currentState: State, intent: Event): Flow<Effect> = flow {
        when (intent) {
            Event.Load -> {
                val settings = getSettings()
                emit(Effect.SetSettings(settings))
            }
            is Event.StartEdit -> emit(Effect.SetEditingField(intent.field))
            Event.CancelEdit -> emit(Effect.SetEditingField(null))
            is Event.SaveField -> {
                val updated = applyFieldUpdate(currentState.settings, intent.field, intent.value)
                saveSettings(updated)
                emit(Effect.SetSettings(updated))
                emit(Effect.SetEditingField(null))
            }
            is Event.AddExcludedCoin -> {
                val coin = intent.coin.uppercase().trim()
                if (coin.isBlank() || coin in currentState.settings.excludedCoins) return@flow
                val updated = currentState.settings.copy(
                    excludedCoins = currentState.settings.excludedCoins + coin
                )
                saveSettings(updated)
                emit(Effect.SetSettings(updated))
            }
            is Event.RemoveExcludedCoin -> {
                val updated = currentState.settings.copy(
                    excludedCoins = currentState.settings.excludedCoins - intent.coin
                )
                saveSettings(updated)
                emit(Effect.SetSettings(updated))
            }
        }
    }

    override fun handleEffect(currentState: State, effect: Effect): State = when (effect) {
        is Effect.SetSettings -> currentState.copy(settings = effect.settings)
        is Effect.SetEditingField -> currentState.copy(editingField = effect.field)
        is Effect.ShowSnackbar -> currentState
    }

    private fun applyFieldUpdate(settings: SettingsData, field: EditingField, value: String) =
        when (field) {
            EditingField.CMC_KEY -> settings.copy(cmcApiKey = value)
            EditingField.MEXC_KEY -> settings.copy(mexcApiKey = value)
            EditingField.MEXC_SECRET -> settings.copy(mexcApiSecret = value)
            EditingField.TOP_COINS_LIMIT -> settings.copy(topCoinsLimit = value.toIntOrNull() ?: settings.topCoinsLimit)
            EditingField.EXCLUDED_COINS -> settings
        }
}
