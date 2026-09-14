package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ai.MicroRoutine
import com.example.ai.RoutineStep
import com.example.notification.NotificationHelper
import com.example.ui.RhythmFitViewModel
import com.example.ui.screens.RhythmFitDashboardScreen
import com.example.ui.theme.RhythmFitTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RhythmFitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if launched from gap notification
        handleNotificationIntent()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            RhythmFitTheme(darkTheme = uiState.isDarkMode) {
                // Runtime Notification Permission Request for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { /* Permission handled gracefully */ }
                    )

                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    RhythmFitDashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent()
    }

    private fun handleNotificationIntent() {
        val routineName = intent?.getStringExtra(NotificationHelper.EXTRA_ROUTINE_NAME)
        val gapMins = intent?.getIntExtra(NotificationHelper.EXTRA_GAP_MINS, 5) ?: 5
        if (!routineName.isNullOrBlank()) {
            val routine = MicroRoutine(
                id = "notification_${System.currentTimeMillis()}",
                title = routineName,
                category = "Gap Window",
                durationMins = gapMins,
                metValue = 2.8,
                calorieEstimateStr = "~15–20 kcal",
                targetMood = "Neutral",
                steps = listOf(
                    RoutineStep(
                        title = "Neck & Shoulder Roll",
                        instruction = "Roll shoulders back in smooth circles, loosening desk tension.",
                        durationSeconds = 45,
                        audioCue = "Roll your shoulders backward smoothly. Release desk tightness."
                    ),
                    RoutineStep(
                        title = "Chest Opening Breath",
                        instruction = "Interlock fingers behind your head, expand chest, take 3 deep belly breaths.",
                        durationSeconds = 60,
                        audioCue = "Expand your chest and inhale deeply. Fill your lungs."
                    )
                )
            )
            viewModel.startRoutine(routine)
        }
    }
}

