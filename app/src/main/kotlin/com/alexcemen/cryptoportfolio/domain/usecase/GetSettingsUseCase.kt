package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor (
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() = settingsRepository.getSettings()
}
