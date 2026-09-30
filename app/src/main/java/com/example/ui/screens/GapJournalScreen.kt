package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GapJournalEntity
import com.example.domain.ScheduleLogic
import com.example.ui.GapTargetInfo
import com.example.ui.RoutineViewModel
import com.example.ui.theme.AgriBorder
import com.example.ui.theme.AgriDanger
import com.example.ui.theme.AgriPrimary
import com.example.ui.theme.AgriPrimaryDark
import com.example.ui.theme.AgriPrimarySoft
import com.example.ui.theme.AgriSurface
import com.example.ui.theme.AgriSurface2
import com.example.ui.theme.AgriText
import com.example.ui.theme.AgriTextMuted

@Composable
fun GapJournalScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closeGapJournalScreen()
    }

    val context = LocalContext.current
    val entries by viewModel.gapJournals.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }

    val filteredEntries = entries.filter {
        searchQuery.isBlank() ||
                it.note.contains(searchQuery, ignoreCase = true) ||
                it.tag.contains(searchQuery, ignoreCase = true) ||
                it.date.contains(searchQuery, ignoreCase = true)
    }.sortedByDescending { "${it.date}_${it.startTime}" }

    val totalMinutes = entries.sumOf { it.durationMinutes }
    val totalHours = totalMinutes / 60
    val remMins = totalMinutes % 60
    val totalTimeText = if (totalHours > 0) "${totalHours}h ${remMins}m" else "${remMins}m"

    val groupedByDate = filteredEntries.groupBy { it.date }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.closeGapJournalScreen() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = AgriText)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Gap Time Journal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriPrimaryDark
                    )
                    Text(
                        text = "History of productive and rest gaps",
                        fontSize = 11.sp,
                        color = AgriTextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        viewModel.exportGapJournal { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Live Routine Gap Journal", text))
                            Toast.makeText(context, "Journal copied to Clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Export Journal", tint = AgriPrimary)
                }

                IconButton(onClick = { showClearConfirm = true }) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = AgriDanger)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Weekly Stat Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AgriPrimarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = null,
                                    tint = AgriPrimaryDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Time Logged in Gaps",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriText
                                )
                                Text(
                                    text = "${entries.size} gap sessions documented",
                                    fontSize = 11.sp,
                                    color = AgriTextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(AgriPrimarySoft)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = totalTimeText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriPrimaryDark
                            )
                        }
                    }
                }
            }

            // Search Filter
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by activity, tag, or date...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AgriTextMuted, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            if (filteredEntries.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AgriSurface),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Gap Journal Entries Found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgriText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap '+ What did I do?' between classes on the Today tab to record your gap activities.",
                                fontSize = 12.sp,
                                color = AgriTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                groupedByDate.forEach { (date, dayEntries) ->
                    item {
                        Text(
                            text = date,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriPrimaryDark,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )
                    }

                    items(dayEntries, key = { it.id }) { entry ->
                        GapJournalEntryCard(
                            entry = entry,
                            onEdit = {
                                viewModel.openEditGap(
                                    GapTargetInfo(
                                        date = entry.date,
                                        startTime = entry.startTime,
                                        endTime = entry.endTime,
                                        durationMinutes = entry.durationMinutes,
                                        existingEntry = entry
                                    )
                                )
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Journal Entries?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all documented gap activities. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllGapJournals()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriDanger)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GapJournalEntryCard(
    entry: GapJournalEntity,
    onEdit: () -> Unit
) {
    val hrs = entry.durationMinutes / 60
    val m = entry.durationMinutes % 60
    val dur = if (hrs > 0 && m > 0) "${hrs}h ${m}m" else if (hrs > 0) "${hrs}h" else "${m}m"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AgriBorder),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${ScheduleLogic.format12Hour(entry.startTime)} – ${ScheduleLogic.format12Hour(entry.endTime)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = AgriPrimaryDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "($dur)",
                        fontSize = 11.sp,
                        color = AgriTextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(AgriPrimarySoft)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entry.tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriPrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = AgriTextMuted)
                    }
                }
            }

            if (entry.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = entry.note,
                    fontSize = 13.sp,
                    color = AgriText,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
