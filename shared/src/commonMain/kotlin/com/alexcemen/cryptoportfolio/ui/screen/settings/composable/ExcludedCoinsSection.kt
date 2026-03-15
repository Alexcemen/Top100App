package com.alexcemen.cryptoportfolio.ui.screen.settings.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import cryptoportfolio.shared.generated.resources.Res
import cryptoportfolio.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExcludedCoinsSection(
    excludedCoins: List<String>,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onAddCoin: (String) -> Unit,
    onRemoveCoin: (String) -> Unit,
) {
    var addCoinInput by remember(isEditing) { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background.secondaryTwo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            // consume taps so they don't reach the outside-click handler in SettingsContent
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {},
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = addCoinInput,
                        onValueChange = { addCoinInput = it.uppercase() },
                        placeholder = {
                            Text(
                                stringResource(Res.string.excluded_coins_add_placeholder),
                                style = AppTheme.textStyle.captionOne,
                                color = AppTheme.colors.text.placeholder,
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        singleLine = true,
                        textStyle = AppTheme.textStyle.bodyOne,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppTheme.colors.text.primary,
                            unfocusedTextColor = AppTheme.colors.text.primary,
                            focusedBorderColor = AppTheme.colors.text.primary,
                            unfocusedBorderColor = AppTheme.colors.text.placeholder,
                            cursorColor = AppTheme.colors.text.primary,
                        ),
                    )
                    IconButton(onClick = {
                        if (addCoinInput.isNotBlank()) {
                            onAddCoin(addCoinInput)
                            addCoinInput = ""
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(Res.string.cd_add_coin), tint = AppTheme.colors.text.primary)
                    }
                    IconButton(onClick = onCancelEdit) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.cd_cancel), tint = AppTheme.colors.text.placeholder)
                    }
                } else {
                    Text(
                        stringResource(Res.string.excluded_coins_title),
                        style = AppTheme.textStyle.captionOne,
                        color = AppTheme.colors.text.placeholder,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onStartEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.cd_edit_excluded_coins), tint = AppTheme.colors.text.placeholder)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(excludedCoins) { coin ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppTheme.colors.background.secondary,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(coin, style = AppTheme.textStyle.captionOne, color = AppTheme.colors.text.primary)
                            if (isEditing) {
                                Spacer(Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onRemoveCoin(coin) },
                                    modifier = Modifier.size(16.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(Res.string.cd_remove_coin, coin),
                                        modifier = Modifier.size(12.dp),
                                        tint = AppTheme.colors.text.placeholder,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
