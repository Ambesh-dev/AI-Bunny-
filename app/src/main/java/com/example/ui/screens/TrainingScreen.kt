package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memory.BunyMemoryManager
import com.example.memory.TaughtKnowledge
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet

@Composable
fun TrainingScreen(
    memoryManager: BunyMemoryManager,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var questionInput by remember { mutableStateOf("") }
    var answerInput by remember { mutableStateOf("") }
    val memories by memoryManager.memories.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = NeonGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Buny Brain Training 🎓",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Dost ki tarah Buny ko nayi baatein sikhao!",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }

        // Training Form Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Teach a New Question & Answer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    placeholder = { Text("Jab main pucho (e.g. mera dost kaun hai)", fontSize = 12.sp, color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("train_question_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = answerInput,
                    onValueChange = { answerInput = it },
                    placeholder = { Text("Toh Buny ye bole (e.g. Aapka dost Aman hai)", fontSize = 12.sp, color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("train_answer_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (questionInput.isNotBlank() && answerInput.isNotBlank()) {
                            memoryManager.learn(questionInput, answerInput)
                            val speech = "Samajh gaya bhai! Abse '$questionInput' puchne par Buny hamesha ye answer dega! Mast sikhaya tune! 🎓"
                            onSpeak(speech)
                            questionInput = ""
                            answerInput = ""
                        }
                    },
                    enabled = questionInput.isNotBlank() && answerInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        disabledContainerColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teach_buny_button")
                ) {
                    Text(
                        text = "Buny ko Sikhao 🧠✨",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Voice Teaching Quick Tip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NeonViolet.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = NeonGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bolke sikhao: Chat ya Mic me bas bolo \"Buny seekh lo: [Question] ka matlab [Answer] hota hai\"",
                    fontSize = 11.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 15.sp
                )
            }
        }

        // Learned Memories List Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Buny ki Memory (${memories.size} Baatein)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )

            if (memories.isNotEmpty()) {
                Text(
                    text = "Clear All",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .clickable { memoryManager.clearAll() }
                        .padding(4.dp)
                )
            }
        }

        // List of Memories
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(memories, key = { it.id }) { item ->
                MemoryItemCard(
                    item = item,
                    onPlay = { onSpeak(item.answer) },
                    onDelete = { memoryManager.forget(item.id) }
                )
            }
        }
    }
}

@Composable
fun MemoryItemCard(
    item: TaughtKnowledge,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Q: ${item.question}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A: ${item.answer}",
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 17.sp
                    )
                }

                Row {
                    IconButton(onClick = onPlay, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Hear",
                            tint = NeonGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
