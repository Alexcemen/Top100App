package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.ui.mvi.MviEffect
import com.alexcemen.cryptoportfolio.ui.mvi.MviEvent
import com.alexcemen.cryptoportfolio.ui.mvi.MviSideEffect
import com.alexcemen.cryptoportfolio.ui.mvi.MviState
import com.alexcemen.cryptoportfolio.ui.mvi.MviUiState

object SettingsStore {
    data class State(
        val settings: SettingsData = SettingsData(),
        val editingField: EditingField? = null,
        val isLoading: Boolean = false,
    ) : MviState

    data class UiState(
        val cmcApiKey: String,
        val mexcApiKey: String,
        val mexcApiSecret: String,
        val topCoinsLimit: Int,
        val excludedCoins: List<String>,
        val editingField: EditingField?,
        val isLoading: Boolean,
    ) : MviUiState

    enum class EditingField { CMC_KEY, MEXC_KEY, MEXC_SECRET, TOP_COINS_LIMIT, EXCLUDED_COINS }

    sealed interface Event : MviEvent {
        data object Load : Event
        data class StartEdit(val field: EditingField) : Event
        data object CancelEdit : Event
        data class SaveField(val field: EditingField, val value: String) : Event
        data class AddExcludedCoin(val coin: String) : Event
        data class RemoveExcludedCoin(val coin: String) : Event
    }

    sealed interface Effect : MviEffect {
        data class SetSettings(val settings: SettingsData) : Effect
        data class SetEditingField(val field: EditingField?) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }

    sealed interface SideEffect : MviSideEffect {
        data class ShowSnackbar(val message: String) : SideEffect
    }
}
