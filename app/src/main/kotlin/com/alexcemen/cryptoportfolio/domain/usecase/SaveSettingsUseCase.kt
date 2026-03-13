package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(settings: SettingsData) = repo.saveSettings(settings)
}
