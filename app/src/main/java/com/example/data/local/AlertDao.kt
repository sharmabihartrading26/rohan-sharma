package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM custom_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<CustomAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: CustomAlertEntity): Long

    @Update
    suspend fun updateAlert(alert: CustomAlertEntity)

    @Query("DELETE FROM custom_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)

    @Query("UPDATE custom_alerts SET isTriggered = 1, triggeredAt = :triggeredAt WHERE id = :id")
    suspend fun markTriggered(id: Int, triggeredAt: Long = System.currentTimeMillis())

    @Query("UPDATE custom_alerts SET isActive = :isActive WHERE id = :id")
    suspend fun setAlertActive(id: Int, isActive: Boolean)
}
