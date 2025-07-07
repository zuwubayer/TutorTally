package com.uwu.tutortally

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    // THIS IS THE MISSING FUNCTION
    @Delete
    suspend fun deleteStudent(student: Student)

    @Insert
    suspend fun insertClassLog(log: ClassLog)

    @Delete
    suspend fun deleteClassLog(log: ClassLog)

    @Transaction
    @Query("SELECT * FROM students ORDER BY id ASC")
    fun getStudentsWithLogs(): Flow<List<StudentWithLogs>>
}