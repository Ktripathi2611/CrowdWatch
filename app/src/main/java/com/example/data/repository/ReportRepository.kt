package com.example.data.repository

import com.example.data.local.AlertDao
import com.example.data.local.AlertEntity
import com.example.data.local.EventDao
import com.example.data.local.ReportDao
import com.example.data.local.ReportEntity
import com.example.data.local.ReportEventEntity
import com.example.data.model.AlertItem
import com.example.data.model.AlertType
import com.example.data.model.EventType
import com.example.data.model.IncidentReport
import com.example.data.model.MediaType
import com.example.data.model.NodeState
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportEvent
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus
import com.example.data.model.UserProfile
import com.example.engine.ConfidenceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ReportRepository(
    private val reportDao: ReportDao,
    private val eventDao: EventDao,
    private val alertDao: AlertDao
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _simulatedNodes = MutableStateFlow<List<NodeState>>(emptyList())
    val simulatedNodes: StateFlow<List<NodeState>> = _simulatedNodes.asStateFlow()

    private val _activeSimulationNodeId = MutableStateFlow("local-node")
    val activeSimulationNodeId: StateFlow<String> = _activeSimulationNodeId.asStateFlow()

    private val _networkEventLogs = MutableStateFlow(
        listOf("CrowdWatch real-time mesh engine online.")
    )
    val networkEventLogs: StateFlow<List<String>> = _networkEventLogs.asStateFlow()

    init {
        coroutineScope.launch {
            purgeLegacyDemoData()
        }
    }

    val allReports: Flow<List<IncidentReport>> = reportDao.getAllReports().map { list ->
        list.map { it.toModel() }
    }

    val allAlerts: Flow<List<AlertItem>> = alertDao.getAllAlerts().map { list ->
        list.map { it.toModel() }
    }

    val allEvents: Flow<List<ReportEvent>> = eventDao.getAllEvents().map { list ->
        list.map { it.toModel() }
    }

    fun getEventsForReport(reportId: String): Flow<List<ReportEvent>> {
        return eventDao.getEventsForReport(reportId).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun getReportById(id: String): IncidentReport? {
        return reportDao.getReportById(id)?.toModel()
    }

    fun observeReportById(id: String): Flow<IncidentReport?> {
        return reportDao.observeReportById(id).map { it?.toModel() }
    }

    suspend fun createReport(report: IncidentReport) {
        val entity = report.toEntity()
        reportDao.insertOrUpdate(entity)

        val creationEvent = ReportEventEntity(
            eventId = "evt-${UUID.randomUUID().toString().take(8)}",
            reportId = report.id,
            nodeId = report.reporterNodeId,
            type = EventType.REPORT_CREATED,
            timestamp = report.createdAt,
            details = "Incident observed & registered locally by node ${report.reporterNodeId}",
            evidencePayload = "Attached ${report.evidenceType.name} evidence",
            syncedToPeers = true
        )
        eventDao.insertEvent(creationEvent)

        // Increment user profile submitted
        _userProfile.value = _userProfile.value.copy(
            reportsSubmitted = _userProfile.value.reportsSubmitted + 1
        )

        addNetworkLog("${_userProfile.value.nodeId} created local report '${report.title}'. Broadcasted to mesh.")
    }

    suspend fun confirmReport(reportId: String, actingNodeId: String = _userProfile.value.nodeId) {
        val reportEntity = reportDao.getReportById(reportId) ?: return
        val current = reportEntity.toModel()

        val updated = current.copy(
            verificationsCount = current.verificationsCount + 1,
            independentObservationsCount = current.independentObservationsCount + 1,
            lastVerifiedAt = System.currentTimeMillis()
        )
        val breakdown = ConfidenceEngine.calculateConfidence(updated)
        val finalReport = updated.copy(confidenceScore = breakdown.finalScore)

        reportDao.insertOrUpdate(finalReport.toEntity())

        val event = ReportEventEntity(
            eventId = "evt-${UUID.randomUUID().toString().take(8)}",
            reportId = reportId,
            nodeId = actingNodeId,
            type = EventType.REPORT_CONFIRMED,
            timestamp = System.currentTimeMillis(),
            details = "Confirmed by independent node $actingNodeId",
            evidencePayload = null,
            syncedToPeers = true
        )
        eventDao.insertEvent(event)

        if (actingNodeId == _userProfile.value.nodeId) {
            _userProfile.value = _userProfile.value.copy(
                verificationsCount = _userProfile.value.verificationsCount + 1,
                confirmedCount = _userProfile.value.confirmedCount + 1
            )
        }

        addNetworkLog("Peer $actingNodeId confirmed '$reportId'. Confidence updated to ${breakdown.finalScore}%.")
    }

    suspend fun disputeReport(reportId: String, actingNodeId: String = _userProfile.value.nodeId, reason: String = "Inaccurate or no longer present") {
        val reportEntity = reportDao.getReportById(reportId) ?: return
        val current = reportEntity.toModel()

        val updated = current.copy(
            disputesCount = current.disputesCount + 1,
            status = if (current.disputesCount >= 2) ReportStatus.DISPUTED else current.status,
            lastVerifiedAt = System.currentTimeMillis()
        )
        val breakdown = ConfidenceEngine.calculateConfidence(updated)
        val finalReport = updated.copy(confidenceScore = breakdown.finalScore)

        reportDao.insertOrUpdate(finalReport.toEntity())

        val event = ReportEventEntity(
            eventId = "evt-${UUID.randomUUID().toString().take(8)}",
            reportId = reportId,
            nodeId = actingNodeId,
            type = EventType.REPORT_DISPUTED,
            timestamp = System.currentTimeMillis(),
            details = "Disputed by node $actingNodeId: $reason",
            evidencePayload = null,
            syncedToPeers = true
        )
        eventDao.insertEvent(event)

        if (actingNodeId == _userProfile.value.nodeId) {
            _userProfile.value = _userProfile.value.copy(
                verificationsCount = _userProfile.value.verificationsCount + 1,
                disputedCount = _userProfile.value.disputedCount + 1
            )
        }

        addNetworkLog("Peer $actingNodeId disputed '$reportId'. Confidence recalculated to ${breakdown.finalScore}%.")
    }

    suspend fun markStillHappening(reportId: String, actingNodeId: String = _userProfile.value.nodeId) {
        val reportEntity = reportDao.getReportById(reportId) ?: return
        val current = reportEntity.toModel()

        val updated = current.copy(
            verificationsCount = current.verificationsCount + 1,
            condition = ReportCondition.HAPPENING_NOW,
            lastVerifiedAt = System.currentTimeMillis()
        )
        val breakdown = ConfidenceEngine.calculateConfidence(updated)
        val finalReport = updated.copy(confidenceScore = breakdown.finalScore)

        reportDao.insertOrUpdate(finalReport.toEntity())

        val event = ReportEventEntity(
            eventId = "evt-${UUID.randomUUID().toString().take(8)}",
            reportId = reportId,
            nodeId = actingNodeId,
            type = EventType.STILL_HAPPENING,
            timestamp = System.currentTimeMillis(),
            details = "Fresh observation by $actingNodeId: Condition confirmed still present",
            evidencePayload = null,
            syncedToPeers = true
        )
        eventDao.insertEvent(event)

        addNetworkLog("Node $actingNodeId confirmed condition 'Still happening' for '$reportId'.")
    }

    suspend fun resolveReport(reportId: String, actingNodeId: String = _userProfile.value.nodeId) {
        val reportEntity = reportDao.getReportById(reportId) ?: return
        val current = reportEntity.toModel()

        val updated = current.copy(
            status = ReportStatus.RESOLVED,
            condition = ReportCondition.RESOLVED,
            confidenceScore = 0,
            lastVerifiedAt = System.currentTimeMillis()
        )
        reportDao.insertOrUpdate(updated.toEntity())

        val event = ReportEventEntity(
            eventId = "evt-${UUID.randomUUID().toString().take(8)}",
            reportId = reportId,
            nodeId = actingNodeId,
            type = EventType.REPORT_RESOLVED,
            timestamp = System.currentTimeMillis(),
            details = "Resolved by node $actingNodeId: Issue cleared from physical site",
            evidencePayload = null,
            syncedToPeers = true
        )
        eventDao.insertEvent(event)

        if (actingNodeId == _userProfile.value.nodeId) {
            _userProfile.value = _userProfile.value.copy(
                verificationsCount = _userProfile.value.verificationsCount + 1,
                resolvedCount = _userProfile.value.resolvedCount + 1
            )
        }

        addNetworkLog("Incident '$reportId' marked as RESOLVED by peer $actingNodeId.")
    }

    suspend fun addEvidence(reportId: String, mediaType: MediaType, payloadLabel: String, actingNodeId: String = _userProfile.value.nodeId) {
        val reportEntity = reportDao.getReportById(reportId) ?: return
        val current = reportEntity.toModel()

        val updated = current.copy(
            evidenceCount = current.evidenceCount + 1,
            lastVerifiedAt = System.currentTimeMillis()
        )
        val breakdown = ConfidenceEngine.calculateConfidence(updated)
        val finalReport = updated.copy(confidenceScore = breakdown.finalScore)

        reportDao.insertOrUpdate(finalReport.toEntity())

        val event = ReportEventEntity(
            eventId = "evt-${UUID.randomUUID().toString().take(8)}",
            reportId = reportId,
            nodeId = actingNodeId,
            type = EventType.EVIDENCE_ADDED,
            timestamp = System.currentTimeMillis(),
            details = "Additional $payloadLabel uploaded by node $actingNodeId",
            evidencePayload = payloadLabel,
            syncedToPeers = true
        )
        eventDao.insertEvent(event)

        addNetworkLog("Peer $actingNodeId added evidence ($payloadLabel) to '$reportId'. Confidence: ${breakdown.finalScore}%.")
    }

    suspend fun markAlertAsRead(alertId: String) {
        alertDao.markAsRead(alertId)
    }

    fun setActiveSimulationNode(nodeId: String) {
        _activeSimulationNodeId.value = nodeId
    }

    fun toggleNodeOnline(nodeId: String) {
        _simulatedNodes.value = _simulatedNodes.value.map {
            if (it.nodeId == nodeId) it.copy(isOnline = !it.isOnline) else it
        }
    }

    suspend fun syncAllNodes() {
        _simulatedNodes.value = _simulatedNodes.value.map {
            it.copy(pendingSync = 0, lastSyncAgo = "Just now")
        }
        addNetworkLog("Global P2P reconciliation completed. All online peer queues synchronized.")
    }

    fun updatePrivacySettings(precision: String, shareEvidence: Boolean, retentionDays: Int) {
        _userProfile.value = _userProfile.value.copy(
            locationPrecision = precision,
            shareEvidenceWithPeers = shareEvidence,
            dataRetentionDays = retentionDays
        )
    }

    private fun addNetworkLog(log: String) {
        val current = _networkEventLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 20) current.removeAt(current.lastIndex)
        _networkEventLogs.value = current
    }

    /**
     * Purges legacy demonstration reports and mock records from the SQLite database.
     */
    suspend fun purgeLegacyDemoData() {
        reportDao.deleteLegacyReports()
    }

    /**
     * Clears all local caches and incident databases for fresh production state.
     */
    suspend fun clearAllLocalData() {
        reportDao.clearAll()
        eventDao.clearAll()
        alertDao.clearAll()
        _networkEventLogs.value = listOf("Local incident database cleared.")
    }

    private fun IncidentReport.toEntity(): ReportEntity = ReportEntity(
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

    private fun ReportEntity.toModel(): IncidentReport = IncidentReport(
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

    private fun ReportEventEntity.toModel(): ReportEvent = ReportEvent(
        eventId = eventId,
        reportId = reportId,
        nodeId = nodeId,
        type = type,
        timestamp = timestamp,
        details = details,
        evidencePayload = evidencePayload,
        syncedToPeers = syncedToPeers
    )

    private fun AlertItem.toEntity(): AlertEntity = AlertEntity(
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

    private fun AlertEntity.toModel(): AlertItem = AlertItem(
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
}
