package com.example.androidproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_sessions")
data class TestSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val iperfServer: String,
    val testDuration: Int,
    val apLatitude: Double,
    val apLongitude: Double,
    val startTime: Long,
    val endTime: Long? = null,
    val testCount: Int = 0
) {
    // Transient property for UI selection state (not stored in database)
    @Transient
    var isSelected: Boolean = false
}