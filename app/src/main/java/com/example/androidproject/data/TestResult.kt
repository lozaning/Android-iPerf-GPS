package com.example.androidproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "test_results",
    foreignKeys = [
        ForeignKey(
            entity = TestSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TestResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val distanceFromAp: Double,
    val speedMbps: Double,
    val testDurationSeconds: Double,
    val dataMB: Double,
    val azimuth: Float,
    val pitch: Float,
    val roll: Float,
    val accuracy: Float
)