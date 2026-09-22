package com.example.schooldatacollector.domain.repository

import com.example.schooldatacollector.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun getAllStudents(limit: Int? = null): Flow<List<Student>>
    fun getStudentsByClass(className: String, limit: Int? = null): Flow<List<Student>>
    fun getStudentsByFatherCnic(fatherCnic: String, limit: Int? = null): Flow<List<Student>>
    suspend fun addStudent(student: Student)
    suspend fun updateStudent(student: Student)
}
