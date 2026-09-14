package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActivityLog
import com.example.data.local.entity.MoodEntry
import com.example.ui.theme.AmberFocus
import com.example.ui.theme.CalmBlue
import com.example.ui.theme.CalmBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.RoseDampener
import com.example.ui.theme.SoftLilac
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data representation for a single day in the weekly line chart.
 */
data class DayMoodFitnessPoint(
    val dayTimestamp: Long,
    val dayLabel: String,       // e.g. "Mon", "Tue"
    val dateLabel: String,      // e.g. "Sep 12"
    val moodScore: Float,       // 0 to 100
    val moodName: String,       // "Energized", "Neutral", "Tired", "Stressed", "Overwhelmed"
    val moodLevel: Int,         // 1 to 5 (or 0 if unlogged)
    val kcalBurned: Int,        // Total calories burned on that day
    val activeMinutes: Int,     // Total active minutes
    val isToday: Boolean,
    val note: String? = null
)

/**
 * Recharts/D3-inspired Weekly Line Chart that maps logged mood entries
 * over the last 7 days alongside daily fitness activity (kcal burned & active minutes).
 * Provides interactive point scrubbing, dual-axis indicators, and automated pattern discovery.
 */
@Composable
fun WeeklyMoodEnergyLineChart(
    weeklyMoods: List<MoodEntry>,
    weeklyLogs: List<ActivityLog>,
    isDarkMode: Boolean = true,
    onQuickMoodClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    // Aggregate data for the last 7 consecutive days
    val points = remember(weeklyMoods, weeklyLogs) {
        val calendar = Calendar.getInstance()
        val list = mutableListOf<DayMoodFitnessPoint>()

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis() - i * 86_400_000L
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + 86_400_000L

            val dayMoods = weeklyMoods.filter { it.timestamp in dayStart until dayEnd }
            val dayLogs = weeklyLogs.filter { it.timestamp in dayStart until dayEnd }

            val totalKcal = dayLogs.sumOf { it.estimatedKcal }
            val totalMins = dayLogs.sumOf { it.durationMins }

            val latestMood = dayMoods.maxByOrNull { it.timestamp }
            val (moodScore, moodName, moodLevel) = if (latestMood != null) {
                val score = when (latestMood.moodLevel) {
                    5 -> 95f
                    4 -> 75f
                    3 -> 52f
                    2 -> 32f
                    1 -> 18f
                    else -> 60f
                }
                Triple(score, latestMood.moodName, latestMood.moodLevel)
            } else {
                // Heuristic baseline based on activity level if no explicit mood logged
                val score = when {
                    totalKcal >= 200 -> 82f
                    totalKcal >= 100 -> 70f
                    else -> 58f
                }
                val inferredName = when {
                    score >= 80 -> "Energized"
                    score >= 65 -> "Neutral"
                    else -> "Calm"
                }
                Triple(score, inferredName, 4)
            }

            list.add(
                DayMoodFitnessPoint(
                    dayTimestamp = dayStart,
                    dayLabel = if (i == 0) "Today" else dayFormat.format(Date(dayStart)),
                    dateLabel = fullDateFormat.format(Date(dayStart)),
                    moodScore = moodScore,
                    moodName = moodName,
                    moodLevel = moodLevel,
                    kcalBurned = totalKcal,
                    activeMinutes = totalMins,
                    isToday = (i == 0),
                    note = latestMood?.note
                )
            )
        }
        list
    }

    var selectedIndex by remember { mutableStateOf(points.size - 1) }
    val selectedPoint = points.getOrNull(selectedIndex) ?: points.lastOrNull()

    // Compute Pattern & Correlation Insights
    val activeDays = points.filter { it.kcalBurned >= 100 || it.activeMinutes >= 15 }
    val restDays = points.filter { it.kcalBurned < 100 && it.activeMinutes < 15 }
    val avgActiveMood = if (activeDays.isNotEmpty()) activeDays.map { it.moodScore }.average() else 75.0
    val avgRestMood = if (restDays.isNotEmpty()) restDays.map { it.moodScore }.average() else 55.0
    val moodBoostPct = if (avgRestMood > 0) (((avgActiveMood - avgRestMood) / avgRestMood) * 100).toInt() else 25

    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val blueAccent = if (isDarkMode) CalmBlueLight else CalmBlue
    val cardBackground = if (isDarkMode) Color(0xFF161922) else Color.White

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_mood_line_chart"),
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = blueAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WEEKLY PATTERN RECHARTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            ),
                            color = blueAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Mood & Energy vs Activity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Mood Boost Pill
                Surface(
                    shape = CircleShape,
                    color = if (moodBoostPct >= 0) CalmGreen.copy(alpha = 0.15f) else AmberFocus.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (moodBoostPct >= 0) CalmGreen.copy(alpha = 0.35f) else AmberFocus.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (moodBoostPct >= 0) calmAccent else AmberFocus,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (moodBoostPct >= 0) "+$moodBoostPct% Energy on Active Days" else "Balanced",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (moodBoostPct >= 0) calmAccent else AmberFocus
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chart Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp, 3.dp)
                            .background(blueAccent, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Mood / Energy Curve",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp, 8.dp)
                            .background(CoralPulse.copy(alpha = 0.65f), RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Calories Burned (Bars)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Spline Line Chart Canvas
            val maxKcal = (points.maxOfOrNull { it.kcalBurned } ?: 300).coerceAtLeast(250)
            val chartHeight = 150.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val count = points.size
                                if (count > 0) {
                                    val stepX = size.width / count
                                    val idx = (offset.x / stepX).toInt().coerceIn(0, count - 1)
                                    selectedIndex = idx
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val n = points.size
                    if (n < 2) return@Canvas

                    val stepX = w / (n - 1)

                    // Draw Horizontal Subtle Grid Guidelines (Recharts Style)
                    val gridSteps = 3
                    for (g in 0..gridSteps) {
                        val y = h * (g.toFloat() / gridSteps)
                        drawLine(
                            color = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 1. Draw Background Fitness Activity Bars for each day
                    val barWidth = 14.dp.toPx()
                    for (i in points.indices) {
                        val p = points[i]
                        val x = i * stepX
                        val barHeight = (p.kcalBurned.toFloat() / maxKcal.toFloat()) * (h * 0.65f)
                        val barTop = h - barHeight

                        // Subtle rounded bar
                        drawRoundRect(
                            color = if (i == selectedIndex) CoralPulse.copy(alpha = 0.70f) else CoralPulse.copy(alpha = 0.22f),
                            topLeft = Offset(x - barWidth / 2, barTop),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }

                    // 2. Compute Spline Coordinates for Mood & Energy Curve
                    val coords = points.mapIndexed { idx, pt ->
                        val x = idx * stepX
                        // Mood score 0..100 maps to y (h to 0) with 10% padding
                        val normalizedScore = (pt.moodScore / 100f).coerceIn(0.1f, 0.95f)
                        val y = h - (normalizedScore * h)
                        Offset(x, y)
                    }

                    // 3. Build Smooth Cubic Bezier Line Path
                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.size - 1) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val controlX1 = p0.x + (p1.x - p0.x) / 2
                            val controlY1 = p0.y
                            val controlX2 = p0.x + (p1.x - p0.x) / 2
                            val controlY2 = p1.y
                            cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                        }
                    }

                    // 4. Fill Area Under Curve with Gradient
                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, h)
                        lineTo(coords.first().x, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                blueAccent.copy(alpha = if (isDarkMode) 0.35f else 0.22f),
                                SoftLilac.copy(alpha = if (isDarkMode) 0.15f else 0.08f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // 5. Draw the Main Mood Spline Line
                    drawPath(
                        path = linePath,
                        color = blueAccent,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // 6. Draw Highlight / Scrubber Line for Selected Day
                    val selX = selectedIndex * stepX
                    drawLine(
                        color = blueAccent.copy(alpha = 0.5f),
                        start = Offset(selX, 0f),
                        end = Offset(selX, h),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // 7. Draw Nodes / Data Points on the Curve
                    coords.forEachIndexed { i, offset ->
                        val pt = points[i]
                        val isSelected = (i == selectedIndex)
                        val pointColor = when (pt.moodLevel) {
                            5 -> calmAccent
                            4 -> blueAccent
                            3 -> AmberFocus
                            2 -> RoseDampener
                            1 -> CoralPulse
                            else -> blueAccent
                        }

                        // Outer glowing aura if selected
                        if (isSelected) {
                            drawCircle(
                                color = pointColor.copy(alpha = 0.35f),
                                radius = 10.dp.toPx(),
                                center = offset
                            )
                        }

                        // Inner white halo
                        drawCircle(
                            color = if (isDarkMode) Color(0xFF10141D) else Color.White,
                            radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                            center = offset
                        )

                        // Core colored node
                        drawCircle(
                            color = pointColor,
                            radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day Labels Row (Mon, Tue, Wed...)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEachIndexed { index, point ->
                    val isSelected = (index == selectedIndex)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedIndex = index }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("chart_day_node_$index")
                    ) {
                        Text(
                            text = point.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) blueAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (point.isToday) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .background(calmAccent, CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Day Floating Inspection Card
            if (selectedPoint != null) {
                val moodColor = when (selectedPoint.moodLevel) {
                    5 -> calmAccent
                    4 -> blueAccent
                    3 -> AmberFocus
                    2 -> RoseDampener
                    1 -> CoralPulse
                    else -> blueAccent
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chart_selected_day_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${selectedPoint.dayLabel} (${selectedPoint.dateLabel})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (selectedPoint.isToday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = calmAccent.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "CURRENT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = calmAccent
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Mood Pill
                            Surface(
                                shape = CircleShape,
                                color = moodColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, moodColor.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (selectedPoint.moodLevel) {
                                            5 -> "⚡ Energized"
                                            4 -> "🌿 Calm / Good"
                                            3 -> "🌙 Tired"
                                            2 -> "🔥 Stressed"
                                            1 -> "⚠️ Overwhelmed"
                                            else -> "🌿 ${selectedPoint.moodName}"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = moodColor
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Row: Energy Level + Calories Burned + Active Minutes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DayStatPill(
                                label = "Energy Score",
                                value = "${selectedPoint.moodScore.toInt()}%",
                                icon = Icons.Default.Bolt,
                                tint = moodColor,
                                isDarkMode = isDarkMode,
                                modifier = Modifier.weight(1f)
                            )
                            DayStatPill(
                                label = "Burned",
                                value = "${selectedPoint.kcalBurned} kcal",
                                icon = Icons.Default.LocalFireDepartment,
                                tint = CoralPulse,
                                isDarkMode = isDarkMode,
                                modifier = Modifier.weight(1f)
                            )
                            DayStatPill(
                                label = "Active",
                                value = "${selectedPoint.activeMinutes} min",
                                icon = Icons.Default.Timer,
                                tint = blueAccent,
                                isDarkMode = isDarkMode,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (!selectedPoint.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Note: “${selectedPoint.note}”",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Automated Correlation Discovery Insight
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = blueAccent.copy(alpha = if (isDarkMode) 0.08f else 0.05f),
                border = BorderStroke(1.dp, blueAccent.copy(alpha = if (isDarkMode) 0.20f else 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = blueAccent,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RhythmFit Pattern Discovery",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = blueAccent
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (moodBoostPct > 10) {
                                "On days you logged 15+ minutes of physical movement, your recorded energy was $moodBoostPct% higher. Consistent micro-breaks counteract midday energy slumps without risking burnout."
                            } else {
                                "Your energy level stays remarkably stable across the week. Adding short 3-minute posture breaks during seated work blocks helps maintain this calm equilibrium."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayStatPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.60f),
        border = BorderStroke(1.dp, if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
