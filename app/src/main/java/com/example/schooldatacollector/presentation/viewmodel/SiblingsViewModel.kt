package com.example.schooldatacollector.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schooldatacollector.domain.FilterManager
import com.example.schooldatacollector.domain.model.Student
import com.example.schooldatacollector.domain.repository.StudentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SiblingsViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Map of Father's CNIC to List of Students (Siblings)
    @OptIn(ExperimentalCoroutinesApi::class)
    val groupedStudents: StateFlow<Map<String, List<Student>>> = FilterManager.filterState
        .flatMapLatest { filter ->
            val limit = if (filter.fetchLimited) 10 else null
            repository.getAllStudents(limit = limit)
                .combine(_searchQuery) { allStudents, query ->
                    val searchFiltered = if (query.isBlank()) {
                        allStudents
                    } else {
                        allStudents.filter { student ->
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
                }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    init {
        // Initialization handled by StateFlow
    }

    // Unused loading function since we now load directly in the flow chain
    // private fun loadAllStudents() { ... }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
