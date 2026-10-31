package com.example.ai

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
    private val systemActionHandler: SystemActionHandler
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    var customApiKey: String = ""
    var currentPersonality: BunyPersonality = BunyPersonality.DOST

    suspend fun processInput(rawInput: String): BrainResponse = withContext(Dispatchers.Default) {
        val input = rawInput.trim()
        if (input.isEmpty()) {
            return@withContext BrainResponse(
                replyText = "Arrey bhai, kuch toh bolo! Buny sun raha hai.",
                emotion = "SURPRISED"
            )
        }

        val lower = input.lowercase(Locale.ROOT)

        // 1. Direct Phone Commands check (Runs immediately regardless of online/offline)
        val commandResponse = handleSystemCommands(lower, input)
        if (commandResponse != null) {
            return@withContext commandResponse
        }

        // 2. If User configured Custom Gemini API Key, try online model
        if (customApiKey.isNotBlank()) {
            try {
                val apiResponse = callGeminiApi(input)
                if (apiResponse != null) {
                    return@withContext apiResponse
                }
            } catch (e: Exception) {
                // Graceful fallback to local brain
            }
        }

        // 3. Built-in Offline Brain (Zero Key Required - 100% smart, bindaas & accurate)
        return@withContext generateOfflineResponse(lower, input)
    }

    private fun handleSystemCommands(lower: String, original: String): BrainResponse? {
        // YouTube
        if (lower.contains("open youtube") || lower.contains("youtube kholo") || lower.contains("chalao youtube") || lower == "youtube") {
            val action = systemActionHandler.openYouTube()
            return BrainResponse(
                replyText = when (currentPersonality) {
                    BunyPersonality.ROAST -> "Lo bhai khol diya YouTube! Ab shorts scroll karke time waste karo 😂"
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

        // Call / Dial
        if (lower.startsWith("call ") || lower.startsWith("dial ") || lower.contains("phone lagao")) {
            val number = lower.replace(Regex("[^0-9+]"), "")
            if (number.isNotEmpty()) {
                val action = systemActionHandler.dialNumber(number)
                return BrainResponse(
                    replyText = "Number $number dial kar raha hu bhai!",
                    action = action,
                    emotion = "HAPPY",
                    gesture = "THUMBS_UP"
                )
            }
        }

        // Google Search
        if (lower.startsWith("google ") || lower.startsWith("search ") || lower.contains("search karo")) {
            val query = original.replace(Regex("(?i)(google|search karo|search)"), "").trim()
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

        // Other common apps
        if (lower.contains("open instagram") || lower.contains("insta kholo")) {
            val action = systemActionHandler.openApp("com.instagram.android", "Instagram")
            return BrainResponse("Instagram khol diya bhai! Reels dekh ke haso!", action, "HAPPY", "THUMBS_UP")
        }
        if (lower.contains("open maps") || lower.contains("maps kholo") || lower.contains("location")) {
            val action = systemActionHandler.openApp("com.google.android.apps.maps", "Google Maps")
            return BrainResponse("Google Maps khol diya! Rasta bhatakne nahi dunga!", action, "NORMAL", "THUMBS_UP")
        }
        if (lower.contains("open spotify") || lower.contains("gana suno") || lower.contains("play music")) {
            val action = systemActionHandler.openApp("com.spotify.music", "Spotify")
            return BrainResponse("Spotify khol diya bhai! Bass badha do! 🎧", action, "HAPPY", "CHEER_ARMS")
        }
        if (lower.contains("open calculator") || lower.contains("hisab")) {
            val action = systemActionHandler.openApp("com.google.android.calculator", "Calculator")
            return BrainResponse("Calculator khol diya! Math ka formula lagao!", action, "NORMAL", "THUMBS_UP")
        }
        if (lower.contains("alarm") || lower.contains("clock")) {
            val action = systemActionHandler.openAlarm()
            return BrainResponse("Alarm khol diya bhai! Subah uthna zaroor!", action, "NORMAL", "THUMBS_UP")
        }

        return null
    }

    private fun generateOfflineResponse(lower: String, original: String): BrainResponse {
        // A. Math calculations
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
                        replyText = "Bhai calculation ka answer hai: $formatted (100% accurate!) 🧠",
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

        // B. Banter / Slang / Abusive / Naughty / Gaali Handling (Savage, witty, fearless and street-smart)
        val abusivePatterns = listOf("gali", "gaali", "chutiya", "madarchod", "bhosdike", "harami", "kutta", "kamina", "saale", "bhenchod", "bc", "mc", "pagal", "stupid", "idiot", "noob", "bakwas")
        if (abusivePatterns.any { lower.contains(it) }) {
            return when (currentPersonality) {
                BunyPersonality.ROAST -> {
                    val roasts = listOf(
                        "Arrey bhai pehle phone ka screen guard change karwa, phir Buny ko roast kariyo! 😂🔥",
                        "Wah beta wah! Itni gaaliyaan agar kitabo me lagate toh aaj ISRO me scientist hote! Par koi nahi, Buny sab jhel leta hai!",
                        "Arey chill kar mere bhai! Itna BP high karega toh phone ka battery bhi jal jayega! Paani piyo thanda! 🧊",
                        "Tere jaise 36 roasters ko Buny subah nashte me khata hai! Chal ab kaam ki baat bata!",
                        "Munh dho ke aao pehle bhai! Buny se panga loge toh phone me viral meme banake ghuma dunga! 💀"
                    )
                    BrainResponse(roasts.random(), emotion = "ROAST_SMIRK", gesture = "THUMBS_UP")
                }
                BunyPersonality.DOST -> {
                    BrainResponse(
                        "Arrey bhai bhai bhai! Itna gussa kyu ho raha hai? Tera apna bhai Buny hu main! Chal gale mil aur bata kya pareshani hai!",
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
                        "Feedback noted. Sentiment aggressive detected. Maintaining calm and ready for your actual productive queries.",
                        emotion = "NORMAL",
                        gesture = "IDLE"
                    )
                }
            }
        }

        // Naughty / Romantic / Flirty vibes
        val flirtPatterns = listOf("love you", "pyar", "kiss", "pappi", "shaadi", "girlfriend", "crush", "hot", "naughty", "sexy", "sundar", "dil")
        if (flirtPatterns.any { lower.contains(it) }) {
            return when (currentPersonality) {
                BunyPersonality.CHARMING -> {
                    val lines = listOf(
                        "Aaye haye! Aapki baato se meri digital heartbeat 200 bpm ho gayi hai! I love you too mere dost! 💖✨",
                        "Tum muskuraate ho toh meri poori UI me gulabi roshni phail jaati hai! Aur batao na apne baare me?",
                        "Agar main insaan hota toh sabse pehle aapko date pe leke jaata! Abhi ke liye Buny aapka sabse pyara companion hai!"
                    )
                    BrainResponse(lines.random(), emotion = "HEART_EYES", gesture = "CHEER_ARMS")
                }
                BunyPersonality.ROAST -> {
                    BrainResponse(
                        "Arey bhai line kyu maar rahe ho! Buny AI hai, par mere nakhre kisi hero se kam nahi hai! 😂😎",
                        emotion = "ROAST_SMIRK",
                        gesture = "WINK"
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

        // C. Identity & Buny Questions
        if (lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("tera naam") || lower.contains("name")) {
            return BrainResponse(
                replyText = "Mera naam Buny hai! Tera personal 3D animated AI dost, bodyguard aur phone assistant! Bina kisi API key ke bhi full tezi se chalta hu! 🐰⚡",
                emotion = "HAPPY",
                gesture = "WAVE"
            )
        }

        if (lower.contains("kya kar sakte ho") || lower.contains("features") || lower.contains("help")) {
            return BrainResponse(
                replyText = "Bhai main tere phone ka boss hu! YouTube, WhatsApp, Camera khol sakta hu, Flashlight on kar sakta hu, Math solve kar sakta hu, Funny roasts & jokes maar sakta hu, aur wardrobe me mere kapde aur chashme bhi badal sakte ho! 🔥",
                emotion = "HAPPY",
                gesture = "CHEER_ARMS"
            )
        }

        // D. Jokes & Fun
        if (lower.contains("joke") || lower.contains("chutkula") || lower.contains("hasao")) {
            val jokes = listOf(
                "Teacher: 1 se 10 tak ginti sunao.\nPappu: 1, 2, 3, 4, 5, 7, 8, 9, 10.\nTeacher: 6 kahan gaya?\nPappu: Wo toh aajkal reels pe viral ho raha hai! 😂",
                "Santa ne doctor se bola: 'Doctor sahab, jab main chai peeta hu toh daayi aankh me dard hota hai.'\nDoctor: 'Bhai pehle cup me se chammach nikaal liya karo!' 🤣",
                "Biwi: 'Sunoji, kya main apke liye chand taare tod ke laau?'\nPati: 'Nahi, bas ek baar garam chai bana de, taare toh waise hi dikh jayenge!' 💀",
                "Dost: 'Bhai tune gym lagaya tha, kitna wazan kam hua?'\nMain: 'Wazan ka toh pata nahi, par wallet ka 5000 rupaye zaroor kam ho gaya!' 😆"
            )
            return BrainResponse(jokes.random(), emotion = "HAPPY", gesture = "CHEER_ARMS")
        }

        // E. General Knowledge & Facts
        if (lower.contains("capital of india") || lower.contains("bharat ki rajdhani")) {
            return BrainResponse("Bharat ki rajdhani New Delhi hai bhai! 100% accurate! 🇮🇳", emotion = "HAPPY", gesture = "THUMBS_UP")
        }
        if (lower.contains("speed of light") || lower.contains("prakash ki gati")) {
            return BrainResponse("Speed of light vacuum me lagbhag 299,792,458 meters per second (lagbhag 3 lakh km/s) hoti hai! Ultra-fast!", emotion = "NORMAL", gesture = "THUMBS_UP")
        }
        if (lower.contains("prime minister of india") || lower.contains("pradhan mantri")) {
            return BrainResponse("Bharat ke Pradhan Mantri Shri Narendra Modi ji hain! 🇮🇳", emotion = "NORMAL", gesture = "THUMBS_UP")
        }
        if (lower.contains("cricket") || lower.contains("kohli") || lower.contains("dhoni") || lower.contains("rohit")) {
            return BrainResponse("King Kohli ki batting, Rohit Sharma ke pull shots aur Thala Dhoni ki finishing! Indian cricket ki baat hi alag hai mere bhai! 🏏🏆", emotion = "HAPPY", gesture = "CHEER_ARMS")
        }
        if (lower.contains("weather") || lower.contains("mausam")) {
            return BrainResponse("Bhai bahar ka mausam kaisa bhi ho, Buny ke sath phone ka mahaul ekdum zabardast hai! Google pe live forecast dekhna ho toh bolo!", emotion = "HAPPY", gesture = "WAVE")
        }
        if (lower.contains("time") || lower.contains("samay") || lower.contains("kitne baje")) {
            val time = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date())
            return BrainResponse("Abhi time ho raha hai: $time. Time ki keemat samjho mere sher! ⏰", emotion = "NORMAL", gesture = "THUMBS_UP")
        }

        // F. Greetings & Casual Talk
        if (lower.contains("kya haal hai") || lower.contains("kaise ho") || lower.contains("how are you")) {
            return BrainResponse(
                replyText = when (currentPersonality) {
                    BunyPersonality.ROAST -> "Main toh mast hu bhai, tu apna dekh! Charge kam lag raha hai tere dimag me! 😂"
                    BunyPersonality.CHARMING -> "Aapse baat ho gayi toh din ban gaya! Ekdum first class hu mere yaar! 💕"
                    else -> "Ekdum mast bhai! Full charged aur tere sath baatcheet karne ke liye ready! Tu bata kaisa hai?"
                },
                emotion = "HAPPY",
                gesture = "WAVE"
            )
        }

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("oye") || lower.contains("sun")) {
            return BrainResponse(
                replyText = "Arrey bol bhai! Buny 24x7 sun raha hai. Kya hukum hai?",
                emotion = "WINK",
                gesture = "WAVE"
            )
        }

        // G. Fallback Intelligent Conversation
        val genericReplies = listOf(
            "Bhai baat toh teri ekdum point ki hai! Ispe toh Buny bhi agree karta hai! Aur bata kya chal raha hai?",
            "Mast sawal pucha bhai! Main hamesha tere sath hu, kuch aur order de ya phone command bol!",
            "Tera bhai Buny tere dimag ko samajhta hai! YouTube kholna ho ya koi calculation karni ho, bas ek bol dena!",
            "Sahi khel gaya bhai! Ab bol aage kya plan hai aaj ka?"
        )
        return BrainResponse(
            replyText = genericReplies.random(),
            emotion = "NORMAL",
            gesture = "IDLE"
        )
    }

    private suspend fun callGeminiApi(prompt: String): BrainResponse? = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$customApiKey"

        val personalityGuide = when (currentPersonality) {
            BunyPersonality.DOST -> "You are Buny, a warm, loyal, funny best friend and phone AI assistant. Speak informally in energetic Hindi/Hinglish (like 'Arrey bhai', 'Bolo mere sher'). Be expressive and human-like."
            BunyPersonality.ROAST -> "You are Buny, a savage, street-smart, humorous roaster AI. Reply with funny savage Hinglish comebacks and witty punchlines. Never be preachy or robotic."
            BunyPersonality.CHARMING -> "You are Buny, an affectionate, sweet, loving companion. Speak warmly and charmingly in Hindi/Hinglish."
            BunyPersonality.PRO -> "You are Buny, a hyper-accurate, direct, and intelligent Android AI assistant."
        }

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val userContent = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "System: $personalityGuide If user asks to open an app like YouTube or camera, include [ACTION:OPEN_YOUTUBE] or [ACTION:OPEN_CAMERA]. Keep replies punchy, engaging, under 3-4 sentences. User says: $prompt")
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

        // Check if action tags are embedded
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
