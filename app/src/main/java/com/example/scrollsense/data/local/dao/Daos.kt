package com.example.scrollsense.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scrollsense.data.local.entities.InterventionEntity
import com.example.scrollsense.data.local.entities.ReflectionEntity
import com.example.scrollsense.data.local.entities.UsageRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: UsageRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<UsageRecordEntity>)

    @Query("SELECT * FROM usage_records WHERE dateEpochMillis = :dateEpochMillis AND packageName = :packageName LIMIT 1")
    fun getRecord(packageName: String, dateEpochMillis: Long): Flow<UsageRecordEntity?>

    @Query("SELECT * FROM usage_records WHERE dateEpochMillis = :dateEpochMillis AND packageName = :packageName LIMIT 1")
    suspend fun getRecordSync(packageName: String, dateEpochMillis: Long): UsageRecordEntity?

    @Query("SELECT * FROM usage_records WHERE dateEpochMillis >= :startEpochMillis ORDER BY dateEpochMillis DESC")
    fun getRecordsSince(startEpochMillis: Long): Flow<List<UsageRecordEntity>>

    @Query("SELECT * FROM usage_records WHERE dateEpochMillis >= :startEpochMillis ORDER BY dateEpochMillis DESC")
    suspend fun getRecordsSinceSync(startEpochMillis: Long): List<UsageRecordEntity>

    @Query("UPDATE usage_records SET estimatedVideos = estimatedVideos + 1 WHERE dateEpochMillis = :dateEpochMillis AND packageName = :packageName")
    suspend fun incrementEstimatedVideos(packageName: String, dateEpochMillis: Long): Int

    @Query("DELETE FROM usage_records WHERE dateEpochMillis = :dateEpochMillis")
    suspend fun deleteForDate(dateEpochMillis: Long)

    @Query("DELETE FROM usage_records")
    suspend fun clearAll()
}

@Dao
interface InterventionDao {
    @Insert
    suspend fun insert(intervention: InterventionEntity): Long

    @Insert
    suspend fun insertAll(interventions: List<InterventionEntity>)

    @Query("SELECT * FROM intervention_events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getInterventionsSince(sinceTimestamp: Long): Flow<List<InterventionEntity>>

    @Query("SELECT * FROM intervention_events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getInterventionsSinceSync(sinceTimestamp: Long): List<InterventionEntity>

    @Query("SELECT COUNT(*) FROM intervention_events WHERE timestamp >= :sinceTimestamp")
    fun getInterventionsCountSince(sinceTimestamp: Long): Flow<Int>

    @Query("DELETE FROM intervention_events WHERE timestamp >= :sinceTimestamp")
    suspend fun deleteSince(sinceTimestamp: Long)

    @Query("DELETE FROM intervention_events")
    suspend fun clearAll()
}

@Dao
interface ReflectionDao {
    @Insert
    suspend fun insert(reflection: ReflectionEntity): Long

    @Insert
    suspend fun insertAll(reflections: List<ReflectionEntity>)

    @Query("SELECT * FROM reflection_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentReflections(limit: Int = 50): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflection_records ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentReflectionsSync(limit: Int = 50): List<ReflectionEntity>

    @Query("DELETE FROM reflection_records")
    suspend fun clearAll()
}
