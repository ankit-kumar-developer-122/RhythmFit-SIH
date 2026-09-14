package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.MicroRoutine
import com.example.ai.RoutineStep
import com.example.data.local.entity.TimetableSlot
import com.example.ui.theme.AmberFocus
import com.example.ui.theme.EmeraldVitality
import com.example.ui.theme.RhythmCyan

/**
 * MODULE 4: Timetable-Linked Schedule Grid & Gap Windows.
 */
@Composable
fun TimetableGridSection(
    slots: List<TimetableSlot>,
    onAddSlotClick: () -> Unit,
    onDeleteSlot: (TimetableSlot) -> Unit,
    onLaunchGapRoutine: (MicroRoutine) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TIMETABLE & MICRO-BREAK GAPS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Auto-nudged during natural schedule transitions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onAddSlotClick,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RhythmCyan),
                modifier = Modifier.testTag("add_timetable_slot_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Slot",
                    tint = RhythmCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Slot",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = RhythmCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (slots.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No timetable slots configured yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add study, work, or lecture blocks to schedule micro-break gap alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                slots.forEach { slot ->
                    TimetableSlotItem(
                        slot = slot,
                        onDelete = { onDeleteSlot(slot) },
                        onLaunchRoutine = {
                            val routine = MicroRoutine(
                                id = "slot_${slot.id}",
                                title = slot.suggestedRoutine,
                                category = "Timetable Gap",
                                durationMins = slot.gapDurationMins,
                                metValue = 2.8,
                                calorieEstimateStr = "~12–18 kcal",
                                targetMood = "Neutral",
                                steps = listOf(
                                    RoutineStep(
                                        title = "Transition Deep Breath",
                                        instruction = "Conclude your block, push back your chair, and take 3 centering breaths.",
                                        durationSeconds = 30,
                                        audioCue = "Take three deep centering breaths to shift focus from your previous task."
                                    ),
                                    RoutineStep(
                                        title = "Spinal Lengthening & Reach",
                                        instruction = "Reach both hands high overhead, intertwine fingers, stretch side to side.",
                                        durationSeconds = 45,
                                        audioCue = "Reach both arms high overhead and gently stretch from side to side."
                                    ),
                                    RoutineStep(
                                        title = "Quick Eye Rest (20-20-20)",
                                        instruction = "Look at an object 20 feet away to relax your ocular focal muscles.",
                                        durationSeconds = 45,
                                        audioCue = "Look away from screens towards the horizon to relax your eyes."
                                    )
                                )
                            )
                            onLaunchGapRoutine(routine)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimetableSlotItem(
    slot: TimetableSlot,
    onDelete: () -> Unit,
    onLaunchRoutine: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AmberFocus.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = AmberFocus,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = slot.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = slot.formatTimeRange(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldVitality.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "⚡ ${slot.gapDurationMins}m gap: ${slot.suggestedRoutine}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldVitality
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onLaunchRoutine,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleOutline,
                        contentDescription = "Start Gap Routine",
                        tint = RhythmCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Slot",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
