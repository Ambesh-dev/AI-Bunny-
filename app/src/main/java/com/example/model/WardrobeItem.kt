package com.example.model

import androidx.compose.ui.graphics.Color

enum class WardrobeCategory(val displayName: String) {
    OUTFIT("Dress / Outfit"),
    HEADWEAR("Headwear / Caps"),
    GLASSES("Glasses / Shades"),
    ACCESSORY("Accessories"),
    AURA("Glow / Aura")
}

data class WardrobeItem(
    val id: String,
    val name: String,
    val description: String,
    val category: WardrobeCategory,
    val primaryColor: Long,
    val secondaryColor: Long = 0xFF000000,
    val iconEmoji: String = "✨"
)

object WardrobeCatalog {
    val outfits = listOf(
        WardrobeItem(
            id = "outfit_cyber",
            name = "Neon Cyber Jacket",
            description = "High-tech jacket with glowing fiber-optic seams",
            category = WardrobeCategory.OUTFIT,
            primaryColor = 0xFF1E1035,
            secondaryColor = 0xFF00E5FF,
            iconEmoji = "🧥"
        ),
        WardrobeItem(
            id = "outfit_hoodie",
            name = "Chill Street Hoodie",
            description = "Oversized cozy street hoodie for relaxing",
            category = WardrobeCategory.OUTFIT,
            primaryColor = 0xFFE11D48,
            secondaryColor = 0xFFFFFFFF,
            iconEmoji = "👕"
        ),
        WardrobeItem(
            id = "outfit_sherwani",
            name = "Royal Kurta Sherwani",
            description = "Traditional royal festive attire with gold embroidery",
            category = WardrobeCategory.OUTFIT,
            primaryColor = 0xFF7C2D12,
            secondaryColor = 0xFFFFD700,
            iconEmoji = "🥻"
        ),
        WardrobeItem(
            id = "outfit_suit",
            name = "Agent 007 Tuxedo",
            description = "Ultra sharp black suit and crisp collar",
            category = WardrobeCategory.OUTFIT,
            primaryColor = 0xFF09090B,
            secondaryColor = 0xFFFFFFFF,
            iconEmoji = "🤵"
        ),
        WardrobeItem(
            id = "outfit_gamer",
            name = "Esports Gamer Jersey",
            description = "Custom esports jersey with team logo",
            category = WardrobeCategory.OUTFIT,
            primaryColor = 0xFF10B981,
            secondaryColor = 0xFF000000,
            iconEmoji = "🎮"
        )
    )

    val headwears = listOf(
        WardrobeItem(
            id = "head_none",
            name = "Natural Ears",
            description = "Free and fluffy bunny ears",
            category = WardrobeCategory.HEADWEAR,
            primaryColor = 0x00000000,
            iconEmoji = "🐰"
        ),
        WardrobeItem(
            id = "head_cap",
            name = "Backward Cap",
            description = "Cool swagger cap turned backward",
            category = WardrobeCategory.HEADWEAR,
            primaryColor = 0xFFDC2626,
            secondaryColor = 0xFFFFFFFF,
            iconEmoji = "🧢"
        ),
        WardrobeItem(
            id = "head_crown",
            name = "Golden Crown",
            description = "King Buny's imperial gold crown",
            category = WardrobeCategory.HEADWEAR,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFEF4444,
            iconEmoji = "👑"
        ),
        WardrobeItem(
            id = "head_beanie",
            name = "Winter Beanie",
            description = "Warm knitted woolen beanie",
            category = WardrobeCategory.HEADWEAR,
            primaryColor = 0xFF0284C7,
            secondaryColor = 0xFFE0F2FE,
            iconEmoji = "🧶"
        ),
        WardrobeItem(
            id = "head_ninja",
            name = "Ninja Headband",
            description = "Symbol of Buny's secret stealth missions",
            category = WardrobeCategory.HEADWEAR,
            primaryColor = 0xFF18181B,
            secondaryColor = 0xFFE11D48,
            iconEmoji = "🥷"
        )
    )

