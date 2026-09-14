package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberFocus
import com.example.ui.theme.CalmMint
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.EmeraldVitality
import com.example.ui.theme.GentleSky
import com.example.ui.theme.MutedRose
import com.example.ui.theme.RhythmCyan
import com.example.ui.theme.SoftLilac
import com.example.ui.theme.WarmPeach

data class FeelingItem(
    val id: String,
    val name: String,
    val emoji: String,
    val level: Int, // 1 (lowest/most stressed) to 5 (optimal/balanced)
    val category: FeelingCategory,
    val subtitle: String,
    val routineTitle: String,
    val accentColor: Color
)

enum class FeelingCategory(val label: String) {
    ALL("All Feelings"),
    MIND("Mind & Calm"),
    ENERGY("Energy State"),
    BODY("Body & Sensation")
}

object FeelingCatalog {
    val allFeelings = listOf(
        FeelingItem(
            id = "calm",
            name = "Calm & Centered",
            emoji = "🧘",
            level = 5,
            category = FeelingCategory.MIND,
            subtitle = "Mindful, grounded, and present in the moment",
            routineTitle = "Mindful Breath & Neck Unwind",
            accentColor = CalmMint
        ),
        FeelingItem(
            id = "fatigued",
            name = "Fatigued & Drained",
            emoji = "🌙",
            level = 2,
            category = FeelingCategory.ENERGY,
            subtitle = "Low battery, weary eyes, need gentle revival",
            routineTitle = "Low-Energy Circulation Wakeup",
            accentColor = SoftLilac
        ),
        FeelingItem(
            id = "anxious",
            name = "Anxious & Overwhelmed",
            emoji = "🌊",
            level = 1,
            category = FeelingCategory.MIND,
            subtitle = "Racing thoughts, sensory overload, need ground",
            routineTitle = "Vagus Nerve & 4-7-8 Breathing Reset",
            accentColor = MutedRose
        ),
        FeelingItem(
            id = "steady",
            name = "Steady & Focused",
            emoji = "🎯",
            level = 4,
            category = FeelingCategory.MIND,
            subtitle = "Productive flow, locked in, posture support",
            routineTitle = "Desk Posture & Spine Lengthening",
            accentColor = GentleSky
        ),
        FeelingItem(
            id = "energized",
            name = "Energized & Vibrant",
            emoji = "⚡",
            level = 5,
            category = FeelingCategory.ENERGY,
            subtitle = "High vitality, upbeat tempo, ready for action",
            routineTitle = "Dynamic Dopamine Micro-Burst",
            accentColor = AmberFocus
        ),
        FeelingItem(
            id = "stiff",
            name = "Stiff & Aching",
            emoji = "🌿",
            level = 2,
            category = FeelingCategory.BODY,
            subtitle = "Desk hunch, tight traps, stiff hips",
            routineTitle = "Somatic Hip & Trap Tension Release",
            accentColor = EmeraldVitality
        ),
        FeelingItem(
            id = "restless",
            name = "Restless & Fidgety",
            emoji = "🌀",
            level = 3,
            category = FeelingCategory.BODY,
            subtitle = "Pent-up physical jitteriness, need discharge",
            routineTitle = "Standing Tension Shake-Out & Stretch",
            accentColor = WarmPeach
        ),
        FeelingItem(
            id = "joyful",
            name = "Joyful & Uplifted",
            emoji = "✨",
            level = 5,
            category = FeelingCategory.ENERGY,
            subtitle = "Heart is light, open posture, radiant energy",
            routineTitle = "Radiant Heart & Core Awakening",
            accentColor = RhythmCyan
        )
    )

    fun findByNameOrId(key: String): FeelingItem {
        val lower = key.lowercase()
        return allFeelings.firstOrNull {
            lower.contains(it.id) || it.name.lowercase().contains(lower) || lower.contains(it.name.lowercase())
        } ?: allFeelings[0]
    }
}
