package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.service.EvidenceProcessor
import com.example.service.LocationService
import com.example.service.TaggedLocation
import com.example.service.PermissionHelper
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.model.MediaType
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.ui.components.EvidenceVisualBanner
import com.example.ui.components.getCategoryBgColor
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.IncidentReportDraft

@Composable
fun ReportCreationScreen(
    currentStep: Int,
    draft: IncidentReportDraft,
    onCategorySelected: (ReportCategory) -> Unit,
    onSubcategorySelected: (String) -> Unit,
    onEvidenceUpdated: (MediaType, String, String, String) -> Unit,
    onLocationTagged: ((TaggedLocation) -> Unit)? = null,
    onConditionSelected: (ReportCondition) -> Unit,
    onStepChange: (Int) -> Unit,
    onPublish: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // App Bar
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (currentStep > 1) onStepChange(currentStep - 1) else onCancel()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentStep) {
                                1 -> "Report an Incident"
                                2 -> "Select Situation"
                                3 -> "Add Evidence"
                                4 -> "Current Condition"
                                5 -> "Review Report"
                                else -> "Report an Incident"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color(0xFF0F172A)
                        )
                    }

                    Text(
                        text = "Step $currentStep of 5",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5-Step Segmented Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (step in 1..5) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (step <= currentStep) PrimaryBlue else Color(0xFFE2E8F0)
                                )
                        )
                    }
                }
            }
        }

        // Step Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (currentStep) {
                1 -> Step1CategorySelection(
                    selectedCategory = draft.category,
                    onSelect = {
                        onCategorySelected(it)
                        onStepChange(2)
                    },
                    onCancel = onCancel
                )
                2 -> Step2SituationDetails(
                    category = draft.category,
                    onSelectSituation = { sit ->
                        onSubcategorySelected(sit)
                        onStepChange(3)
                    },
                    onBack = { onStepChange(1) }
                )
                3 -> Step3EvidenceAttachment(
                    draft = draft,
                    onLocationTagged = onLocationTagged,
                    onNext = { media, sample, desc, area ->
                        onEvidenceUpdated(media, sample, desc, area)
                        onStepChange(4)
                    },
                    onBack = { onStepChange(2) }
                )
                4 -> Step4ConditionSelection(
                    selectedCondition = draft.condition,
                    onSelectCondition = { cond ->
                        onConditionSelected(cond)
                        onStepChange(5)
                    },
                    onBack = { onStepChange(3) }
                )
                5 -> Step5ReviewSubmission(
                    draft = draft,
                    onPublish = onPublish,
                    onEdit = { onStepChange(1) }
                )
            }
        }
    }
}

