package com.example.memory

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

data class TaughtKnowledge(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val keywords: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

class BunyMemoryManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("buny_taught_memory", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, TaughtKnowledge::class.java)
    private val adapter = moshi.adapter<List<TaughtKnowledge>>(listType)

    private val _memories = MutableStateFlow<List<TaughtKnowledge>>(emptyList())
    val memories: StateFlow<List<TaughtKnowledge>> = _memories.asStateFlow()

    init {
        loadMemories()
    }

    private fun loadMemories() {
        val json = prefs.getString("memories_json", null)
        if (json != null) {
            try {
                val list = adapter.fromJson(json) ?: emptyList()
                _memories.value = list
            } catch (e: Exception) {
                _memories.value = getDefaultMemories()
                saveMemories(_memories.value)
            }
        } else {
            // Seed with friendly initial memories
            val initial = getDefaultMemories()
            _memories.value = initial
            saveMemories(initial)
        }
    }

    private fun saveMemories(list: List<TaughtKnowledge>) {
        try {
            val json = adapter.toJson(list)
            prefs.edit().putString("memories_json", json).apply()
            _memories.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun learn(question: String, answer: String): TaughtKnowledge {
        val cleanQ = question.trim()
        val cleanA = answer.trim()
        val keywords = extractKeywords(cleanQ)

        val newKnowledge = TaughtKnowledge(
            id = UUID.randomUUID().toString(),
            question = cleanQ,
            answer = cleanA,
            keywords = keywords
        )

        // If a very similar question exists, update it, else prepend
        val current = _memories.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            it.question.equals(cleanQ, ignoreCase = true)
        }
        if (existingIndex >= 0) {
            current[existingIndex] = newKnowledge
        } else {
            current.add(0, newKnowledge)
        }

        saveMemories(current)
        return newKnowledge
    }

    fun forget(id: String) {
        val updated = _memories.value.filter { it.id != id }
        saveMemories(updated)
    }

    fun clearAll() {
        saveMemories(emptyList())
    }

    fun findAnswer(rawQuery: String): TaughtKnowledge? {
        val query = rawQuery.lowercase(Locale.ROOT).trim()
        if (query.isEmpty()) return null

        val current = _memories.value
        // 1. Exact match
        current.firstOrNull { it.question.lowercase(Locale.ROOT) == query }?.let { return it }

        // 2. Contains match
        current.firstOrNull {
            val qLower = it.question.lowercase(Locale.ROOT)
            query.contains(qLower) || qLower.contains(query)
        }?.let { return it }

        // 3. Keyword overlap
        val queryWords = query.split(Regex("\\s+")).filter { it.length > 2 }
        if (queryWords.isEmpty()) return null

        var bestMatch: TaughtKnowledge? = null
        var maxScore = 0

        for (item in current) {
            var score = 0
            val itemKeywords = item.keywords.ifEmpty { extractKeywords(item.question) }
            for (word in queryWords) {
                if (itemKeywords.any { it.contains(word) || word.contains(it) }) {
                    score += 2
                }
            }
            if (score > maxScore && score >= 2) {
                maxScore = score
                bestMatch = item
            }
        }

        return bestMatch
    }

    private fun extractKeywords(text: String): List<String> {
        val stopWords = setOf(
            "what", "is", "a", "an", "the", "kya", "hai", "hota", "hoti", "hote", "ka", "ki", "ke",
            "kaun", "batao", "mujhe", "samjhao", "buny", "bhai", "please", "meaning", "of"
        )
        return text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && !stopWords.contains(it) }
    }

    private fun getDefaultMemories(): List<TaughtKnowledge> {
        return listOf(
            TaughtKnowledge(
                question = "tumhe kisne sikhaya",
                answer = "Mujhe mere bhai (aapne) train kiya hai! Main aapse hi seekhta hu aur dosto ki tarah baatein karta hu! 🤝",
                keywords = listOf("tumhe", "kisne", "sikhaya", "train")
            ),
            TaughtKnowledge(
                question = "buny ka favourite khana kya hai",
                answer = "Bhai digital gaajar (carrots) aur aapki mast baatein! Aur haan pizza bhi pasand hai! 🥕🍕",
                keywords = listOf("favourite", "khana", "food", "pasand")
            )
        )
    }
}
