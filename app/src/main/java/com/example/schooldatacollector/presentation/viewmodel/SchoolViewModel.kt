package com.example.schooldatacollector.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schooldatacollector.domain.model.School
import com.example.schooldatacollector.domain.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SchoolViewModel(
    private val repository: SchoolRepository
) : ViewModel() {

    private val _schools = MutableStateFlow<List<School>>(emptyList())
    val schools: StateFlow<List<School>> = _schools.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSchools()
    }

    private fun loadSchools() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getSchools()
                .catch { e ->
                    _error.value = e.message ?: "An unknown error occurred"
                    _isLoading.value = false
                }
                .collect { schoolList ->
                    _schools.value = schoolList
                    _isLoading.value = false
                    _error.value = null
                }
        }
    }

    fun addSchool(name: String, address: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addSchool(School(name = name, address = address))
                _isLoading.value = false
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add school"
                _isLoading.value = false
            }
        }
    }
}