// --- STEP 1: What best describes the incident? (Screen 4) ---
@Composable
private fun Step1CategorySelection(
    selectedCategory: ReportCategory,
    onSelect: (ReportCategory) -> Unit,
    onCancel: () -> Unit
) {
    var chosenCategory by remember { mutableStateOf(selectedCategory) }

    val primaryCategories = listOf(
        ReportCategory.TRAFFIC_ROADS,
        ReportCategory.INFRASTRUCTURE,
        ReportCategory.SAFETY,
        ReportCategory.WEATHER_ENV,
        ReportCategory.UTILITIES,
        ReportCategory.OTHER
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "What best describes the incident?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2-Column Grid of 6 Categories
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(primaryCategories) { category ->
                    val color = getCategoryColor(category)
                    val bg = getCategoryBgColor(category)
                    val isSelected = chosenCategory == category

                    val displayName = when (category) {
                        ReportCategory.TRAFFIC_ROADS -> "Traffic"
                        ReportCategory.INFRASTRUCTURE -> "Infrastructure"
                        ReportCategory.SAFETY -> "Hazards"
                        ReportCategory.WEATHER_ENV -> "Weather"
                        ReportCategory.UTILITIES -> "Flooding"
                        ReportCategory.OTHER -> "Other"
                        else -> category.displayName
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(118.dp)
                            .clickable {
                                chosenCategory = category
                            }
                            .testTag("cat_card_${category.name.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) PrimaryBlue else Color(0xFFE2E8F0)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(category),
                                    contentDescription = displayName,
                                    tint = color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = if (isSelected) PrimaryBlue else Color(0xFF0F172A),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Full Width Continue Button
        Button(
            onClick = { onSelect(chosenCategory) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("step1_next_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// --- STEP 2: Select Situation ---
@Composable
private fun Step2SituationDetails(
    category: ReportCategory,
    onSelectSituation: (String) -> Unit,
    onBack: () -> Unit
) {
    val situations = when (category) {
        ReportCategory.TRAFFIC_ROADS -> listOf(
            "Road obstruction", "Accident", "Heavy traffic", "Fallen tree",
            "Flooded road", "Pothole / Damaged road", "Broken traffic signal"
        )
        ReportCategory.WEATHER_ENV -> listOf(
            "Waterlogging", "Flooding", "Heavy downpour", "Fallen tree / Branch",
            "Strong wind damage", "Poor visibility"
        )
        ReportCategory.SAFETY -> listOf(
            "Smoke & Fire Hazard", "Gas leak", "Fallen power wire", "Suspicious activity", "Unsafe structure"
        )
        ReportCategory.INFRASTRUCTURE -> listOf(
            "Streetlight failure", "Traffic signal fault", "Open manhole", "Water pipe burst"
        )
        ReportCategory.CROWD_ACTIVITY -> listOf(
            "Crowd congestion", "Queue bottleneck", "Platform overcrowding"
        )
        ReportCategory.PUBLIC_TRANSPORT -> listOf(
            "Bus disruption", "Train delay", "Station closure"
        )
        ReportCategory.MEDICAL -> listOf(
            "Medical emergency", "Injured person", "Ambulance requested"
        )
        ReportCategory.LOCAL_SERVICES -> listOf(
            "ATM unavailable", "Petrol station queue", "Pharmacy closed"
        )
        ReportCategory.UTILITIES -> listOf(
            "Power blackout", "Water supply cut", "Telecom outage"
        )
        ReportCategory.OTHER -> listOf(
            "Civic alert", "Animal hazard", "Custom event"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Select Specific Situation",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Help your community identify the exact hazard in ${category.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(situations) { situation ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSituation(situation) }
                            .testTag("situation_$situation"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = situation,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF0F172A)
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(top = 8.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Back")
        }
    }
}

// --- STEP 3: Add Evidence (Screen 4) ---
@Composable
private fun Step3EvidenceAttachment(
    draft: IncidentReportDraft,
    onLocationTagged: ((TaggedLocation) -> Unit)? = null,
    onNext: (MediaType, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val evidenceProcessor = remember { EvidenceProcessor(context) }
    val locationService = remember { LocationService(context) }

    var selectedMediaType by remember { mutableStateOf(MediaType.PHOTO) }
    var description by remember { mutableStateOf(draft.description) }
    var areaName by remember { mutableStateOf(draft.areaName) }
    var sampleType by remember { mutableStateOf(draft.evidenceSampleType) }
    var localEvidenceUri by remember { mutableStateOf<String?>(null) }
    var evidenceExifMeta by remember { mutableStateOf<String?>(null) }
    var isDetectingGps by remember { mutableStateOf(false) }
    var liveAccuracyMeters by remember { mutableStateOf(draft.accuracyMeters.toInt()) }
    var currentCoordsText by remember {
        mutableStateOf(String.format(java.util.Locale.US, "%.5f° N, %.5f° E", draft.latitude, draft.longitude))
    }
    var showCameraCapture by remember { mutableStateOf(false) }

    // Fullscreen CameraX Capture Overlay
    if (showCameraCapture) {
        CameraCaptureScreen(
            initialMode = if (selectedMediaType == MediaType.VIDEO) CameraCaptureMode.VIDEO else CameraCaptureMode.PHOTO,
            onMediaCaptured = { uri, isVideo ->
                localEvidenceUri = uri.toString()
                selectedMediaType = if (isVideo) MediaType.VIDEO else MediaType.PHOTO
                sampleType = if (isVideo) "video" else "photo"
                evidenceExifMeta = "CameraX Captured (${if (isVideo) "MP4 Video Clip" else "High-Res JPEG"})"
                showCameraCapture = false
                // Auto tag location upon capture with Play Services Location
                coroutineScope.launch {
                    val loc = locationService.getCurrentLocation() ?: locationService.getLastKnownLocation()
                    if (loc != null) {
                        areaName = loc.areaName
                        liveAccuracyMeters = loc.accuracyMeters.toInt()
                        currentCoordsText = loc.toFormattedCoordinates()
                        onLocationTagged?.invoke(loc)
                    }
                }
            },
            onClose = { showCameraCapture = false }
        )
        return
    }

    // Android 13+ zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val processed = evidenceProcessor.processEvidenceUri(uri)
                localEvidenceUri = processed.localUri
                sampleType = "photo"
                evidenceExifMeta = "EXIF verified (${processed.fileSizeKb} KB" +
                    if (processed.latitude != null) ", GPS tagged" else "" + ")"
                if (processed.latitude != null && processed.longitude != null) {
                    areaName = String.format(java.util.Locale.US, "GPS: %.4f° N, %.4f° E", processed.latitude, processed.longitude)
                }
            }
        }
    }

    // Runtime location permission requester
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                isDetectingGps = true
                val loc = locationService.getCurrentLocation() ?: locationService.getLastKnownLocation()
                if (loc != null) {
                    areaName = loc.areaName
                    liveAccuracyMeters = loc.accuracyMeters.toInt()
                    currentCoordsText = loc.toFormattedCoordinates()
                    onLocationTagged?.invoke(loc)
                }
                isDetectingGps = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Media Selector Tabs: Photo / Video / Audio
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EvidenceMediaPill(
                        label = "Photo",
                        icon = Icons.Default.CameraAlt,
                        isSelected = selectedMediaType == MediaType.PHOTO,
                        onClick = { selectedMediaType = MediaType.PHOTO },
                        modifier = Modifier.weight(1f)
                    )
                    EvidenceMediaPill(
                        label = "Video",
                        icon = Icons.Default.Videocam,
                        isSelected = selectedMediaType == MediaType.VIDEO,
                        onClick = { selectedMediaType = MediaType.VIDEO },
                        modifier = Modifier.weight(1f)
                    )
                    EvidenceMediaPill(
                        label = "Audio",
                        icon = Icons.Default.Mic,
                        isSelected = selectedMediaType == MediaType.AUDIO,
                        onClick = { selectedMediaType = MediaType.AUDIO },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Upload Box: Dashed border light blue box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.5.dp, PrimaryBlue, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Blue camera icon in circle
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (localEvidenceUri != null) "Evidence Attached" else "Add photo or video",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = evidenceExifMeta ?: "Take a photo or select from gallery with EXIF metadata",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (evidenceExifMeta != null) Color(0xFF10B981) else Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                onClick = { showCameraCapture = true },
                                shape = RoundedCornerShape(20.dp),
                                color = PrimaryBlue,
                                shadowElevation = 2.dp,
                                modifier = Modifier.testTag("open_camera_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Camera", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }

                            Surface(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                shadowElevation = 2.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Device Gallery", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                }
                            }
                        }
                    }
                }
            }

            // Location (auto-detected) Card
            item {
                Column {
                    Text(
                        text = "Location (auto-detected GPS)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (PermissionHelper.hasLocationPermission(context)) {
                                    coroutineScope.launch {
                                        isDetectingGps = true
                                        val loc = locationService.getCurrentLocation() ?: locationService.getLastKnownLocation()
                                        if (loc != null) {
                                            areaName = loc.areaName
                                            liveAccuracyMeters = loc.accuracyMeters.toInt()
                                            currentCoordsText = loc.toFormattedCoordinates()
                                            onLocationTagged?.invoke(loc)
                                        }
                                        isDetectingGps = false
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isDetectingGps) "Acquiring Play Services GPS..." else areaName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (isDetectingGps) "Querying FusedLocationProviderClient..." else "$currentCoordsText (±${liveAccuracyMeters}m)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Google Play Services Location Verified",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = "Refresh GPS", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Description (optional)
            item {
                Column {
                    Text(
                        text = "Description (optional)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("report_description_input"),
                        placeholder = { Text("Add more details about what you noticed...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }
        }

        // Bottom Actions: Back & Next
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text("Back", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { onNext(selectedMediaType, sampleType, description, areaName) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("step3_next_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EvidenceMediaPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryBlue else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryBlue else Color(0xFFE2E8F0)),
        modifier = modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else Color(0xFF475569)
            )
        }
    }
}

// --- STEP 4: Current Condition ---
@Composable
private fun Step4ConditionSelection(
    selectedCondition: ReportCondition,
    onSelectCondition: (ReportCondition) -> Unit,
    onBack: () -> Unit
) {
    var condition by remember { mutableStateOf(selectedCondition) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Current Condition",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Is this situation currently ongoing right now?",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(16.dp))

            ReportCondition.values().forEach { cond ->
                val isSelected = condition == cond
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { condition = cond }
                        .testTag("cond_${cond.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) PrimaryBlue else Color(0xFFE2E8F0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = cond.label,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = when (cond) {
                                    ReportCondition.HAPPENING_NOW -> "Active physical hazard currently present"
                                    ReportCondition.CHANGING -> "Situation is resolving or clearing up"
                                    ReportCondition.NOT_SURE -> "Reported recently, present status unconfirmed"
                                    ReportCondition.RESOLVED -> "Hazard has cleared completely"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }

                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back")
            }

            Button(
                onClick = { onSelectCondition(condition) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("condition_next_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- STEP 5: Review Report (Screen 5) ---
@Composable
private fun Step5ReviewSubmission(
    draft: IncidentReportDraft,
    onPublish: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category & Description Card with Edit button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(draft.category),
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = draft.subcategory.ifBlank { "Waterlogging" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        ),
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Hazards / Infrastructure",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Text(
                                text = "Edit",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue,
                                modifier = Modifier
                                    .clickable { onEdit() }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = draft.description.ifBlank { "Water accumulated up to 1.5 ft near the main junction. Slow moving traffic." },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // Location & Status Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Happening now",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }

                            Text(
                                text = "Just now",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = draft.areaName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = String.format(
                                        java.util.Locale.US,
                                        "GPS: %.5f° N, %.5f° E • Accuracy: ±%.0fm",
                                        draft.latitude,
                                        draft.longitude,
                                        draft.accuracyMeters
                                    ),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // Attached Media & Scene Visuals
            item {
                EvidenceVisualBanner(
                    sampleType = draft.evidenceSampleType,
                    evidenceCount = 3,
                    evidenceType = "PHOTOS & VIDEO",
                    modifier = Modifier.height(150.dp)
                )
            }

            // Initial Confidence Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Initial confidence",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "78% Medium",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { 0.78f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryBlue,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // Cryptographic Node Identity & P2P Mesh Broadcast Confirmation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ECDSA P-256 node signature generated",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = Color(0xFF15803D)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ready to broadcast to 8 mesh peers",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action: Broadcast to Mesh (Full-width solid blue)
        Button(
            onClick = onPublish,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("publish_report_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Broadcast to Mesh",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
