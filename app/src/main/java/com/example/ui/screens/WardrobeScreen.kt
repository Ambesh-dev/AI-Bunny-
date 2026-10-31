package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BunyWardrobeState
import com.example.model.WardrobeCatalog
import com.example.model.WardrobeCategory
import com.example.model.WardrobeItem
import com.example.ui.avatar.BunyAvatarView
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet

@Composable
fun WardrobeScreen(
    wardrobe: BunyWardrobeState,
    isSpeaking: Boolean,
    emotion: String,
    gesture: String,
    onEquipItem: (WardrobeCategory, String) -> Unit,
    onAvatarTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(WardrobeCategory.OUTFIT) }

    val currentItems = when (selectedCategory) {
        WardrobeCategory.OUTFIT -> WardrobeCatalog.outfits
        WardrobeCategory.HEADWEAR -> WardrobeCatalog.headwears
        WardrobeCategory.GLASSES -> WardrobeCatalog.glasses
        WardrobeCategory.ACCESSORY -> WardrobeCatalog.accessories
        WardrobeCategory.AURA -> WardrobeCatalog.auras
    }

    val currentEquippedId = when (selectedCategory) {
        WardrobeCategory.OUTFIT -> wardrobe.outfitId
        WardrobeCategory.HEADWEAR -> wardrobe.headwearId
        WardrobeCategory.GLASSES -> wardrobe.glassesId
        WardrobeCategory.ACCESSORY -> wardrobe.accessoryId
        WardrobeCategory.AURA -> wardrobe.auraId
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // 1. Compact Live Avatar Dressing Mirror Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CyberSurface)
                .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            BunyAvatarView(
                isSpeaking = isSpeaking,
                emotion = emotion,
                gesture = gesture,
                wardrobe = wardrobe,
                onAvatarTapped = onAvatarTapped,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Category Tabs Scrollable Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WardrobeCategory.values().forEach { category ->
                val isSelected = category == selectedCategory
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) NeonPink else CyberSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NeonPink else CyberBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedCategory = category }
                        .testTag("wardrobe_cat_${category.name}")
                ) {
                    Text(
                        text = category.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.LightGray,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Grid of Wardrobe Items
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(currentItems, key = { it.id }) { item ->
                val isEquipped = item.id == currentEquippedId
                WardrobeItemCard(
                    item = item,
                    isEquipped = isEquipped,
                    onSelect = { onEquipItem(selectedCategory, item.id) }
                )
            }
        }
    }
}

@Composable
fun WardrobeItemCard(
    item: WardrobeItem,
    isEquipped: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) Color(0xFF261347) else CyberSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isEquipped) 1.5.dp else 1.dp,
            if (isEquipped) NeonCyan else CyberBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("wardrobe_item_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon & Color preview
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.primaryColor != 0L) Color(item.primaryColor).copy(alpha = 0.25f)
                        else CyberBorder
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.iconEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White,
                maxLines = 1
            )

            Text(
                text = item.description,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 2,
                lineHeight = 15.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Action / Status
            if (isEquipped) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Text(
                        text = "Equipped ✨",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Wear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
