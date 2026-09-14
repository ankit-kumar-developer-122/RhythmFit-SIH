package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.vector.ImageVector

enum class RhythmTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val title: String
) {
    RHYTHM(
        route = "rhythm",
        label = "Rhythm",
        icon = Icons.Default.Spa,
        title = "RhythmFit Today"
    ),
    SANCTUARY(
        route = "sanctuary",
        label = "Mind",
        icon = Icons.Default.SelfImprovement,
        title = "Mind & Sanctuary"
    ),
    SCHEDULE(
        route = "schedule",
        label = "Schedule",
        icon = Icons.Default.CalendarMonth,
        title = "Timetable & Gaps"
    ),
    LEADERBOARD(
        route = "leaderboard",
        label = "Arena",
        icon = Icons.Default.EmojiEvents,
        title = "Community Arena"
    ),
    INSIGHTS(
        route = "insights",
        label = "Insights",
        icon = Icons.Default.Insights,
        title = "Insights & Harmony"
    )
}
