package com.example.schooldatacollector.domain.repository

import com.example.schooldatacollector.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun getAllStudents(): Flow<List<Student>>
    fun getStudentsByClass(className: String): Flow<List<Student>>
    fun getStudentsByFatherCnic(fatherCnic: String): Flow<List<Student>>
    suspend fun addStudent(student: Student)
    suspend fun updateStudent(student: Student)
}
