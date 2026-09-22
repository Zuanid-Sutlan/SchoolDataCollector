package com.example.schooldatacollector.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.schooldatacollector.domain.model.Student
import com.example.schooldatacollector.presentation.viewmodel.AdminDashboardViewModel

class CnicVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 13) text.text.substring(0..12) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 4 || i == 11) {
                if (i != trimmed.lastIndex) out += "-"
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset == 5) return if (trimmed.length > 5) 6 else 5
                if (offset <= 11) return offset + 1
                if (offset == 12) return if (trimmed.length > 12) 14 else 13
                if (offset <= 13) return offset + 2
                return 15
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 5) return offset
                if (offset <= 13) return offset - 1
                if (offset <= 15) return offset - 2
                return 13
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var feeStr by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Student") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isNotBlank() && className.isNotBlank()) {
                                val formattedCnic = buildString {
                                    for (i in cnic.indices) {
                                        append(cnic[i])
                                        if (i == 4 && cnic.length > 5) append("-")
                                        if (i == 11 && cnic.length > 12) append("-")
                                    }
                                }

                                viewModel.addStudent(
                                    Student(
                                        name = name,
                                        className = className,
                                        studentId = studentId,
                                        fatherName = fatherName,
                                        fatherCnic = formattedCnic,
                                        fee = feeStr.toDoubleOrNull() ?: 0.0,
                                        comment = comment
                                    )
                                )
                                Toast.makeText(context, "Saved $name successfully!", Toast.LENGTH_SHORT).show()

                                // Clear fields EXCEPT class name
                                name = ""
                                studentId = ""
                                fatherName = ""
                                cnic = ""
                                feeStr = ""
                                comment = ""

                                focusManager.clearFocus()
                            } else {
                                Toast.makeText(context, "Name and Class are required", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("SAVE")
                    }
                }
            )
        },
        modifier = Modifier.imePadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("Class (e.g. Class 5A) *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Student Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            OutlinedTextField(
                value = fatherName,
                onValueChange = { fatherName = it },
                label = { Text("Father's Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth(),
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
                value = studentId,
                onValueChange = { studentId = it },
                label = { Text("Student ID / Roll No") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            OutlinedTextField(
                value = feeStr,
                onValueChange = { feeStr = it },
                label = { Text("Fee") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
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
                label = { Text("Comment") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Button(
                onClick = {
                    if (name.isNotBlank() && className.isNotBlank()) {
                        val formattedCnic = buildString {
                            for (i in cnic.indices) {
                                append(cnic[i])
                                if (i == 4 && cnic.length > 5) append("-")
                                if (i == 11 && cnic.length > 12) append("-")
                            }
                        }

                        viewModel.addStudent(
                            Student(
                                name = name,
                                className = className,
                                studentId = studentId,
                                fatherName = fatherName,
                                fatherCnic = formattedCnic,
                                fee = feeStr.toDoubleOrNull() ?: 0.0,
                                comment = comment
                            )
                        )
                        Toast.makeText(context, "Saved $name successfully!", Toast.LENGTH_SHORT).show()
                        
                        // Clear fields EXCEPT class name
                        name = ""
                        studentId = ""
                        fatherName = ""
                        cnic = ""
                        feeStr = ""
                        comment = ""
                        
                        // Move focus back to Name field to quickly enter the next student
                        focusManager.clearFocus()
                    } else {
                        Toast.makeText(context, "Name and Class are required", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Save and Add Another")
            }
        }
    }
}
