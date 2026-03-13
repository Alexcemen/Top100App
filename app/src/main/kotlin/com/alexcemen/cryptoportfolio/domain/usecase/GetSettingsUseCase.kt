package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke() = repo.getSettings()
}
