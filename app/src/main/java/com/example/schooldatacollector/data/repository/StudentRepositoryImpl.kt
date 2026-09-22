package com.example.schooldatacollector.data.repository

import com.example.schooldatacollector.domain.model.Student
import com.example.schooldatacollector.domain.repository.StudentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StudentRepositoryImpl(
    private val firestore: FirebaseFirestore
) : StudentRepository {

    private val collectionRef = firestore.collection("students")

    override fun getAllStudents(): Flow<List<Student>> = callbackFlow {
        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val students = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Student::class.java)?.copy(id = document.id)
            } ?: emptyList()

            trySend(students)
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override fun getStudentsByClass(className: String): Flow<List<Student>> = callbackFlow {
        val query = collectionRef.whereEqualTo("className", className)
        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val students = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Student::class.java)?.copy(id = document.id)
            } ?: emptyList()

            trySend(students)
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override fun getStudentsByFatherCnic(fatherCnic: String): Flow<List<Student>> = callbackFlow {
        val query = collectionRef.whereEqualTo("fatherCnic", fatherCnic)
        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val students = snapshot?.documents?.mapNotNull { document ->
                document.toObject(Student::class.java)?.copy(id = document.id)
            } ?: emptyList()

            trySend(students)
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun addStudent(student: Student) {
        if (student.id.isEmpty()) {
            collectionRef.add(student).await()
        } else {
            collectionRef.document(student.id).set(student).await()
        }
    }

    override suspend fun updateStudent(student: Student) {
        if (student.id.isNotEmpty()) {
            collectionRef.document(student.id).set(student).await()
        }
    }
}
