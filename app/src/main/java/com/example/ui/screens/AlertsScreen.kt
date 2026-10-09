package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertItem
import com.example.data.model.AlertType
import com.example.data.model.ReportSeverity
import com.example.engine.FreshnessEngine
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PrimaryBlue

enum class AlertFilterTab {
    CRITICAL,
    NEARBY,
    UPDATES
}

@Composable
fun AlertsScreen(
    alerts: List<AlertItem>,
    onAlertClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<AlertFilterTab?>(null) }

    val criticalCount = remember(alerts) { alerts.count { it.severity == ReportSeverity.CRITICAL || it.type == AlertType.CRITICAL } }
    val nearbyCount = remember(alerts) { alerts.count { it.distanceMeters <= 1000 } }
    val updatesCount = remember(alerts) { alerts.count { it.type == AlertType.UPDATES } }

    val filteredAlerts = remember(alerts, selectedFilter) {
        when (selectedFilter) {
            null -> alerts
            AlertFilterTab.CRITICAL -> alerts.filter { it.severity == ReportSeverity.CRITICAL || it.type == AlertType.CRITICAL }
            AlertFilterTab.NEARBY -> alerts.filter { it.distanceMeters <= 1000 }
            AlertFilterTab.UPDATES -> alerts.filter { it.type == AlertType.UPDATES }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrowdWatchColors.Background)
    ) {
        // Top App Bar
        Surface(
            color = CrowdWatchColors.Surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Go back */ }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CrowdWatchColors.TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alerts",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = CrowdWatchColors.TextPrimary
                    )
                }

                // Notification Bell with Badge
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = CrowdWatchColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    // Red indicator dot if unread alerts exist
                    if (alerts.any { !it.isRead }) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(CrowdWatchColors.Emergency)
                        )
                    }
                }
            }
        }

        // Horizontal Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Critical Chip
            AlertFilterPill(
                label = "Critical",
                count = criticalCount,
                isSelected = selectedFilter == AlertFilterTab.CRITICAL,
                activeBg = CrowdWatchColors.Emergency,
                activeText = Color.White,
                defaultBg = CrowdWatchColors.EmergencyBg,
                defaultText = CrowdWatchColors.Emergency,
                onClick = {
                    selectedFilter = if (selectedFilter == AlertFilterTab.CRITICAL) null else AlertFilterTab.CRITICAL
                }
            )

            // Nearby Chip
            AlertFilterPill(
                label = "Nearby",
                count = nearbyCount,
                isSelected = selectedFilter == AlertFilterTab.NEARBY,
                activeBg = CrowdWatchColors.PrimaryBlue,
                activeText = Color.White,
                defaultBg = CrowdWatchColors.PrimaryBlueBg,
                defaultText = CrowdWatchColors.PrimaryBlue,
                onClick = {
                    selectedFilter = if (selectedFilter == AlertFilterTab.NEARBY) null else AlertFilterTab.NEARBY
                }
            )

            // Updates Chip
            AlertFilterPill(
                label = "Updates",
                count = updatesCount,
                isSelected = selectedFilter == AlertFilterTab.UPDATES,
                activeBg = CrowdWatchColors.TextSecondary,
                activeText = Color.White,
                defaultBg = CrowdWatchColors.SurfaceSecondary,
                defaultText = CrowdWatchColors.TextSecondary,
                onClick = {
                    selectedFilter = if (selectedFilter == AlertFilterTab.UPDATES) null else AlertFilterTab.UPDATES
                }
            )
        }

        // Alert Items List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredAlerts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = CrowdWatchColors.TextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No active alerts in this filter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrowdWatchColors.TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredAlerts, key = { it.id }) { alert ->
                    AlertItemCard(
                        alert = alert,
                        onClick = { onAlertClick(alert.reportId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertFilterPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    activeBg: Color,
    activeText: Color,
    defaultBg: Color,
    defaultText: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) activeBg else defaultBg,
        modifier = Modifier.height(34.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) activeText else defaultText
            )
            Text(
                text = "$count",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) activeText else defaultText
            )
        }
    }
}

@Composable
fun AlertItemCard(
    alert: AlertItem,
    onClick: () -> Unit
) {
    val timeAgo = FreshnessEngine.formatTimeAgo(alert.timestamp)

    // Determine visual styling from alert title or severity
    val (icon, iconBg, iconColor, badgeText, badgeBg, badgeTextColor) = when {
        alert.title.contains("Medical", ignoreCase = true) || alert.severity == ReportSeverity.CRITICAL -> {
            AlertVisualMeta(
                icon = Icons.Default.LocalHospital,
                iconBg = CrowdWatchColors.SeverityCriticalBg,
                iconColor = CrowdWatchColors.SeverityCritical,
                badgeText = "Critical",
                badgeBg = CrowdWatchColors.SeverityCriticalBg,
                badgeTextColor = CrowdWatchColors.SeverityCritical
            )
        }
        alert.title.contains("Traffic", ignoreCase = true) || alert.severity == ReportSeverity.HIGH -> {
            AlertVisualMeta(
                icon = Icons.Default.Traffic,
                iconBg = CrowdWatchColors.SeverityHighBg,
                iconColor = CrowdWatchColors.SeverityHigh,
                badgeText = "High",
                badgeBg = CrowdWatchColors.SeverityHighBg,
                badgeTextColor = CrowdWatchColors.SeverityHigh
            )
        }
        alert.title.contains("Water", ignoreCase = true) || alert.title.contains("Flood", ignoreCase = true) -> {
            AlertVisualMeta(
                icon = Icons.Default.WaterDrop,
                iconBg = CrowdWatchColors.SeverityMediumBg,
                iconColor = CrowdWatchColors.SeverityMedium,
                badgeText = "Medium",
                badgeBg = CrowdWatchColors.SeverityMediumBg,
                badgeTextColor = CrowdWatchColors.SeverityMedium
            )
        }
        else -> {
            AlertVisualMeta(
                icon = Icons.Default.DirectionsBus,
                iconBg = CrowdWatchColors.TransportBg,
                iconColor = CrowdWatchColors.Transport,
                badgeText = "Low",
                badgeBg = CrowdWatchColors.TransportBg,
                badgeTextColor = CrowdWatchColors.Transport
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("alert_item_${alert.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circular icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = alert.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = CrowdWatchColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${alert.distanceMeters} m • ${alert.confidenceScore}% confidence",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = CrowdWatchColors.TextSecondary
                    )
                }
            }

            // Right column: Badge & Timestamp
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = timeAgo,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

private data class AlertVisualMeta(
    val icon: ImageVector,
    val iconBg: Color,
    val iconColor: Color,
    val badgeText: String,
    val badgeBg: Color,
    val badgeTextColor: Color
)
