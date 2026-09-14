package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.MicroRoutine
import com.example.data.model.FeelingCatalog
import com.example.data.model.FeelingCategory
import com.example.data.model.FeelingItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.CalmBlue
import com.example.ui.theme.CalmBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.SoftLilac
import kotlinx.coroutines.delay

enum class BreathTechnique(
    val title: String,
    val inhaleSec: Int,
    val holdAfterInhaleSec: Int,
    val exhaleSec: Int,
    val holdAfterExhaleSec: Int,
    val description: String
) {
    RELAX_4_7_8(
        title = "4-7-8 Calm",
        inhaleSec = 4,
        holdAfterInhaleSec = 7,
        exhaleSec = 8,
        holdAfterExhaleSec = 0,
        description = "Activates vagal tone to dissolve anxiety and physical tension."
    ),
    BOX_BREATHING(
        title = "Box Breath",
        inhaleSec = 4,
        holdAfterInhaleSec = 4,
        exhaleSec = 4,
        holdAfterExhaleSec = 4,
        description = "Classic centering technique to steady thoughts and regain composure."
    ),
    VITALITY_PUMP(
        title = "Vitality",
        inhaleSec = 4,
        holdAfterInhaleSec = 0,
        exhaleSec = 2,
        holdAfterExhaleSec = 0,
        description = "Invigorating flow to lift midday fatigue and sharpen alertness."
    )
}

enum class BreathPhase(val prompt: String) {
    INHALE("Breathe In..."),
    HOLD_INHALE("Gently Hold..."),
    EXHALE("Release Out..."),
    HOLD_EXHALE("Rest Still...")
}

/**
 * iPhone-like Minimalist Sanctuary Screen.
 * Guided visual breathwork pacer with calm light green & blue orb,
 * 8-state feelings spectrum, and 60-second nervous system grounding.
 */
