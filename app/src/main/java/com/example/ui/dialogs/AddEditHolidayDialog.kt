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
import com.example.data.model.HolidayEntity
import com.example.domain.ScheduleLogic
import com.example.ui.theme.AgriError
import com.example.ui.theme.ForestGreenPrimary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHolidayDialog(
    initialHoliday: HolidayEntity?,
    onDismiss: () -> Unit,
    onSaveHoliday: (HolidayEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(initialHoliday?.title ?: "") }
    var startDate by remember { mutableStateOf(initialHoliday?.startDate ?: "2026-10-01") }
    var endDate by remember { mutableStateOf(initialHoliday?.endDate ?: "2026-10-01") }
    var type by remember { mutableStateOf(initialHoliday?.type ?: "University Closed") }
    var note by remember { mutableStateOf(initialHoliday?.note ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val holidayTypes = listOf("Public Holiday", "University Closed", "Exam Break", "Custom")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialHoliday == null) "Add Holiday / Break" else "Edit Holiday",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Holiday Title") },
                placeholder = { Text("e.g. Midterm Recess or National Day") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Holiday Type
            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    holidayTypes.forEach { item ->
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

            // Start & End Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Optional Note") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            validationError?.let { err ->
                Text(text = err, fontSize = 12.sp, color = AgriError, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            validationError = "Please enter a title."
                            return@Button
                        }
                        try {
                            val s = ScheduleLogic.parseDate(startDate.trim())
                            val e = ScheduleLogic.parseDate(endDate.trim())
                            if (e.isBefore(s)) {
                                validationError = "End date must be on or after start date."
                                return@Button
                            }
                        } catch (_: Exception) {
                            validationError = "Please enter valid dates in YYYY-MM-DD format."
                            return@Button
                        }

                        val h = HolidayEntity(
                            id = initialHoliday?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            startDate = startDate.trim(),
                            endDate = endDate.trim(),
                            type = type,
                            note = note.trim()
                        )
                        onSaveHoliday(h)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(text = "Save Holiday", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
