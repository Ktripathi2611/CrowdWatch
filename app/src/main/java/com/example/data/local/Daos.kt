package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: String): ReportEntity?

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    fun observeReportById(id: String): Flow<ReportEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<ReportEntity>)

    @Update
    suspend fun update(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM reports WHERE id LIKE 'rep-%' OR id LIKE 'demo-%'")
    suspend fun deleteLegacyReports()

    @Query("DELETE FROM reports")
    suspend fun clearAll()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM report_events WHERE reportId = :reportId ORDER BY timestamp ASC")
    fun getEventsForReport(reportId: String): Flow<List<ReportEventEntity>>

    @Query("SELECT * FROM report_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<ReportEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ReportEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<ReportEventEntity>)

    @Query("DELETE FROM report_events")
    suspend fun clearAll()
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<AlertEntity>)

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM alerts")
    suspend fun clearAll()
}
