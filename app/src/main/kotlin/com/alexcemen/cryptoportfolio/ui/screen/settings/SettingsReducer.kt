package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.ui.mvi.Reducer
import javax.inject.Inject

class SettingsReducer @Inject constructor() :
    Reducer<SettingsStore.State, SettingsStore.UiState> {
    override fun reduce(state: SettingsStore.State) = SettingsStore.UiState(
        cmcApiKey = state.settings.cmcApiKey,
        mexcApiKey = state.settings.mexcApiKey,
        mexcApiSecret = state.settings.mexcApiSecret,
        topCoinsLimit = state.settings.topCoinsLimit,
        excludedCoins = state.settings.excludedCoins,
        editingField = state.editingField,
        isLoading = state.isLoading,
    )
}
