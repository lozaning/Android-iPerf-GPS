package com.example.androidproject.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [TestSession::class, TestResult::class],
    version = 1,
    exportSchema = false
)
abstract class TestDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao
    
    companion object {
        @Volatile
        private var INSTANCE: TestDatabase? = null
        
        fun getDatabase(context: Context): TestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TestDatabase::class.java,
                    "test_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}