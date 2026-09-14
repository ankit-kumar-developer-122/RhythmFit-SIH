package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.MicroRoutine
import com.example.ai.ParsedLogResult
import com.example.audio.TtsAudioGuide
import com.example.data.local.entity.ActivityLog
import com.example.data.local.entity.ConsistencyScore
import com.example.data.local.entity.DailyQuoteEntity
import com.example.data.local.entity.MoodEntry
import com.example.data.local.entity.TimetableSlot
import com.example.data.local.entity.UserProfile
import com.example.data.model.LeaderboardUser
import com.example.data.model.QuoteCatalog
import com.example.data.model.QuoteOfDay
import com.example.engine.RhythmEngine
import com.example.repository.DailyQuoteRepository
import com.example.repository.RhythmFitRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveRoutineState(
    val routine: MicroRoutine,
    val currentStepIndex: Int = 0,
    val secondsRemainingInStep: Int = 0,
    val isPlaying: Boolean = false,
    val totalCaloriesBurned: Int = 0
)

data class RhythmFitUiState(
    val todayScore: Double = 72.0,
    val scoreCategory: RhythmEngine.RhythmCategory = RhythmEngine.getRhythmCategory(72.0),
    val completedRoutinesCount: Int = 0,
    val hasFlaggedDampener: Boolean = false,
    val dampenerReason: String? = null,
    val userProfile: UserProfile = UserProfile(),
    val timetableSlots: List<TimetableSlot> = emptyList(),
    val todayLogs: List<ActivityLog> = emptyList(),
    val totalKcalBurnedToday: Int = 0,
    val totalStepsToday: Int = 0,
    val selectedMood: String = "Neutral",
    val suggestedRoutine: MicroRoutine? = null,
    val isAnalyzingWithGemini: Boolean = false,
    val parsedResultPreview: ParsedLogResult? = null,
    val activeRoutine: ActiveRoutineState? = null,
    val showMentalHealthGuardrail: Boolean = false,
    val showAddSlotDialog: Boolean = false,
    val showRoutinePlayerModal: Boolean = false,
    val isDarkMode: Boolean = false,
    val showDailyQuotePopup: Boolean = false,
    val dailyQuoteEntity: DailyQuoteEntity? = null,
    val dailyQuote: QuoteOfDay = QuoteCatalog.getQuoteForToday(),
    val hasClaimedAffirmationToday: Boolean = false,
    val leaderboardUsers: List<LeaderboardUser> = defaultLeaderboardUsers(),
    val isOnline: Boolean = true,
    val isSyncingLeaderboard: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val nextSyncCountdownSeconds: Int = 600,
    val mongoStatusMessage: String = "Connected to MongoDB: Cluster0JR",
    val weeklyMoods: List<MoodEntry> = emptyList(),
    val weeklyActivityLogs: List<ActivityLog> = emptyList(),
    val userFeedbackMessage: String? = null
)

class RhythmFitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RhythmFitRepository(application)
    private val quoteRepository = DailyQuoteRepository(application)
    private val ttsAudioGuide = TtsAudioGuide(application)

    private val _uiState = MutableStateFlow(RhythmFitUiState())
    val uiState: StateFlow<RhythmFitUiState> = _uiState.asStateFlow()

    private var routineTimerJob: Job? = null
    private var syncTimerJob: Job? = null

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    init {
        // Load Room-backed daily well-wishing quote (served only once per calendar day)
        viewModelScope.launch {
            val initialQuote = quoteRepository.refreshTodayQuote()
            _uiState.update {
                it.copy(
                    dailyQuoteEntity = initialQuote,
                    hasClaimedAffirmationToday = initialQuote?.isClaimed == true
                )
            }
        }
        viewModelScope.launch {
            quoteRepository.todayQuote.collect { quote ->
                _uiState.update {
                    it.copy(
                        dailyQuoteEntity = quote,
                        hasClaimedAffirmationToday = quote?.isClaimed == true
                    )
                }
            }
        }

        // Start 10-minute periodic online MongoDB synchronization loop
        startPeriodicLeaderboardSync()

        // Observe repository online status
        viewModelScope.launch {
            repository.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }
        viewModelScope.launch {
            repository.syncStatusMessage.collect { msg ->
                _uiState.update { it.copy(mongoStatusMessage = msg) }
            }
        }

        // Observe database flows and combine into UI state
        viewModelScope.launch {
            combine(
                repository.todayScore,
                repository.todayLogs,
                repository.timetableSlots,
                repository.userProfile
            ) { score, logs, slots, profile ->
                val effProfile = profile ?: UserProfile()
                val currentScore = score?.score ?: effProfile.baseConsistencyScore
                val microCount = logs.count { it.isMicroBreak }
                val totalKcal = logs.sumOf { it.estimatedKcal }
                val totalSteps = logs.sumOf { it.steps }

                _uiState.update { current ->
                    current.copy(
                        todayScore = currentScore,
                        scoreCategory = RhythmEngine.getRhythmCategory(currentScore),
                        completedRoutinesCount = microCount,
                        hasFlaggedDampener = score?.hasFlaggedDampener ?: false,
                        dampenerReason = score?.dampenerReason,
                        userProfile = effProfile,
                        timetableSlots = slots,
                        todayLogs = logs,
                        totalKcalBurnedToday = totalKcal,
                        totalStepsToday = totalSteps
                    )
                }
            }.collect {}
        }

        // Observe 7-day mood entries for pattern analysis chart
        viewModelScope.launch {
            repository.get7DayMoods().collect { moods ->
                _uiState.update { it.copy(weeklyMoods = moods) }
            }
        }

        // Observe 7-day activity logs for pattern analysis chart
        viewModelScope.launch {
            repository.get7DayLogs().collect { logs ->
                _uiState.update { it.copy(weeklyActivityLogs = logs) }
            }
        }

        // Initialize default recommendation based on initial mood
        loadRoutineForMood("Neutral", 5)
    }

    /**
     * 1-Tap Mood Selector handler
     */
    fun selectMood(level: Int, name: String) {
        _uiState.update { it.copy(selectedMood = name) }
        viewModelScope.launch {
            repository.logMood(level, name)
            loadRoutineForMood(name, 4)

            // Check responsibility guardrail: sustained low mood for 3+ consecutive days
            val sustainedLowMood = repository.checkSustainedLowMoodGuardrail()
            if (sustainedLowMood) {
                _uiState.update { it.copy(showMentalHealthGuardrail = true) }
            }
        }
    }

    private fun loadRoutineForMood(mood: String, durationMins: Int) {
        viewModelScope.launch {
            val routine = repository.getRoutineForMood(mood, durationMins)
            _uiState.update { it.copy(suggestedRoutine = routine) }
        }
    }

    /**
     * Parses free-text log using Gemini AI SDK
     */
    fun parseFreeTextWithGemini(text: String) {
        if (text.isBlank()) return
        _uiState.update { it.copy(isAnalyzingWithGemini = true) }

        viewModelScope.launch {
            val parsed = repository.parseFreeText(text)
            _uiState.update {
                it.copy(
                    isAnalyzingWithGemini = false,
                    parsedResultPreview = parsed
                )
            }
        }
    }

    /**
     * Confirms and logs the parsed activity into Room
     */
    fun confirmParsedActivity(parsed: ParsedLogResult) {
        viewModelScope.launch {
            repository.logActivity(
                name = parsed.activity,
                durationMins = parsed.durationMins,
                steps = parsed.steps,
                tags = parsed.tags,
                isMicroBreak = parsed.tags.any { it.contains("break") || it.contains("routine") }
            )
            _uiState.update {
                it.copy(
                    parsedResultPreview = null,
                    userFeedbackMessage = "Logged: ${parsed.activity} (+${parsed.estimatedKcal} kcal)"
                )
            }
        }
    }

    fun dismissParsedPreview() {
        _uiState.update { it.copy(parsedResultPreview = null) }
    }

    /**
     * Manual activity logging
     */
    fun logManualActivity(name: String, durationMins: Int, steps: Int, tags: List<String>) {
        viewModelScope.launch {
            repository.logActivity(
                name = name,
                durationMins = durationMins,
                steps = steps,
                tags = tags,
                isMicroBreak = false
            )
            _uiState.update { it.copy(userFeedbackMessage = "Activity logged successfully!") }
        }
    }

    /**
     * Quick calorie activity logging that instantly writes to Room and updates circular indicators
     */
    fun logQuickCalorieActivity(name: String, durationMins: Int, estimatedKcal: Int) {
        viewModelScope.launch {
            repository.logActivity(
                name = name,
                durationMins = durationMins,
                steps = durationMins * 70,
                tags = listOf("active_burn", "energy_expenditure"),
                isMicroBreak = false
            )
            _uiState.update {
                it.copy(userFeedbackMessage = "Logged $name (+$estimatedKcal kcal) to Room!")
            }
        }
    }

    /**
     * Sets daily calorie goal in Room user profile
     */
    fun setDailyKcalGoal(goalKcal: Int) {
        viewModelScope.launch {
            repository.updateDailyKcalGoal(goalKcal)
            _uiState.update {
                it.copy(
                    userProfile = it.userProfile.copy(dailyKcalGoal = goalKcal),
                    userFeedbackMessage = "Daily goal set to $goalKcal kcal"
                )
            }
        }
    }

    // --- Routine Player & TTS Audio Guidance ---

    fun startRoutine(routine: MicroRoutine) {
        val firstStep = routine.steps.firstOrNull() ?: return
        val active = ActiveRoutineState(
            routine = routine,
            currentStepIndex = 0,
            secondsRemainingInStep = firstStep.durationSeconds,
            isPlaying = true,
            totalCaloriesBurned = 0
        )
        _uiState.update {
            it.copy(
                activeRoutine = active,
                showRoutinePlayerModal = true
            )
        }

        // Announce routine start & first audio cue via TextToSpeech
        ttsAudioGuide.speak("Starting ${routine.title}. ${firstStep.audioCue}")
        startTimerLoop()
    }

    /**
     * Launches a micro-routine tailored to a selected feeling item
     */
    fun launchFeelingRoutine(feelingName: String, durationMins: Int = 4) {
        viewModelScope.launch {
            val routine = repository.getRoutineForMood(feelingName, durationMins)
            startRoutine(routine)
        }
    }

    /**
     * Launches a gap routine from the timetable slot
     */
    fun launchGapRoutineByName(name: String, durationMins: Int = 5) {
        viewModelScope.launch {
            val routine = repository.getRoutineForMood("Neutral", durationMins)
            startRoutine(routine.copy(title = name, category = "Gap Window"))
        }
    }

    private fun startTimerLoop() {
        routineTimerJob?.cancel()
        routineTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentActive = _uiState.value.activeRoutine ?: break
                if (!currentActive.isPlaying) continue

                val newSecRemaining = currentActive.secondsRemainingInStep - 1
                if (newSecRemaining > 0) {
                    // Spoken 10-second countdown reminder if at 10s
                    if (newSecRemaining == 10) {
                        ttsAudioGuide.speak("Ten seconds remaining.")
                    }
                    _uiState.update {
                        it.copy(
                            activeRoutine = currentActive.copy(
                                secondsRemainingInStep = newSecRemaining
                            )
                        )
                    }
                } else {
                    // Advance to next step
                    val nextStepIndex = currentActive.currentStepIndex + 1
                    if (nextStepIndex < currentActive.routine.steps.size) {
                        val nextStep = currentActive.routine.steps[nextStepIndex]
                        _uiState.update {
                            it.copy(
                                activeRoutine = currentActive.copy(
                                    currentStepIndex = nextStepIndex,
                                    secondsRemainingInStep = nextStep.durationSeconds
                                )
                            )
                        }
                        ttsAudioGuide.speak("Next: ${nextStep.title}. ${nextStep.audioCue}")
                    } else {
                        // All steps finished
                        completeActiveRoutine()
                        break
                    }
                }
            }
        }
    }

    fun togglePlayPauseRoutine() {
        _uiState.update { state ->
            val active = state.activeRoutine ?: return@update state
            val nextPlaying = !active.isPlaying
            if (!nextPlaying) {
                ttsAudioGuide.stop()
            } else {
                val currentStep = active.routine.steps.getOrNull(active.currentStepIndex)
                currentStep?.let { ttsAudioGuide.speak("Resuming. ${it.title}") }
            }
            state.copy(activeRoutine = active.copy(isPlaying = nextPlaying))
        }
    }

    fun completeActiveRoutine() {
        routineTimerJob?.cancel()
        ttsAudioGuide.speak("Routine complete! Great work keeping your rhythm alive.")
        val active = _uiState.value.activeRoutine
        if (active != null) {
            viewModelScope.launch {
                repository.completeMicroRoutine(active.routine)
            }
        }
        _uiState.update {
            it.copy(
                activeRoutine = null,
                showRoutinePlayerModal = false,
                userFeedbackMessage = "Completed ${active?.routine?.title ?: "Routine"}! Consistency score boosted."
            )
        }
    }

    fun dismissRoutineModal() {
        routineTimerJob?.cancel()
        ttsAudioGuide.stop()
        _uiState.update {
            it.copy(
                activeRoutine = null,
                showRoutinePlayerModal = false
            )
        }
    }

    // Mental health guardrail dismissal
    fun dismissMentalHealthGuardrail() {
        _uiState.update { it.copy(showMentalHealthGuardrail = false) }
    }

    // Timetable slot management
    fun showAddSlotDialog() {
        _uiState.update { it.copy(showAddSlotDialog = true) }
    }

    fun dismissAddSlotDialog() {
        _uiState.update { it.copy(showAddSlotDialog = false) }
    }

    fun addTimetableSlot(slot: TimetableSlot) {
        viewModelScope.launch {
            repository.saveTimetableSlot(slot)
            _uiState.update {
                it.copy(
                    showAddSlotDialog = false,
                    userFeedbackMessage = "Added ${slot.title} to timetable grid."
                )
            }
        }
    }

    fun deleteTimetableSlot(slot: TimetableSlot) {
        viewModelScope.launch {
            repository.deleteTimetableSlot(slot)
            _uiState.update {
                it.copy(userFeedbackMessage = "Removed slot: ${slot.title}")
            }
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(userFeedbackMessage = null) }
    }

    // Daily Quote Actions backed by Room
    fun dismissDailyQuote(quoteId: Int) {
        viewModelScope.launch {
            quoteRepository.dismissTodayQuote(quoteId)
            _uiState.update {
                it.copy(
                    dailyQuoteEntity = null,
                    showDailyQuotePopup = false
                )
            }
        }
    }

    fun claimDailyQuote(quoteId: Int) {
        if (_uiState.value.hasClaimedAffirmationToday) return
        viewModelScope.launch {
            quoteRepository.claimTodayQuote(quoteId)
            repository.logActivity(
                name = "Daily Well-Wish Affirmation",
                durationMins = 3,
                steps = 0,
                tags = listOf("mindful", "affirmation", "daily_quote"),
                isMicroBreak = true
            )
            _uiState.update {
                it.copy(
                    dailyQuoteEntity = it.dailyQuoteEntity?.copy(isClaimed = true),
                    hasClaimedAffirmationToday = true,
                    showDailyQuotePopup = false,
                    userFeedbackMessage = "Daily Well-Wish Claimed! +15 Consistency XP added."
                )
            }
        }
    }

    fun openDailyQuotePopup() {
        _uiState.update { it.copy(showDailyQuotePopup = true) }
    }

    fun dismissDailyQuotePopup() {
        val currentQuote = _uiState.value.dailyQuoteEntity
        if (currentQuote != null) {
            dismissDailyQuote(currentQuote.id)
        } else {
            _uiState.update { it.copy(showDailyQuotePopup = false) }
        }
    }

    fun claimDailyAffirmation() {
        val currentQuote = _uiState.value.dailyQuoteEntity
        if (currentQuote != null) {
            claimDailyQuote(currentQuote.id)
        } else {
            if (_uiState.value.hasClaimedAffirmationToday) return
            viewModelScope.launch {
                repository.logActivity(
                    name = "Morning Mindful Affirmation",
                    durationMins = 3,
                    steps = 0,
                    tags = listOf("mindful", "affirmation", "daily_quote"),
                    isMicroBreak = true
                )
                _uiState.update {
                    it.copy(
                        hasClaimedAffirmationToday = true,
                        showDailyQuotePopup = false,
                        userFeedbackMessage = "Daily Affirmation Claimed! +15 Consistency XP added."
                    )
                }
            }
        }
    }

    // Community Leaderboard Actions
    fun cheerUser(targetUserId: String) {
        val target = _uiState.value.leaderboardUsers.find { it.id == targetUserId } ?: return
        if (target.hasUserCheered) return

        viewModelScope.launch {
            repository.cheerUserOnline(targetUserId)
        }

        _uiState.update { state ->
            val updatedList = state.leaderboardUsers.map { u ->
                if (u.id == targetUserId) {
                    u.copy(cheersReceived = u.cheersReceived + 1, hasUserCheered = true)
                } else u
            }
            state.copy(
                leaderboardUsers = updatedList,
                userFeedbackMessage = "High-five sent to ${target.name}! 👋 (+2 Karma)"
            )
        }
    }

    fun launchSprintRoutine() {
        launchFeelingRoutine("Energized", 4)
    }

    /**
     * Periodic 10-minute online synchronization loop with MongoDB Atlas.
     * Updates community leaderboard standings and backs up user logs online.
     */
    private fun startPeriodicLeaderboardSync() {
        syncTimerJob?.cancel()
        syncTimerJob = viewModelScope.launch {
            // Initial sync on app start
            refreshOnlineLeaderboard()

            var secondsRemaining = 600
            while (isActive) {
                delay(1000)
                secondsRemaining--
                if (secondsRemaining <= 0) {
                    secondsRemaining = 600
                    refreshOnlineLeaderboard()
                } else {
                    _uiState.update { it.copy(nextSyncCountdownSeconds = secondsRemaining) }
                }
            }
        }
    }

    /**
     * Pull-to-refresh or explicit user trigger to fetch latest leaderboard standings
     * from MongoDB Atlas online database.
     */
    fun refreshOnlineLeaderboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingLeaderboard = true) }
            // Sync local data first to MongoDB
            repository.syncOnlineMongo()
            // Fetch live online rankings
            val onlineRanks = repository.fetchOnlineLeaderboard()
            _uiState.update {
                it.copy(
                    leaderboardUsers = onlineRanks,
                    isSyncingLeaderboard = false,
                    lastSyncTimestamp = System.currentTimeMillis(),
                    nextSyncCountdownSeconds = 600,
                    mongoStatusMessage = "Live online from MongoDB: Cluster0JR • Updated just now"
                )
            }
        }
    }

    fun syncUserDataOnline() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingLeaderboard = true) }
            val success = repository.syncOnlineMongo()
            _uiState.update {
                it.copy(
                    isSyncingLeaderboard = false,
                    lastSyncTimestamp = System.currentTimeMillis(),
                    userFeedbackMessage = if (success) "Online sync completed with MongoDB Atlas!" else "Sync queued (offline mode)"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        routineTimerJob?.cancel()
        syncTimerJob?.cancel()
        ttsAudioGuide.release()
    }
}

