package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.IncidentReport
import com.example.data.model.MediaType
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus
import com.example.engine.AIService
import com.example.engine.ConfidenceEngine
import com.example.engine.FreshnessEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CrowdWatch", appName)
    }

    @Test
    fun `test confidence engine calculation with confirmations and evidence`() {
        val now = System.currentTimeMillis()
        val report = IncidentReport(
            id = "test-1",
            category = ReportCategory.TRAFFIC_ROADS,
            subcategory = "Road obstruction",
            title = "Fallen tree",
            description = "Fallen tree on transit lane",
            latitude = 19.29,
            longitude = 72.85,
            accuracyMeters = 5f,
            areaName = "Mira Road",
            createdAt = now,
            updatedAt = now,
            status = ReportStatus.ACTIVE,
            severity = ReportSeverity.HIGH,
            confidenceScore = 50,
            reporterNodeId = "node-alpha",
            evidenceCount = 2,
            evidenceType = MediaType.PHOTO,
            evidenceSampleType = "fallen_tree",
            verificationsCount = 5,
            disputesCount = 0,
            independentObservationsCount = 4,
            lastVerifiedAt = now,
            condition = ReportCondition.HAPPENING_NOW
        )

        val breakdown = ConfidenceEngine.calculateConfidence(report, now)
        assertTrue("Score should increase with evidence and observations", breakdown.finalScore >= 80)
    }

    @Test
    fun `test freshness engine time ago formatting`() {
        val now = System.currentTimeMillis()
        assertEquals("Just now", FreshnessEngine.formatTimeAgo(now - 20_000, now))
        assertEquals("5 min ago", FreshnessEngine.formatTimeAgo(now - 300_000, now))
        assertEquals("2 hr ago", FreshnessEngine.formatTimeAgo(now - 7_200_000, now))
    }

    @Test
    fun `test local AI reasoning service avoids hallucination`() {
        val answer = AIService.answerQuery("Can I travel through this area?", emptyList())
        assertTrue(answer.summary.contains("No active road obstructions", ignoreCase = true))
        assertEquals(0, answer.supportingReports.size)
    }

    @Test
    fun `test proximity filtering distance threshold`() {
        // Points ~500m apart
        val lat1 = 19.2961
        val lon1 = 72.8604
        val lat2 = 19.2990
        val lon2 = 72.8630

        val r = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = r * c

        assertTrue("Distance should be approximately 400-500 meters", distance in 350.0..550.0)
    }
}
