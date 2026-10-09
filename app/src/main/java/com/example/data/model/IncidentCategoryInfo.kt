package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Rich domain data model for incident categories across CrowdWatch.
 * Defines display metadata, colors, default severity, and predefined subcategories.
 */
data class IncidentCategoryInfo(
    val category: ReportCategory,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val backgroundColor: Color,
    val defaultSeverity: ReportSeverity,
    val subcategories: List<String>
)

object IncidentCategoryRegistry {
    val categories: List<IncidentCategoryInfo> = listOf(
        IncidentCategoryInfo(
            category = ReportCategory.SAFETY,
            title = "Safety",
            subtitle = "Threats, hazards & emergency risks",
            description = "Physical safety hazards, dangerous areas, suspicious activity, harassment, or emergencies requiring immediate attention.",
            icon = Icons.Default.Security,
            primaryColor = Color(0xFFDC2626), // Emergency Red
            backgroundColor = Color(0xFFFEE2E2),
            defaultSeverity = ReportSeverity.HIGH,
            subcategories = listOf(
                "Street harassment",
                "Unsafe / dark alleyway",
                "Physical danger / brawl",
                "Suspicious hazard",
                "Fire breakout",
                "Stray animal danger"
            )
        ),
        IncidentCategoryInfo(
            category = ReportCategory.TRAFFIC_ROADS,
            title = "Traffic",
            subtitle = "Congestion, road blocks & delays",
            description = "Gridlock traffic, blocked intersections, major road obstructions, vehicle accidents, and broken transit signals.",
            icon = Icons.Default.Traffic,
            primaryColor = Color(0xFFEA580C), // Orange
            backgroundColor = Color(0xFFFFEDD5),
            defaultSeverity = ReportSeverity.MEDIUM,
            subcategories = listOf(
                "Road obstruction",
                "Gridlock traffic jam",
                "Vehicle collision",
                "Fallen tree on road",
                "Broken traffic light",
                "Diversion / roadblock"
            )
        ),
        IncidentCategoryInfo(
            category = ReportCategory.INFRASTRUCTURE,
            title = "Infrastructure",
            subtitle = "Civic damage, roads & utilities",
            description = "Structural damage, road potholes, broken water mains, open manholes, down electrical wires, and public utility outages.",
            icon = Icons.Default.Build,
            primaryColor = Color(0xFF2563EB), // Primary Blue
            backgroundColor = Color(0xFFDBEAFE),
            defaultSeverity = ReportSeverity.MEDIUM,
            subcategories = listOf(
                "Deep pothole hazard",
                "Open manhole drain",
                "Water main burst / leakage",
                "Downed electrical wire",
                "Broken streetlight",
                "Damaged bridge / pavement"
            )
        ),
        IncidentCategoryInfo(
            category = ReportCategory.WEATHER_ENV,
            title = "Weather & Flood",
            subtitle = "Waterlogging, storms & extreme conditions",
            description = "Severe water accumulation, localized flash flooding, storm debris, fallen hoardings, and fallen branches.",
            icon = Icons.Default.WaterDamage,
            primaryColor = Color(0xFF0284C7), // Light Blue / Water
            backgroundColor = Color(0xFFE0F2FE),
            defaultSeverity = ReportSeverity.HIGH,
            subcategories = listOf(
                "Waterlogging (> 1.5 ft)",
                "Severe flash flood",
                "Storm damage / debris",
                "Fallen advertisement hoarding",
                "Submerged railway track",
                "Excessive mud / landslide"
            )
        ),
        IncidentCategoryInfo(
            category = ReportCategory.CROWD_ACTIVITY,
            title = "Crowd Activity",
            subtitle = "Surges, protests & gatherings",
            description = "Station stampede risk, sudden crowd surges, unplanned political rallies, or high-density bottlenecks.",
            icon = Icons.Default.Groups,
            primaryColor = Color(0xFF7C3AED), // Purple
            backgroundColor = Color(0xFFEDE9FE),
            defaultSeverity = ReportSeverity.MEDIUM,
            subcategories = listOf(
                "Station platform overcrowding",
                "Foot-overbridge bottleneck",
                "Protest rally / blockade",
                "Festival crowd surge",
                "Stampede risk alert"
            )
        ),
        IncidentCategoryInfo(
            category = ReportCategory.MEDICAL,
            title = "Medical",
            subtitle = "Health emergencies & first aid needs",
            description = "Urgent first-aid needed, injured pedestrians, heat exhaustion, or ambulance access blocked.",
            icon = Icons.Default.LocalHospital,
            primaryColor = Color(0xFFE11D48), // Rose Red
            backgroundColor = Color(0xFFFFE4E6),
            defaultSeverity = ReportSeverity.CRITICAL,
            subcategories = listOf(
                "Pedestrian injured",
                "Ambulance passage blocked",
                "Heat exhaustion / collapse",
                "Medical first-aid needed"
            )
        ),
        IncidentCategoryInfo(
            category = ReportCategory.OTHER,
            title = "Other",
            subtitle = "Civic concerns not listed above",
            description = "Garbage accumulation, noise nuisance, civic complaints, and miscellaneous localized issues.",
            icon = Icons.Default.MoreHoriz,
            primaryColor = Color(0xFF64748B), // Slate
            backgroundColor = Color(0xFFF1F5F9),
            defaultSeverity = ReportSeverity.LOW,
            subcategories = listOf(
                "Garbage dump accumulation",
                "Severe noise nuisance",
                "Public property vandalism",
                "Other civic concern"
            )
        )
    )

    fun getCategoryInfo(category: ReportCategory): IncidentCategoryInfo {
        return categories.firstOrNull { it.category == category } ?: categories.last()
    }
}
