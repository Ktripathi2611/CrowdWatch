package com.example.p2p.model

import com.example.data.model.EventType
import com.example.data.model.IncidentReport
import com.example.data.model.MediaType
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportEvent
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus
import java.util.UUID

/**
 * Immutable, cryptographically verifiable synchronization event.
 * In CrowdWatch's distributed architecture, mutable reports are never synchronized directly.
 * All mutations and observations exist as append-only immutable events.
 */
data class NodeSyncEvent(
    val eventId: String = "evt-${UUID.randomUUID().toString().take(8)}",
    val reportId: String,
    val originNodeId: String,
    val authorNodeId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: EventType,
    val payload: String = "",
    val version: Int = 1,
    val signature: String = "",
    val previousEventHash: String = "",
    val ttl: Int = 5,
    val hopCount: Int = 0,
    // Optional snapshot fields embedded during REPORT_CREATED or EVIDENCE_ADDED
    val title: String? = null,
    val description: String? = null,
    val category: ReportCategory? = null,
    val subcategory: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val areaName: String? = null,
    val severity: ReportSeverity? = null,
    val condition: ReportCondition? = null,
    val evidenceType: MediaType? = null,
    val evidenceSampleType: String? = null,
    val evidenceUri: String? = null
) {
    /**
     * Serialized representation used for signature verification and tamper detection.
     */
    fun canonicalPayload(): String =
        "$eventId:$reportId:$authorNodeId:$originNodeId:$timestamp:${eventType.name}:$version:$previousEventHash:$payload"

    fun toReportEvent(): ReportEvent = ReportEvent(
        eventId = eventId,
        reportId = reportId,
        nodeId = authorNodeId,
        type = eventType,
        timestamp = timestamp,
        details = payload.ifBlank { "${eventType.name} registered by node $authorNodeId" },
        evidencePayload = evidenceUri,
        syncedToPeers = true
    )
}

/**
 * Compact handshake manifest for lightweight peer reconciliation.
 * Nodes exchange manifest summaries first rather than transferring the entire database.
 */
data class SyncHandshakeManifest(
    val nodeId: String,
    val protocolVersion: Int = 1,
    val knownEventIds: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val signature: String = ""
)

/**
 * Peer-to-Peer evidence request.
 * Raw photos and videos stay strictly local on observing devices until explicitly requested by a peer.
 */
data class EvidenceRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val requesterNodeId: String,
    val reportId: String,
    val evidenceId: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class EvidenceResponse(
    val requestId: String,
    val responderNodeId: String,
    val reportId: String,
    val evidenceId: String,
    val dataBase64: String?,
    val timestamp: Long = System.currentTimeMillis()
)
