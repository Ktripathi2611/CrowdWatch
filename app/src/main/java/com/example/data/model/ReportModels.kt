package com.example.data.model

enum class ReportCategory(val displayName: String, val iconName: String) {
    SAFETY("Safety", "ic_safety"),
    TRAFFIC_ROADS("Traffic & Roads", "ic_traffic"),
    WEATHER_ENV("Weather & Environment", "ic_weather"),
    INFRASTRUCTURE("Infrastructure", "ic_infrastructure"),
    CROWD_ACTIVITY("Crowd & Human Activity", "ic_crowd"),
    PUBLIC_TRANSPORT("Public Transport", "ic_transport"),
    LOCAL_SERVICES("Local Services", "ic_services"),
    MEDICAL("Medical", "ic_medical"),
    UTILITIES("Utilities", "ic_utilities"),
    OTHER("Something Else", "ic_other")
}

enum class ReportSeverity(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}

enum class ReportStatus(val label: String) {
    ACTIVE("Active"),
    DEVELOPING("Developing"),
    VERIFIED("Verified"),
    STALE("Possibly Outdated"),
    RESOLVED("Resolved"),
    DISPUTED("Disputed")
}

enum class ReportCondition(val label: String) {
    HAPPENING_NOW("Happening now"),
    CHANGING("Happening but changing"),
    NOT_SURE("Not sure"),
    RESOLVED("Already resolved")
}

enum class EventType {
    REPORT_CREATED,
    EVIDENCE_ADDED,
    REPORT_CONFIRMED,
    REPORT_DISPUTED,
    STILL_HAPPENING,
    REPORT_RESOLVED,
    CONFIDENCE_UPDATED,
    REPORT_EXPIRED
}

enum class MediaType {
    PHOTO,
    VIDEO,
    AUDIO
}

enum class AlertType {
    CRITICAL,
    NEARBY,
    UPDATES,
    RESOLUTION
}

data class IncidentReport(
    val id: String,
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
    val confidenceScore: Int, // 0 to 100
    val reporterNodeId: String,
    val evidenceCount: Int = 1,
    val evidenceType: MediaType = MediaType.PHOTO,
    val evidenceSampleType: String = "photo", // "fallen_tree", "waterlogging", "traffic", "medical", "streetlight"
    val verificationsCount: Int = 1,
    val disputesCount: Int = 0,
    val independentObservationsCount: Int = 1,
    val lastVerifiedAt: Long = createdAt,
    val condition: ReportCondition = ReportCondition.HAPPENING_NOW,
    val distanceMeters: Int = 250,
    val version: Int = 1
)

data class ReportEvent(
    val eventId: String,
    val reportId: String,
    val nodeId: String,
    val type: EventType,
    val timestamp: Long,
    val details: String,
    val evidencePayload: String? = null,
    val syncedToPeers: Boolean = true
)

data class AlertItem(
    val id: String,
    val reportId: String,
    val title: String,
    val message: String,
    val severity: ReportSeverity,
    val timestamp: Long,
    val distanceMeters: Int,
    val confidenceScore: Int,
    val isRead: Boolean = false,
    val type: AlertType = AlertType.NEARBY
)

data class NodeState(
    val nodeId: String,
    val nodeName: String,
    val isOnline: Boolean,
    val reportsHeld: Int,
    val pendingSync: Int,
    val lastSyncAgo: String,
    val peerCount: Int
)

data class UserProfile(
    val nodeId: String = "local-node",
    val displayName: String = "Civic Contributor",
    val reliabilityScore: Int = 100,
    val reportsSubmitted: Int = 0,
    val verificationsCount: Int = 0,
    val confirmedCount: Int = 0,
    val disputedCount: Int = 0,
    val resolvedCount: Int = 0,
    val locationPrecision: String = "Approximate (1 km)",
    val shareEvidenceWithPeers: Boolean = true,
    val dataRetentionDays: Int = 7
)
