package com.example.schooldatacollector.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FilterState(
    val showOnlyMissingInfo: Boolean = false,
    val showOnlyWithComments: Boolean = false,
    val showOnlyClear: Boolean = false,
    val fetchLimited: Boolean = true // True = fetch max 10, False = fetch all
)

object FilterManager {
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    fun updateFilter(newState: FilterState) {
        _filterState.value = newState
    }
}
