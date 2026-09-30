package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourseEntity
import com.example.data.model.ExamEntity
import com.example.ui.theme.AgriError
import com.example.ui.theme.ForestGreenPrimary
import java.time.LocalTime
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExamDialog(
    initialExam: ExamEntity?,
    courses: List<CourseEntity>,
    onDismiss: () -> Unit,
    onSaveExam: (ExamEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var courseCode by remember { mutableStateOf(initialExam?.courseCode ?: (courses.firstOrNull()?.code ?: "BOT 107")) }
    var type by remember { mutableStateOf(initialExam?.type ?: "Midterm") }
    var date by remember { mutableStateOf(initialExam?.date ?: "2026-10-14") }
    var start by remember { mutableStateOf(initialExam?.start ?: "10:00") }
    var end by remember { mutableStateOf(initialExam?.end ?: "12:00") }
    var hall by remember { mutableStateOf(initialExam?.hall ?: "Hall 302") }
    var syllabus by remember { mutableStateOf(initialExam?.syllabus ?: "") }
    var reminder by remember { mutableStateOf(initialExam?.reminder ?: "1day") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val examTypes = listOf("Class Test", "Midterm", "Final", "Practical", "Assignment", "Viva Voce")
    var typeDropdownExpanded by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialExam == null) "Schedule Assessment" else "Edit Exam",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Course Code
            OutlinedTextField(
                value = courseCode,
                onValueChange = { courseCode = it },
                label = { Text("Course Code (e.g. BOT 107)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Assessment Type Dropdown
            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assessment Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    examTypes.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                type = item
                                typeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Date
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Timing row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = start,
                    onValueChange = { start = it },
                    label = { Text("Start (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = end,
                    onValueChange = { end = it },
                    label = { Text("End (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Hall & Venue
            OutlinedTextField(
                value = hall,
                onValueChange = { hall = it },
                label = { Text("Exam Hall / Room") },
                placeholder = { Text("e.g. Hall 302 or AGRIlab4") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Syllabus
            OutlinedTextField(
                value = syllabus,
                onValueChange = { syllabus = it },
                label = { Text("Syllabus & Material Instructions") },
                placeholder = { Text("Chapters, calculators allowed, field logbook, etc.") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            validationError?.let { err ->
                Text(text = err, fontSize = 12.sp, color = AgriError, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val sTime = LocalTime.parse(start.trim())
                            val eTime = LocalTime.parse(end.trim())
                            if (!eTime.isAfter(sTime)) {
                                validationError = "End time must be later than start time."
                                return@Button
                            }
                        } catch (_: Exception) {
                            validationError = "Please enter valid 24h times in HH:mm format."
                            return@Button
                        }

                        val exam = ExamEntity(
                            id = initialExam?.id ?: UUID.randomUUID().toString(),
                            courseCode = courseCode.trim(),
                            type = type,
                            date = date.trim(),
                            start = start.trim(),
                            end = end.trim(),
                            hall = hall.trim(),
                            syllabus = syllabus.trim(),
                            reminder = reminder,
                            checklistJson = initialExam?.checklistJson ?: "[]"
                        )
                        onSaveExam(exam)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(text = "Save Assessment", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(46.dp)
                ) {
                    Text(text = "Cancel")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
