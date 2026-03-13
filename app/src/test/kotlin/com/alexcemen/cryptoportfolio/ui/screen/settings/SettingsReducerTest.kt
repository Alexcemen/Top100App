package com.alexcemen.cryptoportfolio.ui.screen.settings

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsReducerTest {
    private val reducer = SettingsReducer()

    @Test
    fun loadedState_mapsToUiState() {
        val settings = SettingsData(cmcApiKey = "abc", topCoinsLimit = 30)
        val state = SettingsStore.State(settings = settings, isLoading = false)
        val uiState = reducer.reduce(state)
        assertEquals("abc", uiState.cmcApiKey)
        assertEquals(30, uiState.topCoinsLimit)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun loadingState_isReflected() {
        val state = SettingsStore.State(settings = SettingsData(), isLoading = true)
        val uiState = reducer.reduce(state)
        assertTrue(uiState.isLoading)
    }
}
