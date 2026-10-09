package com.example

import com.example.data.model.EventType
import com.example.data.model.MediaType
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus
import com.example.engine.ReportReducer
import com.example.p2p.model.NodeSyncEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DistributedNodeReducerTest {

    @Test
    fun `test deterministic incident reconstruction from distributed event log`() {
        val now = 1700000000000L
        val reportId = "rep-mesh-test"

        // Event 1: Node A creates incident report
        val createEvent = NodeSyncEvent(
            eventId = "evt-1",
            reportId = reportId,
            originNodeId = "node-alpha",
            authorNodeId = "node-alpha",
            timestamp = now,
            eventType = EventType.REPORT_CREATED,
            payload = "Fallen tree blocking intersection",
            version = 1,
            title = "Fallen tree",
            description = "Fallen tree blocking intersection",
            category = ReportCategory.TRAFFIC_ROADS,
            subcategory = "Road obstruction",
            latitude = 19.2961,
            longitude = 72.8604,
            accuracyMeters = 5.0f,
            areaName = "Mira Road Junction",
            severity = ReportSeverity.HIGH,
            condition = ReportCondition.HAPPENING_NOW,
            evidenceType = MediaType.PHOTO,
            evidenceSampleType = "fallen_tree"
        )

        // Event 2: Node B confirms incident
        val confirmEventB = NodeSyncEvent(
            eventId = "evt-2",
            reportId = reportId,
            originNodeId = "node-alpha",
            authorNodeId = "node-bravo",
            timestamp = now + 15_000,
            eventType = EventType.REPORT_CONFIRMED,
            payload = "Confirmed, traffic backing up",
            version = 2
        )

        // Event 3: Node C adds photo evidence
        val evidenceEventC = NodeSyncEvent(
            eventId = "evt-3",
            reportId = reportId,
            originNodeId = "node-alpha",
            authorNodeId = "node-charlie",
            timestamp = now + 45_000,
            eventType = EventType.EVIDENCE_ADDED,
            payload = "Photo evidence attached from north approach",
            evidenceType = MediaType.PHOTO,
            version = 2
        )

        // Event 4: Node D confirms incident
        val confirmEventD = NodeSyncEvent(
            eventId = "evt-4",
            reportId = reportId,
            originNodeId = "node-alpha",
            authorNodeId = "node-delta",
            timestamp = now + 60_000,
            eventType = EventType.REPORT_CONFIRMED,
            payload = "Highway patrol unit confirms tree across lanes",
            version = 2
        )

        val reduced = ReportReducer.reduce(listOf(createEvent, confirmEventB, evidenceEventC, confirmEventD))

        assertNotNull("Report must be deterministically reduced from events", reduced)
        assertEquals(reportId, reduced?.id)
        assertEquals("Fallen tree", reduced?.title)
        assertEquals(ReportStatus.VERIFIED, reduced?.status)
        assertEquals(4, reduced?.independentObservationsCount)
        assertTrue("Confidence score should reflect multiple node observations and evidence", (reduced?.confidenceScore ?: 0) >= 80)
    }

    @Test
    fun `test incident resolution resets status across distributed nodes`() {
        val now = 1700000000000L
        val reportId = "rep-resolution-test"

        val createEvent = NodeSyncEvent(
            eventId = "evt-1",
            reportId = reportId,
            originNodeId = "node-alpha",
            authorNodeId = "node-alpha",
            timestamp = now,
            eventType = EventType.REPORT_CREATED,
            title = "Water main burst",
            version = 1
        )

        val resolveEvent = NodeSyncEvent(
            eventId = "evt-2",
            reportId = reportId,
            originNodeId = "node-alpha",
            authorNodeId = "node-delta",
            timestamp = now + 120_000,
            eventType = EventType.REPORT_RESOLVED,
            payload = "Municipal crew repaired pipe and cleared road",
            version = 3
        )

        val reduced = ReportReducer.reduce(listOf(createEvent, resolveEvent))

        assertNotNull(reduced)
        assertEquals(ReportStatus.RESOLVED, reduced?.status)
        assertEquals(ReportCondition.RESOLVED, reduced?.condition)
    }
}
