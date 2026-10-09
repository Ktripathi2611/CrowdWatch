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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AIAnswer
import com.example.ui.components.ConfidencePill
import com.example.ui.components.ReportCard
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.ResolvedGreen
import com.example.ui.theme.ResolvedGreenBg

@Composable
fun AIAssistantScreen(
    aiAnswer: AIAnswer?,
    isThinking: Boolean,
    onAsk: (String) -> Unit,
    onReportClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Can I travel through this area?",
        "What's happening around me?",
        "Any active emergencies nearby?",
        "Are roads blocked near me?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrowdWatchColors.Background)
    ) {
        // App Bar
        Surface(
            color = CrowdWatchColors.Surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CrowdWatchColors.TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CrowdWatch AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CrowdWatchColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CrowdWatchColors.InfrastructureBg
                        ) {
                            Text(
                                text = "Local Reasoning",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CrowdWatchColors.Infrastructure,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Zero hallucinations • Evaluates only local node observations",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrowdWatchColors.TextSecondary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Data Boundaries Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Security",
                            tint = CrowdWatchColors.Infrastructure,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Grounded strictly in peer reports held by this node. Never invents events.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrowdWatchColors.TextSecondary
                        )
                    }
                }
            }

            // Quick Questions Carousel
            item {
                Column {
                    Text(
                        text = "Suggested Questions",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CrowdWatchColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        quickQuestions.forEach { prompt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = CrowdWatchColors.PrimaryBlue
                                    )
                                    IconButton(
                                        onClick = { onAsk(prompt) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Ask", tint = CrowdWatchColors.PrimaryBlue, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Thinking indicator
            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = CrowdWatchColors.Infrastructure, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Synthesizing node mesh observations...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrowdWatchColors.TextSecondary
                        )
                    }
                }
            }

            // AI Answer Card
            aiAnswer?.let { answer ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CrowdWatchColors.Surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Question header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(CrowdWatchColors.Infrastructure),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = "AI",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CrowdWatch Assessment",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CrowdWatchColors.TextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CrowdWatchColors.ConfidenceHighBg
                                ) {
                                    Text(
                                        text = "Based on ${answer.observationsCount} community observations",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CrowdWatchColors.ConfidenceHighText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = answer.summary,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = CrowdWatchColors.TextPrimary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                answer.detailPoints.forEach { point ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(text = "• ", color = CrowdWatchColors.Infrastructure, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = point,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CrowdWatchColors.TextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Boundary Disclaimer
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CrowdWatchColors.SurfaceSecondary
                            ) {
                                Text(
                                    text = "ℹ ${answer.boundaryDisclaimer}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrowdWatchColors.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Supporting report cards
                if (answer.supportingReports.isNotEmpty()) {
                    item {
                        Text(
                            text = "Supporting Community Reports (${answer.supportingReports.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CrowdWatchColors.TextPrimary
                        )
                    }

                    items(answer.supportingReports) { rep ->
                        ReportCard(
                            report = rep,
                            onClick = { onReportClick(rep.id) }
                        )
                    }
                }
            }
        }

        // Custom query input bar at bottom
        Surface(
            color = CrowdWatchColors.Surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("Ask about travel, hazards, or safety...", color = CrowdWatchColors.TextTertiary) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_query_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (queryText.isNotBlank()) {
                            onAsk(queryText)
                            queryText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CrowdWatchColors.PrimaryBlue)
                        .testTag("ai_send_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}
