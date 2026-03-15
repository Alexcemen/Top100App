package com.alexcemen.cryptoportfolio.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_table")
data class CoinEntity(
    @PrimaryKey
    val symbol: String,
    val priceUsdt: Double,
    val quantity: Double,
    val cmcId: Int? = null,
)
