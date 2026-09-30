package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.StudentProfile
import com.example.ui.theme.AgriBorder
import com.example.ui.theme.AgriPrimary
import com.example.ui.theme.AgriPrimaryDark
import com.example.ui.theme.AgriPrimarySoft
import com.example.ui.theme.AgriSurface
import com.example.ui.theme.AgriSurface2
import com.example.ui.theme.AgriText
import com.example.ui.theme.AgriTextMuted
import com.example.ui.theme.ForestGreenPrimary

@Composable
fun StudentProfileDialog(
    profile: StudentProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, studentId: String, section: String, faculty: String, session: String, program: String) -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile.name) }
    var studentId by remember(profile) { mutableStateOf(profile.studentId) }
    var section by remember(profile) { mutableStateOf(profile.section) }
    var faculty by remember(profile) { mutableStateOf(profile.faculty) }
    var session by remember(profile) { mutableStateOf(profile.session) }
    var program by remember(profile) { mutableStateOf(profile.program) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ForestGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Student & Program Profile",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriPrimaryDark
                        )
                        Text(
                            text = "Academic Record Details",
                            fontSize = 11.sp,
                            color = AgriTextMuted
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AgriTextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = program,
                    onValueChange = { program = it },
                    label = { Text("Academic Program / Degree") },
                    placeholder = { Text("e.g. BSc Agriculture or BSAg 262") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = AgriPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Class Section (e.g. Section C)") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = AgriPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = faculty,
                    onValueChange = { faculty = it },
                    label = { Text("Faculty / Department") },
                    leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null, tint = AgriPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = session,
                    onValueChange = { session = it },
                    label = { Text("Academic Session / Batch") },
                    placeholder = { Text("e.g. Fall 2026 • BSAg 262") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = AgriPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AgriPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student Roll / Registration ID") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = AgriPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, studentId, section, faculty, session, program) },
                colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AgriTextMuted)
            }
        }
    )
}
