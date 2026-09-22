package com.example.schooldatacollector.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.schooldatacollector.di.AppModule

object AppViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) -> {
                AdminDashboardViewModel(AppModule.studentRepository) as T
            }
            modelClass.isAssignableFrom(TeacherDashboardViewModel::class.java) -> {
                TeacherDashboardViewModel(AppModule.studentRepository) as T
            }
            modelClass.isAssignableFrom(SchoolViewModel::class.java) -> {
                SchoolViewModel(AppModule.schoolRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
