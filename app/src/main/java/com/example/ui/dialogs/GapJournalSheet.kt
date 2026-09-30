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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GapJournalEntity
import com.example.domain.ScheduleLogic
import com.example.ui.GapTargetInfo
import com.example.ui.theme.AgriBorder
import com.example.ui.theme.AgriDanger
import com.example.ui.theme.AgriDangerSoft
import com.example.ui.theme.AgriPrimary
import com.example.ui.theme.AgriPrimaryDark
import com.example.ui.theme.AgriPrimarySoft
import com.example.ui.theme.AgriSurface
import com.example.ui.theme.AgriSurface2
import com.example.ui.theme.AgriText
import com.example.ui.theme.AgriTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GapJournalSheet(
    target: GapTargetInfo,
    onDismiss: () -> Unit,
    onSave: (GapJournalEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val existing = target.existingEntry
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var selectedTag by remember { mutableStateOf(existing?.tag ?: "Studied") }

    val quickPicks = listOf(
        "Studied" to "📖",
        "Library" to "🏛️",
        "Lunch/Snacks" to "🥪",
        "Prayer" to "🤲",
        "Friends" to "👥",
        "Assignment" to "📝",
        "Rest" to "☕",
        "Lab work" to "🔬",
        "Other" to "✨"
    )

    val hours = target.durationMinutes / 60
    val mins = target.durationMinutes % 60
    val durationFormatted = if (hours > 0 && mins > 0) "${hours}h ${mins}m" else if (hours > 0) "${hours}h" else "${mins}m"
    val timeRangeText = "${ScheduleLogic.format12Hour(target.startTime)} – ${ScheduleLogic.format12Hour(target.endTime)}"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = AgriSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (existing == null) "Log Gap Time Activity" else "Edit Gap Journal Entry",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriPrimaryDark
                    )
                    Text(
                        text = "Track your productive study or rest time between classes",
                        fontSize = 11.sp,
                        color = AgriTextMuted
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AgriText)
                }
            }

            // Top Gap Info Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AgriSurface2),
                border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AgriPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = timeRangeText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = AgriText
                            )
                            Text(
                                text = "Date: ${target.date}",
                                fontSize = 11.sp,
                                color = AgriTextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AgriPrimarySoft)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = durationFormatted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriPrimaryDark
                        )
                    }
                }
            }

            // Quick-pick Tag Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Quick Tag",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriText
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickPicks) { (tag, emoji) ->
                        val selected = selectedTag == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (selected) AgriPrimary
                                    else AgriSurface2
                                )
                                .clickable { selectedTag = tag }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "$emoji $tag",
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else AgriText
                            )
                        }
                    }
                }
            }

            // Note Text Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "What did you do in this gap?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriText
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("e.g. Read Agronomy chapter 4, had tea at cafeteria, finished botany drawing", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 4
                )
            }

            // Save / Delete / Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val entry = GapJournalEntity(
                            id = "${target.date}_${target.startTime}",
                            date = target.date,
                            startTime = target.startTime,
                            endTime = target.endTime,
                            durationMinutes = target.durationMinutes,
                            tag = selectedTag,
                            note = note.trim()
                        )
                        onSave(entry)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(text = "Save Entry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                if (existing != null) {
                    Button(
                        onClick = { onDelete(existing.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriDangerSoft),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AgriDanger, modifier = Modifier.size(18.dp))
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(46.dp)
                ) {
                    Text(text = "Cancel", color = AgriTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
