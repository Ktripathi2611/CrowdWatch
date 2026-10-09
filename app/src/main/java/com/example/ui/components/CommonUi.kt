package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncidentReport
import com.example.data.model.ReportCategory
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus
import com.example.engine.FreshnessEngine
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.theme.CardBgDark
import com.example.ui.theme.CardBgLight
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DisruptionYellow
import com.example.ui.theme.DisruptionYellowBg
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBg
import com.example.ui.theme.HazardOrange
import com.example.ui.theme.HazardOrangeBg
import com.example.ui.theme.InfrastructurePurple
import com.example.ui.theme.InfrastructurePurpleBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.ResolvedGreen
import com.example.ui.theme.ResolvedGreenBg
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TransportBlue
import com.example.ui.theme.TransportBlueBg
import com.example.ui.viewmodel.NavigationTab

import androidx.compose.material.icons.filled.ArrowDropDown
import com.example.ui.components.SceneThumbnail

import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

@Composable
fun CrowdWatchTopBar(
    areaName: String = "Local Vicinity",
    peerCount: Int = 0,
    isNearbyActive: Boolean = false,
    hasUnreadAlerts: Boolean = false,
    onAlertsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNetworkClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location dropdown: 📍 Local Vicinity ▾ / Real-Time Mesh
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { }
                    .padding(vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = areaName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Real-Time Mesh",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 22.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Network badge pill: 🟢 Connected · 8 peers
                val meta = when {
                    isNearbyActive && peerCount > 0 -> NetworkStatusMeta(
                        CrowdWatchColors.ConfidenceHighBg,
                        Color(0xFFA7F3D0),
                        CrowdWatchColors.NetworkOnline,
                        CrowdWatchColors.ConfidenceHighText,
                        "Connected · $peerCount peers"
                    )
                    isNearbyActive -> NetworkStatusMeta(
                        CrowdWatchColors.ConfidenceMediumBg,
                        Color(0xFFFDE68A),
                        CrowdWatchColors.NetworkConnecting,
                        CrowdWatchColors.ConfidenceMediumText,
                        "Connecting..."
                    )
                    else -> NetworkStatusMeta(
                        CrowdWatchColors.SurfaceSecondary,
                        CrowdWatchColors.Border,
                        CrowdWatchColors.NetworkOffline,
                        CrowdWatchColors.TextSecondary,
                        "Offline · Local mode"
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = meta.bg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, meta.border),
                    modifier = Modifier
                        .clickable(onClick = onNetworkClick)
                        .testTag("network_status_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(meta.dot)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = meta.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = meta.text
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar Icon button
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("profile_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class NetworkStatusMeta(
    val bg: Color,
    val border: Color,
    val dot: Color,
    val text: Color,
    val label: String
)

@Composable
fun CrowdWatchBottomNav(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    onReportClick: () -> Unit,
    unreadAlertsCount: Int = 3
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CrowdWatchColors.Surface,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Tab
            BottomNavItem(
                icon = Icons.Outlined.Home,
                label = "Live",
                isSelected = currentTab == NavigationTab.LIVE,
                onClick = { onTabSelected(NavigationTab.LIVE) },
                testTag = "nav_live"
            )

            // Map Tab
            BottomNavItem(
                icon = Icons.Outlined.Map,
                label = "Map",
                isSelected = currentTab == NavigationTab.MAP,
                onClick = { onTabSelected(NavigationTab.MAP) },
                testTag = "nav_map"
            )

            // Center Prominent Floating Add (+) Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CrowdWatchColors.PrimaryBlue)
                    .clickable(onClick = onReportClick)
                    .testTag("nav_report"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Report",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Alerts Tab
            BottomNavItem(
                icon = Icons.Outlined.Notifications,
                label = "Alerts",
                isSelected = currentTab == NavigationTab.ALERTS,
                badgeCount = unreadAlertsCount,
                onClick = { onTabSelected(NavigationTab.ALERTS) },
                testTag = "nav_alerts"
            )

            // Profile Tab
            BottomNavItem(
                icon = Icons.Outlined.Person,
                label = "Profile",
                isSelected = currentTab == NavigationTab.PROFILE,
                onClick = { onTabSelected(NavigationTab.PROFILE) },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    testTag: String
) {
    val contentColor = if (isSelected) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.TextTertiary
    val iconBgColor = if (isSelected) CrowdWatchColors.PrimaryBlueBg else Color.Transparent

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(CrowdWatchColors.Emergency),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$badgeCount",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
fun ReportCard(
    report: IncidentReport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(report.category)
    val categoryBg = getCategoryBgColor(report.category)
    val timeAgo = FreshnessEngine.formatTimeAgo(report.createdAt)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("report_card_${report.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                // Top row: Category Pill + Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryBg,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, categoryColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(report.category),
                                contentDescription = report.category.displayName,
                                tint = categoryColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = report.category.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = categoryColor
                            )
                        }
                    }

                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = CrowdWatchColors.TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = CrowdWatchColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Location line with chevron
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.areaName,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = CrowdWatchColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${report.distanceMeters} m away",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = CrowdWatchColors.TextTertiary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View",
                        tint = CrowdWatchColors.Border,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: Confidence Pill + Observers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Confidence Pill with exact 3 states
                    val (confBg, confText, confLabel) = when {
                        report.confidenceScore >= 80 -> Triple(
                            CrowdWatchColors.ConfidenceHighBg,
                            CrowdWatchColors.ConfidenceHighText,
                            "${report.confidenceScore}% High confidence"
                        )
                        report.confidenceScore >= 50 -> Triple(
                            CrowdWatchColors.ConfidenceMediumBg,
                            CrowdWatchColors.ConfidenceMediumText,
                            "${report.confidenceScore}% Developing"
                        )
                        else -> Triple(
                            CrowdWatchColors.ConfidenceLowBg,
                            CrowdWatchColors.ConfidenceLowText,
                            "${report.confidenceScore}% Unverified"
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = confBg
                    ) {
                        Text(
                            text = confLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = confText,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${report.verificationsCount} observers",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = CrowdWatchColors.TextTertiary
                    )
                }
            }

            // Right Scene Thumbnail with 3 photos tag
            SceneThumbnail(
                sampleType = report.evidenceSampleType,
                size = 72.dp,
                shapeRadius = 12.dp,
                badgeText = "${maxOf(2, (report.verificationsCount % 3) + 2)} photos"
            )
        }
    }
}

@Composable
fun EvidenceVisualBanner(
    sampleType: String,
    evidenceCount: Int,
    evidenceType: String,
    modifier: Modifier = Modifier
) {
    val gradientColors = when (sampleType) {
        "fallen_tree" -> listOf(Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF33691E))
        "waterlogging" -> listOf(Color(0xFF0277BD), Color(0xFF01579B), Color(0xFF006064))
        "traffic" -> listOf(Color(0xFFE65100), Color(0xFFBF360C), Color(0xFFD84315))
        "medical" -> listOf(Color(0xFFC62828), Color(0xFFB71C1C), Color(0xFF880E4F))
        "streetlight" -> listOf(Color(0xFF4A148C), Color(0xFF311B92), Color(0xFF1A237E))
        else -> listOf(PrimaryBlue, Color(0xFF1E3A8A))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        SceneThumbnail(sampleType = sampleType, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.5f))
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.45f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Evidence",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$evidenceCount $evidenceType • Node verified",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.45f)
            ) {
                Text(
                    text = "GPS Encrypted",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Subtitle caption
        Text(
            text = when (sampleType) {
                "fallen_tree" -> "📷 Road blocked across both lanes"
                "waterlogging" -> "📷 1.5 ft surface water on cross road"
                "traffic" -> "📷 Stalled vehicles queue"
                "medical" -> "📷 Emergency assistance on site"
                else -> "📷 Verified community evidence"
            },
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Color.White
        )
        }
    }
}

