package com.example.model

enum class BunyPersonality(
    val title: String,
    val subtitle: String,
    val badgeEmoji: String,
    val accentHex: Long,
    val sampleGreeting: String
) {
    DOST(
        title = "Dost / Bestie",
        subtitle = "Warm, funny & loyal bhai vibe",
        badgeEmoji = "🤝",
        accentHex = 0xFF00E5FF,
        sampleGreeting = "Arrey mere bhai! Bol kya haal-chaal? Tera Buny haazir hai!"
    ),
    ROAST(
        title = "Savage / Roast King",
        subtitle = "Street-smart, witty comebacks, zero boring talk",
        badgeEmoji = "🔥",
        accentHex = 0xFFFF2A85,
        sampleGreeting = "Aaye haaye, screen pe itna dhyan doge toh Buny ki nazar lag jayegi! Bol kya chahiye?"
    ),
    CHARMING(
        title = "Sweet / Flirty",
        subtitle = "Cute, caring, heart-warming vibe",
        badgeEmoji = "💖",
        accentHex = 0xFFA855F7,
        sampleGreeting = "Aap aaye toh meri screen pe phool khil gaye! Kaho, kaise khidmat karu?"
    ),
    PRO(
        title = "Pro Assistant",
        subtitle = "Hyper-fast, accurate & analytical",
        badgeEmoji = "⚡",
        accentHex = 0xFFFFB703,
        sampleGreeting = "Buny AI system initialized. All commands, apps, and queries ready at your command."
    )
}

enum class MessageSender {
    USER,
    BUNY
}

enum class ActionType {
    NONE,
    OPEN_YOUTUBE,
    OPEN_WHATSAPP,
    OPEN_CAMERA,
    OPEN_APP,
    SEARCH_WEB,
    SEARCH_YOUTUBE,
    FLASHLIGHT,
    DEVICE_STATUS,
    CALL_PHONE
}

data class ExecutedAction(
    val type: ActionType,
    val label: String,
    val detail: String,
    val success: Boolean = true
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: ExecutedAction? = null,
    val emotionTrigger: String = "NORMAL"
)
