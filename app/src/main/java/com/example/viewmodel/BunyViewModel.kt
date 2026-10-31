package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.BunyBrain
import com.example.model.ActionType
import com.example.model.BunyPersonality
import com.example.model.BunyWardrobeState
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.model.WardrobeCategory
import com.example.service.BunyAssistantService
import com.example.speech.BunySpeechManager
import com.example.system.SystemActionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BunyViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("buny_settings", Context.MODE_PRIVATE)

    val systemActionHandler = SystemActionHandler(application)
    val speechManager = BunySpeechManager(application, viewModelScope)
    val brain = BunyBrain(systemActionHandler)

    // UI States
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _latestSpeech = MutableStateFlow("Arrey mere bhai! Bol kya haal-chaal? Tera Buny haazir hai! 🐰⚡")
    val latestSpeech: StateFlow<String> = _latestSpeech.asStateFlow()

    private val _emotion = MutableStateFlow("HAPPY")
    val emotion: StateFlow<String> = _emotion.asStateFlow()

    private val _gesture = MutableStateFlow("WAVE")
    val gesture: StateFlow<String> = _gesture.asStateFlow()

    private val _personality = MutableStateFlow(BunyPersonality.DOST)
    val personality: StateFlow<BunyPersonality> = _personality.asStateFlow()

    private val _wardrobe = MutableStateFlow(
        BunyWardrobeState(
            outfitId = prefs.getString("wardrobe_outfit", "outfit_cyber") ?: "outfit_cyber",
            headwearId = prefs.getString("wardrobe_head", "head_none") ?: "head_none",
            glassesId = prefs.getString("wardrobe_glass", "glass_cyber") ?: "glass_cyber",
            accessoryId = prefs.getString("wardrobe_acc", "acc_gold_chain") ?: "acc_gold_chain",
            auraId = prefs.getString("wardrobe_aura", "aura_violet") ?: "aura_violet"
        )
    )
    val wardrobe: StateFlow<BunyWardrobeState> = _wardrobe.asStateFlow()

    private val _isBackgroundServiceActive = MutableStateFlow(BunyAssistantService.isServiceRunning)
    val isBackgroundServiceActive: StateFlow<Boolean> = _isBackgroundServiceActive.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    val isSpeaking = speechManager.isSpeaking
    val isListening = speechManager.isListening

    init {
        // Load saved settings
        val savedPersonalityName = prefs.getString("personality", BunyPersonality.DOST.name)
        val loadedPersonality = try {
            BunyPersonality.valueOf(savedPersonalityName ?: BunyPersonality.DOST.name)
        } catch (e: Exception) {
            BunyPersonality.DOST
        }
        _personality.value = loadedPersonality
        brain.currentPersonality = loadedPersonality
        brain.customApiKey = _customApiKey.value

        // Initial welcome message
        val welcomeMsg = ChatMessage(
            sender = MessageSender.BUNY,
            text = loadedPersonality.sampleGreeting,
            emotionTrigger = "HAPPY"
        )
        _messages.value = listOf(welcomeMsg)
        _latestSpeech.value = welcomeMsg.text
        speechManager.speak(welcomeMsg.text)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = text.trim()
        )
        _messages.value = _messages.value + userMsg

        // Buny is thinking
        _emotion.value = "THINKING"
        _gesture.value = "THINKING_CHIN"

        viewModelScope.launch {
            val response = brain.processInput(text)

            val bunyMsg = ChatMessage(
                sender = MessageSender.BUNY,
                text = response.replyText,
                action = response.action,
                emotionTrigger = response.emotion
            )
            _messages.value = _messages.value + bunyMsg
            _latestSpeech.value = response.replyText
            _emotion.value = response.emotion
            _gesture.value = response.gesture

            speechManager.speak(response.replyText)

            // Return to idle after speech completes or 4 seconds
            delay(4000)
            if (_emotion.value != "HEART_EYES") {
                _emotion.value = "NORMAL"
            }
            _gesture.value = "IDLE"
        }
    }

    fun startVoiceInput() {
        speechManager.startListening(
            onResult = { spokenText ->
                sendMessage(spokenText)
            },
            onError = { error ->
                _latestSpeech.value = error
                speechManager.speak(error)
            }
        )
    }

    fun stopVoiceInput() {
        speechManager.stopListening()
    }

    fun setPersonality(newPersonality: BunyPersonality) {
        _personality.value = newPersonality
        brain.currentPersonality = newPersonality
        prefs.edit().putString("personality", newPersonality.name).apply()

        val switchGreeting = when (newPersonality) {
            BunyPersonality.ROAST -> "Savage Roast Mode ON! Ab aayega maza! Sambhal ke baat karna beta! 🔥😈"
            BunyPersonality.CHARMING -> "Sweet Flirty Mode ON! Aapke pyaar me giraftaar ho gaya hu! 💕✨"
            BunyPersonality.PRO -> "Pro Assistant Mode engaged. Ready for high efficiency tasks. ⚡"
            BunyPersonality.DOST -> "Bhai-chara mode ON! Apun ka dost Buny full form me! 🤝"
        }

        _latestSpeech.value = switchGreeting
        _emotion.value = when (newPersonality) {
            BunyPersonality.ROAST -> "ROAST_SMIRK"
            BunyPersonality.CHARMING -> "HEART_EYES"
            else -> "HAPPY"
        }
        _gesture.value = "WAVE"
        speechManager.speak(switchGreeting)
    }

    fun equipWardrobe(category: WardrobeCategory, itemId: String) {
        val current = _wardrobe.value
        val updated = when (category) {
            WardrobeCategory.OUTFIT -> current.copy(outfitId = itemId)
            WardrobeCategory.HEADWEAR -> current.copy(headwearId = itemId)
            WardrobeCategory.GLASSES -> current.copy(glassesId = itemId)
            WardrobeCategory.ACCESSORY -> current.copy(accessoryId = itemId)
            WardrobeCategory.AURA -> current.copy(auraId = itemId)
        }
        _wardrobe.value = updated

        // Save to prefs
        prefs.edit().apply {
            when (category) {
                WardrobeCategory.OUTFIT -> putString("wardrobe_outfit", itemId)
                WardrobeCategory.HEADWEAR -> putString("wardrobe_head", itemId)
                WardrobeCategory.GLASSES -> putString("wardrobe_glass", itemId)
                WardrobeCategory.ACCESSORY -> putString("wardrobe_acc", itemId)
                WardrobeCategory.AURA -> putString("wardrobe_aura", itemId)
            }
            apply()
        }

        // Buny remarks on the new drip!
        val remarks = listOf(
            "Waah mere bhai! Kya lag raha hu main! Ekdum hero! 🔥",
            "Drip check passed! Buny ka swag dekh ke log jalenge! 😎✨",
            "Ye look toh aag hai bhai! Outfit of the day ban gaya! 🚀",
            "Maza aa gaya bhai! Ab bol kahan chalna hai party me?"
        )
        val line = remarks.random()
        _latestSpeech.value = line
        _emotion.value = "HAPPY"
        _gesture.value = "CHEER_ARMS"
        speechManager.speak(line)
    }

    fun toggleBackgroundService(enable: Boolean) {
        val context = getApplication<Application>()
        if (enable) {
            BunyAssistantService.start(context)
            _isBackgroundServiceActive.value = true
            val line = "Buny background me active ho gaya bhai! Ab notification se bhi command de sakte ho! 🐰⚡"
            _latestSpeech.value = line
            speechManager.speak(line)
        } else {
            BunyAssistantService.stop(context)
            _isBackgroundServiceActive.value = false
            val line = "Background service band ho gayi bhai."
            _latestSpeech.value = line
            speechManager.speak(line)
        }
    }

    fun toggleVoiceMute() {
        speechManager.isVoiceEnabled = !speechManager.isVoiceEnabled
        if (!speechManager.isVoiceEnabled) {
            speechManager.stopSpeaking()
        }
    }

    fun updateApiKey(newKey: String) {
        _customApiKey.value = newKey.trim()
        brain.customApiKey = newKey.trim()
        prefs.edit().putString("custom_api_key", newKey.trim()).apply()
        val line = if (newKey.isBlank()) "API Key removed. Built-in Offline Brain is 100% active!" else "Gemini API Key saved! Buny is now supercharged!"
        _latestSpeech.value = line
        speechManager.speak(line)
    }

    fun onAvatarTapped(touchType: String) {
        if (touchType == "HEAD") {
            _emotion.value = "HEART_EYES"
            _gesture.value = "WAVE"
            val giggles = listOf(
                "Hehe gudgudi ho rahi hai bhai! Sir pe mat thoko! 🐰😂",
                "Arrey bhai itna pyaar! Buny ka dil garden-garden ho gaya! 💖",
                "Hehehe! Bunny ears pe gudgudi hoti hai mere yaar! ✨"
            )
            val line = giggles.random()
            _latestSpeech.value = line
            speechManager.speak(line)
        } else {
            _emotion.value = "HAPPY"
            _gesture.value = "THUMBS_UP"
            val bellyLines = listOf(
                "Oye pet pe tap mat karo bhai, abhi pizza khaya tha! 🍕",
                "High five bhai! Buny full energy me hai! 🖐️",
                "Bata bhai kya hukum hai mere maalik! ⚡"
            )
            val line = bellyLines.random()
            _latestSpeech.value = line
            speechManager.speak(line)
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        val line = "Chat saaf ho gayi bhai! Fresh start karte hain!"
        _latestSpeech.value = line
        speechManager.speak(line)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