    val glasses = listOf(
        WardrobeItem(
            id = "glass_none",
            name = "Bare Eyes",
            description = "Natural expressive eyes",
            category = WardrobeCategory.GLASSES,
            primaryColor = 0x00000000,
            iconEmoji = "👀"
        ),
        WardrobeItem(
            id = "glass_cyber",
            name = "Neon Cyber Visor",
            description = "Holographic HUD visor showing data streams",
            category = WardrobeCategory.GLASSES,
            primaryColor = 0xFF00E5FF,
            secondaryColor = 0xFFFF007F,
            iconEmoji = "🕶️"
        ),
        WardrobeItem(
            id = "glass_thug",
            name = "Thug Life Pixel Shades",
            description = "Retro black pixel shades for savage moments",
            category = WardrobeCategory.GLASSES,
            primaryColor = 0xFF000000,
            secondaryColor = 0xFFFFFFFF,
            iconEmoji = "😎"
        ),
        WardrobeItem(
            id = "glass_nerd",
            name = "Smart Round Wireframes",
            description = "Gold circular spectacles for high IQ mode",
            category = WardrobeCategory.GLASSES,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0x55000000,
            iconEmoji = "👓"
        ),
        WardrobeItem(
            id = "glass_aviator",
            name = "Top Gun Aviators",
            description = "Classic mirrored aviator sunglasses",
            category = WardrobeCategory.GLASSES,
            primaryColor = 0xFF334155,
            secondaryColor = 0xFF94A3B8,
            iconEmoji = "🛩️"
        )
    )

    val accessories = listOf(
        WardrobeItem(
            id = "acc_none",
            name = "No Neckwear",
            description = "Simple and clean",
            category = WardrobeCategory.ACCESSORY,
            primaryColor = 0x00000000,
            iconEmoji = "⚪"
        ),
        WardrobeItem(
            id = "acc_gold_chain",
            name = "Heavy Gold Chain",
            description = "Blingy hip-hop chain with diamond dollar charm",
            category = WardrobeCategory.ACCESSORY,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFF59E0B,
            iconEmoji = "🪙"
        ),
        WardrobeItem(
            id = "acc_headset",
            name = "Pro Gaming Headset",
            description = "RGB lit headset with noise-cancelling boom mic",
            category = WardrobeCategory.ACCESSORY,
            primaryColor = 0xFF1E293B,
            secondaryColor = 0xFF00E5FF,
            iconEmoji = "🎧"
        ),
        WardrobeItem(
            id = "acc_bowtie",
            name = "Ruby Silk Bowtie",
            description = "Sophisticated red silk bowtie",
            category = WardrobeCategory.ACCESSORY,
            primaryColor = 0xFFDC2626,
            secondaryColor = 0xFF991B1B,
            iconEmoji = "🎀"
        )
    )

    val auras = listOf(
        WardrobeItem(
            id = "aura_violet",
            name = "Cyber Violet Pulse",
            description = "Mystical cybernetic purple glow",
            category = WardrobeCategory.AURA,
            primaryColor = 0xFFA855F7,
            iconEmoji = "🟣"
        ),
        WardrobeItem(
            id = "aura_cyan",
            name = "Electric Cyan Wave",
            description = "High energy quantum electric aura",
            category = WardrobeCategory.AURA,
            primaryColor = 0xFF00E5FF,
            iconEmoji = "🔵"
        ),
        WardrobeItem(
            id = "aura_gold",
            name = "Super Saiyan Solar",
            description = "Radiant golden energy field",
            category = WardrobeCategory.AURA,
            primaryColor = 0xFFFFB703,
            iconEmoji = "🟡"
        ),
        WardrobeItem(
            id = "aura_pink",
            name = "Neon Sakura Charm",
            description = "Sweet romantic pink starlight aura",
            category = WardrobeCategory.AURA,
            primaryColor = 0xFFFF2A85,
            iconEmoji = "💖"
        )
    )
}

data class BunyWardrobeState(
    val outfitId: String = "outfit_cyber",
    val headwearId: String = "head_none",
    val glassesId: String = "glass_cyber",
    val accessoryId: String = "acc_gold_chain",
    val auraId: String = "aura_violet"
)
