package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MediaType
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val category: ReportCategory,
    val subcategory: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val areaName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val status: ReportStatus,
    val severity: ReportSeverity,
    val confidenceScore: Int,
    val reporterNodeId: String,
    val evidenceCount: Int,
    val evidenceType: MediaType,
    val evidenceSampleType: String,
    val verificationsCount: Int,
    val disputesCount: Int,
    val independentObservationsCount: Int,
    val lastVerifiedAt: Long,
    val condition: ReportCondition,
    val distanceMeters: Int,
    val version: Int
)

@Entity(tableName = "report_events")
data class ReportEventEntity(
    @PrimaryKey val eventId: String,
    val reportId: String,
    val nodeId: String,
    val type: com.example.data.model.EventType,
    val timestamp: Long,
    val details: String,
    val evidencePayload: String?,
    val syncedToPeers: Boolean
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val reportId: String,
    val title: String,
    val message: String,
    val severity: ReportSeverity,
    val timestamp: Long,
    val distanceMeters: Int,
    val confidenceScore: Int,
    val isRead: Boolean,
    val type: com.example.data.model.AlertType
)
