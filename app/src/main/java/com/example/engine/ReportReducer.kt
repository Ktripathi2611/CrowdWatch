package com.example.engine

import com.example.data.model.IncidentReport
import com.example.data.model.MediaType
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus
import com.example.data.model.EventType
import com.example.p2p.model.NodeSyncEvent

/**
 * ReportReducer: Deterministic incident reconstruction from append-only immutable event history.
 * Every CrowdWatch node runs this exact reducer locally.
 *
 * Events in history:
 *  REPORT_CREATED
 *  EVIDENCE_ADDED
 *  REPORT_CONFIRMED
 *  REPORT_DISPUTED
 *  STILL_HAPPENING
 *  REPORT_RESOLVED
 *
 * Given the same set of valid cryptographically verified events, any node will compute the identical
 * state for an incident without relying on an authoritative centralized server.
 */
object ReportReducer {

    fun reduce(events: List<NodeSyncEvent>, existingReport: IncidentReport? = null): IncidentReport? {
        if (events.isEmpty() && existingReport == null) return null

        // Chronological sort
        val sortedEvents = events.sortedWith(compareBy({ it.timestamp }, { it.version }))

        val creationEvent = sortedEvents.firstOrNull { it.eventType == EventType.REPORT_CREATED }

        var id = creationEvent?.reportId ?: existingReport?.id ?: return null
        var category = creationEvent?.category ?: existingReport?.category ?: ReportCategory.OTHER
        var subcategory = creationEvent?.subcategory ?: existingReport?.subcategory ?: "Incident"
        var title = creationEvent?.title ?: existingReport?.title ?: "Community Incident"
        var description = creationEvent?.description ?: existingReport?.description ?: ""
        var latitude = creationEvent?.latitude ?: existingReport?.latitude ?: 19.2961
        var longitude = creationEvent?.longitude ?: existingReport?.longitude ?: 72.8604
        var accuracyMeters = creationEvent?.accuracyMeters ?: existingReport?.accuracyMeters ?: 10f
        var areaName = creationEvent?.areaName ?: existingReport?.areaName ?: "Local Area"
        var createdAt = creationEvent?.timestamp ?: existingReport?.createdAt ?: System.currentTimeMillis()
        var updatedAt = createdAt
        var status = existingReport?.status ?: ReportStatus.ACTIVE
        var severity = creationEvent?.severity ?: existingReport?.severity ?: ReportSeverity.MEDIUM
        var reporterNodeId = creationEvent?.authorNodeId ?: existingReport?.reporterNodeId ?: "unknown"
        var evidenceCount = existingReport?.evidenceCount ?: (if (creationEvent?.evidenceType != null) 1 else 0)
        var evidenceType = creationEvent?.evidenceType ?: existingReport?.evidenceType ?: MediaType.PHOTO
        var evidenceSampleType = creationEvent?.evidenceSampleType ?: existingReport?.evidenceSampleType ?: "photo"
        var lastVerifiedAt = createdAt
        var condition = creationEvent?.condition ?: existingReport?.condition ?: ReportCondition.HAPPENING_NOW
        var distanceMeters = existingReport?.distanceMeters ?: 300
        var version = existingReport?.version ?: 1

        // Independent observation tracking by unique node identities
        val confirmingNodes = mutableSetOf<String>()
        val disputingNodes = mutableSetOf<String>()
        val observingNodes = mutableSetOf<String>()

        if (reporterNodeId.isNotBlank()) {
            observingNodes.add(reporterNodeId)
        }

        // Fold remaining events over state
        for (event in sortedEvents) {
            if (event.timestamp > updatedAt) {
                updatedAt = event.timestamp
            }
            version = maxOf(version, event.version)

            when (event.eventType) {
                EventType.REPORT_CREATED -> {
                    observingNodes.add(event.authorNodeId)
                }

                EventType.EVIDENCE_ADDED -> {
                    evidenceCount += 1
                    observingNodes.add(event.authorNodeId)
                    event.evidenceType?.let { evidenceType = it }
                    event.evidenceSampleType?.let { evidenceSampleType = it }
                    lastVerifiedAt = maxOf(lastVerifiedAt, event.timestamp)
                }

                EventType.REPORT_CONFIRMED -> {
                    confirmingNodes.add(event.authorNodeId)
                    observingNodes.add(event.authorNodeId)
                    disputingNodes.remove(event.authorNodeId)
                    lastVerifiedAt = maxOf(lastVerifiedAt, event.timestamp)
                    if (status != ReportStatus.RESOLVED) {
                        status = if (confirmingNodes.size >= 2) ReportStatus.VERIFIED else ReportStatus.ACTIVE
                    }
                }

                EventType.REPORT_DISPUTED -> {
                    disputingNodes.add(event.authorNodeId)
                    observingNodes.add(event.authorNodeId)
                    confirmingNodes.remove(event.authorNodeId)
                    if (disputingNodes.size > confirmingNodes.size && status != ReportStatus.RESOLVED) {
                        status = ReportStatus.DISPUTED
                    }
                }

                EventType.STILL_HAPPENING -> {
                    condition = ReportCondition.HAPPENING_NOW
                    observingNodes.add(event.authorNodeId)
                    lastVerifiedAt = maxOf(lastVerifiedAt, event.timestamp)
                    if (status == ReportStatus.STALE || status == ReportStatus.RESOLVED) {
                        status = ReportStatus.ACTIVE
                    }
                }

                EventType.REPORT_RESOLVED -> {
                    condition = ReportCondition.RESOLVED
                    status = ReportStatus.RESOLVED
                    observingNodes.add(event.authorNodeId)
                    lastVerifiedAt = maxOf(lastVerifiedAt, event.timestamp)
                }

                EventType.CONFIDENCE_UPDATED, EventType.REPORT_EXPIRED -> {
                    // Handled during confidence calculation
                }
            }
        }

        // Total unique independent observers
        val independentCount = observingNodes.size.coerceAtLeast(1)
        val verificationsCount = (confirmingNodes.size + 1).coerceAtLeast(1) // +1 for original creator
        val disputesCount = disputingNodes.size

        val intermediateReport = IncidentReport(
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
            confidenceScore = 50, // Calculated next
            reporterNodeId = reporterNodeId,
            evidenceCount = evidenceCount,
            evidenceType = evidenceType,
            evidenceSampleType = evidenceSampleType,
            verificationsCount = verificationsCount,
            disputesCount = disputesCount,
            independentObservationsCount = independentCount,
            lastVerifiedAt = lastVerifiedAt,
            condition = condition,
            distanceMeters = distanceMeters,
            version = version
        )

        // Deterministically compute local confidence score using the latest event time
        val referenceTime = maxOf(lastVerifiedAt, updatedAt)
        val breakdown = ConfidenceEngine.calculateConfidence(intermediateReport, referenceTime)
        return intermediateReport.copy(confidenceScore = breakdown.finalScore)
    }
}
