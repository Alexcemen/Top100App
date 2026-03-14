package com.alexcemen.cryptoportfolio.ui.screen.settings.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsStore.UiState,
    onEvent: (SettingsStore.Event) -> Unit,
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    var addCoinInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rows 1-4
            SettingRow(
                label = "CMC API Key",
                value = uiState.cmcApiKey,
                isEditing = uiState.editingField == SettingsStore.EditingField.CMC_KEY,
                onStartEdit = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.CMC_KEY)) },
                onSave = { onEvent(SettingsStore.Event.SaveField(SettingsStore.EditingField.CMC_KEY, it)) },
                onCancel = { onEvent(SettingsStore.Event.CancelEdit) },
            )
            SettingRow(
                label = "MEXC API Key",
                value = uiState.mexcApiKey,
                isEditing = uiState.editingField == SettingsStore.EditingField.MEXC_KEY,
                onStartEdit = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.MEXC_KEY)) },
                onSave = { onEvent(SettingsStore.Event.SaveField(SettingsStore.EditingField.MEXC_KEY, it)) },
                onCancel = { onEvent(SettingsStore.Event.CancelEdit) },
            )
            SettingRow(
                label = "MEXC API Secret",
                value = uiState.mexcApiSecret,
                isEditing = uiState.editingField == SettingsStore.EditingField.MEXC_SECRET,
                onStartEdit = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.MEXC_SECRET)) },
                onSave = { onEvent(SettingsStore.Event.SaveField(SettingsStore.EditingField.MEXC_SECRET, it)) },
                onCancel = { onEvent(SettingsStore.Event.CancelEdit) },
            )
            SettingRow(
                label = "Top Coins Limit",
                value = uiState.topCoinsLimit.toString(),
                isEditing = uiState.editingField == SettingsStore.EditingField.TOP_COINS_LIMIT,
                onStartEdit = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.TOP_COINS_LIMIT)) },
                onSave = { onEvent(SettingsStore.Event.SaveField(SettingsStore.EditingField.TOP_COINS_LIMIT, it)) },
                onCancel = { onEvent(SettingsStore.Event.CancelEdit) },
            )

            // Row 5: Excluded Coins
            val isEditingExcluded = uiState.editingField == SettingsStore.EditingField.EXCLUDED_COINS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Excluded Coins", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                if (!isEditingExcluded) {
                    IconButton(onClick = { onEvent(SettingsStore.Event.StartEdit(SettingsStore.EditingField.EXCLUDED_COINS)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit excluded coins")
                    }
                } else {
                    IconButton(onClick = { onEvent(SettingsStore.Event.CancelEdit) }) {
                        Icon(Icons.Default.Close, contentDescription = "Done editing")
                    }
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.excludedCoins) { coin ->
                    if (isEditingExcluded) {
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(coin) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { onEvent(SettingsStore.Event.RemoveExcludedCoin(coin)) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove $coin")
                                }
                            }
                        )
                    } else {
                        AssistChip(onClick = {}, label = { Text(coin) })
                    }
                }
                if (isEditingExcluded) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = addCoinInput,
                                onValueChange = { addCoinInput = it.uppercase() },
                                placeholder = { Text("Add coin") },
                                modifier = Modifier.width(120.dp),
                                singleLine = true,
                            )
                            IconButton(onClick = {
                                if (addCoinInput.isNotBlank()) {
                                    onEvent(SettingsStore.Event.AddExcludedCoin(addCoinInput))
                                    addCoinInput = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add coin")
                            }
                        }
                    }
                }
            }
        }
    }
}

private val previewSettingsUiState = SettingsStore.UiState(
    cmcApiKey = "abc-123-def",
    mexcApiKey = "mexc-key-xyz",
    mexcApiSecret = "mexc-secret-xyz",
    topCoinsLimit = 20,
    excludedCoins = listOf("USDT", "USDC", "BUSD"),
    editingField = null,
    isLoading = false,
)

@Preview(name = "Settings — default", showBackground = true)
@Composable
private fun SettingsContentPreview() {
    MaterialTheme {
        SettingsContent(
            uiState = previewSettingsUiState,
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(name = "Settings — editing CMC key", showBackground = true)
@Composable
private fun SettingsContentEditingPreview() {
    MaterialTheme {
        SettingsContent(
            uiState = previewSettingsUiState.copy(editingField = SettingsStore.EditingField.CMC_KEY),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(name = "Settings — loading", showBackground = true)
@Composable
private fun SettingsContentLoadingPreview() {
    MaterialTheme {
        SettingsContent(
            uiState = previewSettingsUiState.copy(isLoading = true),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(name = "SettingRow — view mode", showBackground = true)
@Composable
private fun SettingRowViewPreview() {
    MaterialTheme {
        SettingRow(label = "CMC API Key", value = "abc-123-def", isEditing = false, onStartEdit = {}, onSave = {}, onCancel = {})
    }
}

@Preview(name = "SettingRow — edit mode", showBackground = true)
@Composable
private fun SettingRowEditPreview() {
    MaterialTheme {
        SettingRow(label = "CMC API Key", value = "abc-123-def", isEditing = true, onStartEdit = {}, onSave = {}, onCancel = {})
    }
}

@Composable
private fun SettingRow(
    label: String,
    value: String,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var editText by remember(isEditing) { mutableStateOf(value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.4f)
        )
        if (isEditing) {
            OutlinedTextField(
                value = editText,
                onValueChange = { editText = it },
                modifier = Modifier.weight(0.6f),
                singleLine = true,
            )
            IconButton(onClick = { onSave(editText) }) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }
        } else {
            Text(
                text = value.ifBlank { "(not set)" },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(0.6f),
            )
            IconButton(onClick = onStartEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit $label")
            }
        }
    }
}
