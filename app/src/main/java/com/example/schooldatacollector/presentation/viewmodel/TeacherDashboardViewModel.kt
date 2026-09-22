package com.example.schooldatacollector.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.schooldatacollector.domain.FilterManager
import com.example.schooldatacollector.domain.model.Student
import com.example.schooldatacollector.domain.repository.StudentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class TeacherDashboardViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Assuming we get the class name from login or selection later.
    // For now, let's observe a specific class.
    private val _currentClass = MutableStateFlow("")
    val currentClass: StateFlow<String> = _currentClass.asStateFlow()

    private var fetchJob: Job? = null

    init {
        // We will no longer load a default class on init
        // loadStudentsForClass(_currentClass.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadStudentsForClass(className: String) {
        _currentClass.value = className
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _isLoading.value = true
            FilterManager.filterState
                .flatMapLatest { filter ->
                    val limit = if (filter.fetchLimited) 10 else null
                    repository.getStudentsByClass(className, limit)
                        .catch { e ->
                            _error.value = e.message ?: "Failed to load students"
                            _isLoading.value = false
                        }
                        .combine(flowOf(filter)) { students, f ->
                            students.filter { student ->
                                val isMissingInfo = student.fatherCnic.isBlank() || student.fatherName.isBlank()
                                val hasComment = student.comment.isNotBlank()
                                val isClear = !isMissingInfo && !hasComment

                                if (f.showOnlyMissingInfo && !isMissingInfo) return@filter false
                                if (f.showOnlyWithComments && !hasComment) return@filter false
                                if (f.showOnlyClear && !isClear) return@filter false
                                true
                            }
                        }
                }
                .collect { studentList ->
                    _students.value = studentList
                    _isLoading.value = false
                    _error.value = null
                }
        }
    }

    fun updateStudentDetails(student: Student, fatherName: String, cnic: String, fee: Double, comment: String) {
        viewModelScope.launch {
            try {
                val updatedStudent = student.copy(
                    fatherName = fatherName,
                    fatherCnic = cnic,
                    fee = fee,
                    comment = comment
                )
                repository.updateStudent(updatedStudent)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update student"
            }
        }
    }

    // A helper method if we ever need to fetch siblings in this VM
    fun getSiblings(cnic: String, onResult: (List<Student>) -> Unit) {
        viewModelScope.launch {
            repository.getStudentsByFatherCnic(cnic).collect { siblings ->
                onResult(siblings)
            }
        }
    }
}
