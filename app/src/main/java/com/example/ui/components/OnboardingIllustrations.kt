package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.ResolvedGreen
import kotlin.math.cos
import kotlin.math.sin

// --- 1. Splash City Illustration ---
@Composable
fun SplashCityIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soft sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFEFF6FF), Color(0xFFE0F2FE), Color(0xFFF0FDF4)),
                startY = 0f,
                endY = h
            )
        )

        // Geometric stylized city skyline
        val bldgColor1 = Color(0xFF93C5FD)
        val bldgColor2 = Color(0xFF60A5FA)
        val bldgColor3 = Color(0xFF3B82F6)

        // Background buildings
        drawRoundRect(bldgColor1, topLeft = Offset(w * 0.08f, h * 0.42f), size = Size(w * 0.16f, h * 0.45f), cornerRadius = CornerRadius(12f, 12f))
        drawRoundRect(bldgColor2, topLeft = Offset(w * 0.28f, h * 0.32f), size = Size(w * 0.18f, h * 0.55f), cornerRadius = CornerRadius(14f, 14f))
        drawRoundRect(bldgColor3, topLeft = Offset(w * 0.50f, h * 0.22f), size = Size(w * 0.22f, h * 0.65f), cornerRadius = CornerRadius(16f, 16f))
        drawRoundRect(bldgColor2, topLeft = Offset(w * 0.74f, h * 0.38f), size = Size(w * 0.18f, h * 0.49f), cornerRadius = CornerRadius(12f, 12f))

        // Road at bottom
        val roadTop = h * 0.76f
        drawRect(Color(0xFF334155), topLeft = Offset(0f, roadTop), size = Size(w, h - roadTop))

        // Center dashed yellow lane markings
        val dashWidth = w * 0.08f
        val dashGap = w * 0.04f
        var startX = w * 0.05f
        val laneY = roadTop + (h - roadTop) / 2f
        while (startX < w) {
            drawLine(
                color = Color(0xFFFACC15),
                start = Offset(startX, laneY),
                end = Offset((startX + dashWidth).coerceAtMost(w), laneY),
                strokeWidth = 5f
            )
            startX += dashWidth + dashGap
        }

        // Concentric wireless signal waves radiating from central building
        val beaconCenter = Offset(w * 0.61f, h * 0.22f)
        for (r in listOf(28f, 52f, 80f, 115f)) {
            drawCircle(
                color = PrimaryBlue.copy(alpha = (1f - r / 140f).coerceIn(0.15f, 0.7f)),
                radius = r,
                center = beaconCenter,
                style = Stroke(width = 3.5f)
            )
        }
        drawCircle(color = PrimaryBlue, radius = 8f, center = beaconCenter)

        // Floating Incident Pin Pill in sky
        val pinCenter = Offset(w * 0.30f, h * 0.20f)
        drawCircle(color = EmergencyRed, radius = 14f, center = pinCenter)
        drawCircle(color = Color.White, radius = 5f, center = pinCenter)
    }
}

// --- 2. Share Directly Illustration (P2P Mesh Nodes) ---
@Composable
fun PeopleNodesIllustration(modifier: Modifier = Modifier) = ShareDirectlyIllustration(modifier)

