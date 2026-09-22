package com.example.schooldatacollector.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schooldatacollector.domain.FilterManager
import com.example.schooldatacollector.domain.model.Student
import com.example.schooldatacollector.domain.repository.StudentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SiblingsViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    private val _allStudents = MutableStateFlow<List<Student>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Map of Father's CNIC to List of Students (Siblings)
    val groupedStudents: StateFlow<Map<String, List<Student>>> = combine(
        _allStudents,
        _searchQuery,
        FilterManager.filterState
    ) { all, query, filter ->
        val searchFiltered = if (query.isBlank()) {
            all
        } else {
            all.filter { student ->
                student.name.contains(query, ignoreCase = true) ||
                student.studentId.contains(query, ignoreCase = true) ||
                student.fatherName.contains(query, ignoreCase = true) ||
                student.fatherCnic.contains(query, ignoreCase = true)
            }
        }
        
        val fullyFiltered = searchFiltered.filter { student ->
            val isMissingInfo = student.fatherCnic.isBlank() || student.fatherName.isBlank()
            val hasComment = student.comment.isNotBlank()
            val isClear = !isMissingInfo && !hasComment

            if (filter.showOnlyMissingInfo && !isMissingInfo) return@filter false
            if (filter.showOnlyWithComments && !hasComment) return@filter false
            if (filter.showOnlyClear && !isClear) return@filter false
            true
        }
        
        // Group by CNIC. If CNIC is empty, group by Father's Name as a fallback.
        fullyFiltered.groupBy { it.fatherCnic.ifBlank { "N/A" } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    init {
        loadAllStudents()
    }

    private fun loadAllStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllStudents()
                .catch { e ->
                    _error.value = e.message ?: "Failed to load students"
                    _isLoading.value = false
                }
                .collect { studentList ->
                    _allStudents.value = studentList
                    _isLoading.value = false
                    _error.value = null
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
