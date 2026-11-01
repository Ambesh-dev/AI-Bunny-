package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BunyWardrobeState
import com.example.ui.avatar.BunyAvatarView
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet

@Composable
fun HomeScreen(
    latestSpeech: String,
    isSpeaking: Boolean,
    isListening: Boolean,
    emotion: String,
    gesture: String,
    wardrobe: BunyWardrobeState,
    onSendMessage: (String) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onReplaySpeech: () -> Unit,
    onAvatarTapped: (String) -> Unit,
    onOpenWardrobe: () -> Unit,
    onOpenTraining: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Mic pulsing animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "MicPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MicScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Live Animated 3D Buny Avatar
        BunyAvatarView(
            isSpeaking = isSpeaking,
            emotion = emotion,
            gesture = gesture,
            wardrobe = wardrobe,
            onAvatarTapped = onAvatarTapped,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Tap hint for user
        Text(
            text = "✨ Tap Buny's head or belly for reactions!",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 2. Speech Bubble Card (Real-time lipsync & voice feedback)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, CyberBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("buny_speech_bubble")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSpeaking) NeonPink else NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSpeaking) "Buny is speaking... 🗣️" else "Buny says:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSpeaking) NeonPink else NeonCyan
                        )
                    }

                    IconButton(
                        onClick = onReplaySpeech,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("replay_speech_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Replay voice",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = latestSpeech,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3. Quick Action Suggestion Chips
        val suggestions = listOf(
            "🤖 What is AI?",
            "💻 Computer kya hai?",
            "🎓 Sikhao Buny ko",
            "🎬 Open YouTube",
            "🌐 Internet kya hai?",
            "🔥 Roast me",
            "💡 Flashlight",
            "😂 Ek joke sunao",
            "🧠 Machine learning kya hai?",
            "🔋 Battery status"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { chipText ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CyberSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (chipText.contains("Sikhao")) {
                                onOpenTraining()
                            } else {
                                onSendMessage(chipText)
                            }
                        }
                        .testTag("quick_chip_${chipText.filter { it.isLetterOrDigit() }}")
                ) {
                    Text(
                        text = chipText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 4. Voice Listening Wave Indicator
        AnimatedVisibility(visible = isListening) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NeonPink.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = NeonPink,
                        modifier = Modifier.scale(micScale)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buny sun raha hai, bolo bhai... 🎙️",
                        fontWeight = FontWeight.Bold,
                        color = NeonPink,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Interactive Chat & Mic Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Microphone Button with gradient & pulse
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(if (isListening) micScale else 1f)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            if (isListening) listOf(Color(0xFFFF0055), Color(0xFFFF5500))
                            else listOf(NeonCyan, NeonViolet)
                        )
                    )
                    .clickable {
                        if (isListening) onStopVoice() else onStartVoice()
                    }
                    .testTag("mic_input_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice input",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Buny se pucho ya order do...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_textfield"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberBorder,
                    focusedContainerColor = CyberSurface,
                    unfocusedContainerColor = CyberSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                }),
                trailingIcon = {
                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSendMessage(inputText)
                                inputText = ""
                            },
                            modifier = Modifier.testTag("send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = NeonCyan
                            )
                        }
                    }
                }
            )
        }

        // Brain Training Quick Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onOpenTraining() }
                .testTag("open_training_banner")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎓", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Teach Buny (Brain Training)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Dost ki tarah Buny ko nayi baatein sikhao",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Sikhao 🧠",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Wardrobe Quick Access Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onOpenWardrobe() }
                .testTag("open_wardrobe_banner")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👗", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Buny's Wardrobe Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Customize Outfits, Shades, Caps & Aura",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonPink.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Dress Up ✨",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPink,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
