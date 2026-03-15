package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.R
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme

@Composable
internal fun AppHeader(onEvent: (PortfolioStore.Event) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color.Unspecified,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.app_name),
                style = AppTheme.textStyle.titleOne,
                color = AppTheme.colors.text.primary,
            )
        }
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = AppTheme.colors.background.secondaryTwo,
        ) {
            IconButton(onClick = { onEvent(PortfolioStore.Event.NavigateToSettings) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    modifier = Modifier.size(32.dp),
                    tint = AppTheme.colors.text.primary,
                )
            }
        }
    }
}

@Preview(name = "AppHeader", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun AppHeaderPreview() {
    AppTheme(themeDark = true) {
        AppHeader(onEvent = {})
    }
}
