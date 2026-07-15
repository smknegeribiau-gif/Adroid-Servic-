package com.example.domain.usecase

import com.example.domain.repository.GeminiRepository

class GetTroubleshootingUseCase(private val repository: GeminiRepository) {
    suspend operator fun invoke(complaint: String): Result<String> {
        return runCatching {
            if (complaint.isBlank()) {
                throw IllegalArgumentException("Keluhan tidak boleh kosong.")
            }
            repository.getTroubleshootingSteps(complaint)
        }
    }
}
