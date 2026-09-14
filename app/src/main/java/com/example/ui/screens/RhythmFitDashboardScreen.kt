package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.ActivityLog
import com.example.ui.RhythmFitViewModel
import com.example.ui.components.DailyKcalProgressCard
import com.example.ui.components.DailyQuotePopup
import com.example.ui.components.DailyWellWishingQuoteCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassMeshBackground
import com.example.ui.components.IosThemeToggle
import com.example.ui.components.MoodSelectorSection
import com.example.ui.components.MultiModalLoggerSection
import com.example.ui.components.RoutinePlayerBottomSheet
import com.example.ui.components.SupportiveMentalHealthBottomSheet
import com.example.ui.components.TimetableEditorDialog
import com.example.ui.components.TodayRhythmCard
import com.example.ui.components.WeeklyMoodEnergyLineChart
import com.example.ui.navigation.RhythmTab
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.theme.CalmBlue
import com.example.ui.theme.CalmBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassSurface
import com.example.ui.theme.LightGlassBorder
import com.example.ui.theme.LightGlassSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern, calm Master Screen with iPhone-like Minimalist Aesthetic:
 * - Edge-to-edge ambient mesh background with calm light blue and calm light green
 * - Apple-style frosted glass navigation & cards
 * - Dynamic Dark Mode / Light Mode toggle in top bar
 * - Floating glass pill tab navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RhythmFitDashboardScreen(
    viewModel: RhythmFitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by remember { mutableStateOf(RhythmTab.RHYTHM) }

    val isDark = uiState.isDarkMode
    val calmAccent = if (isDark) CalmGreenLight else CalmGreen
    val blueAccent = if (isDark) CalmBlueLight else CalmBlue

    LaunchedEffect(uiState.userFeedbackMessage) {
        uiState.userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    GlassMeshBackground(
        isDarkMode = isDark,
        modifier = modifier.fillMaxSize().testTag("rhythmfit_dashboard_screen")
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_rhythmfit_logo),
                                contentDescription = "RhythmFit Logo",
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentTab.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.3).sp,
                                        fontSize = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = when (currentTab) {
                                        RhythmTab.RHYTHM -> "Fitness that adapts"
                                        RhythmTab.SANCTUARY -> "Mind, breath & somatic"
                                        RhythmTab.SCHEDULE -> "Timetable & micro-gaps"
                                        RhythmTab.LEADERBOARD -> "Diamond league arena"
                                        RhythmTab.INSIGHTS -> "Soft consistency harmony"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = calmAccent
                                    )
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            // Daily Quote / Well-Wish Button
                            Surface(
                                shape = CircleShape,
                                color = calmAccent.copy(alpha = if (isDark) 0.16f else 0.10f),
                                border = BorderStroke(1.dp, calmAccent.copy(alpha = if (isDark) 0.35f else 0.20f)),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { viewModel.openDailyQuotePopup() }
                                    .testTag("open_daily_quote_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Daily Well-Wish",
                                        tint = calmAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Wish",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = calmAccent
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // iPhone-style Dark/Light Theme Toggle
                            IosThemeToggle(
                                isDarkMode = isDark,
                                onToggle = { viewModel.toggleDarkMode() }
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Nudges Status Badge
                            Surface(
                                shape = CircleShape,
                                color = calmAccent.copy(alpha = if (isDark) 0.16f else 0.10f),
                                border = BorderStroke(1.dp, calmAccent.copy(alpha = if (isDark) 0.35f else 0.20f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Alarm Nudges Active",
                                        tint = calmAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Nudges On",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = calmAccent
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                // Floating iOS Glass Capsule Navigation Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) DarkGlassSurface else LightGlassSurface,
                        border = BorderStroke(1.dp, if (isDark) DarkGlassBorder else LightGlassBorder),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rhythmfit_bottom_navigation")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RhythmTab.entries.forEach { tab ->
                                val isSelected = currentTab == tab
                                val interactionSource = remember { MutableInteractionSource() }

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) { currentTab = tab }
                                        .background(
                                            if (isSelected) calmAccent.copy(alpha = if (isDark) 0.22f else 0.16f)
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("nav_tab_${tab.route}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label,
                                            tint = if (isSelected) calmAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.size(19.dp)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = tab.label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = calmAccent
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition",
                modifier = Modifier.padding(innerPadding)
            ) { tab ->
                when (tab) {
                    RhythmTab.RHYTHM -> {
                        RhythmHomeContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            isDarkMode = isDark,
                            onNavigateToSanctuary = { currentTab = RhythmTab.SANCTUARY },
                            onNavigateToSchedule = { currentTab = RhythmTab.SCHEDULE }
                        )
                    }
                    RhythmTab.SANCTUARY -> {
                        MindAndBreatheScreen(
                            currentMood = uiState.selectedMood,
                            isDarkMode = isDark,
                            onSelectFeeling = { feeling ->
                                viewModel.selectMood(feeling.level, feeling.name)
                                viewModel.launchFeelingRoutine(feeling.name, 4)
                            },
                            onStartRoutine = { routine ->
                                viewModel.startRoutine(routine)
                            }
                        )
                    }
                    RhythmTab.SCHEDULE -> {
                        ScheduleScreen(
                            slots = uiState.timetableSlots,
                            isDarkMode = isDark,
                            onAddSlotClick = { viewModel.showAddSlotDialog() },
                            onDeleteSlot = { slot -> viewModel.deleteTimetableSlot(slot) },
                            onLaunchGapRoutine = { routineName ->
                                viewModel.launchGapRoutineByName(routineName)
                            }
                        )
                    }
                    RhythmTab.LEADERBOARD -> {
                        LeaderboardScreen(
                            users = uiState.leaderboardUsers,
                            currentUserScore = uiState.todayScore,
                            currentUserRoutinesCount = uiState.completedRoutinesCount,
                            isDarkMode = isDark,
                            onCheerUser = { userId -> viewModel.cheerUser(userId) },
                            onStartSprintRoutine = { viewModel.launchSprintRoutine() },
                            isOnline = uiState.isOnline,
                            isSyncing = uiState.isSyncingLeaderboard,
                            lastSyncTimestamp = uiState.lastSyncTimestamp,
                            nextSyncCountdownSeconds = uiState.nextSyncCountdownSeconds,
                            mongoStatusMessage = uiState.mongoStatusMessage,
                            onRefreshOnline = { viewModel.refreshOnlineLeaderboard() }
                        )
                    }
                    RhythmTab.INSIGHTS -> {
                        InsightsScreen(
                            score = uiState.todayScore,
                            category = uiState.scoreCategory,
                            completedRoutinesCount = uiState.completedRoutinesCount,
                            totalKcal = uiState.totalKcalBurnedToday,
                            totalSteps = uiState.totalStepsToday,
                            hasFlaggedDampener = uiState.hasFlaggedDampener,
                            dampenerReason = uiState.dampenerReason,
                            logs = uiState.todayLogs,
                            weeklyMoods = uiState.weeklyMoods,
                            weeklyLogs = uiState.weeklyActivityLogs,
                            isDarkMode = isDark
                        )
                    }
                }
            }
        }
    }

    // Modal: Micro-Break Routine Player with Audio Guide
    if (uiState.showRoutinePlayerModal && uiState.activeRoutine != null) {
        RoutinePlayerBottomSheet(
            activeState = uiState.activeRoutine!!,
            onTogglePlayPause = { viewModel.togglePlayPauseRoutine() },
            onCompleteRoutine = { viewModel.completeActiveRoutine() },
            onDismiss = { viewModel.dismissRoutineModal() }
        )
    }

    // Modal: Supportive Mental Health Resource Guardrail
    if (uiState.showMentalHealthGuardrail) {
        SupportiveMentalHealthBottomSheet(
            onDismiss = { viewModel.dismissMentalHealthGuardrail() }
        )
    }

    // Dialog: Add Timetable Slot
    if (uiState.showAddSlotDialog) {
        TimetableEditorDialog(
            onDismiss = { viewModel.dismissAddSlotDialog() },
            onSave = { slot -> viewModel.addTimetableSlot(slot) }
        )
    }

    // Modal: Daily Well-Wishing Quote of the Day
    if (uiState.showDailyQuotePopup) {
        DailyQuotePopup(
            quote = uiState.dailyQuote,
            isDarkMode = isDark,
            hasClaimedAffirmation = uiState.hasClaimedAffirmationToday,
            onClaimAffirmation = { viewModel.claimDailyAffirmation() },
            onDismiss = { viewModel.dismissDailyQuotePopup() }
        )
    }
}

/**
 * Rhythm Home Tab: Today's consistency, quick feeling picker, routine recommendation,
 * next timetable gap preview, multi-modal Gemini logger, and today's activity stream.
 */
@Composable
private fun RhythmHomeContent(
    uiState: com.example.ui.RhythmFitUiState,
    viewModel: RhythmFitViewModel,
    isDarkMode: Boolean,
    onNavigateToSanctuary: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 0. Daily Well-Wishing Quote of the Day (Dismissible Card at top of dashboard, served once per day)
        val quoteEntity = uiState.dailyQuoteEntity
        if (quoteEntity != null && !quoteEntity.isDismissed) {
            item(key = "daily_quote_card_${quoteEntity.id}") {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    DailyWellWishingQuoteCard(
                        quote = quoteEntity,
                        isDarkMode = isDarkMode,
                        onClaimAffirmation = { quoteId -> viewModel.claimDailyQuote(quoteId) },
                        onDismiss = { quoteId -> viewModel.dismissDailyQuote(quoteId) }
                    )
                }
            }
        }

        // 1. Today's Rhythm Card
        item {
            TodayRhythmCard(
                score = uiState.todayScore,
                category = uiState.scoreCategory,
                completedRoutinesCount = uiState.completedRoutinesCount,
                totalKcal = uiState.totalKcalBurnedToday,
                steps = uiState.totalStepsToday,
                hasFlaggedDampener = uiState.hasFlaggedDampener,
                dampenerReason = uiState.dampenerReason,
                isDarkMode = isDarkMode
            )
        }

        // 1.5. Daily Kcal Goal Progress Indicator Card (Circular indicator synced from Room)
        item {
            DailyKcalProgressCard(
                burnedKcal = uiState.totalKcalBurnedToday,
                goalKcal = uiState.userProfile.dailyKcalGoal,
                todayLogsCount = uiState.todayLogs.size,
                isDarkMode = isDarkMode,
                onQuickLogKcal = { name, durationMins, kcal ->
                    viewModel.logQuickCalorieActivity(name, durationMins, kcal)
                },
                onUpdateGoalKcal = { newGoal ->
                    viewModel.setDailyKcalGoal(newGoal)
                }
            )
        }

        // 2. 1-Tap Mood Selector & Contextual Micro-Routine
        item {
            MoodSelectorSection(
                selectedMood = uiState.selectedMood,
                suggestedRoutine = uiState.suggestedRoutine,
                isDarkMode = isDarkMode,
                onMoodSelected = { level, name -> viewModel.selectMood(level, name) },
                onStartRoutine = { routine -> viewModel.startRoutine(routine) },
                onNavigateToSanctuary = onNavigateToSanctuary
            )
        }

        // 3. Timetable Gap Window Preview Glass Card
        item {
            TimetableGapPreviewCard(
                slotsCount = uiState.timetableSlots.size,
                nextSlot = uiState.timetableSlots.firstOrNull(),
                isDarkMode = isDarkMode,
                onNavigateToSchedule = onNavigateToSchedule,
                onStartGapRoutine = { routineName ->
                    viewModel.launchGapRoutineByName(routineName)
                }
            )
        }

        // 4. Multi-Modal Free-Text Logging (Gemini AI + Context Tags)
        item {
            MultiModalLoggerSection(
                isAnalyzing = uiState.isAnalyzingWithGemini,
                parsedResultPreview = uiState.parsedResultPreview,
                isDarkMode = isDarkMode,
                onAnalyzeText = { text -> viewModel.parseFreeTextWithGemini(text) },
                onConfirmParsedLog = { parsed -> viewModel.confirmParsedActivity(parsed) },
                onDismissPreview = { viewModel.dismissParsedPreview() },
                onQuickTagLogged = { tag ->
                    viewModel.logManualActivity(
                        name = "Context: $tag",
                        durationMins = 10,
                        steps = 0,
                        tags = listOf(tag.lowercase())
                    )
                }
            )
        }

        // 5. Weekly Mood & Energy vs Activity Line Chart (7-Day Recharts / D3 Native Spline)
        item {
            WeeklyMoodEnergyLineChart(
                weeklyMoods = uiState.weeklyMoods,
                weeklyLogs = uiState.weeklyActivityLogs,
                isDarkMode = isDarkMode
            )
        }

        // 6. Today's Activity Timeline
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TODAY'S LOGGED ACTIVITIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (uiState.todayLogs.isEmpty()) {
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
                            text = "No activities logged today yet. Try a 2-min micro-routine or log free text above!",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.todayLogs, key = { it.id }) { log ->
                ActivityLogGlassItem(log = log, isDarkMode = isDarkMode)
            }
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@Composable
private fun TimetableGapPreviewCard(
    slotsCount: Int,
    nextSlot: com.example.data.local.entity.TimetableSlot?,
    isDarkMode: Boolean,
    onNavigateToSchedule: () -> Unit,
    onStartGapRoutine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val blueAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    GlassCard(
        modifier = modifier.fillMaxWidth(),
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(calmAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreTime,
                            contentDescription = null,
                            tint = calmAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SCHEDULE & GAP WINDOWS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        ),
                        color = calmAccent
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                    modifier = Modifier.clip(CircleShape).clickable { onNavigateToSchedule() }
                ) {
                    Text(
                        text = "$slotsCount blocks",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (nextSlot != null) {
                val startTime = String.format(Locale.getDefault(), "%02d:%02d", nextSlot.startHour, nextSlot.startMinute)
                val endTime = String.format(Locale.getDefault(), "%02d:%02d", nextSlot.endHour, nextSlot.endMinute)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = blueAccent.copy(alpha = if (isDarkMode) 0.10f else 0.08f),
                    border = BorderStroke(1.dp, blueAccent.copy(alpha = if (isDarkMode) 0.30f else 0.20f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Next: ${nextSlot.title} ($startTime–$endTime)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${nextSlot.gapDurationMins}-min pause: ${nextSlot.suggestedRoutine}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = blueAccent
                                )
                            )
                        }

                        Button(
                            onClick = { onStartGapRoutine(nextSlot.suggestedRoutine) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Start",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Add classes or work blocks in the Schedule tab to automatically discover calm 2–5 min movement pauses.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActivityLogGlassItem(
    log: ActivityLog,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val timeFormatted = remember(log.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.timestamp))
    }
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val blueAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (log.isMicroBreak) calmAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f)
                            else blueAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (log.isMicroBreak) Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
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
                            fontSize = 13.sp,
                            letterSpacing = (-0.1).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${log.durationMins}m • $timeFormatted",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (log.isMicroBreak) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = calmAccent.copy(alpha = if (isDarkMode) 0.18f else 0.12f)
                            ) {
                                Text(
                                    text = "Micro-Break",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = calmAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = CoralPulse,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${log.estimatedKcal} kcal",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CoralPulse
                        )
                    )
                }
                if (log.steps > 0) {
                    Text(
                        text = "${log.steps} steps",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
