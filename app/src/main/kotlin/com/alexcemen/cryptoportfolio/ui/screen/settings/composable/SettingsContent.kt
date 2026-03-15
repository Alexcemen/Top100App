package com.alexcemen.cryptoportfolio.ui.screen.settings.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme

@Composable
fun SettingsContent(
    uiState: SettingsStore.UiState,
    onEvent: (SettingsStore.Event) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val isExcludedCoinsEditing = uiState.editingField == SettingsStore.EditingField.EXCLUDED_COINS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background.basic)
            .then(
                if (isExcludedCoinsEditing) Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onEvent(SettingsStore.Event.CancelEdit) }
                else Modifier
            ),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SettingRow(
                    label = "CMC API Key",
                    value = uiState.cmcApiKey,
                    isEditing = uiState.editingField == SettingsStore.EditingField.CMC_KEY,
                    onStartEdit = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.CMC_KEY)) },
                    onSave = { onEvent(SettingsStore.Event.SaveField(SettingsStore.EditingField.CMC_KEY, it)) },
                    onCancel = { onEvent(SettingsStore.Event.CancelEdit) },
                )
            }
            item {
                SettingRow(
                    label = "MEXC API Key",
                    value = uiState.mexcApiKey,
                    isEditing = uiState.editingField == SettingsStore.EditingField.MEXC_KEY,
                    onStartEdit = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.MEXC_KEY)) },
                    onSave = { onEvent(SettingsStore.Event.SaveField(SettingsStore.EditingField.MEXC_KEY, it)) },
                    onCancel = { onEvent(SettingsStore.Event.CancelEdit) },
                )
            }
            item {
                SettingRow(
                    label = "MEXC API Secret",
                    value = uiState.mexcApiSecret,
                    isEditing = uiState.editingField == SettingsStore.EditingField.MEXC_SECRET,
                    onStartEdit = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.MEXC_SECRET)) },
                    onSave = { onEvent(SettingsStore.Event.SaveField(SettingsStore.EditingField.MEXC_SECRET, it)) },
                    onCancel = { onEvent(SettingsStore.Event.CancelEdit) },
                )
            }
            item {
                SettingRow(
                    label = "Top Coins Limit",
                    value = uiState.topCoinsLimit.toString(),
                    isEditing = uiState.editingField == SettingsStore.EditingField.TOP_COINS_LIMIT,
                    onStartEdit = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.TOP_COINS_LIMIT)) },
                    onSave = { onEvent(SettingsStore.Event.SaveField(SettingsStore.EditingField.TOP_COINS_LIMIT, it)) },
                    onCancel = { onEvent(SettingsStore.Event.CancelEdit) },
                )
            }
            item {
                ExcludedCoinsSection(
                    excludedCoins = uiState.excludedCoins,
                    isEditing = uiState.editingField == SettingsStore.EditingField.EXCLUDED_COINS,
                    onStartEdit = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.EXCLUDED_COINS)) },
                    onCancelEdit = { onEvent(SettingsStore.Event.CancelEdit) },
                    onAddCoin = { onEvent(SettingsStore.Event.AddExcludedCoin(it)) },
                    onRemoveCoin = { onEvent(SettingsStore.Event.RemoveExcludedCoin(it)) },
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.4f),
                ) {}
                CircularProgressIndicator(color = AppTheme.colors.text.primary)
            }
        }
    }
}

private val previewUiState = SettingsStore.UiState(
    cmcApiKey = "abc-123-def",
    mexcApiKey = "mexc-key-xyz",
    mexcApiSecret = "mexc-secret-xyz",
    topCoinsLimit = 20,
    excludedCoins = listOf("USDT", "USDC", "BUSD"),
    editingField = null,
    isLoading = false,
)

@Preview(name = "Settings — default", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SettingsContentPreview() {
    AppTheme(themeDark = true) {
        SettingsContent(uiState = previewUiState, onEvent = {})
    }
}

@Preview(name = "Settings — editing CMC key", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SettingsContentEditingPreview() {
    AppTheme(themeDark = true) {
        SettingsContent(
            uiState = previewUiState.copy(editingField = SettingsStore.EditingField.CMC_KEY),
            onEvent = {},
        )
    }
}

@Preview(name = "Settings — loading", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SettingsContentLoadingPreview() {
    AppTheme(themeDark = true) {
        SettingsContent(
            uiState = previewUiState.copy(isLoading = true),
            onEvent = {},
        )
    }
}
