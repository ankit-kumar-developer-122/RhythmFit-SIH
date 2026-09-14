package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardUser
import com.example.ui.components.GlassCard
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
import java.util.Locale

/**
 * iPhone-like Minimalist Community Leaderboard ("Arena").
 * Pushes everyone to work hard through healthy peer momentum,
 * dynamic gap callouts, weekly league promotions, podium rankings,
 * and high-five encouragement.
 */
@Composable
fun LeaderboardScreen(
    users: List<LeaderboardUser>,
    currentUserScore: Double,
    currentUserRoutinesCount: Int,
    isDarkMode: Boolean,
    onCheerUser: (String) -> Unit,
    onStartSprintRoutine: () -> Unit,
    isOnline: Boolean = true,
    isSyncing: Boolean = false,
    lastSyncTimestamp: Long = System.currentTimeMillis(),
    nextSyncCountdownSeconds: Int = 600,
    mongoStatusMessage: String = "Connected to MongoDB: Cluster0JR",
    onRefreshOnline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val blueAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    var selectedTimeframe by remember { mutableStateOf("WEEK") } // "TODAY", "WEEK", "ALL_TIME"

    // Sort users by score descending and re-index ranks
    val rankedUsers = remember(users, currentUserScore, currentUserRoutinesCount, selectedTimeframe) {
        val updated = users.map { user ->
            if (user.isCurrentUser) {
                user.copy(
                    score = when (selectedTimeframe) {
                        "TODAY" -> currentUserScore
                        "WEEK" -> currentUserScore * 3.4 + 110.0
                        else -> currentUserScore * 8.2 + 420.0
                    },
                    microBreaks = when (selectedTimeframe) {
                        "TODAY" -> currentUserRoutinesCount
                        "WEEK" -> currentUserRoutinesCount + 14
                        else -> currentUserRoutinesCount + 48
                    }
                )
            } else {
                user.copy(
                    score = when (selectedTimeframe) {
                        "TODAY" -> user.score
                        "WEEK" -> user.score * 3.2 + 95.0
                        else -> user.score * 7.8 + 390.0
                    },
                    microBreaks = when (selectedTimeframe) {
                        "TODAY" -> user.microBreaks
                        "WEEK" -> user.microBreaks + 12
                        else -> user.microBreaks + 42
                    }
                )
            }
        }.sortedByDescending { it.score }

        updated.mapIndexed { index, user ->
            user.copy(rank = index + 1)
        }
    }

    val currentUser = rankedUsers.find { it.isCurrentUser } ?: rankedUsers.first()
    val higherUser = rankedUsers.getOrNull(currentUser.rank - 2) // User immediately above current user
    val pointsToOvertake = if (higherUser != null) (higherUser.score - currentUser.score).coerceAtLeast(0.1) else 0.0

    val topThree = rankedUsers.take(3)
    val remainingUsers = rankedUsers.drop(3)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("leaderboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 0. MongoDB Online Synchronization Status Banner (Updates every 10 min)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                isDarkMode = isDarkMode,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Pulsing online dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSyncing) AmberFocus else if (isOnline) calmAccent else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isSyncing) "Syncing with MongoDB..." else "MongoDB Atlas (Cluster0JR)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isOnline) calmAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val minutes = nextSyncCountdownSeconds / 60
                            val seconds = nextSyncCountdownSeconds % 60
                            val timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                            Text(
                                text = "Auto-updates every 10 min • Next update in $timeStr",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Manual refresh button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isSyncing) { onRefreshOnline() }
                            .testTag("leaderboard_refresh_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = blueAccent.copy(alpha = if (isDarkMode) 0.18f else 0.10f),
                        border = BorderStroke(1.dp, blueAccent.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(13.dp),
                                    strokeWidth = 2.dp,
                                    color = blueAccent
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Leaderboard from MongoDB",
                                    modifier = Modifier.size(14.dp),
                                    tint = blueAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSyncing) "Syncing" else "Sync",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = blueAccent
                            )
                        }
                    }
                }
            }
        }
        // 1. Header & Division Status
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMMUNITY ARENA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.3.sp,
                                fontSize = 11.sp
                            ),
                            color = blueAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Diamond League",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // League Badge
                    Surface(
                        shape = CircleShape,
                        color = blueAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f),
                        border = BorderStroke(1.dp, blueAccent.copy(alpha = if (isDarkMode) 0.38f else 0.22f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AmberFocus,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Week 37 • Top 5%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = blueAccent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Friendly accountability that pushes everyone forward. Consistency beats intensity.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Dynamic Motivation "Push Yourself" Banner Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CoralPulse.copy(alpha = if (isDarkMode) 0.18f else 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = CoralPulse,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Your Position: Rank #${currentUser.rank}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", currentUser.score)} pts • ${currentUser.microBreaks} micro-breaks",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Flame Streak Pill
                        Surface(
                            shape = CircleShape,
                            color = CoralPulse.copy(alpha = if (isDarkMode) 0.16f else 0.10f),
                            border = BorderStroke(1.dp, CoralPulse.copy(alpha = if (isDarkMode) 0.35f else 0.20f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = CoralPulse,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentUser.streakDays}d Streak",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = CoralPulse
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Competitive Push Text
                    val motivationText = if (higherUser != null) {
                        "🔥 You are only ${String.format(Locale.getDefault(), "%.1f", pointsToOvertake)} pts behind ${higherUser.name} (Rank #${higherUser.rank})! Complete one 3-minute micro-break now to overtake."
                    } else {
                        "👑 You are leading the Diamond League! Keep your momentum alive to defend your #1 crown."
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                        border = BorderStroke(
                            1.dp,
                            if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = motivationText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // 3. Active Community Sprint Micro-Challenge
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                shape = RoundedCornerShape(22.dp)
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(calmAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = calmAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Daily Sprint Challenge",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Log 2 micro-breaks today for +40 League XP",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = calmAccent
                            )
                        }
                    }

                    Button(
                        onClick = onStartSprintRoutine,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = calmAccent),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Work Hard",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 4. iOS Segmented Timeframe Toggle Pill
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isDarkMode) DarkGlassSurface else LightGlassSurface,
                    border = BorderStroke(1.dp, if (isDarkMode) DarkGlassBorder else LightGlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "TODAY" to "Today",
                            "WEEK" to "This Week",
                            "ALL_TIME" to "All-Time"
                        ).forEach { (key, label) ->
                            val isSelected = selectedTimeframe == key
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { selectedTimeframe = key }
                                    .background(
                                        if (isSelected) calmAccent.copy(alpha = if (isDarkMode) 0.22f else 0.16f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (isSelected) calmAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Top 3 Podium Visual Showcase
        item {
            if (topThree.size >= 3) {
                PodiumSection(
                    first = topThree[0],
                    second = topThree[1],
                    third = topThree[2],
                    isDarkMode = isDarkMode,
                    onCheerUser = onCheerUser
                )
            }
        }

        // 6. Ranked Peers List
        item {
            Text(
                text = "COMMUNITY COMPETITORS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(remainingUsers, key = { it.id }) { user ->
            LeaderboardItemCard(
                user = user,
                isDarkMode = isDarkMode,
                onCheer = { onCheerUser(user.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

/**
 * Visual 3-place podium for Rank 1, 2, and 3.
 */
@Composable
private fun PodiumSection(
    first: LeaderboardUser,
    second: LeaderboardUser,
    third: LeaderboardUser,
    isDarkMode: Boolean,
    onCheerUser: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place (Silver)
        PodiumStep(
            user = second,
            rank = 2,
            rankColor = Color(0xFF9E9E9E),
            height = 135.dp,
            isDarkMode = isDarkMode,
            onCheer = { onCheerUser(second.id) },
            modifier = Modifier.weight(1f)
        )

        // 1st Place (Gold)
        PodiumStep(
            user = first,
            rank = 1,
            rankColor = AmberFocus,
            height = 160.dp,
            isDarkMode = isDarkMode,
            onCheer = { onCheerUser(first.id) },
            modifier = Modifier.weight(1.1f)
        )

        // 3rd Place (Bronze)
        PodiumStep(
            user = third,
            rank = 3,
            rankColor = Color(0xFFCD7F32),
            height = 120.dp,
            isDarkMode = isDarkMode,
            onCheer = { onCheerUser(third.id) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PodiumStep(
    user: LeaderboardUser,
    rank: Int,
    rankColor: Color,
    height: androidx.compose.ui.unit.Dp,
    isDarkMode: Boolean,
    onCheer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with medal badge
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                shape = CircleShape,
                color = rankColor.copy(alpha = if (isDarkMode) 0.22f else 0.16f),
                border = BorderStroke(2.dp, rankColor),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.avatarInitials,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = rankColor,
                modifier = Modifier.size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (user.isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (user.isCurrentUser) CalmGreenLight else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )

        Text(
            text = "${String.format(Locale.getDefault(), "%.0f", user.score)} pts",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = rankColor
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Frosted Column Base
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = if (isDarkMode) DarkGlassSurface else LightGlassSurface,
            border = BorderStroke(1.dp, rankColor.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${user.microBreaks} breaks",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Cheer High-Five Button
                Surface(
                    shape = CircleShape,
                    color = rankColor.copy(alpha = 0.15f),
                    modifier = Modifier.clip(CircleShape).clickable { onCheer() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👋", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${user.cheersReceived}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = rankColor
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardItemCard(
    user: LeaderboardUser,
    isDarkMode: Boolean,
    onCheer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val blueAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    val isUser = user.isCurrentUser
    val cardBorder = if (isUser) {
        BorderStroke(1.5.dp, calmAccent.copy(alpha = 0.6f))
    } else null

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(18.dp),
        border = cardBorder
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank number
                Text(
                    text = "#${user.rank}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    color = if (isUser) calmAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(32.dp)
                )

                // Avatar
                Surface(
                    shape = CircleShape,
                    color = if (isUser) calmAccent.copy(alpha = 0.20f) else blueAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (isUser) calmAccent else blueAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user.avatarInitials,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) calmAccent else blueAccent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isUser) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp,
                                letterSpacing = (-0.1).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = calmAccent.copy(alpha = 0.20f)
                            ) {
                                Text(
                                    text = "YOU",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = calmAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${user.microBreaks} micro-breaks • ${user.streakDays}d streak",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", user.score)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isUser) calmAccent else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "pts",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // High-Five Cheer Button
                Surface(
                    shape = CircleShape,
                    color = if (user.hasUserCheered) calmAccent.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(
                        1.dp,
                        if (user.hasUserCheered) calmAccent.copy(alpha = 0.5f) else Color.Transparent
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onCheer() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👋", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${user.cheersReceived}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (user.hasUserCheered) calmAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
