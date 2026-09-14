package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.ParsedLogResult
import com.example.ui.theme.AmberFocus
import com.example.ui.theme.CalmBlue
import com.example.ui.theme.CalmBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.RoseDampener
import java.util.Locale

val PRESET_CONTEXT_TAGS = listOf(
    "Exam Stress" to RoseDampener,
    "Travel" to AmberFocus,
    "Illness" to CoralPulse,
    "College Walk" to CalmBlue,
    "Desk Stretch" to CalmGreen
)

/**
 * iPhone-like Minimalist Daily Activity & Context Logging Section.
 * Natural language parsing via Gemini SDK with frosted glass card styling.
 */
@Composable
fun MultiModalLoggerSection(
    isAnalyzing: Boolean,
    parsedResultPreview: ParsedLogResult?,
    onAnalyzeText: (String) -> Unit,
    onConfirmParsedLog: (ParsedLogResult) -> Unit,
    onDismissPreview: () -> Unit,
    onQuickTagLogged: (String) -> Unit,
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    var freeTextInput by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var speechStatusText by remember { mutableStateOf<String?>(null) }
    var showSpeechFallbackDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen
    val blueAccent = if (isDarkMode) CalmBlueLight else CalmBlue

    // Pulse animation for listening microphone state
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                freeTextInput = spokenText
                speechStatusText = "Heard: “$spokenText”"
                // Immediately send speech text to Gemini AI API for structured parsing
                onAnalyzeText(spokenText)
            }
        } else {
            speechStatusText = null
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                isListening = true
                speechStatusText = "Listening for your activity log..."
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe what you did (e.g. '30 min jogging and 10 min stretch')")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                speechRecognizerLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                isListening = false
                showSpeechFallbackDialog = true
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice activity logging", Toast.LENGTH_SHORT).show()
        }
    }

    fun startVoiceInput() {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            try {
                isListening = true
                speechStatusText = "Listening for your activity log..."
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe what you did (e.g. '30 min jogging and 10 min stretch')")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                speechRecognizerLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                isListening = false
                showSpeechFallbackDialog = true
            }
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Voice sample selection fallback dialog
    if (showSpeechFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showSpeechFallbackDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = CoralPulse)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voice Activity Logging", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column {
                    Text(
                        text = "Android Speech Recognizer service is not available on this device or emulator. Select a spoken activity to parse with Gemini AI:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val voiceSamples = listOf(
                        "Ran for 25 minutes in the park and did 5 minutes of core stretch",
                        "Stationary cycling for 40 minutes, burned around 280 calories",
                        "Walked 4500 steps across campus with friends",
                        "High stress exam study for 5 hours, took 2 quick posture breaks"
                    )
                    voiceSamples.forEach { sample ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    showSpeechFallbackDialog = false
                                    freeTextInput = sample
                                    speechStatusText = "Voice captured: “$sample”"
                                    onAnalyzeText(sample)
                                }
                        ) {
                            Text(
                                text = "“$sample”",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeechFallbackDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "ACTIVITY & CONTEXT LOG",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.3.sp,
                fontSize = 11.sp
            ),
            color = blueAccent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Tell RhythmFit what you did today",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Natural Language Glass Card with Voice Speech-to-Text
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDarkMode = isDarkMode,
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Listening status pill
                AnimatedVisibility(visible = isListening || speechStatusText != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isListening) CoralPulse.copy(alpha = 0.15f) else CalmGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isListening) CoralPulse.copy(alpha = 0.4f) else CalmGreen.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isListening) CoralPulse else calmAccent,
                                modifier = Modifier
                                    .size(15.dp)
                                    .scale(if (isListening) micPulseScale else 1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = speechStatusText ?: "Listening... speak your workout or day context",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = if (isListening) CoralPulse else calmAccent
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = freeTextInput,
                    onValueChange = { freeTextInput = it },
                    placeholder = {
                        Text(
                            text = "Speak or type e.g. 'Ran 30 min in the park and stretched' or 'Studied for 4 hours'",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon = {
                        // In-field Microphone Action Button
                        IconButton(
                            onClick = { startVoiceInput() },
                            modifier = Modifier
                                .testTag("voice_input_mic_button")
                                .scale(if (isListening) micPulseScale else 1f)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = "Record activity via voice",
                                tint = if (isListening) CoralPulse else blueAccent
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("free_text_log_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isListening) CoralPulse else blueAccent,
                        unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f),
                        focusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.50f),
                        unfocusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.40f)
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (freeTextInput.isNotBlank()) {
                            onAnalyzeText(freeTextInput)
                        }
                    })
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = blueAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gemini AI Engine",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = blueAccent
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dedicated Voice Input Pill Button
                        Surface(
                            shape = CircleShape,
                            color = if (isListening) CoralPulse.copy(alpha = 0.2f) else blueAccent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, if (isListening) CoralPulse else blueAccent.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { startVoiceInput() }
                                .testTag("voice_log_pill_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Log",
                                    tint = if (isListening) CoralPulse else blueAccent,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .scale(if (isListening) micPulseScale else 1f)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isListening) "Listening..." else "Voice Log",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isListening) CoralPulse else blueAccent
                                )
                            }
                        }

                        // iOS Pill Action Button: Analyze & Log
                        Button(
                            onClick = {
                                if (freeTextInput.isNotBlank()) {
                                    onAnalyzeText(freeTextInput)
                                }
                            },
                            enabled = !isAnalyzing && freeTextInput.isNotBlank(),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                            modifier = Modifier.testTag("analyze_gemini_button")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyzing...", style = MaterialTheme.typography.labelSmall)
                            } else {
                                Text(
                                    "Analyze & Log",
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
            }
        }

        // Parsed Result Preview Banner
        AnimatedVisibility(visible = parsedResultPreview != null) {
            if (parsedResultPreview != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ParsedPreviewGlassCard(
                    parsed = parsedResultPreview,
                    isDarkMode = isDarkMode,
                    onConfirm = {
                        onConfirmParsedLog(parsedResultPreview)
                        freeTextInput = ""
                    },
                    onDismiss = onDismissPreview
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Context Dampener Tags Row
        Text(
            text = "QUICK LIFE CONTEXT TAGS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                fontSize = 10.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PRESET_CONTEXT_TAGS.forEach { (tag, color) ->
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = if (isDarkMode) 0.14f else 0.10f),
                    border = BorderStroke(1.dp, color.copy(alpha = if (isDarkMode) 0.40f else 0.30f)),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onQuickTagLogged(tag) }
                        .testTag("quick_tag_$tag")
                ) {
                    Text(
                        text = "+ $tag",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = color
                        ),
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ParsedPreviewGlassCard(
    parsed: ParsedLogResult,
    isDarkMode: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val calmAccent = if (isDarkMode) CalmGreenLight else CalmGreen

    GlassCard(
        shape = RoundedCornerShape(20.dp),
        isDarkMode = isDarkMode,
        border = BorderStroke(1.dp, calmAccent.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().testTag("gemini_parsed_preview")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = calmAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Extracted Activity",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = calmAccent
                        )
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = parsed.activity,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = CalmBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${parsed.durationMins} mins", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = CoralPulse, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("~${parsed.estimatedKcal} kcal (MET)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
                }

                if (parsed.steps > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = AmberFocus, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${parsed.steps} steps", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
                    }
                }
            }

            if (parsed.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    parsed.tags.forEach { tag ->
                        Surface(
                            shape = CircleShape,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onConfirm,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = calmAccent),
                modifier = Modifier.fillMaxWidth().testTag("confirm_parsed_log_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Confirm & Save to Room",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
