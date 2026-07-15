package com.example.domain.repository

interface GeminiRepository {
    suspend fun getTroubleshootingSteps(complaint: String): String
}