@Composable
fun SeverityBadge(severity: ReportSeverity) {
    val (bgColor, textColor, label) = when (severity) {
        ReportSeverity.CRITICAL -> Triple(CrowdWatchColors.SeverityCriticalBg, CrowdWatchColors.SeverityCritical, "Critical")
        ReportSeverity.HIGH -> Triple(CrowdWatchColors.SeverityHighBg, CrowdWatchColors.SeverityHigh, "High")
        ReportSeverity.MEDIUM -> Triple(CrowdWatchColors.SeverityMediumBg, CrowdWatchColors.SeverityMedium, "Medium")
        ReportSeverity.LOW -> Triple(CrowdWatchColors.SeverityLowBg, CrowdWatchColors.SeverityLow, "Low")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ConfidencePill(score: Int) {
    val (bgColor, textColor, dotColor, label) = when {
        score >= 80 -> Quadruple(
            CrowdWatchColors.ConfidenceHighBg,
            CrowdWatchColors.ConfidenceHighText,
            CrowdWatchColors.ConfidenceHigh,
            "$score% High"
        )
        score >= 50 -> Quadruple(
            CrowdWatchColors.ConfidenceMediumBg,
            CrowdWatchColors.ConfidenceMediumText,
            CrowdWatchColors.ConfidenceMedium,
            "$score% Developing"
        )
        else -> Quadruple(
            CrowdWatchColors.ConfidenceLowBg,
            CrowdWatchColors.ConfidenceLowText,
            CrowdWatchColors.ConfidenceLow,
            "$score% Unverified"
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

fun getCategoryIcon(category: ReportCategory): ImageVector = when (category) {
    ReportCategory.SAFETY -> Icons.Default.Warning
    ReportCategory.TRAFFIC_ROADS -> Icons.Default.Traffic
    ReportCategory.WEATHER_ENV -> Icons.Default.WaterDamage
    ReportCategory.INFRASTRUCTURE -> Icons.Default.Hub
    ReportCategory.CROWD_ACTIVITY -> Icons.Default.Person
    ReportCategory.PUBLIC_TRANSPORT -> Icons.Default.Route
    ReportCategory.LOCAL_SERVICES -> Icons.Default.Info
    ReportCategory.MEDICAL -> Icons.Default.Emergency
    ReportCategory.UTILITIES -> Icons.Default.Refresh
    ReportCategory.OTHER -> Icons.Default.Info
}

fun getCategoryColor(category: ReportCategory): Color = when (category) {
    ReportCategory.SAFETY -> CrowdWatchColors.Emergency
    ReportCategory.MEDICAL -> CrowdWatchColors.Emergency
    ReportCategory.TRAFFIC_ROADS -> CrowdWatchColors.Traffic
    ReportCategory.WEATHER_ENV -> CrowdWatchColors.Weather
    ReportCategory.INFRASTRUCTURE -> CrowdWatchColors.Infrastructure
    ReportCategory.CROWD_ACTIVITY -> CrowdWatchColors.Crowd
    ReportCategory.PUBLIC_TRANSPORT -> CrowdWatchColors.Transport
    ReportCategory.LOCAL_SERVICES -> CrowdWatchColors.Services
    ReportCategory.UTILITIES -> CrowdWatchColors.Hazard
    ReportCategory.OTHER -> CrowdWatchColors.TextTertiary
}

fun getCategoryBgColor(category: ReportCategory): Color = when (category) {
    ReportCategory.SAFETY -> CrowdWatchColors.EmergencyBg
    ReportCategory.MEDICAL -> CrowdWatchColors.EmergencyBg
    ReportCategory.TRAFFIC_ROADS -> CrowdWatchColors.TrafficBg
    ReportCategory.WEATHER_ENV -> CrowdWatchColors.WeatherBg
    ReportCategory.INFRASTRUCTURE -> CrowdWatchColors.InfrastructureBg
    ReportCategory.CROWD_ACTIVITY -> CrowdWatchColors.CrowdBg
    ReportCategory.PUBLIC_TRANSPORT -> CrowdWatchColors.TransportBg
    ReportCategory.LOCAL_SERVICES -> CrowdWatchColors.ServicesBg
    ReportCategory.UTILITIES -> CrowdWatchColors.HazardBg
    ReportCategory.OTHER -> CrowdWatchColors.SurfaceSecondary
}
