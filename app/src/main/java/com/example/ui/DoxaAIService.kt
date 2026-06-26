package com.example.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import org.json.JSONArray

object DoxaAIService {
    private const val GROQ_KEY = "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"
    private const val MISTRAL_KEY = "NDJ8J04fWvfnpDVHLYBh64hNJT5mgAsh"
    private const val OPENROUTER_KEY = "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"

    // Limit tracking
    private var groqTokenUsed = 0
    private var mistralTokenUsed = 0
    private var openrouterTokenUsed = 0
    private const val TOKEN_LIMIT = 14000

    init {
        // Reset groq limits periodically every 60 seconds
        val timer = java.util.Timer()
        timer.scheduleAtFixedRate(object : java.util.TimerTask() {
            override fun run() {
                groqTokenUsed = 0
            }
        }, 60000L, 60000L)
    }

    data class Message(val role: String, val content: String)
    data class Route(val provider: String, val model: String)

    val ROUTES = mapOf(
        "easy" to listOf(
            Route("groq", "llama-3.1-8b-instant"),
            Route("mistral", "mistral-small-latest"),
            Route("openrouter", "mistralai/mistral-7b-instruct:free")
        ),
        "medium" to listOf(
            Route("groq", "llama-3.3-70b-versatile"),
            Route("mistral", "mistral-small-latest"),
            Route("openrouter", "meta-llama/llama-3.1-70b-instruct:free")
        ),
        "hard" to listOf(
            Route("groq", "llama-3.3-70b-versatile"),
            Route("mistral", "mistral-large-latest"),
            Route("openrouter", "meta-llama/llama-3.1-70b-instruct:free")
        ),
        "sos" to listOf(
            Route("groq", "llama-3.3-70b-versatile"),
            Route("mistral", "mistral-small-latest"),
            Route("openrouter", "mistralai/mistral-7b-instruct:free")
        )
    )

    fun getDifficulty(text: String): String {
        val t = text.lowercase().trim()
        val sosRegex = Regex("sos|help me|danger|emergency|attack|accident|fire|injured|unsafe|bachao|ambulance|kidnap|robbery|trapped|being followed|hurt me")
        val hardRegex = Regex("\\b(write code|function|algorithm|implement|debug|create a|build a|explain how|step by step|analyze|design|architecture|calculate|difference between|how does.*work|generate)\\b")
        val easyRegex = Regex("^(hi|hello|hey|thanks|ok|okay|sure|bye|yes|no|what is your name|who are you)\\b")

        return when {
            sosRegex.containsMatchIn(t) -> "sos"
            t.length > 120 || hardRegex.containsMatchIn(t) -> "hard"
            t.length < 28 || easyRegex.containsMatchIn(t) -> "easy"
            else -> "medium"
        }
    }

    suspend fun sendMessage(history: List<Message>, systemPrompt: String, difficulty: String): String = withContext(Dispatchers.IO) {
        val routes = ROUTES[difficulty] ?: ROUTES["medium"]!!
        var lastError: Exception? = null

        for (route in routes) {
            val key = when (route.provider) {
                "groq" -> if (groqTokenUsed >= TOKEN_LIMIT) null else GROQ_KEY
                "mistral" -> if (mistralTokenUsed >= TOKEN_LIMIT) null else MISTRAL_KEY
                "openrouter" -> if (openrouterTokenUsed >= TOKEN_LIMIT) null else OPENROUTER_KEY
                else -> null
            }
            if (key == null) continue

            try {
                val urlString = when (route.provider) {
                    "groq" -> "https://api.groq.com/openai/v1/chat/completions"
                    "mistral" -> "https://api.mistral.ai/v1/chat/completions"
                    "openrouter" -> "https://openrouter.ai/api/v1/chat/completions"
                    else -> throw IllegalArgumentException("Unknown provider")
                }

                val conn = URL(urlString).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $key")
                if (route.provider == "openrouter") {
                    conn.setRequestProperty("HTTP-Referer", "https://crowmix.app")
                    conn.setRequestProperty("X-Title", "Doxa AI")
                }
                conn.connectTimeout = 15000
                conn.readTimeout = 15000

                // Build Request Body
                val jsonRequest = JSONObject().apply {
                    put("model", route.model)
                    put("max_tokens", 1024)
                    
                    val messagesArray = JSONArray().apply {
                        // System Prompt
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                        // History
                        history.forEach { msg ->
                            put(JSONObject().apply {
                                put("role", msg.role)
                                put("content", msg.content)
                            })
                        }
                    }
                    put("messages", messagesArray)
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(jsonRequest.toString())
                    writer.flush()
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(responseText)
                    val reply = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    val tokens = jsonResponse.optJSONObject("usage")?.optInt("total_tokens", 100) ?: 100

                    // Update token logs
                    when (route.provider) {
                        "groq" -> groqTokenUsed += tokens
                        "mistral" -> mistralTokenUsed += tokens
                        "openrouter" -> openrouterTokenUsed += tokens
                    }

                    return@withContext reply
                } else {
                    val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                    throw Exception("Provider ${route.provider} returned error: $errorText")
                }
            } catch (e: Exception) {
                lastError = e
                // Continue to the next route silently
                continue
            }
        }

        return@withContext "⚠️ All AI providers are temporarily rate-limited or unavailable. Detail: ${lastError?.message ?: "Keys or network issue."}"
    }
}
