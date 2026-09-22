package com.example.schooldatacollector.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.schooldatacollector.presentation.screen.CnicVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.schooldatacollector.domain.model.Student
import com.example.schooldatacollector.presentation.viewmodel.TeacherDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: TeacherDashboardViewModel,
    onNavigateToFilter: () -> Unit
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    var searchClassQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    var expanded by remember { mutableStateOf(false) }

    // Derive a list of unique classes from all loaded students (or you could load this from a global state/repository)
    // For now, we allow the teacher to either type or pick from recently searched classes
    val recentClasses = remember { mutableStateListOf("4A", "4B", "4C", "4D") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = searchClassQuery,
                            onValueChange = { searchClassQuery = it },
                            placeholder = { Text("Enter Class (e.g. Class 5A)") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = { 
                                    if (searchClassQuery.isNotBlank()) {
                                        if (!recentClasses.contains(searchClassQuery)) {
                                            recentClasses.add(searchClassQuery)
                                        }
                                        viewModel.loadStudentsForClass(searchClassQuery)
                                        expanded = false
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            modifier = Modifier.menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            val filteredClasses = recentClasses.filter { it.contains(searchClassQuery, ignoreCase = true) }
                            if (filteredClasses.isNotEmpty()) {
                                filteredClasses.forEach { className ->
                                    DropdownMenuItem(
                                        text = { Text(className) },
                                        onClick = {
                                            searchClassQuery = className
                                            viewModel.loadStudentsForClass(className)
                                            expanded = false
                                            focusManager.clearFocus()
                                        }
                                    )
                                }
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Load '$searchClassQuery'") },
                                    onClick = {
                                        if (searchClassQuery.isNotBlank()) {
                                            if (!recentClasses.contains(searchClassQuery)) {
                                                recentClasses.add(searchClassQuery)
                                            }
                                            viewModel.loadStudentsForClass(searchClassQuery)
                                            expanded = false
                                            focusManager.clearFocus()
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFilter) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                }
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
    val hasComment = student.comment.isNotBlank()
    
    val containerColor = when {
        isMissingInfo -> MaterialTheme.colorScheme.errorContainer
        hasComment -> Color(0xFFFFF9C4) // Yellow 100 for warning/comment
        else -> Color(0xFFE8F5E9) // Green 50 for everything perfect
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor)
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

    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student Details\n${student.name}") },
        modifier = Modifier.imePadding(),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father's Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                OutlinedTextField(
                    value = cnic,
                    onValueChange = { newValue ->
                        val digits = newValue.filter { it.isDigit() }
                        if (digits.length <= 13) cnic = digits
                    },
                    label = { Text("Father's CNIC") },
                    placeholder = { Text("00000-0000000-0")},
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    visualTransformation = CnicVisualTransformation()
                )
                OutlinedTextField(
                    value = feeStr,
                    onValueChange = { feeStr = it },
                    label = { Text("Fee") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comments / Issues") },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val formattedCnic = buildString {
                        for (i in cnic.indices) {
                            append(cnic[i])
                            if (i == 4 && cnic.length > 5) append("-")
                            if (i == 11 && cnic.length > 12) append("-")
                        }
                    }
                    val fee = feeStr.toDoubleOrNull() ?: 0.0
                    onSave(fatherName, formattedCnic, fee, comment)
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
