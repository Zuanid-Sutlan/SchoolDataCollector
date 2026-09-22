package com.example.schooldatacollector.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.schooldatacollector.domain.FilterManager
import com.example.schooldatacollector.domain.model.Student
import com.example.schooldatacollector.domain.repository.StudentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAllStudents()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadAllStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            FilterManager.filterState
                .flatMapLatest { filter ->
                    val limit = if (filter.fetchLimited) 10 else null
                    repository.getAllStudents(limit = limit)
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

    fun addStudent(student: Student) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addStudent(student)
                _isLoading.value = false
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add student"
                _isLoading.value = false
            }
        }
    }
}

