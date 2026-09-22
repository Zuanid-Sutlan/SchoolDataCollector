package com.example.schooldatacollector.domain.model

data class Student(
    val id: String = "",
    val studentId: String = "",
    val name: String = "",
    val className: String = "",
    val fatherName: String = "",
    val fatherCnic: String = "",
    val fee: Double = 0.0,
    val comment: String = "",
    val branch: String = "2",
)
