package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberFocus
import com.example.ui.theme.CalmBlue
import com.example.ui.theme.CalmBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassSurface
import com.example.ui.theme.LightGlassBorder
import com.example.ui.theme.LightGlassSurface

/**
 * iPhone & Apple-Fitness inspired Circular Progress Indicator Component.
 * Visualizes the user's daily kcal goal completion.
 * Reacts automatically to Room database updates whenever any activity log is inserted or synced.
 */
@Composable
fun DailyKcalProgressCard(
    burnedKcal: Int,
    goalKcal: Int,
    todayLogsCount: Int,
    isDarkMode: Boolean,
    onQuickLogKcal: (name: String, durationMins: Int, kcal: Int) -> Unit,
    onUpdateGoalKcal: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeGoal = goalKcal.coerceAtLeast(50)
    val rawProgress = (burnedKcal.toFloat() / safeGoal.toFloat()).coerceAtLeast(0f)
    val clampedProgress = rawProgress.coerceIn(0f, 1f)
    val percentage = (rawProgress * 100).toInt()
    val isGoalAchieved = burnedKcal >= safeGoal

    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "kcalRingProgress"
    )

    var showGoalPresets by remember { mutableStateOf(false) }

    val ringGradients = remember(isDarkMode, isGoalAchieved) {
        if (isGoalAchieved) {
            listOf(
                CalmGreen,
                CalmGreenLight,
                AmberFocus
            )
        } else {
            listOf(
                CoralPulse,
                AmberFocus,
                if (isDarkMode) CalmGreenLight else CalmGreen
            )
        }
    }

    val primaryAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val secondaryAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .testTag("daily_kcal_progress_card"),
        shape = RoundedCornerShape(26.dp),
        color = if (isDarkMode) DarkGlassSurface else LightGlassSurface,
        border = BorderStroke(1.dp, if (isDarkMode) DarkGlassBorder else LightGlassBorder),
        shadowElevation = if (isDarkMode) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header: Section title, Goal pill, and Goal edit trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = CoralPulse,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "DAILY ENERGY EXPENDITURE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                fontSize = 10.5.sp
                            ),
                            color = CoralPulse
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isGoalAchieved) "Goal Completed!" else "Active Calorie Burn",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Goal Pill / Toggle presets
                Surface(
                    shape = CircleShape,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.05f),
                    border = BorderStroke(
                        1.dp,
                        if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showGoalPresets = !showGoalPresets }
                        .testTag("toggle_goal_presets_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Goal: ${safeGoal} kcal",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Adjust Goal",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Optional Animated Goal Presets Row
            AnimatedVisibility(
                visible = showGoalPresets,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Set daily target (saved to local Room profile):",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(300, 450, 600, 800).forEach { preset ->
                            val isSelected = safeGoal == preset
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) CoralPulse.copy(alpha = 0.20f)
                                else if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) CoralPulse else Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onUpdateGoalKcal(preset)
                                        showGoalPresets = false
                                    }
                                    .testTag("goal_preset_$preset")
                            ) {
                                Text(
                                    text = "$preset kcal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) CoralPulse else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Central Display: Circular Progress Indicator & Breakdown Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress Indicator Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(130.dp)
                        .testTag("kcal_circular_indicator")
                ) {
                    val trackColor = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                    val strokeWidthPx = 13.dp

                    // Custom Canvas Ring with Round Caps and Gradient Arc
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val strokePx = strokeWidthPx.toPx()
                        val diameter = size.minDimension - strokePx
                        val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                        val arcSize = Size(diameter, diameter)

                        // 1. Background Track Ring (Full 360)
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )

                        // 2. Active Animated Gradient Arc
                        val sweep = animatedProgress * 360f
                        if (sweep > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = ringGradients,
                                    center = Offset(size.width / 2f, size.height / 2f)
                                ),
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Inner Content: Flame icon, Current Burned Kcal, and Percentage
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isGoalAchieved) CalmGreen.copy(alpha = 0.18f)
                                    else CoralPulse.copy(alpha = 0.18f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGoalAchieved) Icons.Default.CheckCircle else Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = if (isGoalAchieved) CalmGreen else CoralPulse,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "$burnedKcal",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isGoalAchieved) CalmGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Detailed Summary Stack
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Headline Status
                    if (isGoalAchieved) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CalmGreen.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, CalmGreen.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CalmGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Goal Reached! (+${burnedKcal - safeGoal} surplus)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = CalmGreen
                                )
                            }
                        }
                    } else {
                        val remaining = (safeGoal - burnedKcal).coerceAtLeast(0)
                        Text(
                            text = "$remaining kcal remaining",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (isGoalAchieved)
                            "Great momentum! Your active burn exceeded today's baseline target."
                        else
                            "Calculated from MET values and your body weight profile in local Room database.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Secondary Mini-Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatMiniItem(label = "Target", value = "$safeGoal kcal")
                        StatMiniItem(label = "Logged", value = "$todayLogsCount sessions")
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Section: Instant Quick-Log Activities to test/add active burns into Room
            Text(
                text = "QUICK ADD TO ROOM",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickLogPill(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    label = "+45 kcal Walk",
                    isDarkMode = isDarkMode,
                    onClick = { onQuickLogKcal("Brisk Walk", 12, 45) },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_walk_button"
                )
                QuickLogPill(
                    icon = Icons.Default.FitnessCenter,
                    label = "+85 kcal Bodyweight",
                    isDarkMode = isDarkMode,
                    onClick = { onQuickLogKcal("Bodyweight Circuit", 15, 85) },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_bodyweight_button"
                )
                QuickLogPill(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "+120 kcal Cardio",
                    isDarkMode = isDarkMode,
                    onClick = { onQuickLogKcal("High-Pace Cardio", 18, 120) },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_cardio_button"
                )
            }
        }
    }
}

@Composable
private fun StatMiniItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun QuickLogPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val pillBg = if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    val pillBorder = if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = pillBg,
        border = BorderStroke(1.dp, pillBorder),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CoralPulse,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
