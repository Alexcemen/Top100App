package com.alexcemen.cryptoportfolio.platform

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

fun formatNumber(value: Double, decimals: Int): String {
    val sign = if (value < 0) "-" else ""
    val absValue = abs(value)
    val factor = 10.0.pow(decimals).roundToLong()
    val long = (absValue * factor).roundToLong()
    val intPart = long / factor
    val fracPart = long % factor
    return "$sign$intPart.${fracPart.toString().padStart(decimals, '0')}"
}
