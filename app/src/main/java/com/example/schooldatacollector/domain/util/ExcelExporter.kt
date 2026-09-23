package com.example.schooldatacollector.domain.util

import android.content.Context
import android.net.Uri
import com.example.schooldatacollector.domain.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhatim.fastexcel.Workbook
import java.io.OutputStream

object ExcelExporter {
    
    suspend fun exportStudentsToExcel(context: Context, uri: Uri, students: List<Student>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                    val wb = Workbook(outputStream, "SchoolDataCollector", "1.0")
                    val ws = wb.newWorksheet("Students")

                    // Create Headers
                    ws.value(0, 0, "Class")
                    ws.value(0, 1, "Name")
                    ws.value(0, 2, "Father's Name")
                    ws.value(0, 3, "CNIC")
                    ws.value(0, 4, "Student ID/Roll No")
                    ws.value(0, 5, "Fee")
                    ws.value(0, 6, "Comments")

                    // Apply Bold styling to Headers
                    for (col in 0..6) {
                        ws.style(0, col).bold().set()
                    }

                    // Populate Data
                    for ((index, student) in students.withIndex()) {
                        val row = index + 1 // Row 0 is header
                        ws.value(row, 0, student.className)
                        ws.value(row, 1, student.name)
                        ws.value(row, 2, student.fatherName)
                        ws.value(row, 3, student.fatherCnic)
                        ws.value(row, 4, student.studentId)
                        ws.value(row, 5, student.fee)
                        ws.value(row, 6, student.comment)
                    }

                    wb.finish()
                } ?: return@withContext Result.failure(Exception("Could not open output stream"))

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
