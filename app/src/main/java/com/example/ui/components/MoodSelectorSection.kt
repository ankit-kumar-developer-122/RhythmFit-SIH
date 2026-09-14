package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ai.MicroRoutine
import com.example.data.model.FeelingCatalog
import com.example.data.model.FeelingItem
import com.example.ui.theme.CalmBlue
import com.example.ui.theme.CalmBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.SoftLilac

/**
 * iPhone-like Minimalist Feelings Selector with Frosted Glass styling.
 * Displays quick states with 1-tap routine recommendations.
 */
@Composable
fun MoodSelectorSection(
    selectedMood: String,
    suggestedRoutine: MicroRoutine?,
    onMoodSelected: (Int, String) -> Unit,
    onStartRoutine: (MicroRoutine) -> Unit,
    isDarkMode: Boolean = true,
    onNavigateToSanctuary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val blueAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CHECK-IN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp,
                        fontSize = 11.sp
                    ),
                    color = blueAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "How are you feeling?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Minimal iOS Glass Pill for Sanctuary Link
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onNavigateToSanctuary() },
                shape = CircleShape,
                color = SoftLilac.copy(alpha = if (isDarkMode) 0.14f else 0.10f),
                border = BorderStroke(1.dp, SoftLilac.copy(alpha = if (isDarkMode) 0.35f else 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = SoftLilac,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "8 States",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = SoftLilac
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal scrollable feelings row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(FeelingCatalog.allFeelings, key = { it.id }) { feeling ->
                val isSelected = selectedMood.contains(feeling.name, ignoreCase = true) ||
                    selectedMood.contains(feeling.id, ignoreCase = true) ||
                    feeling.name.contains(selectedMood, ignoreCase = true)

                FeelingGlassPill(
                    feeling = feeling,
                    isSelected = isSelected,
                    isDarkMode = isDarkMode,
                    onClick = { onMoodSelected(feeling.level, feeling.name) }
                )
            }
        }

        // Context-aware Routine Suggestion based on selected feeling
        if (suggestedRoutine != null) {
            Spacer(modifier = Modifier.height(14.dp))
            SuggestedRoutineGlassCard(
                routine = suggestedRoutine,
                isDarkMode = isDarkMode,
                onStartRoutine = { onStartRoutine(suggestedRoutine) }
            )
        }
    }
}

@Composable
private fun FeelingGlassPill(
    feeling: FeelingItem,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillBg = if (isSelected) {
        feeling.accentColor.copy(alpha = if (isDarkMode) 0.22f else 0.16f)
    } else {
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.70f)
    }

    val pillBorder = if (isSelected) {
        feeling.accentColor.copy(alpha = if (isDarkMode) 0.75f else 0.65f)
    } else {
        if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("feeling_pill_${feeling.id}"),
        shape = RoundedCornerShape(18.dp),
        color = pillBg,
        border = BorderStroke(width = if (isSelected) 1.5.dp else 1.dp, color = pillBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = feeling.emoji,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = feeling.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isSelected) {
                        feeling.accentColor
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            )
        }
    }
}

@Composable
fun SuggestedRoutineGlassCard(
    routine: MicroRoutine,
    isDarkMode: Boolean,
    onStartRoutine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(calmAccent.copy(alpha = if (isDarkMode) 0.16f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = calmAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${routine.durationMins} MIN • ${routine.calorieEstimateStr}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = CoralPulse
                        )
                    }
                    Text(
                        text = routine.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.1).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tailored for ${routine.targetMood.lowercase()} state",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Minimalist Apple-style CTA Button
            Button(
                onClick = onStartRoutine,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = calmAccent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("start_routine_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Routine",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
