package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncidentReport
import com.example.data.model.ReportCategory
import com.example.data.model.ReportSeverity
import com.example.ui.components.ConfidencePill
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.theme.DisruptionYellow
import com.example.ui.theme.DisruptionYellowBg
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.ResolvedGreen
import com.example.ui.theme.ResolvedGreenBg

data class NavigationManeuver(
    val title: String,
    val detail: String,
    val distanceMeters: Int,
    val icon: ImageVector,
    val isHazardWarning: Boolean = false,
    val speedLimitKmh: Int = 40
)

@Composable
fun RouteIntelligenceScreen(
    fromLocation: String,
    toLocation: String,
    onLocationsChange: (String, String) -> Unit,
    reports: List<IncidentReport>,
    onReportClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fromText by remember { mutableStateOf(fromLocation.ifBlank { "Current Location" }) }
    var toText by remember { mutableStateOf(toLocation.ifBlank { "Civic Hub / Metro Station" }) }
    var isNavigating by remember { mutableStateOf(false) }
    var currentManeuverIndex by remember { mutableIntStateOf(0) }

    val routeHazards = remember(reports, fromText, toText) {
        reports.filter {
            it.category == ReportCategory.TRAFFIC_ROADS ||
            it.category == ReportCategory.WEATHER_ENV ||
            it.severity == ReportSeverity.HIGH ||
            it.severity == ReportSeverity.CRITICAL
        }
    }

    val hasHighDisruption = routeHazards.any { it.severity == ReportSeverity.HIGH || it.severity == ReportSeverity.CRITICAL }

    // Dynamic Turn-by-Turn Maneuvers along the sector
    val maneuvers = remember(fromText, toText, hasHighDisruption, routeHazards) {
        val list = mutableListOf<NavigationManeuver>()
        list.add(
            NavigationManeuver(
                title = "Head North on ${fromText.ifBlank { "Sector Way" }}",
                detail = "Continue straight for 350 meters towards intersection",
                distanceMeters = 350,
                icon = Icons.Default.Navigation,
                speedLimitKmh = 40
            )
        )
        list.add(
            NavigationManeuver(
                title = "In 250m, Turn Right onto Link Corridor",
                detail = "Keep right at the junction towards Central Expressway",
                distanceMeters = 250,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                speedLimitKmh = 50
            )
        )
        if (hasHighDisruption && routeHazards.isNotEmpty()) {
            val hazard = routeHazards.first()
            list.add(
                NavigationManeuver(
                    title = "⚠️ Alert: ${hazard.title}",
                    detail = "Community reported obstacle 180m ahead in ${hazard.areaName}. Slow to 20 km/h.",
                    distanceMeters = 180,
                    icon = Icons.Default.Warning,
                    isHazardWarning = true,
                    speedLimitKmh = 20
                )
            )
        }
        list.add(
            NavigationManeuver(
                title = "Continue 1.1 km on Expressway Vector",
                detail = "Follow transit corridor lane 2",
                distanceMeters = 1100,
                icon = Icons.Default.Navigation,
                speedLimitKmh = 60
            )
        )
        list.add(
            NavigationManeuver(
                title = "Turn Left onto Civic Avenue",
                detail = "Prepare to reach destination at ${toText.ifBlank { "Destination" }}",
                distanceMeters = 200,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                speedLimitKmh = 30
            )
        )
        list.add(
            NavigationManeuver(
                title = "Arrive at ${toText.ifBlank { "Destination" }}",
                detail = "Destination is on your right",
                distanceMeters = 50,
                icon = Icons.Default.CheckCircle,
                speedLimitKmh = 20
            )
        )
        list
    }

    val currentManeuver = maneuvers.getOrElse(currentManeuverIndex) { maneuvers.first() }

    fun launchExternalMaps() {
        val destination = toText.ifBlank { "Destination" }
        val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(destination)}"))
            try {
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {}
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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CrowdWatchColors.TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isNavigating) "Turn-by-Turn GPS Guidance" else "Route Intelligence",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        color = CrowdWatchColors.TextPrimary
                    )
                    Text(
                        text = if (isNavigating) "Live vector telemetry active" else "Real-time community hazards along transit corridors",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = CrowdWatchColors.TextSecondary
                    )
                }
                if (isNavigating) {
                    IconButton(onClick = { isNavigating = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Navigation", tint = EmergencyRed)
                    }
                }
            }
        }

        if (isNavigating) {
            // ==================== LIVE TURN-BY-TURN HUD & VECTOR CANVAS ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
            ) {
                // High Contrast Turn-by-Turn Green HUD Header
                Surface(
                    color = if (currentManeuver.isHazardWarning) EmergencyRed else Color(0xFF065F46),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = currentManeuver.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${currentManeuver.distanceMeters} m",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                            Text(
                                text = currentManeuver.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentManeuver.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Vector Route Polyline Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Grid road vectors
                        val gridPaint = Color.White.copy(alpha = 0.05f)
                        for (y in 0..canvasHeight.toInt() step 80) {
                            drawLine(
                                color = gridPaint,
                                start = Offset(0f, y.toFloat()),
                                end = Offset(canvasWidth, y.toFloat()),
                                strokeWidth = 1f
                            )
                        }

                        // Transit Corridor Polyline Path
                        val path = Path().apply {
                            moveTo(canvasWidth * 0.5f, canvasHeight * 0.85f)
                            lineTo(canvasWidth * 0.5f, canvasHeight * 0.60f)
                            lineTo(canvasWidth * 0.72f, canvasHeight * 0.45f)
                            lineTo(canvasWidth * 0.35f, canvasHeight * 0.25f)
                            lineTo(canvasWidth * 0.5f, canvasHeight * 0.12f)
                        }

                        // Outer Glow
                        drawPath(
                            path = path,
                            color = Color(0xFF10B981).copy(alpha = 0.3f),
                            style = Stroke(width = 24f)
                        )

                        // Main Route Line
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF34D399), Color(0xFF059669))
                            ),
                            style = Stroke(
                                width = 12f,
                                pathEffect = PathEffect.cornerPathEffect(30f)
                            )
                        )

                        // Maneuver Waypoints
                        val waypoints = listOf(
                            Offset(canvasWidth * 0.5f, canvasHeight * 0.85f),
                            Offset(canvasWidth * 0.5f, canvasHeight * 0.60f),
                            Offset(canvasWidth * 0.72f, canvasHeight * 0.45f),
                            Offset(canvasWidth * 0.35f, canvasHeight * 0.25f),
                            Offset(canvasWidth * 0.5f, canvasHeight * 0.12f)
                        )

                        waypoints.forEachIndexed { index, pt ->
                            val isPassed = index < currentManeuverIndex
                            val isCurrent = index == currentManeuverIndex
                            drawCircle(
                                color = if (isCurrent) Color.White else if (isPassed) Color(0xFF10B981) else Color(0xFF64748B),
                                radius = if (isCurrent) 10f else 6f,
                                center = pt
                            )
                            if (isCurrent) {
                                drawCircle(
                                    color = Color(0xFF10B981).copy(alpha = 0.4f),
                                    radius = 20f,
                                    center = pt
                                )
                            }
                        }

                        // Current Vehicle Cursor
                        val currentPt = waypoints.getOrElse(currentManeuverIndex) { waypoints.first() }
                        drawCircle(
                            color = Color(0xFF38BDF8),
                            radius = 12f,
                            center = currentPt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = currentPt
                        )

                        // Hazard Marker if disruption present
                        if (hasHighDisruption) {
                            val hazardPt = Offset(canvasWidth * 0.72f, canvasHeight * 0.45f)
                            drawCircle(
                                color = EmergencyRed,
                                radius = 14f,
                                center = hazardPt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4f,
                                center = hazardPt
                            )
                        }
                    }

                    // Floating GPS Speed & Telemetry Pill
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${currentManeuver.speedLimitKmh}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                            Text(
                                text = "KM/H",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Bottom Navigation Control Strip
                Surface(
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Step ${currentManeuverIndex + 1} of ${maneuvers.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "ETA: ~${maneuvers.drop(currentManeuverIndex).sumOf { it.distanceMeters } / 300 + 2} mins",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        if (currentManeuverIndex > 0) currentManeuverIndex--
                                    },
                                    enabled = currentManeuverIndex > 0,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Back", color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        if (currentManeuverIndex < maneuvers.lastIndex) {
                                            currentManeuverIndex++
                                        } else {
                                            isNavigating = false
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text(if (currentManeuverIndex < maneuvers.lastIndex) "Next Turn" else "Arrived", color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Voice Navigation / Google Maps External Intent Button
                        OutlinedButton(
                            onClick = { launchExternalMaps() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in Google Maps (Turn-by-Turn Voice)", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // ==================== ROUTE PLANNING & HAZARD ASSESSMENT ====================
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Origin & Destination Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = fromText,
                                        onValueChange = {
                                            fromText = it
                                            onLocationsChange(it, toText)
                                        },
                                        label = { Text("From (Origin)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        leadingIcon = {
                                            Icon(Icons.Default.LocationOn, contentDescription = "From", tint = CrowdWatchColors.PrimaryBlue)
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = toText,
                                        onValueChange = {
                                            toText = it
                                            onLocationsChange(fromText, it)
                                        },
                                        label = { Text("To (Destination)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        leadingIcon = {
                                            Icon(Icons.Default.Navigation, contentDescription = "To", tint = CrowdWatchColors.ConfidenceHigh)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        val temp = fromText
                                        fromText = toText
                                        toText = temp
                                        onLocationsChange(fromText, toText)
                                    }
                                ) {
                                    Icon(Icons.Default.SwapVert, contentDescription = "Swap", tint = CrowdWatchColors.PrimaryBlue)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons: Start Turn-by-Turn GPS & Open Maps
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        currentManeuverIndex = 0
                                        isNavigating = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ResolvedGreen),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(46.dp)
                                        .testTag("start_turn_by_turn_button")
                                ) {
                                    Icon(Icons.Default.NearMe, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Start Navigation", fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = { launchExternalMaps() },
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.PrimaryBlue),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("open_external_maps_button")
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, tint = CrowdWatchColors.PrimaryBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Google Maps", color = CrowdWatchColors.PrimaryBlue, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Route Assessment Summary Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasHighDisruption) CrowdWatchColors.SeverityCriticalBg else CrowdWatchColors.ConfidenceHighBg
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hasHighDisruption) CrowdWatchColors.SeverityCritical.copy(alpha = 0.3f) else CrowdWatchColors.ConfidenceHigh.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (hasHighDisruption) CrowdWatchColors.SeverityCritical.copy(alpha = 0.15f) else CrowdWatchColors.ConfidenceHigh.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasHighDisruption) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = "Status",
                                    tint = if (hasHighDisruption) CrowdWatchColors.SeverityCritical else CrowdWatchColors.ConfidenceHigh,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (hasHighDisruption) "High-Impact Sector Disruption" else "Clear Transit Corridor",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (hasHighDisruption) CrowdWatchColors.SeverityCritical else CrowdWatchColors.ConfidenceHighText
                                )
                                Text(
                                    text = if (hasHighDisruption)
                                        "${routeHazards.size} hazards reported along path. Turn-by-turn navigation alerts will warn at each waypoint."
                                    else
                                        "No major bottlenecks or obstructions reported by community nodes along this vector.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasHighDisruption) CrowdWatchColors.ConfidenceLowText else CrowdWatchColors.ConfidenceHighText
                                )
                            }
                        }
                    }
                }

                // List of Affecting Incidents
                item {
                    Text(
                        text = "Incidents along this sector (${routeHazards.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CrowdWatchColors.TextPrimary
                    )
                }

                items(routeHazards) { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CrowdWatchColors.TextPrimary
                                )
                                SeverityBadge(severity = report.severity)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${report.areaName} • ${report.distanceMeters}m off route",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = CrowdWatchColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${report.verificationsCount} node confirmations",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrowdWatchColors.TextSecondary
                                )
                                ConfidencePill(score = report.confidenceScore)
                            }
                        }
                    }
                }
            }
        }
    }
}
