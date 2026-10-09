package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SceneThumbnail(
    sampleType: String,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    shapeRadius: Dp = 12.dp,
    badgeText: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(shapeRadius))
            .background(Color(0xFF1E293B))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when {
                sampleType.contains("tree") || sampleType.contains("obstruction") -> {
                    drawFallenTreeScene()
                }
                sampleType.contains("traffic") -> {
                    drawTrafficScene()
                }
                sampleType.contains("water") -> {
                    drawWaterloggingScene()
                }
                sampleType.contains("medical") -> {
                    drawMedicalScene()
                }
                else -> {
                    drawGeneralHazardScene()
                }
            }
        }

        if (badgeText != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawFallenTreeScene() {
    val w = size.width
    val h = size.height

    // Background sky and lush green trees
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF87CEEB), Color(0xFFC8E6C9), Color(0xFF2E7D32)),
            startY = 0f,
            endY = h * 0.5f
        ),
        size = Size(w, h * 0.5f)
    )

    // Distant background foliage
    drawCircle(Color(0xFF1B5E20), radius = w * 0.35f, center = Offset(w * 0.25f, h * 0.3f))
    drawCircle(Color(0xFF2E7D32), radius = w * 0.3f, center = Offset(w * 0.75f, h * 0.25f))

    // Asphalt Road in perspective
    val roadPath = Path().apply {
        moveTo(w * 0.1f, h * 0.45f)
        lineTo(w * 0.9f, h * 0.45f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(roadPath, color = Color(0xFF334155))

    // Yellow road center divider line
    drawLine(
        color = Color(0xFFFACC15),
        start = Offset(w * 0.5f, h * 0.55f),
        end = Offset(w * 0.5f, h * 0.95f),
        strokeWidth = 3f
    )

    // Fallen tree trunk lying across the road
    val trunkPath = Path().apply {
        moveTo(w * 0.05f, h * 0.65f)
        lineTo(w * 0.95f, h * 0.75f)
        lineTo(w * 0.92f, h * 0.85f)
        lineTo(w * 0.03f, h * 0.75f)
        close()
    }
    drawPath(trunkPath, color = Color(0xFF5D4037))
    // Tree bark texture
    drawLine(
        color = Color(0xFF3E2723),
        start = Offset(w * 0.1f, h * 0.7f),
        end = Offset(w * 0.85f, h * 0.8f),
        strokeWidth = 2f
    )

    // Green leafy branches on trunk
    drawCircle(Color(0xFF15803D), radius = w * 0.18f, center = Offset(w * 0.25f, h * 0.65f))
    drawCircle(Color(0xFF16A34A), radius = w * 0.14f, center = Offset(w * 0.45f, h * 0.7f))
    drawCircle(Color(0xFF22C55E), radius = w * 0.12f, center = Offset(w * 0.35f, h * 0.6f))
}

private fun DrawScope.drawTrafficScene() {
    val w = size.width
    val h = size.height

    // Urban highway background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1), Color(0xFF475569)),
            startY = 0f,
            endY = h * 0.4f
        ),
        size = Size(w, h * 0.4f)
    )

    // Distant highway overpass/bridge
    drawRect(
        color = Color(0xFF334155),
        topLeft = Offset(0f, h * 0.28f),
        size = Size(w, h * 0.12f)
    )

    // Multi-lane asphalt road
    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(0f, h * 0.4f),
        size = Size(w, h * 0.6f)
    )

    // White lane dividers
    drawLine(
        color = Color(0xFFE2E8F0),
        start = Offset(w * 0.35f, h * 0.45f),
        end = Offset(w * 0.35f, h * 0.95f),
        strokeWidth = 2f
    )
    drawLine(
        color = Color(0xFFE2E8F0),
        start = Offset(w * 0.68f, h * 0.45f),
        end = Offset(w * 0.68f, h * 0.95f),
        strokeWidth = 2f
    )

    // Traffic queue (Cars)
    // Red car in lane 1
    drawRoundRect(
        color = Color(0xFFEF4444),
        topLeft = Offset(w * 0.12f, h * 0.55f),
        size = Size(w * 0.2f, h * 0.35f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Red car tail lights
    drawCircle(Color(0xFFFF0000), radius = 3f, center = Offset(w * 0.16f, h * 0.86f))
    drawCircle(Color(0xFFFF0000), radius = 3f, center = Offset(w * 0.28f, h * 0.86f))

    // Silver SUV in lane 2
    drawRoundRect(
        color = Color(0xFF94A3B8),
        topLeft = Offset(w * 0.42f, h * 0.48f),
        size = Size(w * 0.22f, h * 0.42f),
        cornerRadius = CornerRadius(5f, 5f)
    )
    drawCircle(Color(0xFFFF0000), radius = 3f, center = Offset(w * 0.46f, h * 0.86f))
    drawCircle(Color(0xFFFF0000), radius = 3f, center = Offset(w * 0.6f, h * 0.86f))

    // Yellow bus in lane 3
    drawRoundRect(
        color = Color(0xFFEAB308),
        topLeft = Offset(w * 0.74f, h * 0.42f),
        size = Size(w * 0.22f, h * 0.52f),
        cornerRadius = CornerRadius(5f, 5f)
    )
}

private fun DrawScope.drawWaterloggingScene() {
    val w = size.width
    val h = size.height

    // Overcast rainy sky and city street curb
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF64748B), Color(0xFF94A3B8)),
            startY = 0f,
            endY = h * 0.35f
        ),
        size = Size(w, h * 0.35f)
    )

    // Curb edge
    drawRect(
        color = Color(0xFF475569),
        topLeft = Offset(0f, h * 0.35f),
        size = Size(w, h * 0.15f)
    )

    // Flooded water body covering road
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF0C4A6E)),
            startY = h * 0.5f,
            endY = h
        ),
        topLeft = Offset(0f, h * 0.5f),
        size = Size(w, h * 0.5f)
    )

    // Water ripple rings
    drawCircle(
        color = Color(0xFFBAE6FD).copy(alpha = 0.6f),
        radius = w * 0.15f,
        center = Offset(w * 0.35f, h * 0.75f),
        style = Stroke(width = 2f)
    )
    drawCircle(
        color = Color(0xFFBAE6FD).copy(alpha = 0.5f),
        radius = w * 0.22f,
        center = Offset(w * 0.65f, h * 0.68f),
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawMedicalScene() {
    val w = size.width
    val h = size.height

    // City street background with red glow
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFEF4444).copy(alpha = 0.4f), Color(0xFF0F172A)),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = w * 0.6f
        )
    )

    // Pavement
    drawRect(
        color = Color(0xFF334155),
        topLeft = Offset(0f, h * 0.6f),
        size = Size(w, h * 0.4f)
    )

    // Ambulance / First responder white box
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(w * 0.2f, h * 0.35f),
        size = Size(w * 0.6f, h * 0.45f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Red Cross emblem
    drawRect(
        color = Color(0xFFEF4444),
        topLeft = Offset(w * 0.45f, h * 0.43f),
        size = Size(w * 0.1f, h * 0.28f)
    )
    drawRect(
        color = Color(0xFFEF4444),
        topLeft = Offset(w * 0.36f, h * 0.52f),
        size = Size(w * 0.28f, h * 0.1f)
    )

    // Flashing blue/red beacon on roof
    drawCircle(Color(0xFF3B82F6), radius = 5f, center = Offset(w * 0.35f, h * 0.3f))
    drawCircle(Color(0xFFEF4444), radius = 5f, center = Offset(w * 0.65f, h * 0.3f))
}

private fun DrawScope.drawGeneralHazardScene() {
    val w = size.width
    val h = size.height

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFF97316), Color(0xFFEA580C)),
            start = Offset.Zero,
            end = Offset(w, h)
        )
    )
    // Warning symbol
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.25f)
        lineTo(w * 0.8f, h * 0.75f)
        lineTo(w * 0.2f, h * 0.75f)
        close()
    }
    drawPath(path, color = Color.White)
    drawLine(
        color = Color(0xFFEA580C),
        start = Offset(w * 0.5f, h * 0.42f),
        end = Offset(w * 0.5f, h * 0.6f),
        strokeWidth = 4f
    )
    drawCircle(Color(0xFFEA580C), radius = 3f, center = Offset(w * 0.5f, h * 0.68f))
}
