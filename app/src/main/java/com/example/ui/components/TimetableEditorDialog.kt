package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TimetableSlot
import com.example.ui.theme.RhythmCyan

@Composable
fun TimetableEditorDialog(
    onDismiss: () -> Unit,
    onSave: (TimetableSlot) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startHour by remember { mutableIntStateOf(9) }
    var startMin by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(10) }
    var endMin by remember { mutableIntStateOf(30) }
    var gapMins by remember { mutableIntStateOf(5) }
    var routine by remember { mutableStateOf("Desk Neck & Shoulder Relief") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Timetable Block",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Block Title (e.g. Study / Sprint)") },
                    modifier = Modifier.fillMaxWidth().testTag("slot_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RhythmCyan)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "$startHour",
                        onValueChange = { startHour = it.toIntOrNull()?.coerceIn(0, 23) ?: 9 },
                        label = { Text("Start Hr (24h)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = "$startMin",
                        onValueChange = { startMin = it.toIntOrNull()?.coerceIn(0, 59) ?: 0 },
                        label = { Text("Start Min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "$endHour",
                        onValueChange = { endHour = it.toIntOrNull()?.coerceIn(0, 23) ?: 10 },
                        label = { Text("End Hr (24h)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = "$endMin",
                        onValueChange = { endMin = it.toIntOrNull()?.coerceIn(0, 59) ?: 30 },
                        label = { Text("End Min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = "$gapMins",
                    onValueChange = { gapMins = it.toIntOrNull()?.coerceIn(1, 15) ?: 5 },
                    label = { Text("Gap Window (2-5 mins)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = routine,
                    onValueChange = { routine = it },
                    label = { Text("Suggested Routine") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            TimetableSlot(
                                title = title.trim(),
                                startHour = startHour,
                                startMinute = startMin,
                                endHour = endHour,
                                endMinute = endMin,
                                gapDurationMins = gapMins,
                                suggestedRoutine = routine.ifBlank { "Desk Neck & Shoulder Relief" },
                                isNudgeEnabled = true
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RhythmCyan),
                modifier = Modifier.testTag("save_slot_button")
            ) {
                Text(
                    text = "Save Slot",
                    color = androidx.compose.ui.graphics.Color(0xFF0A0F1D),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
