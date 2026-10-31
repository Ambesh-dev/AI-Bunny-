package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BunyPersonality
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun BunyTopBar(
    personality: BunyPersonality,
    isVoiceMuted: Boolean,
    isBackgroundActive: Boolean,
    onToggleVoice: () -> Unit,
    onToggleBackground: () -> Unit,
    onPersonalityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Title & Avatar Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPersonalityClick() }
                    .padding(4.dp)
                    .testTag("personality_badge_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(personality.accentHex).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐰",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Buny",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = NeonCyan
                        )
                        Text(
                            text = " AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = NeonPink
                        )
                    }

                    // Vibe Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(personality.accentHex).copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "${personality.badgeEmoji} ${personality.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(personality.accentHex),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick Actions: Voice Mute & Background toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Background Service Toggle
                IconButton(
                    onClick = onToggleBackground,
                    modifier = Modifier.testTag("toggle_background_button")
                ) {
                    Icon(
                        imageVector = if (isBackgroundActive) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = "Background service",
                        tint = if (isBackgroundActive) NeonCyan else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Voice Mute Toggle
                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier.testTag("toggle_voice_button")
                ) {
                    Icon(
                        imageVector = if (!isVoiceMuted) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Voice toggle",
                        tint = if (!isVoiceMuted) NeonPink else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
