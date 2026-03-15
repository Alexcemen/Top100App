package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import top100app.shared.generated.resources.Res
import top100app.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ActionButtonsRow(isLoading: Boolean, onEvent: (PortfolioStore.Event) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton(stringResource(Res.string.action_update), Icons.Default.Refresh, !isLoading, Modifier.weight(1f)) {
            onEvent(PortfolioStore.Event.Update)
        }
        ActionButton(stringResource(Res.string.action_rebalance), Icons.Default.SwapVert, !isLoading, Modifier.weight(1f)) {
            onEvent(PortfolioStore.Event.Rebalance)
        }
        ActionButton(stringResource(Res.string.action_sell), Icons.AutoMirrored.Filled.CallMade, !isLoading, Modifier.weight(1f)) {
            onEvent(PortfolioStore.Event.OpenSellSheet)
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val contentAlpha = if (enabled) 1f else 0.4f
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = AppTheme.colors.background.secondaryTwo,
        onClick = onClick,
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colors.text.primary.copy(alpha = contentAlpha),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = AppTheme.textStyle.captionOne,
                color = AppTheme.colors.text.primary.copy(alpha = contentAlpha),
            )
        }
    }
}
