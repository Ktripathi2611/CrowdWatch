package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.IncidentReport
import com.example.data.model.ReportSeverity
import java.util.concurrent.ConcurrentHashMap

/**
 * IncidentNotificationManager:
 * Dedicated notification system that alerts users when a new high-priority or critical
 * civic incident report is generated within their immediate geographic vicinity.
 */
class IncidentNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Cache recently notified report IDs to prevent duplicate alert notifications
    private val notifiedReportIds = ConcurrentHashMap.newKeySet<String>()

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val highPriorityChannel = NotificationChannel(
                CHANNEL_ID_HIGH_PRIORITY,
                "High-Priority Civic Vicinity Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent notifications for critical and high-severity incidents within your local area"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 450, 200, 450)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(highPriorityChannel)
        }
    }

    /**
     * Checks if the app has permission to post notifications.
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Evaluates an incident report and triggers a heads-up push alert if the report is
     * high-priority and within the user's immediate geographic radius.
     *
     * @param report The incoming or newly created incident report
     * @param userLocation The user's current GPS location from LocationService
     * @param vicinityRadiusMeters Maximum distance in meters to consider "immediate vicinity" (default: 3000m)
     * @return true if an alert was triggered, false otherwise
     */
    fun evaluateAndNotify(
        report: IncidentReport,
        userLocation: TaggedLocation?,
        vicinityRadiusMeters: Double = 3000.0
    ): Boolean {
        // Only trigger for High or Critical severity incidents
        val isHighPriority = report.severity == ReportSeverity.HIGH || report.severity == ReportSeverity.CRITICAL
        if (!isHighPriority) return false

        // Deduplicate
        if (notifiedReportIds.contains(report.id)) return false

        // Compute distance to user's real-time coordinates
        val distanceMeters = if (userLocation != null) {
            LocationService.computeDistanceMeters(
                userLocation.latitude,
                userLocation.longitude,
                report.latitude,
                report.longitude
            )
        } else {
            report.distanceMeters
        }

        val isWithinVicinity = distanceMeters <= vicinityRadiusMeters

        if (isWithinVicinity) {
            triggerVicinityNotification(report, distanceMeters)
            notifiedReportIds.add(report.id)
            return true
        }

        return false
    }

    /**
     * Dispatches the system notification with heads-up display, custom vibration, and tap intent.
     */
    fun triggerVicinityNotification(report: IncidentReport, distanceMeters: Int) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REPORT_ID, report.id)
            putExtra(EXTRA_LATITUDE, report.latitude)
            putExtra(EXTRA_LONGITUDE, report.longitude)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            report.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val severityEmoji = if (report.severity == ReportSeverity.CRITICAL) "🚨 CRITICAL" else "⚠️ HIGH PRIORITY"
        val title = "$severityEmoji • ${report.category.displayName}"
        val contentText = "${report.title} • ${distanceMeters}m away (${report.areaName})"

        val bigText = buildString {
            appendLine(report.description.ifBlank { "${report.subcategory} reported in your immediate vicinity." })
            appendLine("📍 Location: ${report.areaName} (±${report.accuracyMeters.toInt()}m)")
            appendLine("⚡ Status: ${report.status.label} • Condition: ${report.condition.label}")
            append("🛡️ Confidence Score: ${report.confidenceScore}% (${report.verificationsCount} peer verifications)")
        }

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_HIGH_PRIORITY)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(defaultSound)
            .setVibrate(longArrayOf(0, 450, 200, 450))
            .setColor(if (report.severity == ReportSeverity.CRITICAL) Color.RED else Color.parseColor("#EA580C"))

        try {
            notificationManager.notify(report.id.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Handled when permission is revoked at runtime
        }
    }

    companion object {
        const val CHANNEL_ID_HIGH_PRIORITY = "channel_high_priority_incidents"
        const val EXTRA_REPORT_ID = "extra_report_id"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
    }
}
