package com.uwu.tutortally

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// UPDATED: Added ClassLog to entities and bumped version to 2.
@Database(entities = [Student::class, ClassLog::class], version = 4, exportSchema = false)
abstract class TutorTallyDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var INSTANCE: TutorTallyDatabase? = null

        fun getDatabase(context: Context): TutorTallyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TutorTallyDatabase::class.java,
                    "tutor_tally_database"
                )
                    // This tells Room to just rebuild the database if the schema changes.
                    // It's simple and fine for our app.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}