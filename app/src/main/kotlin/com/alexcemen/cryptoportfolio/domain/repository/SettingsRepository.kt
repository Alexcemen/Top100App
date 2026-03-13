package com.alexcemen.cryptoportfolio.domain.repository

import com.alexcemen.cryptoportfolio.domain.model.SettingsData

interface SettingsRepository {
    suspend fun getSettings(): SettingsData
    suspend fun saveSettings(settings: SettingsData)
}
