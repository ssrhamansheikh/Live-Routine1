package com.example.ui.dialogs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourseEntity
import com.example.data.model.OverrideEntity
import com.example.domain.EffectiveClass
import com.example.domain.ScheduleLogic
import com.example.ui.theme.AgriAmber
import com.example.ui.theme.AgriAmberContainer
import com.example.ui.theme.AgriAmberText
import com.example.ui.theme.AgriError
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ForestGreenSecondary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClassSheet(
    targetClass: EffectiveClass,
    courses: List<CourseEntity>,
    onDismiss: () -> Unit,
    onSaveOverride: (OverrideEntity) -> Unit,
    onRevert: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCourseCode by remember { mutableStateOf(targetClass.courseCode) }
    var changeType by remember { mutableStateOf(targetClass.changeType ?: "room") }
    var scope by remember { mutableStateOf("day") } // "day" | "future" | "semester"
    var newRoom by remember { mutableStateOf(targetClass.room) }
    var newStart by remember { mutableStateOf(targetClass.start) }
    var newEnd by remember { mutableStateOf(targetClass.end) }
    var note by remember { mutableStateOf(targetClass.overrideNote ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Schedule Override",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Modify timing, relocate room, or cancel session",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Change Type Chips: Room | Time | Cancelled | Extra Class | Swap
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Change Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("room" to "Room", "time" to "Time", "cancelled" to "Cancelled", "extra" to "Extra", "swap" to "Swap").forEach { (type, label) ->
                        val selected = changeType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clickable { changeType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Scope Selector: This day only | All future weeks | Whole semester
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Scope of Change", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("day" to "This Day Only", "future" to "All Future Weeks", "semester" to "Whole Semester").forEach { (s, label) ->
                        val selected = scope == s
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow
                                )
                                .clickable { scope = s }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // New Venue Room Input (if room or swap)
            if (changeType != "cancelled") {
                OutlinedTextField(
                    value = newRoom,
                    onValueChange = { newRoom = it },
                    label = { Text("Relocated Room / Venue") },
                    placeholder = { Text("e.g. AGRIlab4 or Room 104B") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Time Pickers (if time or swap or extra)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newStart,
                        onValueChange = { newStart = it },
                        label = { Text("Start (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newEnd,
                        onValueChange = { newEnd = it },
                        label = { Text("End (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Reason / Note for Students") },
                placeholder = { Text("e.g. Emergency maintenance or lab assay trial") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // LIVE PREVIEW CARD
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AgriAmberContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "LIVE CHANGE PREVIEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = AgriAmberText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${targetClass.courseCode}: ${targetClass.courseTitle}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (changeType == "cancelled") {
                        Text(
                            text = "STATUS: CANCELLED (${scope.uppercase()})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriError
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Venue: ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (newRoom != targetClass.room) {
                                Text(
                                    text = targetClass.room,
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = " → $newRoom",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            } else {
                                Text(
                                    text = newRoom,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }

                        if (newStart != targetClass.start || newEnd != targetClass.end) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Time: ",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${targetClass.start}-${targetClass.end}",
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = " → $newStart-$newEnd",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Save and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val override = OverrideEntity(
                            id = targetClass.overrideId ?: UUID.randomUUID().toString(),
                            slotId = targetClass.slotId,
                            courseCode = selectedCourseCode,
                            changeType = changeType,
                            scope = scope,
                            date = "2026-09-29",
                            newRoom = if (changeType != "cancelled") newRoom else null,
                            newStart = if (changeType != "cancelled") newStart else null,
                            newEnd = if (changeType != "cancelled") newEnd else null,
                            note = note
                        )
                        onSaveOverride(override)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(text = "Save Change", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(46.dp)
                ) {
                    Text(text = "Cancel")
                }
            }

            // Revert to original routine
            OutlinedButton(
                onClick = { onRevert(targetClass.slotId) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriError),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriError),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Revert to original routine", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
