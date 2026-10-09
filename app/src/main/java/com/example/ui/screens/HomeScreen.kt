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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncidentReport
import com.example.data.model.ReportCategory
import com.example.ui.components.CrowdWatchTopBar
import com.example.ui.components.IncidentSearchAndFilterBar
import com.example.ui.components.ReportCard
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.ProximityFilter
import com.example.ui.viewmodel.VerificationStatusFilter

@Composable
fun HomeScreen(
    reports: List<IncidentReport>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: ReportCategory?,
    onCategorySelected: (ReportCategory?) -> Unit,
    proximityFilter: ProximityFilter = ProximityFilter.ANY,
    onProximitySelected: (ProximityFilter) -> Unit = {},
    verificationFilter: VerificationStatusFilter = VerificationStatusFilter.ALL,
    onVerificationSelected: (VerificationStatusFilter) -> Unit = {},
    onResetFilters: () -> Unit = {},
    onReportClick: (String) -> Unit,
    onNavigateTab: (NavigationTab) -> Unit,
    onAlertsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNetworkClick: () -> Unit,
    activePeerCount: Int = 0,
    isNearbyActive: Boolean = false,
    areaName: String = "Local Vicinity",
    hasUnreadAlerts: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = selectedCategory != null ||
            proximityFilter != ProximityFilter.ANY ||
            verificationFilter != VerificationStatusFilter.ALL ||
            searchQuery.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrowdWatchColors.Background)
    ) {
        // Top Bar
        CrowdWatchTopBar(
            areaName = areaName,
            peerCount = activePeerCount,
            isNearbyActive = isNearbyActive,
            hasUnreadAlerts = hasUnreadAlerts,
            onAlertsClick = onAlertsClick,
            onProfileClick = onProfileClick,
            onNetworkClick = onNetworkClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Dedicated Multi-Criteria Search & Filter Component
            item {
                IncidentSearchAndFilterBar(
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                    selectedProximity = proximityFilter,
                    onProximitySelected = onProximitySelected,
                    selectedVerification = verificationFilter,
                    onVerificationSelected = onVerificationSelected,
                    onResetFilters = onResetFilters,
                    totalMatchingCount = reports.size
                )
            }

            // Quick Tools Carousel: Route Intelligence, CrowdWatch AI, Network Simulator
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickToolPill(
                        title = "Route Intel",
                        icon = Icons.Default.Route,
                        color = CrowdWatchColors.Transport,
                        onClick = { onNavigateTab(NavigationTab.ROUTE_INTELLIGENCE) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickToolPill(
                        title = "Ask AI",
                        icon = Icons.Default.AutoAwesome,
                        color = CrowdWatchColors.Infrastructure,
                        onClick = { onNavigateTab(NavigationTab.AI_ASSISTANT) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickToolPill(
                        title = "P2P Mesh",
                        icon = Icons.Default.Hub,
                        color = CrowdWatchColors.MeshActive,
                        onClick = { onNavigateTab(NavigationTab.NETWORK_SIMULATOR) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section Subheader
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (reports.isNotEmpty()) "${reports.size} reports nearby • Real-time mesh active" else "No reports nearby • Mesh monitoring active",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = CrowdWatchColors.TextSecondary
                    )
                }
            }

            // Feed Items Grouped by Distance
            if (reports.isEmpty()) {
                item {
                    EmptyReportsView(
                        hasActiveFilters = hasActiveFilters,
                        onResetFilters = onResetFilters
                    )
                }
            } else {
                val within500m = reports.filter { it.distanceMeters <= 500 }
                val beyond500m = reports.filter { it.distanceMeters > 500 }

                if (within500m.isNotEmpty()) {
                    item {
                        Text(
                            text = "Within 500 m",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = CrowdWatchColors.TextPrimary,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
                        )
                    }
                    items(within500m, key = { it.id }) { report ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            ReportCard(
                                report = report,
                                onClick = { onReportClick(report.id) }
                            )
                        }
                    }
                }

                if (beyond500m.isNotEmpty()) {
                    item {
                        Text(
                            text = "Within 2 km",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = CrowdWatchColors.TextPrimary,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
                        )
                    }
                    items(beyond500m, key = { it.id }) { report ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            ReportCard(
                                report = report,
                                onClick = { onReportClick(report.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickToolPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(12.dp)),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = color
            )
        }
    }
}

@Composable
fun EmptyReportsView(
    hasActiveFilters: Boolean = false,
    onResetFilters: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (hasActiveFilters) CrowdWatchColors.PrimaryBlueBg else CrowdWatchColors.ConfidenceHighBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasActiveFilters) Icons.Default.Search else Icons.Default.Verified,
                    contentDescription = if (hasActiveFilters) "No Filter Matches" else "Clear",
                    tint = if (hasActiveFilters) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.ConfidenceHigh,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (hasActiveFilters) "No Matching Incidents" else "You're all clear!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CrowdWatchColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasActiveFilters)
                    "No reports match your active proximity, category, or verification filter settings."
                else
                    "No significant reports nearby. You will receive peer-to-peer mesh alerts if any hazard occurs.",
                style = MaterialTheme.typography.bodySmall,
                color = CrowdWatchColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (hasActiveFilters) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrowdWatchColors.PrimaryBlue),
                    modifier = Modifier.testTag("empty_state_reset_filters")
                ) {
                    Text(
                        text = "Reset All Filters",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