private fun defaultLeaderboardUsers(): List<LeaderboardUser> {
    return listOf(
        LeaderboardUser(
            id = "u1",
            name = "Marcus Chen",
            avatarInitials = "MC",
            rank = 1,
            score = 94.2,
            microBreaks = 9,
            streakDays = 21,
            division = "Diamond",
            cheersReceived = 38
        ),
        LeaderboardUser(
            id = "u2",
            name = "Elena Rostova",
            avatarInitials = "ER",
            rank = 2,
            score = 91.5,
            microBreaks = 8,
            streakDays = 15,
            division = "Diamond",
            cheersReceived = 31
        ),
        LeaderboardUser(
            id = "u3",
            name = "Sarah K.",
            avatarInitials = "SK",
            rank = 3,
            score = 86.8,
            microBreaks = 6,
            streakDays = 12,
            division = "Diamond",
            cheersReceived = 24
        ),
        LeaderboardUser(
            id = "u_current",
            name = "You (Ankit)",
            avatarInitials = "ME",
            rank = 4,
            score = 78.4,
            microBreaks = 4,
            streakDays = 8,
            isCurrentUser = true,
            division = "Diamond",
            cheersReceived = 19
        ),
        LeaderboardUser(
            id = "u4",
            name = "Liam O'Connor",
            avatarInitials = "LO",
            rank = 5,
            score = 74.0,
            microBreaks = 4,
            streakDays = 6,
            division = "Platinum",
            cheersReceived = 15
        ),
        LeaderboardUser(
            id = "u5",
            name = "Priya Patel",
            avatarInitials = "PP",
            rank = 6,
            score = 71.5,
            microBreaks = 3,
            streakDays = 5,
            division = "Platinum",
            cheersReceived = 12
        ),
        LeaderboardUser(
            id = "u6",
            name = "Aiden Brooks",
            avatarInitials = "AB",
            rank = 7,
            score = 68.2,
            microBreaks = 3,
            streakDays = 4,
            division = "Gold",
            cheersReceived = 9
        ),
        LeaderboardUser(
            id = "u7",
            name = "Zoe Vance",
            avatarInitials = "ZV",
            rank = 8,
            score = 64.0,
            microBreaks = 2,
            streakDays = 3,
            division = "Gold",
            cheersReceived = 7
        )
    )
}
