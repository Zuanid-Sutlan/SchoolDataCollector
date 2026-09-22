package com.example.schooldatacollector.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.schooldatacollector.domain.model.Student
import com.example.schooldatacollector.presentation.viewmodel.TeacherDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: TeacherDashboardViewModel
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentClass by viewModel.currentClass.collectAsStateWithLifecycle()

    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard - $currentClass") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading && students.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students) { student ->
                        StudentItem(
                            student = student,
                            onClick = { selectedStudent = student }
                        )
                    }
                }
            }

            error?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = paddingValues.calculateBottomPadding() + 16.dp)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }

    selectedStudent?.let { student ->
        StudentEditDialog(
            student = student,
            onDismiss = { selectedStudent = null },
            onSave = { fatherName, cnic, fee, comment ->
                viewModel.updateStudentDetails(student, fatherName, cnic, fee, comment)
                selectedStudent = null
            }
        )
    }
}

@Composable
fun StudentItem(
    student: Student,
    onClick: () -> Unit
) {
    val isMissingInfo = student.fatherCnic.isBlank() || student.fatherName.isBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isMissingInfo) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                if (student.studentId.isNotBlank()) {
                    Text(text = "ID: ${student.studentId}", style = MaterialTheme.typography.bodySmall)
                }
                if (student.fatherName.isNotBlank()) {
                    Text(text = "Father: ${student.fatherName}", style = MaterialTheme.typography.bodyMedium)
                }
                if (student.fatherCnic.isNotBlank()) {
                    Text(text = "CNIC: ${student.fatherCnic}", style = MaterialTheme.typography.bodyMedium)
                }
                if (student.fee > 0) {
                    Text(text = "Fee: ${student.fee}", style = MaterialTheme.typography.bodyMedium)
                }
                if (student.comment.isNotBlank()) {
                    Text(
                        text = "Note: ${student.comment}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isMissingInfo) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Missing Info",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StudentEditDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (fatherName: String, cnic: String, fee: Double, comment: String) -> Unit
) {
    var fatherName by remember { mutableStateOf(student.fatherName) }
    var cnic by remember { mutableStateOf(student.fatherCnic) }
    var feeStr by remember { mutableStateOf(if (student.fee > 0) student.fee.toString() else "") }
    var comment by remember { mutableStateOf(student.comment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student Details\n${student.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father's Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = cnic,
                    onValueChange = {
                        // Allow only numbers and optionally format with dashes (e.g. 12345-1234567-1)
                        if (it.length <= 15) cnic = it
                    },
                    label = { Text("Father's CNIC") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = feeStr,
                    onValueChange = { feeStr = it },
                    label = { Text("Fee") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comments / Issues") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val fee = feeStr.toDoubleOrNull() ?: 0.0
                    onSave(fatherName, cnic, fee, comment)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
