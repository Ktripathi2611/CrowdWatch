package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.EventType
import com.example.data.model.IncidentReport
import com.example.data.model.MediaType
import com.example.data.model.ReportEvent
import com.example.data.model.ReportStatus
import com.example.engine.ConfidenceEngine
import com.example.engine.FreshnessEngine
import com.example.ui.components.ConfidencePill
import com.example.ui.components.EvidenceVisualBanner
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.theme.DisruptionYellow
import com.example.ui.theme.DisruptionYellowBg
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedBg
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.ResolvedGreen
import com.example.ui.theme.ResolvedGreenBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportDetailScreen(
    report: IncidentReport,
    events: List<ReportEvent>,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onDispute: (String) -> Unit,
    onStillHappening: () -> Unit,
    onResolve: () -> Unit,
    onAddEvidence: (MediaType, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDisputeDialog by remember { mutableStateOf(false) }
    var disputeReason by remember { mutableStateOf("") }
    var showEvidenceDialog by remember { mutableStateOf(false) }

    val confidenceBreakdown = remember(report) {
        ConfidenceEngine.calculateConfidence(report)
    }

    val timeAgo = FreshnessEngine.formatTimeAgo(report.createdAt)

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                    }
                    Text(
                        text = report.category.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        color = Color(0xFF0F172A)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeverityBadge(severity = report.severity)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { /* Simulated native share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF64748B))
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Status
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = report.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                            color = Color(0xFF0F172A)
                        )
                        StatusPill(status = report.status)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = report.description,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),
                        color = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${report.areaName} • ${report.distanceMeters} m away • $timeAgo",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Photo Evidence
            item {
                EvidenceVisualBanner(
                    sampleType = report.evidenceSampleType,
                    evidenceCount = report.evidenceCount,
                    evidenceType = report.evidenceType.name
                )
            }

            // Confidence Score Breakdown Section
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
                                text = "Community Confidence",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F172A)
                            )
                            ConfidencePill(score = report.confidenceScore)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { report.confidenceScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (report.confidenceScore >= 80) ResolvedGreen else PrimaryBlue,
                            trackColor = Color(0xFFF1F5F9)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Factors breakdown list
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            confidenceBreakdown.factors.forEach { factor ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (factor.contains("-")) EmergencyRed else ResolvedGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = factor,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confidence updates automatically as nearby peers confirm, dispute, or upload evidence.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Community Verification Actions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Community Verification",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // 2 Primary action buttons: Confirm vs Dispute
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onConfirm,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("action_confirm"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrowdWatchColors.ConfidenceHigh,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Confirm", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("I can confirm", fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { showDisputeDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("action_dispute"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CrowdWatchColors.ButtonDestructiveBg,
                                    contentColor = CrowdWatchColors.ButtonDestructiveText
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.ButtonDestructiveBorder)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dispute", modifier = Modifier.size(16.dp), tint = CrowdWatchColors.ButtonDestructiveText)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dispute", fontWeight = FontWeight.SemiBold, color = CrowdWatchColors.ButtonDestructiveText)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary actions: Still happening, Mark resolved, Add evidence
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onStillHappening,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CrowdWatchColors.ButtonNeutralBg,
                                    contentColor = CrowdWatchColors.ButtonNeutralText
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Still happening", modifier = Modifier.size(14.dp), tint = CrowdWatchColors.ButtonNeutralText)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Still here", fontSize = 12.sp, color = CrowdWatchColors.ButtonNeutralText)
                            }

                            OutlinedButton(
                                onClick = onResolve,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CrowdWatchColors.ButtonNeutralBg,
                                    contentColor = CrowdWatchColors.ButtonNeutralText
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.TaskAlt, contentDescription = "Resolved", modifier = Modifier.size(14.dp), tint = CrowdWatchColors.ButtonNeutralText)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resolved", fontSize = 12.sp, color = CrowdWatchColors.ButtonNeutralText)
                            }

                            OutlinedButton(
                                onClick = { showEvidenceDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CrowdWatchColors.ButtonNeutralBg,
                                    contentColor = CrowdWatchColors.ButtonNeutralText
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Add evidence", modifier = Modifier.size(14.dp), tint = CrowdWatchColors.ButtonNeutralText)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Photo", fontSize = 12.sp, color = CrowdWatchColors.ButtonNeutralText)
                            }
                        }
                    }
                }
            }

            // Chronological Incident Timeline
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Incident Timeline",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (events.isEmpty()) {
                            Text(
                                text = "Initial report registered on local mesh.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        } else {
                            events.sortedByDescending { it.timestamp }.forEachIndexed { idx, event ->
                                TimelineRowItem(event = event, isLast = idx == events.lastIndex)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dispute Dialog
    if (showDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showDisputeDialog = false },
            title = { Text("Dispute Report") },
            text = {
                Column {
                    Text("Help your community avoid incorrect reports. Why is this report inaccurate?")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = disputeReason,
                        onValueChange = { disputeReason = it },
                        placeholder = { Text("e.g. Cleared by police, wrong location, not visible") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDispute(disputeReason.ifBlank { "Disputed by local node" })
                        showDisputeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Submit Dispute")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisputeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Evidence Dialog
    if (showEvidenceDialog) {
        AlertDialog(
            onDismissRequest = { showEvidenceDialog = false },
            title = { Text("Attach Supporting Evidence") },
            text = {
                Text("Select camera or gallery snapshot to verify current status with fellow nodes.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddEvidence(MediaType.PHOTO, "Photo evidence (verified node)")
                        showEvidenceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Add Verified Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEvidenceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TimelineRowItem(event: ReportEvent, isLast: Boolean) {
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val timeStr = formatter.format(Date(event.timestamp))

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (event.type) {
                            EventType.REPORT_CREATED -> PrimaryBlue
                            EventType.REPORT_CONFIRMED -> ResolvedGreen
                            EventType.REPORT_DISPUTED -> EmergencyRed
                            EventType.REPORT_RESOLVED -> Color(0xFF10B981)
                            else -> PrimaryBlue
                        }
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.details,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Node ${event.nodeId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusPill(status: ReportStatus) {
    val (bg, textColor, label) = when (status) {
        ReportStatus.VERIFIED -> Triple(CrowdWatchColors.ConfidenceHighBg, CrowdWatchColors.ConfidenceHighText, "Verified")
        ReportStatus.ACTIVE -> Triple(CrowdWatchColors.PrimaryBlueBg, CrowdWatchColors.PrimaryBlue, "Active")
        ReportStatus.DEVELOPING -> Triple(CrowdWatchColors.ConfidenceMediumBg, CrowdWatchColors.ConfidenceMediumText, "Developing")
        ReportStatus.DISPUTED -> Triple(CrowdWatchColors.ConfidenceLowBg, CrowdWatchColors.ConfidenceLowText, "Disputed")
        ReportStatus.RESOLVED -> Triple(CrowdWatchColors.ResolvedBg, CrowdWatchColors.ResolvedText, "✓ Resolved")
        ReportStatus.STALE -> Triple(CrowdWatchColors.SurfaceSecondary, CrowdWatchColors.TextTertiary, "Possibly Outdated")
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