@Composable
fun ShareDirectlyIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val orbitRadius = 110.dp.toPx()

            // Dashed orbit circle
            drawCircle(
                color = Color(0xFFCBD5E1),
                radius = orbitRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Radiating connecting lines to 6 peers
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60.0) - 30.0)
                val peerX = center.x + (orbitRadius * cos(angle)).toFloat()
                val peerY = center.y + (orbitRadius * sin(angle)).toFloat()

                drawLine(
                    color = PrimaryBlue.copy(alpha = 0.45f),
                    start = center,
                    end = Offset(peerX, peerY),
                    strokeWidth = 3f
                )
            }
        }

        // Center Phone / Hub Card
        Card(
            modifier = Modifier.size(width = 80.dp, height = 110.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Mesh Hub",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // 6 Surrounding Peer Avatar Circles
        val avatarBgs = listOf(
            Color(0xFFFEF3C7), Color(0xFFDBEAFE), Color(0xFFDCFCE7),
            Color(0xFFF3E8FF), Color(0xFFFFE4E6), Color(0xFFCFFAFE)
        )
        val avatarTints = listOf(
            Color(0xFFD97706), Color(0xFF2563EB), Color(0xFF16A34A),
            Color(0xFF9333EA), Color(0xFFE11D48), Color(0xFF0891B2)
        )

        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60.0) - 30.0)
            val offsetX = (110.0 * cos(angle)).dp
            val offsetY = (110.0 * sin(angle)).dp

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp)
                    .offset(x = offsetX, y = offsetY)
                    .clip(CircleShape)
                    .background(avatarBgs[i]),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = avatarTints[i],
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// --- 3. Data Stays Local Illustration ---
@Composable
fun DataLocalIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Phone Outline Card in Center
        Card(
            modifier = Modifier.size(width = 110.dp, height = 180.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(3.dp, PrimaryBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Speaker bar
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .size(width = 36.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFCBD5E1))
                )

                // Central Padlock
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Local Lock",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        // Orbiting feature icons
        val features = listOf(
            Icons.Default.CameraAlt to Color(0xFF3B82F6),
            Icons.Default.Videocam to Color(0xFF8B5CF6),
            Icons.Default.Mic to Color(0xFFEC4899),
            Icons.Default.Place to Color(0xFF10B981),
            Icons.Default.Security to Color(0xFFF59E0B),
            Icons.Default.Check to Color(0xFF06B6D4)
        )

        for (i in features.indices) {
            val angle = Math.toRadians((i * 60.0))
            val offsetX = (115.0 * cos(angle)).dp
            val offsetY = (95.0 * sin(angle)).dp
            val (icon, color) = features[i]

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .offset(x = offsetX, y = offsetY)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// --- 4. Communities Verify Illustration ---
@Composable
fun CommunitiesVerifyIllustration(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card 1: Verified observation with green check
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Box(modifier = Modifier.size(width = 110.dp, height = 8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFCBD5E1)))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.size(width = 65.dp, height = 6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE2E8F0)))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ResolvedGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmed", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Card 2: Disputed observation with red X
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Box(modifier = Modifier.size(width = 95.dp, height = 8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFCBD5E1)))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.size(width = 50.dp, height = 6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE2E8F0)))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(EmergencyRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Disputed", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Card 3: Validated observation with blue check
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3E8FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Box(modifier = Modifier.size(width = 120.dp, height = 8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFCBD5E1)))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.size(width = 70.dp, height = 6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE2E8F0)))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirmed", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// --- 5. Confidence Changes Illustration ---
@Composable
fun ConfidenceChangesIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Circular progress dial with 82%
        Canvas(modifier = Modifier.size(150.dp)) {
            val stroke = 12f
            val radius = (size.minDimension - stroke) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background gray track
            drawCircle(
                color = Color(0xFFE2E8F0),
                radius = radius,
                center = center,
                style = Stroke(width = stroke)
            )

            // Active Green 82% Arc
            drawArc(
                color = ResolvedGreen,
                startAngle = -90f,
                sweepAngle = 360f * 0.82f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // Center Score Typography
        Text(
            text = "82%",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ResolvedGreen
            )
        )

        // Surrounding mini square icon tiles
        val iconList = listOf(
            Icons.Default.CameraAlt to Color(0xFF0284C7),
            Icons.Default.Place to Color(0xFFEF4444),
            Icons.Default.Check to Color(0xFF10B981),
            Icons.Default.Search to Color(0xFF8B5CF6),
            Icons.Default.Security to Color(0xFFF59E0B)
        )

        for (i in iconList.indices) {
            val angle = Math.toRadians((i * 72.0) - 90.0)
            val offsetX = (105.0 * cos(angle)).dp
            val offsetY = (105.0 * sin(angle)).dp
            val (icon, tint) = iconList[i]

            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .offset(x = offsetX, y = offsetY),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// --- 6. Get Real-time Alerts Illustration ---
@Composable
fun RealtimeAlertsIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Soft map backdrop with street grids and pins
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft grid
            drawRect(Color(0xFFF1F5F9))
            drawLine(Color(0xFFE2E8F0), start = Offset(0f, h * 0.3f), end = Offset(w, h * 0.3f), strokeWidth = 3f)
            drawLine(Color(0xFFE2E8F0), start = Offset(0f, h * 0.7f), end = Offset(w, h * 0.7f), strokeWidth = 3f)
            drawLine(Color(0xFFE2E8F0), start = Offset(w * 0.25f, 0f), end = Offset(w * 0.25f, h), strokeWidth = 3f)
            drawLine(Color(0xFFE2E8F0), start = Offset(w * 0.75f, 0f), end = Offset(w * 0.75f, h), strokeWidth = 3f)

            // Incident Pins on map
            drawCircle(EmergencyRed, radius = 10f, center = Offset(w * 0.2f, h * 0.25f))
            drawCircle(Color(0xFF8B5CF6), radius = 10f, center = Offset(w * 0.8f, h * 0.22f))
            drawCircle(ResolvedGreen, radius = 8f, center = Offset(w * 0.85f, h * 0.75f))
        }

        // Elevated Alert Card popping up
        Card(
            modifier = Modifier
                .size(width = 170.dp, height = 120.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Bell Icon circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alert Bell",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFCBD5E1))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 50.dp, height = 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE2E8F0))
                )
            }
        }
    }
}
