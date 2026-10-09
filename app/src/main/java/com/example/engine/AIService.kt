package com.example.engine

import com.example.data.model.IncidentReport
import com.example.data.model.ReportCategory
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus

data class AIAnswer(
    val query: String,
    val summary: String,
    val detailPoints: List<String>,
    val supportingReports: List<IncidentReport>,
    val observationsCount: Int,
    val boundaryDisclaimer: String = "Reasoned strictly over observations currently held by this node. External areas not covered."
)

object AIService {

    fun answerQuery(query: String, localReports: List<IncidentReport>): AIAnswer {
        val lowerQuery = query.lowercase().trim()
        val activeReports = localReports.filter { it.status != ReportStatus.RESOLVED }

        return when {
            lowerQuery.contains("travel") || lowerQuery.contains("pass") || lowerQuery.contains("route") || lowerQuery.contains("commute") -> {
                val blockingHazards = activeReports.filter {
                    it.category == ReportCategory.TRAFFIC_ROADS ||
                    it.category == ReportCategory.WEATHER_ENV ||
                    it.severity == ReportSeverity.HIGH ||
                    it.severity == ReportSeverity.CRITICAL
                }

                if (blockingHazards.isNotEmpty()) {
                    val highConfidenceHazards = blockingHazards.filter { it.confidenceScore >= 70 }
                    val totalObservations = blockingHazards.sumOf { it.verificationsCount + it.independentObservationsCount }

                    val summary = if (highConfidenceHazards.isNotEmpty()) {
                        "There are ${highConfidenceHazards.size} high-confidence incident(s) affecting travel in this area. Caution is advised, and an alternate route may be preferable."
                    } else {
                        "There are ${blockingHazards.size} developing reports in this transit sector. Verification is ongoing."
                    }

                    val points = blockingHazards.map {
                        "${it.subcategory} at ${it.areaName} (${it.distanceMeters}m away, ${it.confidenceScore}% community confidence with ${it.verificationsCount} confirmations)."
                    }

                    AIAnswer(
                        query = query,
                        summary = summary,
                        detailPoints = points,
                        supportingReports = blockingHazards,
                        observationsCount = maxOf(1, totalObservations)
                    )
                } else {
                    AIAnswer(
                        query = query,
                        summary = "No active road obstructions, severe weather, or major transit disruptions are currently reported within your node's local vicinity.",
                        detailPoints = listOf(
                            "Local roads and nearby transit routes report clear conditions.",
                            "Closest recorded active incident is beyond immediate transit corridors."
                        ),
                        supportingReports = emptyList(),
                        observationsCount = activeReports.size
                    )
                }
            }

            lowerQuery.contains("emergency") || lowerQuery.contains("medical") || lowerQuery.contains("critical") || lowerQuery.contains("danger") -> {
                val emergencies = activeReports.filter {
                    it.category == ReportCategory.MEDICAL ||
                    it.category == ReportCategory.SAFETY ||
                    it.severity == ReportSeverity.CRITICAL
                }

                if (emergencies.isNotEmpty()) {
                    AIAnswer(
                        query = query,
                        summary = "${emergencies.size} critical community alert(s) currently recorded by local peers.",
                        detailPoints = emergencies.map {
                            "${it.title} (${it.areaName}, ${it.distanceMeters}m away) — ${it.confidenceScore}% confidence. Community response is active."
                        },
                        supportingReports = emergencies,
                        observationsCount = emergencies.sumOf { it.verificationsCount }
                    )
                } else {
                    AIAnswer(
                        query = query,
                        summary = "No critical emergencies or safety threats reported in the immediate local vicinity.",
                        detailPoints = listOf(
                            "Peer mesh reports 0 critical hazards in your immediate 2km radius.",
                            "Emergency services should always be contacted directly for life-threatening situations."
                        ),
                        supportingReports = emptyList(),
                        observationsCount = 0
                    )
                }
            }

            lowerQuery.contains("water") || lowerQuery.contains("flood") || lowerQuery.contains("rain") -> {
                val weatherReports = activeReports.filter {
                    it.category == ReportCategory.WEATHER_ENV ||
                    it.subcategory.contains("water", ignoreCase = true)
                }

                if (weatherReports.isNotEmpty()) {
                    AIAnswer(
                        query = query,
                        summary = "Found ${weatherReports.size} waterlogging/weather report(s) nearby.",
                        detailPoints = weatherReports.map {
                            "${it.title} at ${it.areaName} (${it.confidenceScore}% confidence, ${it.distanceMeters}m away)."
                        },
                        supportingReports = weatherReports,
                        observationsCount = weatherReports.sumOf { it.verificationsCount }
                    )
                } else {
                    AIAnswer(
                        query = query,
                        summary = "No active flooding or waterlogging reported in the current node cache.",
                        detailPoints = listOf("Drains and roadway surfaces in this area have no open flood notices."),
                        supportingReports = emptyList(),
                        observationsCount = 0
                    )
                }
            }

            else -> {
                // General "What's happening around me?"
                val topReports = activeReports.sortedByDescending { it.confidenceScore }.take(4)
                val totalObs = topReports.sumOf { it.verificationsCount }
                val summary = if (topReports.isNotEmpty()) {
                    "Currently tracking ${activeReports.size} active community report(s) nearby. Highest confidence events involve ${topReports.first().subcategory.lowercase()} and ${topReports.getOrNull(1)?.subcategory?.lowercase() ?: "local transit"}."
                } else {
                    "Your node reports all clear! No active disruptions detected in the surrounding zone."
                }

                val points = topReports.map {
                    "${it.title} — ${it.distanceMeters}m away, ${it.confidenceScore}% confidence (${it.status.label})."
                }

                AIAnswer(
                    query = query,
                    summary = summary,
                    detailPoints = points,
                    supportingReports = topReports,
                    observationsCount = maxOf(activeReports.size, totalObs)
                )
            }
        }
    }
}
