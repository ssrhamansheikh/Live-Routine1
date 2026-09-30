package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamEntity
import com.example.domain.ScheduleLogic
import com.example.ui.RoutineViewModel
import com.example.ui.theme.AgriAmber
import com.example.ui.theme.AgriAmberContainer
import com.example.ui.theme.AgriAmberText
import com.example.ui.theme.AgriError
import com.example.ui.theme.AgriErrorContainer
import com.example.ui.theme.AgriPurple
import com.example.ui.theme.AgriPurpleContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ForestGreenSecondary
import org.json.JSONArray
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun ExamsScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val exams by viewModel.exams.collectAsState()
    val filter by viewModel.examFilter.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val semester by viewModel.semester.collectAsState()
    val activeDate by viewModel.activeDate.collectAsState()

    var examToDelete by remember { mutableStateOf<ExamEntity?>(null) }

    // Dynamic reference date from activeDate
    val referenceDate = activeDate

    val filterOptions = listOf("All", "Upcoming", "Class Test", "Midterm", "Final", "Practical", "Assignment", "Viva Voce")

    val filteredExams = exams.filter { exam ->
        when (filter) {
            "All" -> true
            "Upcoming" -> {
                val d = try { ScheduleLogic.parseDate(exam.date) } catch (_: Exception) { referenceDate }
                !d.isBefore(referenceDate)
            }
            else -> exam.type.equals(filter, ignoreCase = true)
        }
    }.sortedBy { it.date }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Term Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = semester.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Text(
                                text = "${exams.size} Total Scheduled Assessments & Vivas",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AgriAmberContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentLate,
                                contentDescription = null,
                                tint = AgriAmberText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions) { opt ->
                        val selected = filter == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clickable { viewModel.setExamFilter(opt) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = opt,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // List of Exam Cards
            if (filteredExams.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No exams matching '$filter'",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap the + button below to schedule an assessment",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredExams, key = { it.id }) { exam ->
                    ExamCard(
                        exam = exam,
                        referenceDate = referenceDate,
                        onEdit = { viewModel.editExam(exam) },
                        onDelete = { examToDelete = exam },
                        onToggleChecklist = { itemId -> viewModel.toggleExamChecklist(exam, itemId) },
                        onAddChecklistItem = { text -> viewModel.addExamChecklistItem(exam, text) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Add Exam FAB
        FloatingActionButton(
            onClick = { viewModel.showAddExam() },
            shape = RoundedCornerShape(28.dp),
            containerColor = ForestGreenPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_exam_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Schedule Exam")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Add Exam", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Delete Confirm Dialog
    examToDelete?.let { exam ->
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text(text = "Remove Exam?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove the scheduled ${exam.type} for ${exam.courseCode}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExam(exam.id)
                        examToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriError)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { examToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ExamCard(
    exam: ExamEntity,
    referenceDate: LocalDate,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleChecklist: (String) -> Unit,
    onAddChecklistItem: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var newChecklistText by remember { mutableStateOf("") }
    var showAddInput by remember { mutableStateOf(false) }

    // Parse checklist items
    val checklistItems = remember(exam.checklistJson) {
        val list = mutableListOf<ChecklistItemUi>()
        try {
            val arr = JSONArray(exam.checklistJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ChecklistItemUi(
                        id = obj.optString("id", "$i"),
                        text = obj.optString("text", ""),
                        done = obj.optBoolean("done", false)
                    )
                )
            }
        } catch (_: Exception) {}
        list
    }

    // Countdown calculation
    val examDate = try { ScheduleLogic.parseDate(exam.date) } catch (_: Exception) { referenceDate }
    val daysDiff = ChronoUnit.DAYS.between(referenceDate, examDate)
    val (countdownText, countdownBg, countdownTextColor) = when {
        daysDiff < 0 -> Triple("Passed", MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant)
        daysDiff == 0L -> Triple("Today", AgriErrorContainer, AgriError)
        daysDiff == 1L -> Triple("Tomorrow", AgriAmberContainer, AgriAmberText)
        else -> Triple("In $daysDiff Days", MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.primary)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Countdown chip & Time range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(countdownBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = countdownText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = countdownTextColor
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = exam.type,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = AgriError)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${exam.courseCode}: ${exam.type}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${ScheduleLogic.format12Hour(exam.start)} – ${ScheduleLogic.format12Hour(exam.end)} • Date: ${exam.date}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )

            if (exam.hall.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = AgriError)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = exam.hall, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (exam.syllabus.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = exam.syllabus,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prep Checklist Accordion
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val doneCount = checklistItems.count { it.done }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preparation Checklist ($doneCount/${checklistItems.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            checklistItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onToggleChecklist(item.id) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (item.done) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        tint = if (item.done) ForestGreenSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.text,
                                        fontSize = 12.sp,
                                        color = if (item.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (item.done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                }
                            }

                            if (showAddInput) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newChecklistText,
                                        onValueChange = { newChecklistText = it },
                                        placeholder = { Text("Add study item...", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            if (newChecklistText.isNotBlank()) {
                                                onAddChecklistItem(newChecklistText)
                                                newChecklistText = ""
                                                showAddInput = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                                    ) {
                                        Text("Add", fontSize = 11.sp)
                                    }
                                }
                            } else {
                                TextButton(
                                    onClick = { showAddInput = true },
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Add Checklist Item", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ChecklistItemUi(
    val id: String,
    val text: String,
    val done: Boolean
)
