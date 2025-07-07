package com.uwu.tutortally

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_logs")
data class ClassLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: Int, // This links the log back to a specific student.
    val timestamp: Long,
    val cycleNumber: Int // NEW: To group logs by cycle.
)