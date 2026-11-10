package com.example.scrollsense.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_records",
    indices = [Index(value = ["dateEpochMillis", "packageName"], unique = true)]
)
data class UsageRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochMillis: Long,
    val packageName: String,
    val foregroundMinutes: Int,
    val estimatedVideos: Int = 0,
    val lateNightMinutes: Int = 0,
    val longestSessionMinutes: Int = 0
)

@Entity(tableName = "intervention_events")
data class InterventionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val usedMinutes: Int,
    val limitMinutes: Int
)

@Entity(tableName = "reflection_records")
data class ReflectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val triggerReason: String,
    val mood: String,
    val notes: String = ""
)
