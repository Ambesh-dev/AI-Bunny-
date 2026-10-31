package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.system.SystemActionHandler
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet

data class ToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val accentColor: Color,
    val onClick: () -> Unit
)

@Composable
fun ToolsScreen(
    systemHandler: SystemActionHandler,
    isCustomKeyConfigured: Boolean,
    onSpeakFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        ToolItem(
            id = "tool_youtube",
            title = "YouTube",
            subtitle = "Play videos & shorts",
            emoji = "🎬",
            accentColor = Color(0xFFFF0033),
            onClick = {
                val action = systemHandler.openYouTube()
                onSpeakFeedback(action.detail)
            }
        ),
        ToolItem(
            id = "tool_whatsapp",
            title = "WhatsApp",
            subtitle = "Open chats & calls",
            emoji = "💬",
            accentColor = NeonGreen,
            onClick = {
                val action = systemHandler.openWhatsApp()
                onSpeakFeedback(action.detail)
            }
        ),
        ToolItem(
            id = "tool_camera",
            title = "Camera",
            subtitle = "Take photos & selfies",
            emoji = "📸",
            accentColor = NeonCyan,
            onClick = {
                val action = systemHandler.openCamera()
                onSpeakFeedback(action.detail)
            }
        ),
        ToolItem(
            id = "tool_torch",
            title = "Flashlight",
            subtitle = "Toggle LED torch",
            emoji = "💡",
            accentColor = NeonGold,
            onClick = {
                val action = systemHandler.toggleFlashlight()
                onSpeakFeedback(action.detail)
            }
        ),
        ToolItem(
            id = "tool_alarm",
            title = "Alarms / Clock",
            subtitle = "View active alarms",
            emoji = "⏰",
            accentColor = NeonViolet,
            onClick = {
                val action = systemHandler.openAlarm()
                onSpeakFeedback(action.detail)
            }
        ),
        ToolItem(
            id = "tool_battery",
            title = "Battery Check",
            subtitle = "Level & charge state",
            emoji = "🔋",
            accentColor = NeonGreen,
            onClick = {
                val action = systemHandler.getBatteryStatus()
                onSpeakFeedback(action.detail)
            }
        ),
        ToolItem(
            id = "tool_maps",
            title = "Google Maps",
            subtitle = "GPS navigation",
            emoji = "🗺️",
            accentColor = Color(0xFF3B82F6),
            onClick = {
                val action = systemHandler.openApp("com.google.android.apps.maps", "Google Maps")
                onSpeakFeedback(action.detail)
            }
        ),
        ToolItem(
            id = "tool_spotify",
            title = "Spotify",
            subtitle = "Stream music & vibes",
            emoji = "🎧",
            accentColor = NeonGreen,
            onClick = {
                val action = systemHandler.openApp("com.spotify.music", "Spotify")
                onSpeakFeedback(action.detail)
            }
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Device Status Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Phone Command Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCustomKeyConfigured) NeonCyan.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isCustomKeyConfigured) "Gemini Supercharged" else "Zero-Key Offline AI Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomKeyConfigured) NeonCyan else NeonGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = systemHandler.getDeviceSummary(),
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }

        Text(
            text = "One-Tap Commands",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(tools, key = { it.id }) { tool ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { tool.onClick() }
                        .testTag("tool_card_${tool.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(tool.accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(tool.emoji, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = tool.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = tool.subtitle,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
