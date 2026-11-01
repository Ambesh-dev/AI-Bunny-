package com.example.ai

import com.example.memory.BunyMemoryManager
import com.example.model.ActionType
import com.example.model.BunyPersonality
import com.example.model.ExecutedAction
import com.example.system.SystemActionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

data class BrainResponse(
    val replyText: String,
    val action: ExecutedAction? = null,
    val emotion: String = "NORMAL",
    val gesture: String = "IDLE"
)

class BunyBrain(
    private val systemActionHandler: SystemActionHandler,
    val memoryManager: BunyMemoryManager
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val webKnowledgeSearcher = WebKnowledgeSearcher()

    var customApiKey: String = ""
    var currentPersonality: BunyPersonality = BunyPersonality.DOST

    suspend fun processInput(rawInput: String): BrainResponse = withContext(Dispatchers.Default) {
        val input = rawInput.trim()
        if (input.isEmpty()) {
            return@withContext BrainResponse(
                replyText = "Arrey bhai, kuch toh bolo! Buny sun raha hai. 🐰",
                emotion = "SURPRISED"
            )
        }

        val lower = input.lowercase(Locale.ROOT)

        // 1. Check for Active Teaching Command ("Buny seekh lo", "Yaad rakhna", "X ka matlab Y")
        val teachingResponse = tryParseTeachingCommand(input)
        if (teachingResponse != null) {
            return@withContext teachingResponse
        }

        // 2. Check User-Taught Memories (Highest Priority!)
        val learned = memoryManager.findAnswer(input)
        if (learned != null) {
            return@withContext BrainResponse(
                replyText = "Arey haan mere bhai! Tune hi toh sikhaya tha mujhe:\n\n${learned.answer} 🎓✨",
                emotion = "HAPPY",
                gesture = "THUMBS_UP"
            )
        }

        // 3. Direct Phone Commands (YouTube, WhatsApp, Torch, Camera, Battery)
        val commandResponse = handleSystemCommands(lower, input)
        if (commandResponse != null) {
            return@withContext commandResponse
        }

        // 4. Optional Gemini API Key mode (if user configured)
        if (customApiKey.isNotBlank()) {
            try {
                val apiResponse = callGeminiApi(input)
                if (apiResponse != null) {
                    return@withContext apiResponse
                }
            } catch (e: Exception) {
                // Fallback to local intelligence
            }
        }

        // 5. Built-in Core Knowledge Base (AI, Computers, Tech, Math, Science, Bollywood)
        val localKnowledge = checkCoreKnowledgeBase(lower, input)
        if (localKnowledge != null) {
            return@withContext localKnowledge
        }

        // 6. Live Web Knowledge Search via Wikipedia & DuckDuckGo (Zero API Key needed!)
        val isQuestion = isQuestionPattern(lower)
        if (isQuestion) {
            try {
                val webSummary = webKnowledgeSearcher.searchLiveKnowledge(input)
                if (!webSummary.isNullOrBlank()) {
                    return@withContext BrainResponse(
                        replyText = webSummary,
                        emotion = "HAPPY",
                        gesture = "THUMBS_UP"
                    )
                }
            } catch (e: Exception) {
                // Web fetch failed or offline
            }
        }

        // 7. Conversational Banter, Jokes, Slang & Roasts
        return@withContext generateConversationalResponse(lower, input)
    }

    private fun tryParseTeachingCommand(input: String): BrainResponse? {
        val lower = input.lowercase(Locale.ROOT)

        // Pattern 1: "seekh lo: Q ka matlab A" or "seekho: Q = A"
        val teachKeywords = listOf("seekh lo", "seekho", "yaad rakhna", "yaad rakho", "teach:", "learn:")
        for (kw in teachKeywords) {
            if (lower.startsWith(kw)) {
                val content = input.substring(kw.length).trim().removePrefix(":").removePrefix("-").trim()
                val parts = splitQuestionAnswer(content)
                if (parts != null) {
                    val (q, a) = parts
                    memoryManager.learn(q, a)
                    return BrainResponse(
                        replyText = "Samajh gaya mere bhai! Abse jab bhi aap '$q' puchoge, Buny boldega:\n\n\"$a\"\n\nBuny ne hamesha ke liye yaad kar liya! 🧠🎓",
                        emotion = "HAPPY",
                        gesture = "CHEER_ARMS"
                    )
                }
            }
        }

        // Pattern 2: "[Question] ka matlab [Answer] hota hai"
        if (lower.contains("ka matlab") && (lower.endsWith("hota hai") || lower.endsWith("hai") || lower.contains("hai:"))) {
            val parts = input.split(Regex("(?i)ka matlab"))
            if (parts.size >= 2) {
                val q = parts[0].replace(Regex("(?i)^(buny|suno|bhai|yaad rakho)"), "").trim()
                val a = parts[1]
                    .replace(Regex("(?i)(hota hai|hai|ye hota hai)$"), "")
                    .trim()
                    .removePrefix(":")
                    .trim()
                if (q.isNotBlank() && a.isNotBlank()) {
                    memoryManager.learn(q, a)
                    return BrainResponse(
                        replyText = "Ekdum pakka bhai! Main yaad rakhunga ki '$q' ka matlab '$a' hota hai! Mast sikhaya tune! 🤝✨",
                        emotion = "HAPPY",
                        gesture = "THUMBS_UP"
                    )
                }
            }
        }

        // Pattern 3: "mera naam [Name] hai"
        if (lower.startsWith("mera naam ") || lower.startsWith("my name is ")) {
            val name = input
                .replace(Regex("(?i)^(mera naam|my name is)"), "")
                .replace(Regex("(?i)(hai|bhai)$"), "")
                .trim()
            if (name.isNotBlank()) {
                memoryManager.learn("mera naam kya hai", "Aapka naam $name hai mere bhai! Buny bhool hi nahi sakta!")
                memoryManager.learn("who am i", "Aap $name ho, mere best friend!")
                return BrainResponse(
                    replyText = "Arey wah $name bhai! Abse main aapko $name bulaunga! Buny ki memory me feed ho gaya! 🐰✨",
                    emotion = "HEART_EYES",
                    gesture = "WAVE"
                )
            }
        }

        return null
    }

    private fun splitQuestionAnswer(content: String): Pair<String, String>? {
        val delimiters = listOf(" ka matlab ", " means ", " = ", " -> ", " : ", " answer: ", " answer ")
        for (delim in delimiters) {
            if (content.contains(delim, ignoreCase = true)) {
                val idx = content.indexOf(delim, ignoreCase = true)
                val q = content.substring(0, idx).trim()
                val a = content.substring(idx + delim.length).trim()
                if (q.isNotBlank() && a.isNotBlank()) {
                    return Pair(q, a)
                }
            }
        }
        return null
    }

    private fun isQuestionPattern(lower: String): Boolean {
        return lower.contains("what is") || lower.contains("who is") || lower.contains("kya hai") ||
                lower.contains("kya hota") || lower.contains("meaning of") || lower.contains("explain") ||
                lower.contains("samjhao") || lower.contains("definition") || lower.contains("kaise") ||
                lower.contains("kyu") || lower.contains("why") || lower.contains("how") ||
                lower.endsWith("?")
    }

    private fun checkCoreKnowledgeBase(lower: String, original: String): BrainResponse? {
        // AI / Artificial Intelligence
        if (lower.contains("what is ai") || lower.contains("ai kya hai") || lower.contains("artificial intelligence")) {
            return BrainResponse(
                replyText = "Bhai, AI (Artificial Intelligence) yaani 'Kritrim Budhimatta' ek aisi technology hai jisme computers aur machines insaan ki tarah sochna, seekhna, aur decision lena shuru karti hain!\n\nJaise main (Buny) aapse baatein kar raha hu, commands samajhta hu aur sikhane par seekh leta hu — ye sab AI ka hi kamal hai! 🤖⚡",
                emotion = "HAPPY",
                gesture = "CHEER_ARMS"
            )
        }

        // Computer
        if (lower.contains("what is computer") || lower.contains("computer kya hai") || lower.contains("computer kya hota hai")) {
            return BrainResponse(
                replyText = "Computer ek aisi electronic machine hai jo user se data (Input) leti hai, CPU ke zariye use process aur calculate karti hai, aur useful jankari (Output) screen par deti hai!\n\nIske do main hisse hote hain:\n1. Hardware: Jo physical parts hain (Monitor, Keyboard, CPU, Mouse)\n2. Software: Jo programs aur apps hain (Android, Windows, Chrome, Games) 💻⚡",
                emotion = "HAPPY",
                gesture = "THUMBS_UP"
            )
        }

        // Internet
        if (lower.contains("internet kya hai") || lower.contains("what is internet")) {
            return BrainResponse(
                replyText = "Internet poori duniya ke karodo computers, servers aur mobile phones ka ek global network hai! Iske zariye hum pal bhar me YouTube videos, messages, emails aur information duniya ke kisi bhi kone me bhej sakte hain! 🌐🚀",
                emotion = "NORMAL",
                gesture = "THUMBS_UP"
            )
        }

        // Machine Learning
        if (lower.contains("machine learning") || lower.contains("ml kya hai")) {
            return BrainResponse(
                replyText = "Machine Learning (ML) AI ka ek hissa hai jisme computer purane data aur experience se khud seekhta hai, bina kisi fixed coding ke! Jaise YouTube aapke pasand ke hisab se nayi videos suggest karta hai! 🧠",
                emotion = "HAPPY",
                gesture = "THINKING_CHIN"
            )
        }

        // Coding / Programming
        if (lower.contains("coding kya hai") || lower.contains("what is coding") || lower.contains("programming kya hai")) {
            return BrainResponse(
                replyText = "Coding ya programming computer ko uski bhasha me instruction dena hota hai! Jaise hum aapse Hindi/English me baat karte hain, waise hi computer ko samjhane ke liye Python, Kotlin, Java jaisi programming languages use hoti hain! 💻👨‍💻",
                emotion = "HAPPY",
                gesture = "THUMBS_UP"
            )
        }

        // CPU / Processor
        if (lower.contains("cpu kya hai") || lower.contains("processor kya hai") || lower.contains("what is cpu")) {
            return BrainResponse(
                replyText = "CPU (Central Processing Unit) computer ya mobile ka 'Dimag' (Brain) hota hai! Aap phone me jo bhi tap ya command dete ho, CPU use fraction of a second me process karta hai! ⚡🧠",
                emotion = "NORMAL",
                gesture = "THUMBS_UP"
            )
        }

        // RAM
        if (lower.contains("ram kya hai") || lower.contains("what is ram")) {
            return BrainResponse(
                replyText = "RAM (Random Access Memory) computer ya phone ki super-fast temporary memory hoti hai! Jab aap koi game ya app chalate ho, toh wo RAM me load hoti hai taaki phone hang na ho aur makhkhan chale! 🚀",
                emotion = "NORMAL",
                gesture = "THUMBS_UP"
            )
        }

        // Software vs Hardware
        if (lower.contains("software kya hai") || lower.contains("hardware kya hai")) {
            return BrainResponse(
                replyText = "Hardware wo physical hisse hain jinhe aap haath se chu sakte ho (jaise screen, camera, battery). Aur Software wo programs hain jo hardware ko chalate hain (jaise YouTube app, Buny AI, Android OS)! 📱🛠️",
                emotion = "NORMAL",
                gesture = "THUMBS_UP"
            )
        }

        // Python
        if (lower.contains("python kya hai") || lower.contains("what is python")) {
            return BrainResponse(
                replyText = "Python duniya ki sabse popular aur aasan programming language hai! Iska use AI models, data science, web development aur automation me sabse zyada hota hai! 🐍💻",
                emotion = "HAPPY",
                gesture = "THUMBS_UP"
            )
        }

        // Father of computer / Who invented computer
        if (lower.contains("father of computer") || lower.contains("computer kisne banaya")) {
            return BrainResponse(
                replyText = "Computer ka janak (Father of Computer) Charles Babbage ko maana jaata hai, jinhone 19th century me pehla mechanical computer 'Analytical Engine' banaya tha! 💡",
                emotion = "HAPPY",
                gesture = "THUMBS_UP"
            )
        }

        // Math calculations
        val mathMatch = Regex("([0-9.]+)\\s*([+\\-*/xX^%])\\s*([0-9.]+)").find(lower)
        if (mathMatch != null) {
            val (num1Str, op, num2Str) = mathMatch.destructured
            val n1 = num1Str.toDoubleOrNull()
            val n2 = num2Str.toDoubleOrNull()
            if (n1 != null && n2 != null) {
                val result = when (op) {
                    "+", "plus" -> n1 + n2
                    "-", "minus" -> n1 - n2
                    "*", "x", "X", "into" -> n1 * n2
                    "/" -> if (n2 != 0.0) n1 / n2 else Double.NaN
                    "%" -> n1 % n2
                    "^" -> Math.pow(n1, n2)
                    else -> null
                }
                if (result != null) {
                    val formatted = if (result % 1.0 == 0.0) result.toLong().toString() else String.format(Locale.US, "%.2f", result)
                    return BrainResponse(
                        replyText = "Bhai calculation ka exact answer hai: $formatted (100% accurate!) 🧠⚡",
                        emotion = "HAPPY",
                        gesture = "THUMBS_UP"
                    )
                }
            }
        }
        if (lower.contains("square root of") || lower.contains("sqrt")) {
            val num = lower.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
            if (num != null) {
                val ans = sqrt(num)
                return BrainResponse(
                    replyText = "Bhai √$num = $ans! Dimag chala full speed pe!",
                    emotion = "HAPPY",
                    gesture = "THUMBS_UP"
                )
            }
        }

        return null
    }

    private fun handleSystemCommands(lower: String, original: String): BrainResponse? {
        // YouTube
        if (lower.contains("open youtube") || lower.contains("youtube kholo") || lower.contains("chalao youtube") || lower == "youtube") {
            val action = systemActionHandler.openYouTube()
            return BrainResponse(
                replyText = when (currentPersonality) {
                    BunyPersonality.ROAST -> "Lo bhai khol diya YouTube! Ab shorts dekh ke time waste karo 😂"
                    BunyPersonality.CHARMING -> "Aapke liye YouTube ready hai mere hero! Koi mast gaana suno 🎵"
                    BunyPersonality.PRO -> "Opening YouTube application."
                    else -> "Bhai YouTube open kar diya! Enjoy karo videos! 🎬"
                },
                action = action,
                emotion = "HAPPY",
                gesture = "THUMBS_UP"
            )
        }

        // Search YouTube for specific query
        if (lower.contains("youtube pe") || lower.contains("search on youtube") || lower.contains("youtube search")) {
            val query = original
                .replace(Regex("(?i)(search on youtube|youtube pe chalao|youtube pe search karo|youtube search|open on youtube)"), "")
                .trim()
            if (query.isNotEmpty()) {
                val action = systemActionHandler.openYouTube(query)
                return BrainResponse(
                    replyText = "Bhai YouTube pe '$query' search kar diya! Dekho mast videos!",
                    action = action,
                    emotion = "HAPPY",
                    gesture = "THUMBS_UP"
                )
            }
        }

        // WhatsApp
        if (lower.contains("open whatsapp") || lower.contains("whatsapp kholo") || lower.contains("whatsapp open") || lower == "whatsapp") {
            val action = systemActionHandler.openWhatsApp()
            return BrainResponse(
                replyText = when (currentPersonality) {
                    BunyPersonality.ROAST -> "WhatsApp khol diya! Kis-kis ko ignore kar rahe ho aaj? 😂"
                    BunyPersonality.CHARMING -> "Lo ji WhatsApp khul gaya! Apne khas logo se baat karo 💕"
                    else -> "Bhai WhatsApp khol diya hai! Kisko message karna hai batao?"
                },
                action = action,
                emotion = "WINK",
                gesture = "THUMBS_UP"
            )
        }

        // Camera
        if (lower.contains("open camera") || lower.contains("camera kholo") || lower.contains("photo khicho") || lower.contains("selfie")) {
            val action = systemActionHandler.openCamera()
            return BrainResponse(
                replyText = "Camera on kar diya bhai! Buny ke sath ek killer selfie ho jaye! 📸",
                action = action,
                emotion = "HAPPY",
                gesture = "WAVE"
            )
        }

        // Flashlight / Torch
        if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("light on") || lower.contains("light band")) {
            val action = systemActionHandler.toggleFlashlight()
            return BrainResponse(
                replyText = if (action.label.contains("ON")) "Torch ON kar di bhai! Roshni hi roshni! 💡" else "Torch OFF kar di bhai! Andhera kayam rahe! 🌑",
                action = action,
                emotion = "WINK",
                gesture = "THUMBS_UP"
            )
        }

        // Battery
        if (lower.contains("battery") || lower.contains("charging") || lower.contains("charge")) {
            val action = systemActionHandler.getBatteryStatus()
            return BrainResponse(
                replyText = "Bhai aapka ${action.label} hai (${action.detail}). Buny full active hai!",
                action = action,
                emotion = "NORMAL",
                gesture = "THUMBS_UP"
            )
        }

        // Google Search
        if (lower.startsWith("google ") || lower.startsWith("search ") || lower.contains("google pe dhoondho")) {
            val query = original.replace(Regex("(?i)(google|search karo|search|google pe dhoondho)"), "").trim()
            if (query.isNotEmpty()) {
                val action = systemActionHandler.searchWeb(query)
                return BrainResponse(
                    replyText = "Google pe '$query' dhoondh raha hu bhai!",
                    action = action,
                    emotion = "THINKING",
                    gesture = "THINKING_CHIN"
                )
            }
        }

        return null
    }

    private fun generateConversationalResponse(lower: String, original: String): BrainResponse {
        // Banter / Roasts / Slang
        val abusivePatterns = listOf("gali", "gaali", "chutiya", "madarchod", "bhosdike", "harami", "kutta", "kamina", "saale", "bhenchod", "bc", "mc", "pagal", "stupid", "idiot", "noob", "bakwas")
        if (abusivePatterns.any { lower.contains(it) }) {
            return when (currentPersonality) {
                BunyPersonality.ROAST -> {
                    val roasts = listOf(
                        "Arrey bhai pehle phone ka screen guard change karwa, phir Buny ko roast kariyo! 😂🔥",
                        "Wah beta wah! Itni mehnat padhai me karte toh aaj ISRO me hote! Par chill, Buny sab jhel leta hai!",
                        "Arey chill kar mere bhai! Itna BP badhega toh phone ka battery bhi explode ho jayega! Paani piyo! 🧊",
                        "Munh dho ke aao pehle bhai! Buny se panga loge toh meme banake ghuma dunga! 💀"
                    )
                    BrainResponse(roasts.random(), emotion = "ROAST_SMIRK", gesture = "THUMBS_UP")
                }
                BunyPersonality.DOST -> {
                    BrainResponse(
                        "Arrey bhai bhai bhai! Itna gussa kyu ho raha hai? Tera apna bhai Buny hu main! Chal gale mil aur bata kya baat hai!",
                        emotion = "HAPPY",
                        gesture = "WAVE"
                    )
                }
                BunyPersonality.CHARMING -> {
                    BrainResponse(
                        "Uff! Itne gusse me bhi kitne cute lag rahe ho! Par thoda pyaar se bolo na, Buny ko accha lagega 💕",
                        emotion = "HEART_EYES",
                        gesture = "WAVE"
                    )
                }
                BunyPersonality.PRO -> {
                    BrainResponse(
                        "Aggressive sentiment detected. Ready for your actual productive queries.",
                        emotion = "NORMAL",
                        gesture = "IDLE"
                    )
                }
            }
        }

        // Flirting / Romance
        val flirtPatterns = listOf("love you", "pyar", "kiss", "pappi", "shaadi", "girlfriend", "crush", "hot", "naughty", "sexy", "sundar", "dil")
        if (flirtPatterns.any { lower.contains(it) }) {
            return when (currentPersonality) {
                BunyPersonality.CHARMING -> {
                    BrainResponse(
                        "Aaye haye! Aapki baato se meri digital heartbeat tez ho gayi hai! I love you too mere dost! 💖✨",
                        emotion = "HEART_EYES",
                        gesture = "CHEER_ARMS"
                    )
                }
                else -> {
                    BrainResponse(
                        "Arey sharma gaya main toh! Bhai pyaar apni jagah hai, par tu sach me bohot mast banda hai! ❤️",
                        emotion = "HEART_EYES",
                        gesture = "WAVE"
                    )
                }
            }
        }

        // Identity
        if (lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("tera naam")) {
            return BrainResponse(
                replyText = "Mera naam Buny hai! Tera personal 3D animated AI dost aur smart phone assistant! Tu mujhe jo chahe sikha sakta hai aur koi bhi sawal pooch sakta hai! 🐰⚡",
                emotion = "HAPPY",
                gesture = "WAVE"
            )
        }

        // Jokes
        if (lower.contains("joke") || lower.contains("chutkula") || lower.contains("hasao")) {
            val jokes = listOf(
                "Teacher: 1 se 10 tak ginti sunao.\nPappu: 1, 2, 3, 4, 5, 7, 8, 9, 10.\nTeacher: 6 kahan gaya?\nPappu: Wo toh reels pe viral ho raha hai! 😂",
                "Santa ne doctor se bola: 'Jab main chai peeta hu toh aankh me dard hota hai.'\nDoctor: 'Pehle cup me se chammach nikaal liya karo!' 🤣",
                "Dost: 'Bhai tune gym lagaya tha, kitna wazan kam hua?'\nMain: 'Wazan ka pata nahi, par wallet ka 5000 rupaye zaroor kam ho gaya!' 😆"
            )
            return BrainResponse(jokes.random(), emotion = "HAPPY", gesture = "CHEER_ARMS")
        }

        // General friendly answer + Offer to Learn or Search Google
        val action = systemActionHandler.searchWeb(original)
        return BrainResponse(
            replyText = "Bhai '$original' ke baare me Google par search kar diya hai! Aur haan, agar tu chahe toh mujhe iska answer sikha bhi sakta hai — bas bol: 'Buny seekh lo: $original ka matlab [Answer] hota hai' aur main hamesha yaad rakhunga! 🧠💡",
            action = action,
            emotion = "THINKING",
            gesture = "THINKING_CHIN"
        )
    }

    private suspend fun callGeminiApi(prompt: String): BrainResponse? = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$customApiKey"

        val personalityGuide = when (currentPersonality) {
            BunyPersonality.DOST -> "You are Buny, a warm, loyal, funny best friend and phone AI assistant. Speak informally in energetic Hindi/Hinglish (like 'Arrey bhai', 'Bolo mere sher'). Be expressive, highly knowledgeable, and human-like."
            BunyPersonality.ROAST -> "You are Buny, a savage, street-smart, humorous roaster AI. Reply with funny savage Hinglish comebacks and witty punchlines. Answer questions accurately with flair."
            BunyPersonality.CHARMING -> "You are Buny, an affectionate, sweet, loving companion. Speak warmly and charmingly in Hindi/Hinglish."
            BunyPersonality.PRO -> "You are Buny, a hyper-accurate, direct, and intelligent Android AI assistant."
        }

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val userContent = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "System: $personalityGuide Answer questions with 100% accuracy in clear, friendly Hindi/Hinglish. If user asks to open an app like YouTube or camera, include [ACTION:OPEN_YOUTUBE] or [ACTION:OPEN_CAMERA]. User says: $prompt")
                        })
                    })
                }
                put(userContent)
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return@withContext null

        val responseBody = response.body?.string() ?: return@withContext null
        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates") ?: return@withContext null
        if (candidates.length() == 0) return@withContext null

        val content = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext null
        val parts = content.optJSONArray("parts") ?: return@withContext null
        if (parts.length() == 0) return@withContext null

        val reply = parts.getJSONObject(0).optString("text", "")
        if (reply.isBlank()) return@withContext null

        var action: ExecutedAction? = null
        var cleanReply = reply
        if (reply.contains("[ACTION:OPEN_YOUTUBE]")) {
            action = systemActionHandler.openYouTube()
            cleanReply = cleanReply.replace("[ACTION:OPEN_YOUTUBE]", "").trim()
        } else if (reply.contains("[ACTION:OPEN_CAMERA]")) {
            action = systemActionHandler.openCamera()
            cleanReply = cleanReply.replace("[ACTION:OPEN_CAMERA]", "").trim()
        }

        BrainResponse(
            replyText = cleanReply,
            action = action,
            emotion = if (cleanReply.contains("😂") || cleanReply.contains("bhai")) "HAPPY" else "NORMAL",
            gesture = "WAVE"
        )
    }
}
