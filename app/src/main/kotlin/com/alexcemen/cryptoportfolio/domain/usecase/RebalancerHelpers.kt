package com.alexcemen.cryptoportfolio.domain.usecase

import kotlin.math.floor

internal fun buildAvailableCoins(
    topCmc: List<String>,
    tradableMexc: Set<String>,
    excluded: Set<String>,
): List<String> = topCmc.filter { it in tradableMexc && it !in excluded }

internal fun buildCoinsToSell(
    mine: Set<String>,
    available: List<String>,
    excluded: Set<String>,
): List<String> = mine.filter { it !in available && it !in excluded }

internal fun Double.floor2(): Double = floor(this * 100.0) / 100.0
