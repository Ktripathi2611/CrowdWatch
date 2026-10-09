package com.example.engine

import com.example.data.model.IncidentReport
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportStatus

object ConfidenceEngine {

    data class ConfidenceBreakdown(
        val finalScore: Int,
        val statusText: String, // "High confidence", "Strong confidence", "Developing", "Low confidence", "Unverified"
        val factors: List<String>
    )

    fun calculateConfidence(
        report: IncidentReport,
        currentTime: Long = System.currentTimeMillis()
    ): ConfidenceBreakdown {
        if (report.status == ReportStatus.RESOLVED || report.condition == ReportCondition.RESOLVED) {
            return ConfidenceBreakdown(
                finalScore = 0,
                statusText = "Resolved",
                factors = listOf("Marked as resolved by community", "Situation no longer present")
            )
        }

        var score = 50 // Base new report score
        val factors = mutableListOf<String>()
        factors.add("Base report created: +50%")

        // Evidence factor
        if (report.evidenceCount > 0) {
            val evidenceBonus = minOf(15, report.evidenceCount * 12)
            score += evidenceBonus
            factors.add("Verified media evidence attached: +$evidenceBonus%")
        }

        // Confirmations & independent observations
        val independentBonus = minOf(35, report.independentObservationsCount * 8)
        if (independentBonus > 0) {
            score += independentBonus
            factors.add("${report.independentObservationsCount} independent peer nodes confirmed: +$independentBonus%")
        }

        // Disputes
        if (report.disputesCount > 0) {
            val disputePenalty = report.disputesCount * 18
            score -= disputePenalty
            factors.add("${report.disputesCount} community disputes recorded: -$disputePenalty%")
        }

        // Freshness penalty
        val ageMinutes = (currentTime - report.lastVerifiedAt) / (1000 * 60)
        val decay = when (report.category) {
            ReportCategory.TRAFFIC_ROADS -> (ageMinutes / 10).toInt() * 4
            ReportCategory.CROWD_ACTIVITY -> (ageMinutes / 12).toInt() * 4
            ReportCategory.WEATHER_ENV -> (ageMinutes / 20).toInt() * 3
            ReportCategory.SAFETY -> (ageMinutes / 30).toInt() * 2
            ReportCategory.INFRASTRUCTURE -> (ageMinutes / 120).toInt() * 2
            else -> (ageMinutes / 15).toInt() * 3
        }
        if (decay > 0) {
            val clampedDecay = minOf(30, decay)
            score -= clampedDecay
            factors.add("Time decay without recent verification: -$clampedDecay%")
        }

        val finalScore = score.coerceIn(5, 99)
        val statusText = when (finalScore) {
            in 90..100 -> "High confidence"
            in 70..89 -> "Strong confidence"
            in 50..69 -> "Developing"
            in 30..49 -> "Low confidence"
            else -> "Unverified"
        }

        return ConfidenceBreakdown(finalScore, statusText, factors)
    }
}
