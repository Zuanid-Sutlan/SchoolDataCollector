package com.example.schooldatacollector.data.repository

import com.example.schooldatacollector.domain.model.School
import com.example.schooldatacollector.domain.repository.SchoolRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SchoolRepositoryImpl(
    private val firestore: FirebaseFirestore
) : SchoolRepository {

    private val collectionRef = firestore.collection("schools")

    override fun getSchools(): Flow<List<School>> = callbackFlow {
        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val schools = snapshot?.documents?.mapNotNull { document ->
                document.toObject(School::class.java)?.copy(id = document.id)
            } ?: emptyList()

            trySend(schools)
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun addSchool(school: School) {
        // DocumentReference.set() auto-generates ID if we use document() without path
        // Alternatively, use collection.add(school)
        if (school.id.isEmpty()) {
            collectionRef.add(school).await()
        } else {
            collectionRef.document(school.id).set(school).await()
        }
    }
}
