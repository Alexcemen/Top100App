package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import javax.inject.Inject

class CheckSettingsUseCase @Inject constructor(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(): Boolean {
        val s = repo.getSettings()
        return s.cmcApiKey.isNotBlank() && s.mexcApiKey.isNotBlank() && s.mexcApiSecret.isNotBlank()
    }
}
