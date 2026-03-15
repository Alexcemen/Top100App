package com.alexcemen.cryptoportfolio.ui.screen.settings.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
internal fun SettingRow(
    label: String,
    value: String,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var editText by remember(isEditing) { mutableStateOf(value) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background.secondaryTwo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = AppTheme.textStyle.captionOne,
                    color = AppTheme.colors.text.placeholder,
                )
                if (isEditing) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true,
                        textStyle = AppTheme.textStyle.bodyOne,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppTheme.colors.text.primary,
                            unfocusedTextColor = AppTheme.colors.text.primary,
                            focusedBorderColor = AppTheme.colors.text.primary,
                            unfocusedBorderColor = AppTheme.colors.text.placeholder,
                            focusedLabelColor = AppTheme.colors.text.primary,
                            unfocusedLabelColor = AppTheme.colors.text.placeholder,
                            cursorColor = AppTheme.colors.text.primary,
                        ),
                    )
                } else {
                    Text(
                        text = value.ifBlank { stringResource(Res.string.value_not_set) },
                        style = AppTheme.textStyle.bodyOne,
                        color = if (value.isBlank()) AppTheme.colors.text.placeholder else AppTheme.colors.text.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            if (isEditing) {
                IconButton(onClick = { onSave(editText) }) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(Res.string.cd_save), tint = AppTheme.colors.text.primary)
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.cd_cancel), tint = AppTheme.colors.text.placeholder)
                }
            } else {
                IconButton(onClick = onStartEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.cd_edit, label), tint = AppTheme.colors.text.placeholder)
                }
            }
        }
    }
}
