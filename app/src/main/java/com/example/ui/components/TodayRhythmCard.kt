package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RhythmEngine
import com.example.ui.theme.AmberFocus
import com.example.ui.theme.CalmBlue
import com.example.ui.theme.CalmBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.CoralPulse

/**
 * iPhone-like Minimalist Frosted Glass Rhythm Consistency Card.
 * Displays Soft Consistency Score (0–100), harmonious calm blue & green gauge,
 * micro-break counters, and dynamic life context shield.
 */
@Composable
fun TodayRhythmCard(
    score: Double,
    category: RhythmEngine.RhythmCategory,
    completedRoutinesCount: Int,
    totalKcal: Int,
    steps: Int,
    hasFlaggedDampener: Boolean,
    dampenerReason: String?,
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val progressAnimated by animateFloatAsState(
        targetValue = (score / 100.0).toFloat().coerceIn(0.04f, 1f),
        animationSpec = tween(800),
        label = "scoreProgress"
    )

    val primaryAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val secondaryAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_rhythm_card"),
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(26.dp),
        elevation = if (isDarkMode) 0.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            // Top Section: Category Label & Multiplier Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RHYTHM CONSISTENCY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.3.sp,
                            fontSize = 11.sp
                        ),
                        color = secondaryAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Apple-style Glass Capsule: Multiplier
                Surface(
                    shape = CircleShape,
                    color = primaryAccent.copy(alpha = if (isDarkMode) 0.16f else 0.12f),
                    border = BorderStroke(1.dp, primaryAccent.copy(alpha = if (isDarkMode) 0.35f else 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+4.5x bonus",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = primaryAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center: Circular Glass Ring & Supportive Context
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glass Circular Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    // Track circle
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(100.dp),
                        color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                        strokeWidth = 9.dp,
                        strokeCap = StrokeCap.Round
                    )
                    // Active animated circle with calm green/blue
                    CircularProgressIndicator(
                        progress = { progressAnimated },
                        modifier = Modifier.size(100.dp),
                        color = primaryAccent,
                        strokeWidth = 9.dp,
                        strokeCap = StrokeCap.Round
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = score.toInt().toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "/100",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.1).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.tone,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Context Dampener Pill (Shield active)
            if (hasFlaggedDampener) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AmberFocus.copy(alpha = if (isDarkMode) 0.12f else 0.08f),
                    border = BorderStroke(1.dp, AmberFocus.copy(alpha = if (isDarkMode) 0.35f else 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Context Protection",
                            tint = AmberFocus,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grace Dampener Active: ${dampenerReason ?: "Context Flagged"} (-80% decay)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = AmberFocus
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // iOS-Style Minimalist Stat Capsule Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MinimalGlassStatChip(
                    icon = Icons.Default.CheckCircle,
                    value = "$completedRoutinesCount",
                    label = "Breaks",
                    tint = primaryAccent,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
                MinimalGlassStatChip(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "$totalKcal",
                    label = "kcal",
                    tint = CoralPulse,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
                MinimalGlassStatChip(
                    icon = Icons.Default.FitnessCenter,
                    value = "$steps",
                    label = "Steps",
                    tint = secondaryAccent,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MinimalGlassStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tint: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val chipBg = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    val chipBorder = if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = chipBg,
        border = BorderStroke(1.dp, chipBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = if (isDarkMode) 0.18f else 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
