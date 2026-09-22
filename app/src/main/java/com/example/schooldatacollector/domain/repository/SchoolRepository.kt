package com.example.schooldatacollector.domain.repository

import com.example.schooldatacollector.domain.model.School
import kotlinx.coroutines.flow.Flow

interface SchoolRepository {
    fun getSchools(): Flow<List<School>>
    suspend fun addSchool(school: School)
}
