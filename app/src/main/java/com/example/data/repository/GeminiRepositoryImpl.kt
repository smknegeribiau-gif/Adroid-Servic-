package com.example.data.repository

import com.example.BuildConfig
import com.example.domain.repository.GeminiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRepositoryImpl : GeminiRepository {

    // Stable OkHttpClient with 60s timeouts as recommended for Gemini API
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun getTroubleshootingSteps(complaint: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key belum dikonfigurasi. Harap pasang GEMINI_API_KEY Anda di panel Secrets Google AI Studio.")
        }

        // Endpoint REST API untuk model gemini-3.5-flash
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        // Build request body using standard, safe org.json objects (built into Android)
        val requestJson = JSONObject().apply {
            // System instructions
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", "Anda adalah asisten ahli teknisi komputer dan printer berpengalaman. " +
                            "Berikan langkah-langkah troubleshooting awal yang aman, praktis, dan terstruktur " +
                            "untuk membantu pengguna melakukan solusi mandiri sebelum memanggil teknisi profesional. " +
                            "Gunakan bahasa Indonesia yang ramah, jelas, mudah dipahami, dan beri penekanan pada tindakan pencegahan keselamatan.")
                }))
            })

            // Contents (the prompt)
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", complaint)
                }))
            }))

            // Generation config
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw Exception("Gagal menghubungi AI Studio (Status: ${response.code}). Detail: $errorBody")
            }

            val responseBodyString = response.body?.string() ?: throw Exception("Respons dari AI kosong.")
            val responseJson = JSONObject(responseBodyString)
            
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            val text = firstPart?.optString("text")

            text ?: throw Exception("Respons AI tidak dapat diuraikan.")
        }
    }
}
