package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncidentReport
import com.example.data.model.ReportCategory
import com.example.data.model.ReportSeverity
import com.example.location.TaggedLocation
import com.example.service.LocationService
import com.example.ui.components.ConfidencePill
import com.example.ui.components.SceneThumbnail
import com.example.ui.components.SeverityBadge
import com.example.ui.components.getCategoryBgColor
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CardBgLight
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.ResolvedGreen
import com.example.ui.viewmodel.MapViewMode
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    reports: List<IncidentReport>,
    selectedReportId: String?,
    onSelectReport: (String?) -> Unit,
    onViewReportDetails: (String) -> Unit,
    onConfirmReport: (String) -> Unit,
    mapViewMode: MapViewMode,
    onMapViewModeChange: (MapViewMode) -> Unit,
    selectedCategory: ReportCategory?,
    onCategorySelected: (ReportCategory?) -> Unit,
    userLocation: TaggedLocation? = null,
    onRefreshLocation: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationService = remember { LocationService(context) }
    var currentDeviceLocation by remember { mutableStateOf(userLocation) }

    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            currentDeviceLocation = userLocation
        } else {
            val loc = locationService.getCurrentLocation() ?: locationService.getLastKnownLocation()
            if (loc != null) {
                currentDeviceLocation = loc
            }
        }
    }

    val baseLat = currentDeviceLocation?.latitude ?: 19.2961
    val baseLng = currentDeviceLocation?.longitude ?: 72.8604
    val baseAccuracy = currentDeviceLocation?.accuracyMeters ?: 8f

    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing radar animation for real-time GPS location marker
    val infiniteTransition = rememberInfiniteTransition(label = "gps_radar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    // Filter reports by category if selected
    val displayReports = remember(reports, selectedCategory) {
        if (selectedCategory == null) reports else reports.filter { it.category == selectedCategory }
    }

    val activeReport = reports.find { it.id == selectedReportId }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFE2E8F0))) {
        // Interactive Custom Canvas Map
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("map_canvas")
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.0f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
                .pointerInput(displayReports, zoomScale, panOffsetX, panOffsetY, baseLat, baseLng) {
                    detectTapGestures { tapOffset ->
                        // Map marker click hit-test
                        val center = Offset(size.width / 2f + panOffsetX, size.height / 2f + panOffsetY)
                        var clickedReport: IncidentReport? = null

                        for (rep in displayReports) {
                            val markerX = center.x + ((rep.longitude - baseLng) * 14000f * zoomScale).toFloat()
                            val markerY = center.y - ((rep.latitude - baseLat) * 14000f * zoomScale).toFloat()
                            val dist = (tapOffset - Offset(markerX, markerY)).getDistance()
                            if (dist < 34.dp.toPx()) {
                                clickedReport = rep
                                break
                            }
                        }
                        onSelectReport(clickedReport?.id)
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val center = Offset(canvasW / 2f + panOffsetX, canvasH / 2f + panOffsetY)

            // 1. Draw Map Background & Grid
            drawRect(color = Color(0xFFF1F5F9))

            // Draw City Blocks & Secondary Streets
            val streetColor = Color(0xFFE2E8F0)
            val mainRoadColor = Color(0xFFCBD5E1)
            val highwayColor = Color(0xFFFFEDD5)
            val riverColor = Color(0xFFBAE6FD)

            // Simulated river / canal
            val riverPath = Path().apply {
                moveTo(center.x - 300f * zoomScale, 0f)
                cubicTo(
                    center.x - 200f * zoomScale, canvasH * 0.3f,
                    center.x - 380f * zoomScale, canvasH * 0.7f,
                    center.x - 250f * zoomScale, canvasH
                )
            }
            drawPath(riverPath, color = riverColor, style = Stroke(width = 32f * zoomScale))

            // Grid streets
            for (i in -6..6) {
                val y = center.y + (i * 120f * zoomScale)
                drawLine(
                    color = streetColor,
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 6f * zoomScale
                )
                val x = center.x + (i * 140f * zoomScale)
                drawLine(
                    color = streetColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 6f * zoomScale
                )
            }

            // Main Highway (Western Express Hwy simulation)
            drawLine(
                color = highwayColor,
                start = Offset(center.x - 180f * zoomScale, 0f),
                end = Offset(center.x + 180f * zoomScale, canvasH),
                strokeWidth = 18f * zoomScale
            )
            drawLine(
                color = Color(0xFFFB923C),
                start = Offset(center.x - 180f * zoomScale, 0f),
                end = Offset(center.x + 180f * zoomScale, canvasH),
                strokeWidth = 2f * zoomScale
            )

            // Station Arterial Road
            drawLine(
                color = mainRoadColor,
                start = Offset(0f, center.y + 60f * zoomScale),
                end = Offset(canvasW, center.y - 60f * zoomScale),
                strokeWidth = 14f * zoomScale
            )

            // 2. Real-Time GPS User Location Marker with Radar Wave
            val userNodeX = center.x
            val userNodeY = center.y
            // Accuracy radius halo
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.08f),
                radius = (baseAccuracy * 2.6f * zoomScale).coerceIn(24f, 90f),
                center = Offset(userNodeX, userNodeY)
            )
            // Animated radar ripple
            drawCircle(
                color = PrimaryBlue.copy(alpha = (1f - radarPulse) * 0.35f),
                radius = 12f + (36f * radarPulse * zoomScale),
                center = Offset(userNodeX, userNodeY),
                style = Stroke(width = 2.5f * zoomScale)
            )
            // White outline ring
            drawCircle(
                color = Color.White,
                radius = 12f * zoomScale,
                center = Offset(userNodeX, userNodeY)
            )
            // Solid blue core
            drawCircle(
                color = PrimaryBlue,
                radius = 9f * zoomScale,
                center = Offset(userNodeX, userNodeY)
            )

            // 3. Community Incident Markers / Interactive Pins
            for (rep in displayReports) {
                val markerX = center.x + ((rep.longitude - baseLng) * 14000f * zoomScale).toFloat()
                val markerY = center.y - ((rep.latitude - baseLat) * 14000f * zoomScale).toFloat()

                val markerColor = getCategoryColor(rep.category)
                val confRingColor = when {
                    rep.confidenceScore >= 80 -> CrowdWatchColors.ConfidenceHigh
                    rep.confidenceScore >= 50 -> CrowdWatchColors.ConfidenceMedium
                    else -> CrowdWatchColors.ConfidenceLow
                }
                val isSelected = rep.id == selectedReportId
                val baseRadius = if (isSelected) 20f else 15f
                val pinRadius = baseRadius * zoomScale

                // Outer Severity Halo
                val severityHaloColor = when (rep.severity) {
                    ReportSeverity.CRITICAL -> EmergencyRed.copy(alpha = 0.28f)
                    ReportSeverity.HIGH -> Color(0xFFFB923C).copy(alpha = 0.22f)
                    else -> Color.Transparent
                }
                drawCircle(
                    color = severityHaloColor,
                    radius = pinRadius + (16f * zoomScale),
                    center = Offset(markerX, markerY)
                )

                // Accuracy radius: subtle transparent halo
                drawCircle(
                    color = markerColor.copy(alpha = 0.08f),
                    radius = pinRadius + (18f * zoomScale),
                    center = Offset(markerX, markerY)
                )

                // Selection halo
                if (isSelected) {
                    drawCircle(
                        color = CrowdWatchColors.PrimaryBlue.copy(alpha = 0.25f),
                        radius = pinRadius + (10f * zoomScale),
                        center = Offset(markerX, markerY)
                    )
                }

                // Confidence ring around marker
                drawCircle(
                    color = confRingColor,
                    radius = pinRadius + (2.5f * zoomScale),
                    center = Offset(markerX, markerY),
                    style = Stroke(width = 2.5f * zoomScale)
                )

                // Pin Body
                drawCircle(
                    color = Color.White,
                    radius = pinRadius,
                    center = Offset(markerX, markerY)
                )
                drawCircle(
                    color = markerColor,
                    radius = pinRadius - (2.5f * zoomScale),
                    center = Offset(markerX, markerY)
                )

                // White focal center
                drawCircle(
                    color = Color.White,
                    radius = pinRadius * 0.35f,
                    center = Offset(markerX, markerY)
                )
            }
        }

        // Top Header: GPS Coordinates Pill + Category Chips
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Real-Time GPS Status Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 3.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ResolvedGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GPS: ${currentDeviceLocation?.areaName ?: "Local Area"} (±${baseAccuracy.toInt()}m)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format("%.4f, %.4f", baseLat, baseLng),
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All Incidents" Chip
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("All (${reports.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    )
                )

                // Category Chips
                listOf(
                    ReportCategory.SAFETY,
                    ReportCategory.TRAFFIC_ROADS,
                    ReportCategory.INFRASTRUCTURE,
                    ReportCategory.WEATHER_ENV,
                    ReportCategory.UTILITIES,
                    ReportCategory.CROWD_ACTIVITY
                ).forEach { cat ->
                    val isCatSelected = selectedCategory == cat
                    val count = reports.count { it.category == cat }
                    FilterChip(
                        selected = isCatSelected,
                        onClick = { onCategorySelected(if (isCatSelected) null else cat) },
                        label = {
                            Text(
                                text = "${cat.displayName} ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getCategoryColor(cat),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Map Control buttons (Zoom Pill + Recenter to LocationService)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Combined Zoom Pill
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 3.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.0f) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom in", tint = Color(0xFF475569), modifier = Modifier.size(20.dp))
                    }
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                    IconButton(
                        onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.6f) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom out", tint = Color(0xFF475569), modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Recenter / My Location Button (Refreshes coordinates from LocationService)
            Surface(
                onClick = {
                    zoomScale = 1.0f
                    panOffsetX = 0f
                    panOffsetY = 0f
                    coroutineScope.launch {
                        val loc = locationService.getCurrentLocation() ?: locationService.getLastKnownLocation()
                        if (loc != null) {
                            currentDeviceLocation = loc
                        }
                        onRefreshLocation?.invoke()
                    }
                },
                shape = CircleShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 3.dp,
                modifier = Modifier.size(42.dp).testTag("recenter_my_location")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter on My Location", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Bottom Selected Report Preview Card
        val displayReport = activeReport ?: displayReports.firstOrNull()
        if (displayReport != null) {
            val distMeters = LocationService.computeDistanceMeters(
                baseLat,
                baseLng,
                displayReport.latitude,
                displayReport.longitude
            ).toInt()
            val bearing = LocationService.computeBearing(
                baseLat,
                baseLng,
                displayReport.latitude,
                displayReport.longitude
            )
            val bearingText = LocationService.getBearingDirection(bearing)

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("map_preview_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(getCategoryBgColor(displayReport.category)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(displayReport.category),
                                contentDescription = null,
                                tint = getCategoryColor(displayReport.category),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayReport.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = CrowdWatchColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = displayReport.areaName,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = CrowdWatchColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        SeverityBadge(severity = displayReport.severity)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 $distMeters m $bearingText • ${displayReport.confidenceScore}% confidence",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = PrimaryBlue
                        )
                        Text(
                            text = "${displayReport.verificationsCount} verified • ${displayReport.evidenceCount} evidence",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = CrowdWatchColors.TextTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onConfirmReport(displayReport.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm (+1)", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { onViewReportDetails(displayReport.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CrowdWatchColors.PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("View Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                            .background(CrowdWatchColors.ConfidenceHighBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "All Clear",
                            tint = ResolvedGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Area is all clear",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "No active hazards reported in your immediate vicinity.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}
