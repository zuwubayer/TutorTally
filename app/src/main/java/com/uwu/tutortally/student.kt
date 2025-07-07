package com.uwu.tutortally

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val place: String,
    val classesPerMonth: Int,
    // NEW: We will now store the active cycle number for each student.
    val currentCycle: Int = 1
)

// This helper class remains the same
data class StudentWithLogs(
    @Embedded val student: Student,
    @Relation(
        parentColumn = "id",
        entityColumn = "studentId"
    )
    val logs: List<ClassLog>
)