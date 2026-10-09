package com.example.engine

import com.example.data.model.IncidentReport
import com.example.data.model.ReportCategory
import com.example.data.model.ReportStatus

object FreshnessEngine {

    fun formatTimeAgo(timestamp: Long, currentTime: Long = System.currentTimeMillis()): String {
        val diffSeconds = (currentTime - timestamp) / 1000
        return when {
            diffSeconds < 60 -> "Just now"
            diffSeconds < 3600 -> "${diffSeconds / 60} min ago"
            diffSeconds < 86400 -> "${diffSeconds / 3600} hr ago"
            else -> "${diffSeconds / 86400} d ago"
        }
    }

    fun isStale(report: IncidentReport, currentTime: Long = System.currentTimeMillis()): Boolean {
        val elapsedMinutes = (currentTime - report.lastVerifiedAt) / (1000 * 60)
        return when (report.category) {
            ReportCategory.TRAFFIC_ROADS -> elapsedMinutes > 40
            ReportCategory.CROWD_ACTIVITY -> elapsedMinutes > 50
            ReportCategory.WEATHER_ENV -> elapsedMinutes > 180
            ReportCategory.SAFETY -> elapsedMinutes > 240
            ReportCategory.INFRASTRUCTURE -> elapsedMinutes > 2880
            else -> elapsedMinutes > 120
        }
    }

    fun getEffectiveStatus(report: IncidentReport, currentTime: Long = System.currentTimeMillis()): ReportStatus {
        if (report.status == ReportStatus.RESOLVED || report.status == ReportStatus.DISPUTED) {
            return report.status
        }
        if (isStale(report, currentTime)) {
            return ReportStatus.STALE
        }
        return when {
            report.confidenceScore >= 85 -> ReportStatus.VERIFIED
            report.confidenceScore >= 60 -> ReportStatus.ACTIVE
            else -> ReportStatus.DEVELOPING
        }
    }
}