@Composable
fun MindAndBreatheScreen(
    currentMood: String,
    onSelectFeeling: (FeelingItem) -> Unit,
    onStartRoutine: (MicroRoutine) -> Unit,
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedTechnique by remember { mutableStateOf(BreathTechnique.RELAX_4_7_8) }
    var isBreathingActive by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf(BreathPhase.INHALE) }
    var secondsInPhase by remember { mutableIntStateOf(selectedTechnique.inhaleSec) }
    var completedCycles by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(FeelingCategory.ALL) }

    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val blueAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    // Breathing timer coroutine loop
    LaunchedEffect(isBreathingActive, selectedTechnique) {
        if (!isBreathingActive) {
            currentPhase = BreathPhase.INHALE
            secondsInPhase = selectedTechnique.inhaleSec
            return@LaunchedEffect
        }

        while (isBreathingActive) {
            // Phase 1: Inhale
            currentPhase = BreathPhase.INHALE
            for (sec in selectedTechnique.inhaleSec downTo 1) {
                secondsInPhase = sec
                delay(1000)
            }

            // Phase 2: Hold after Inhale (if > 0)
            if (selectedTechnique.holdAfterInhaleSec > 0) {
                currentPhase = BreathPhase.HOLD_INHALE
                for (sec in selectedTechnique.holdAfterInhaleSec downTo 1) {
                    secondsInPhase = sec
                    delay(1000)
                }
            }

            // Phase 3: Exhale
            currentPhase = BreathPhase.EXHALE
            for (sec in selectedTechnique.exhaleSec downTo 1) {
                secondsInPhase = sec
                delay(1000)
            }

            // Phase 4: Hold after Exhale (if > 0)
            if (selectedTechnique.holdAfterExhaleSec > 0) {
                currentPhase = BreathPhase.HOLD_EXHALE
                for (sec in selectedTechnique.holdAfterExhaleSec downTo 1) {
                    secondsInPhase = sec
                    delay(1000)
                }
            }

            completedCycles++
        }
    }

    // Dynamic scale for the breathing circle
    val targetScale = when (currentPhase) {
        BreathPhase.INHALE -> 1.30f
        BreathPhase.HOLD_INHALE -> 1.30f
        BreathPhase.EXHALE -> 0.88f
        BreathPhase.HOLD_EXHALE -> 0.88f
    }
    val animatedScale by animateFloatAsState(
        targetValue = if (isBreathingActive) targetScale else 1f,
        animationSpec = tween(
            durationMillis = if (currentPhase == BreathPhase.INHALE) selectedTechnique.inhaleSec * 1000
            else if (currentPhase == BreathPhase.EXHALE) selectedTechnique.exhaleSec * 1000
            else 600,
            easing = FastOutSlowInEasing
        ),
        label = "breath_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("mind_and_breathe_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Hero Sanctuary Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SANCTUARY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp,
                        fontSize = 11.sp
                    ),
                    color = blueAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Mind & Breath",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Restore somatic balance with guided paced breathing and mindful check-ins.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Interactive Visual Breathwork Pacer Card (Apple Mindfulness style)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interactive_breathwork_card"),
                isDarkMode = isDarkMode,
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row with Technique Title & Cycle Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(calmAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = calmAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Breathwork Pacer",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (isBreathingActive) {
                            Surface(
                                shape = CircleShape,
                                color = calmAccent.copy(alpha = if (isDarkMode) 0.16f else 0.12f),
                                border = BorderStroke(1.dp, calmAccent.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "$completedCycles cycles",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = calmAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // iOS-Style Glass Segmented Tabs for Techniques
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(BreathTechnique.entries.toTypedArray()) { technique ->
                            val isSelected = selectedTechnique == technique
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        if (selectedTechnique != technique) {
                                            selectedTechnique = technique
                                            isBreathingActive = false
                                            completedCycles = 0
                                        }
                                    },
                                shape = CircleShape,
                                color = if (isSelected) {
                                    calmAccent.copy(alpha = if (isDarkMode) 0.22f else 0.16f)
                                } else {
                                    if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) calmAccent else (if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f))
                                )
                            ) {
                                Text(
                                    text = technique.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) calmAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Animated Breathing Visual Sphere (Calm light green & light blue gradient orb)
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient pulsing halo
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .scale(animatedScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            calmAccent.copy(alpha = if (isBreathingActive) 0.35f else 0.15f),
                                            blueAccent.copy(alpha = if (isBreathingActive) 0.20f else 0.06f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Inner core glass orb
                        Box(
                            modifier = Modifier
                                .size(126.dp)
                                .scale(animatedScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            calmAccent,
                                            blueAccent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isBreathingActive) "$secondsInPhase" else "Start",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = if (isBreathingActive) "seconds" else "tap below",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Phase Text
                    Text(
                        text = if (isBreathingActive) currentPhase.prompt else selectedTechnique.description,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isBreathingActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        ),
                        color = if (isBreathingActive) calmAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                isBreathingActive = !isBreathingActive
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_breathing_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBreathingActive) {
                                    if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
                                } else calmAccent
                            ),
                            border = if (isBreathingActive) BorderStroke(1.dp, calmAccent) else null
                        ) {
                            Icon(
                                imageVector = if (isBreathingActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isBreathingActive) calmAccent else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBreathingActive) "Pause Pacer" else "Begin Breathing",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isBreathingActive) calmAccent else Color.White
                            )
                        }

                        if (isBreathingActive || completedCycles > 0) {
                            OutlinedButton(
                                onClick = {
                                    isBreathingActive = false
                                    completedCycles = 0
                                    currentPhase = BreathPhase.INHALE
                                    secondsInPhase = selectedTechnique.inhaleSec
                                },
                                shape = CircleShape,
                                border = BorderStroke(1.dp, if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Full Feelings Spectrum Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FEELINGS CATALOG (8 STATES)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp,
                        fontSize = 11.sp
                    ),
                    color = SoftLilac
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Somatic Emotional Spectrum",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FeelingCategory.entries.toTypedArray()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            shape = CircleShape,
                            label = { Text(category.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = calmAccent.copy(alpha = if (isDarkMode) 0.20f else 0.15f),
                                selectedLabelColor = calmAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == category,
                                borderColor = if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f),
                                selectedBorderColor = calmAccent
                            )
                        )
                    }
                }
            }
        }

        // Feeling Cards List
        val filteredFeelings = FeelingCatalog.allFeelings.filter {
            selectedCategory == FeelingCategory.ALL || it.category == selectedCategory
        }

        items(filteredFeelings, key = { it.id }) { feeling ->
            val isCurrent = currentMood.contains(feeling.name, ignoreCase = true) ||
                currentMood.contains(feeling.id, ignoreCase = true)

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feeling_card_${feeling.id}"),
                isDarkMode = isDarkMode,
                shape = RoundedCornerShape(22.dp),
                border = if (isCurrent) BorderStroke(1.5.dp, feeling.accentColor) else null,
                onClick = { onSelectFeeling(feeling) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(feeling.accentColor.copy(alpha = if (isDarkMode) 0.18f else 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = feeling.emoji,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = feeling.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.1).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = feeling.accentColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Active",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = feeling.accentColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = feeling.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Somatic Reset: ${feeling.routineTitle}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = feeling.accentColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Quick Select/Start action
                    IconButton(
                        onClick = { onSelectFeeling(feeling) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrent) Icons.Default.Check else Icons.Default.PlayArrow,
                            contentDescription = "Select feeling",
                            tint = feeling.accentColor
                        )
                    }
                }
            }
        }

        // 3. Somatic Sensory Grounding (5-4-3-2-1 Guide)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(calmAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = calmAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "60-Second Sensory Grounding",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = calmAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "When thoughts run ahead, gently anchor your nervous system back to immediate reality:\n" +
                            "• 5 things you can visually observe\n" +
                            "• 4 tactile contact points you can feel\n" +
                            "• 3 subtle ambient sounds\n" +
                            "• 2 slow chest & belly breaths\n" +
                            "• 1 gentle affirmation of presence",
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 20.sp,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
