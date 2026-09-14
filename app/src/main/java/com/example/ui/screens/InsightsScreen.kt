package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.data.local.entity.ActivityLog
import com.example.data.local.entity.MoodEntry
import com.example.engine.RhythmEngine
import com.example.ui.components.GlassCard
import com.example.ui.components.WeeklyMoodEnergyLineChart
import com.example.ui.theme.AmberFocus
import com.example.ui.theme.CalmBlue
import com.example.ui.theme.CalmBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.RoseDampener
import com.example.ui.theme.SoftLilac
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * iPhone-like Minimalist Insights & Sustainable Harmony Screen.
 * Apple Health-style metric cards, soft-decay consistency score,
 * life context grace protection shield, and categorized activity timeline.
 */
@Composable
fun InsightsScreen(
    score: Double,
    category: RhythmEngine.RhythmCategory,
    completedRoutinesCount: Int,
    totalKcal: Int,
    totalSteps: Int,
    hasFlaggedDampener: Boolean,
    dampenerReason: String?,
    logs: List<ActivityLog>,
    weeklyMoods: List<MoodEntry> = emptyList(),
    weeklyLogs: List<ActivityLog> = emptyList(),
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf("ALL") }
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val blueAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    val filteredLogs = remember(logs, filterType) {
        when (filterType) {
            "MICRO" -> logs.filter { it.isMicroBreak }
            "WORKOUTS" -> logs.filter { !it.isMicroBreak }
            else -> logs
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("insights_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "INSIGHTS & HARMONY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp,
                        fontSize = 11.sp
                    ),
                    color = blueAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sustainable Rhythm",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Progress measured through calm consistency rather than punishing streaks.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Soft Consistency Harmony Glass Card (Apple Health-like)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SOFT CONSISTENCY SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 11.sp
                                ),
                                color = calmAccent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", score),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-1.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " / 100",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        // Apple-style Category Pill
                        Surface(
                            shape = CircleShape,
                            color = calmAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f),
                            border = BorderStroke(1.dp, calmAccent.copy(alpha = if (isDarkMode) 0.40f else 0.25f))
                        ) {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = calmAccent
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Unlike rigid apps that reset your streak to 0 on busy days, RhythmFit uses gentle soft-decay mathematics. Even a 2-minute stretch revitalizes your score immediately.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3 iPhone Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricGlassPill(
                            title = "Micro-Breaks",
                            value = "$completedRoutinesCount",
                            color = calmAccent,
                            isDarkMode = isDarkMode,
                            modifier = Modifier.weight(1f)
                        )
                        MetricGlassPill(
                            title = "Calories",
                            value = "$totalKcal",
                            color = CoralPulse,
                            isDarkMode = isDarkMode,
                            modifier = Modifier.weight(1f)
                        )
                        MetricGlassPill(
                            title = "Steps",
                            value = "$totalSteps",
                            color = blueAccent,
                            isDarkMode = isDarkMode,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Context Shield / Grace Dampening Glass Card
        item {
            val shieldColor = if (hasFlaggedDampener) RoseDampener else blueAccent

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                shape = RoundedCornerShape(24.dp),
                border = if (hasFlaggedDampener) BorderStroke(1.dp, RoseDampener.copy(alpha = 0.5f)) else null
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(shieldColor.copy(alpha = if (isDarkMode) 0.18f else 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = shieldColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Life Context Protection",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.1).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (hasFlaggedDampener) "Active: $dampenerReason" else "Ready when life happens",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = if (hasFlaggedDampener) RoseDampener else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = shieldColor.copy(alpha = if (isDarkMode) 0.18f else 0.12f),
                            border = BorderStroke(1.dp, shieldColor.copy(alpha = if (isDarkMode) 0.40f else 0.25f))
                        ) {
                            Text(
                                text = if (hasFlaggedDampener) "80% Shield" else "Shield Ready",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = shieldColor
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "When you log life context like 'exam stress', 'illness', or 'travel', RhythmFit dampens score decay by 80%. Health honors seasons of rest as deeply as seasons of movement.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Weekly Mood & Energy vs Activity Line Chart (Recharts / D3 Native Spline)
        item {
            WeeklyMoodEnergyLineChart(
                weeklyMoods = weeklyMoods,
                weeklyLogs = weeklyLogs,
                isDarkMode = isDarkMode
            )
        }

        // 4. Activity Timeline with Filter Chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ACTIVITY HISTORY (${filteredLogs.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filterType == "ALL",
                            onClick = { filterType = "ALL" },
                            shape = CircleShape,
                            label = { Text("All Activities", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = calmAccent.copy(alpha = if (isDarkMode) 0.20f else 0.15f),
                                selectedLabelColor = calmAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterType == "ALL",
                                borderColor = if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f),
                                selectedBorderColor = calmAccent
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterType == "MICRO",
                            onClick = { filterType = "MICRO" },
                            shape = CircleShape,
                            label = { Text("Micro-Breaks", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = calmAccent.copy(alpha = if (isDarkMode) 0.20f else 0.15f),
                                selectedLabelColor = calmAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterType == "MICRO",
                                borderColor = if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f),
                                selectedBorderColor = calmAccent
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterType == "WORKOUTS",
                            onClick = { filterType = "WORKOUTS" },
                            shape = CircleShape,
                            label = { Text("Workouts", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = calmAccent.copy(alpha = if (isDarkMode) 0.20f else 0.15f),
                                selectedLabelColor = calmAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterType == "WORKOUTS",
                                borderColor = if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f),
                                selectedBorderColor = calmAccent
                            )
                        )
                    }
                }
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                GlassCard(
                    shape = RoundedCornerShape(20.dp),
                    isDarkMode = isDarkMode,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No activities match this filter yet",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                val timeFormatted = remember(log.timestamp) {
                    SimpleDateFormat("MMM d • h:mm a", Locale.getDefault()).format(Date(log.timestamp))
                }

                GlassCard(
                    shape = RoundedCornerShape(20.dp),
                    isDarkMode = isDarkMode,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (log.isMicroBreak) calmAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f)
                                        else blueAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (log.isMicroBreak) Icons.Default.Spa else Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = if (log.isMicroBreak) calmAccent else blueAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = log.activityName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.1).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${log.durationMins} mins • $timeFormatted",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${log.estimatedKcal} kcal",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CoralPulse
                                )
                            )
                            if (log.steps > 0) {
                                Text(
                                    text = "${log.steps} steps",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun MetricGlassPill(
    title: String,
    value: String,
    color: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = if (isDarkMode) 0.12f else 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = if (isDarkMode) 0.30f else 0.20f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
