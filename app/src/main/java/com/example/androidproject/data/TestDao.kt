package com.example.androidproject.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Query("SELECT * FROM test_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TestSession>>
    
    @Query("SELECT * FROM test_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: Long): TestSession?
    
    @Insert
    suspend fun insertSession(session: TestSession): Long
    
    @Update
    suspend fun updateSession(session: TestSession)
    
    @Query("SELECT * FROM test_results WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getResultsForSession(sessionId: Long): Flow<List<TestResult>>
    
    @Query("SELECT * FROM test_results ORDER BY timestamp ASC")
    fun getAllResults(): Flow<List<TestResult>>
    
    @Query("SELECT * FROM test_results WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastResultForSession(sessionId: Long): TestResult?
    
    @Insert
    suspend fun insertResult(result: TestResult): Long
    
    @Query("SELECT COUNT(*) FROM test_results WHERE sessionId = :sessionId")
    suspend fun getTestCountForSession(sessionId: Long): Int
    
    @Query("DELETE FROM test_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}