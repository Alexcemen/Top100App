package com.alexcemen.cryptoportfolio.ui.navigation

import kotlinx.serialization.Serializable
import com.alexcemen.cryptoportfolio.ui.mvi.AppNavKey

@Serializable
data class PortfolioScreen(
    override val type: String = PortfolioScreen::class.simpleName.toString()
) : AppNavKey()

@Serializable
data class SettingsScreen(
    override val type: String = SettingsScreen::class.simpleName.toString()
) : AppNavKey()
