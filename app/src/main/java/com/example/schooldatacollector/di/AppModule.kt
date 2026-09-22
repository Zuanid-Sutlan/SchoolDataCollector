package com.example.schooldatacollector.di

import com.example.schooldatacollector.data.repository.StudentRepositoryImpl
import com.example.schooldatacollector.domain.repository.StudentRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object AppModule {
    val firestore = Firebase.firestore

    val studentRepository: StudentRepository by lazy {
        StudentRepositoryImpl(firestore)
    }
}
