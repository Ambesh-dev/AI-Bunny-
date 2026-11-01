package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class WebKnowledgeSearcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun searchLiveKnowledge(query: String): String? = withContext(Dispatchers.IO) {
        val cleanTopic = extractSearchTopic(query)
        if (cleanTopic.isBlank()) return@withContext null

        // 1. Try English Wikipedia REST API
        val wikiResult = fetchWikipediaSummary(cleanTopic, "en")
        if (!wikiResult.isNullOrBlank()) {
            return@withContext wikiResult
        }

        // 2. Try Hindi Wikipedia REST API if query has Hindi terms
        val hindiWikiResult = fetchWikipediaSummary(cleanTopic, "hi")
        if (!hindiWikiResult.isNullOrBlank()) {
            return@withContext hindiWikiResult
        }

        // 3. Try DuckDuckGo Instant Answer API
        val ddgResult = fetchDuckDuckGoAnswer(cleanTopic)
        if (!ddgResult.isNullOrBlank()) {
            return@withContext ddgResult
        }

        return@withContext null
    }

    private fun fetchWikipediaSummary(topic: String, lang: String): String? {
        return try {
            val encoded = URLEncoder.encode(topic.trim(), StandardCharsets.UTF_8.toString())
            val url = "https://$lang.wikipedia.org/api/rest_v1/page/summary/$encoded"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BunyAI/1.0 (Android Personal Assistant)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null

            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            val extract = json.optString("extract", "")
            val title = json.optString("title", topic)

            if (extract.isNotBlank() && extract.length > 20) {
                // Shorten if too long (keep 2-3 sentences for natural conversational delivery)
                val sentences = extract.split(". ")
                val shortExtract = sentences.take(3).joinToString(". ").trimEnd('.') + "."
                "Bhai $title ke baare me info mili hai:\n\n$shortExtract"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchDuckDuckGoAnswer(query: String): String? {
        return try {
            val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
            val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&skip_disambig=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BunyAI/1.0 (Android Personal Assistant)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null

            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            val abstractText = json.optString("AbstractText", "")
            val definition = json.optString("Definition", "")
            val answer = if (abstractText.isNotBlank()) abstractText else definition

            if (answer.isNotBlank()) {
                "Internet se live jankari mili hai:\n\n$answer"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractSearchTopic(raw: String): String {
        return raw
            .replace(Regex("(?i)^(what is a|what is an|what is the|what is|what are|who is|who was|who are)"), "")
            .replace(Regex("(?i)^(kya hai|kya hota hai|kisko kehte hain|batao|samjhao|ke baare me batao)"), "")
            .replace(Regex("(?i)(kya hai|kya hota hai|kisko kehte hain|ke baare me batao|batao|samjhao)$"), "")
            .replace(Regex("(?i)(buny|bhai|please|google|search)"), "")
            .trim()
    }
}
