package com.example.service

import android.util.Log
import com.example.data.model.IncidentReport
import com.example.data.model.MediaType
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * CrowdWatchFirebaseMessagingService:
 * Receives remote Firebase Cloud Messaging (FCM) push notifications when the device
 * is locked or the app process is placed into the background.
 * Instantly parses incoming community hazard payloads and posts high-priority vicinity alerts.
 */
class CrowdWatchFirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "CrowdWatchFCM"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(tag, "Refreshed FCM Device Push Token: $token")
        // Token can be registered with the civic relay when online
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i(tag, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            val reportId = data["reportId"] ?: "fcm-${System.currentTimeMillis()}"
            val title = data["title"] ?: remoteMessage.notification?.title ?: "Civic Hazard Alert"
            val description = data["description"] ?: remoteMessage.notification?.body ?: "Emergency alert reported in your vicinity."
            val categoryStr = data["category"] ?: "PUBLIC_SAFETY"
            val severityStr = data["severity"] ?: "HIGH"
            val lat = data["latitude"]?.toDoubleOrNull() ?: 0.0
            val lng = data["longitude"]?.toDoubleOrNull() ?: 0.0
            val areaName = data["areaName"] ?: "Nearby District"

            val category = try {
                ReportCategory.valueOf(categoryStr)
            } catch (_: Exception) {
                ReportCategory.SAFETY
            }

            val severity = try {
                ReportSeverity.valueOf(severityStr)
            } catch (_: Exception) {
                ReportSeverity.HIGH
            }

            val report = IncidentReport(
                id = reportId,
                title = title,
                description = description,
                category = category,
                subcategory = data["subcategory"] ?: "Civic Incident",
                latitude = lat,
                longitude = lng,
                accuracyMeters = 10f,
                areaName = areaName,
                distanceMeters = 250,
                severity = severity,
                status = ReportStatus.VERIFIED,
                condition = ReportCondition.HAPPENING_NOW,
                evidenceCount = 1,
                verificationsCount = 3,
                independentObservationsCount = 2,
                confidenceScore = 85,
                reporterNodeId = "fcm-gateway",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val notificationManager = IncidentNotificationManager(applicationContext)
            notificationManager.triggerVicinityNotification(report, report.distanceMeters)
        } else if (remoteMessage.notification != null) {
            val notif = remoteMessage.notification!!
            val fallbackReport = IncidentReport(
                id = "fcm-${System.currentTimeMillis()}",
                title = notif.title ?: "Urgent Community Alert",
                description = notif.body ?: "High priority hazard reported nearby.",
                category = ReportCategory.SAFETY,
                subcategory = "Civic Alert",
                latitude = 0.0,
                longitude = 0.0,
                accuracyMeters = 10f,
                areaName = "Local Vicinity",
                distanceMeters = 300,
                severity = ReportSeverity.HIGH,
                status = ReportStatus.VERIFIED,
                condition = ReportCondition.HAPPENING_NOW,
                evidenceCount = 0,
                verificationsCount = 1,
                independentObservationsCount = 1,
                confidenceScore = 80,
                reporterNodeId = "fcm-gateway",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val notificationManager = IncidentNotificationManager(applicationContext)
            notificationManager.triggerVicinityNotification(fallbackReport, fallbackReport.distanceMeters)
        }
    }
}
