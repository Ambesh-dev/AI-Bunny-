package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BunyTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.WardrobeScreen
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.viewmodel.BunyViewModel

enum class MainTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Buny", Icons.Default.Home),
    CHAT("Chat", Icons.Default.Forum),
    WARDROBE("Closet", Icons.Default.Checkroom),
    TOOLS("Tools", Icons.Default.Build),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun BunyMainScreen(
    viewModel: BunyViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    val messages by viewModel.messages.collectAsState()
    val latestSpeech by viewModel.latestSpeech.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val emotion by viewModel.emotion.collectAsState()
    val gesture by viewModel.gesture.collectAsState()
    val personality by viewModel.personality.collectAsState()
    val wardrobe by viewModel.wardrobe.collectAsState()
    val isBackgroundActive by viewModel.isBackgroundServiceActive.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()

    // BackHandler: Return to Home if on secondary tab
    BackHandler(enabled = currentTab != MainTab.HOME) {
        currentTab = MainTab.HOME
    }

    Scaffold(
        topBar = {
            BunyTopBar(
                personality = personality,
                isVoiceMuted = !viewModel.speechManager.isVoiceEnabled,
                isBackgroundActive = isBackgroundActive,
                onToggleVoice = { viewModel.toggleVoiceMute() },
                onToggleBackground = { viewModel.toggleBackgroundService(!isBackgroundActive) },
                onPersonalityClick = { currentTab = MainTab.SETTINGS }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("buny_bottom_nav")
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonPink,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        latestSpeech = latestSpeech,
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        emotion = emotion,
                        gesture = gesture,
                        wardrobe = wardrobe,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onStartVoice = { viewModel.startVoiceInput() },
                        onStopVoice = { viewModel.stopVoiceInput() },
                        onReplaySpeech = { viewModel.speechManager.speak(latestSpeech) },
                        onAvatarTapped = { touchType -> viewModel.onAvatarTapped(touchType) },
                        onOpenWardrobe = { currentTab = MainTab.WARDROBE }
                    )
                }
                MainTab.CHAT -> {
                    ChatScreen(
                        messages = messages,
                        isListening = isListening,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onStartVoice = { viewModel.startVoiceInput() },
                        onStopVoice = { viewModel.stopVoiceInput() },
                        onReplayMessage = { viewModel.speechManager.speak(it) },
                        onClearChat = { viewModel.clearChat() }
                    )
                }
                MainTab.WARDROBE -> {
                    WardrobeScreen(
                        wardrobe = wardrobe,
                        isSpeaking = isSpeaking,
                        emotion = emotion,
                        gesture = gesture,
                        onEquipItem = { category, itemId ->
                            viewModel.equipWardrobe(category, itemId)
                        },
                        onAvatarTapped = { touchType -> viewModel.onAvatarTapped(touchType) }
                    )
                }
                MainTab.TOOLS -> {
                    ToolsScreen(
                        systemHandler = viewModel.systemActionHandler,
                        isCustomKeyConfigured = customApiKey.isNotBlank(),
                        onSpeakFeedback = { line ->
                            viewModel.speechManager.speak(line)
                        }
                    )
                }
                MainTab.SETTINGS -> {
                    SettingsScreen(
                        currentPersonality = personality,
                        onSelectPersonality = { newPersonality ->
                            viewModel.setPersonality(newPersonality)
                        },
                        customApiKey = customApiKey,
                        onSaveApiKey = { newKey ->
                            viewModel.updateApiKey(newKey)
                        },
                        isBackgroundActive = isBackgroundActive,
                        onToggleBackground = { enable ->
                            viewModel.toggleBackgroundService(enable)
                        },
                        speechManager = viewModel.speechManager
                    )
                }
            }
        }
    }
}
