package com.example.data.local

import com.example.data.model.AlertItem
import com.example.data.model.IncidentReport
import com.example.data.model.ReportEvent

fun IncidentReport.toEntity(): ReportEntity = ReportEntity(
    id = id,
    category = category,
    subcategory = subcategory,
    title = title,
    description = description,
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracyMeters,
    areaName = areaName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    status = status,
    severity = severity,
    confidenceScore = confidenceScore,
    reporterNodeId = reporterNodeId,
    evidenceCount = evidenceCount,
    evidenceType = evidenceType,
    evidenceSampleType = evidenceSampleType,
    verificationsCount = verificationsCount,
    disputesCount = disputesCount,
    independentObservationsCount = independentObservationsCount,
    lastVerifiedAt = lastVerifiedAt,
    condition = condition,
    distanceMeters = distanceMeters,
    version = version
)

fun ReportEntity.toModel(): IncidentReport = IncidentReport(
    id = id,
    category = category,
    subcategory = subcategory,
    title = title,
    description = description,
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracyMeters,
    areaName = areaName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    status = status,
    severity = severity,
    confidenceScore = confidenceScore,
    reporterNodeId = reporterNodeId,
    evidenceCount = evidenceCount,
    evidenceType = evidenceType,
    evidenceSampleType = evidenceSampleType,
    verificationsCount = verificationsCount,
    disputesCount = disputesCount,
    independentObservationsCount = independentObservationsCount,
    lastVerifiedAt = lastVerifiedAt,
    condition = condition,
    distanceMeters = distanceMeters,
    version = version
)

fun ReportEventEntity.toModel(): ReportEvent = ReportEvent(
    eventId = eventId,
    reportId = reportId,
    nodeId = nodeId,
    type = type,
    timestamp = timestamp,
    details = details,
    evidencePayload = evidencePayload,
    syncedToPeers = syncedToPeers
)

fun AlertEntity.toModel(): AlertItem = AlertItem(
    id = id,
    reportId = reportId,
    title = title,
    message = message,
    severity = severity,
    timestamp = timestamp,
    distanceMeters = distanceMeters,
    confidenceScore = confidenceScore,
    isRead = isRead,
    type = type
)

fun AlertItem.toEntity(): AlertEntity = AlertEntity(
    id = id,
    reportId = reportId,
    title = title,
    message = message,
    severity = severity,
    timestamp = timestamp,
    distanceMeters = distanceMeters,
    confidenceScore = confidenceScore,
    isRead = isRead,
    type = type
)
