package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * CrowdWatch Global Color System
 * Strictly locks the color hierarchy across all screens:
 * Blue = CrowdWatch / action
 * Green = confidence / verified / resolved / connected
 * Orange = developing / caution / hazard
 * Red = critical / dispute / low confidence
 * Cyan = P2P network activity
 * Purple = infrastructure/crowd-related categories
 * Gray = neutral/inactive/offline
 */
object CrowdWatchColors {

    // Brand
    val PrimaryBlue = Color(0xFF2563EB)
    val PrimaryBlueDark = Color(0xFF1D4ED8)
    val PrimaryBlueLight = Color(0xFF3B82F6)
    val PrimaryBlueBg = Color(0xFFEFF6FF)
    val PrimaryBlueSoft = Color(0xFFDBEAFE)

    // Background System
    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSecondary = Color(0xFFF1F5F9)
    val SurfaceTertiary = Color(0xFFE2E8F0)

    // Borders & Dividers
    val Border = Color(0xFFE2E8F0)
    val Divider = Color(0xFFE2E8F0)

    // Typography
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF475569)
    val TextTertiary = Color(0xFF64748B)
    val TextDisabled = Color(0xFF94A3B8)
    val TextOnPrimary = Color(0xFFFFFFFF)

    // Confidence
    val ConfidenceHigh = Color(0xFF10B981)
    val ConfidenceHighBg = Color(0xFFECFDF5)
    val ConfidenceHighText = Color(0xFF047857)

    val ConfidenceMedium = Color(0xFFF59E0B)
    val ConfidenceMediumBg = Color(0xFFFFFBEB)
    val ConfidenceMediumText = Color(0xFFB45309)

    val ConfidenceLow = Color(0xFFEF4444)
    val ConfidenceLowBg = Color(0xFFFEF2F2)
    val ConfidenceLowText = Color(0xFFB91C1C)

    // Severity
    val SeverityCritical = Color(0xFFDC2626)
    val SeverityCriticalBg = Color(0xFFFEF2F2)
    val SeverityHigh = Color(0xFFEA580C)
    val SeverityHighBg = Color(0xFFFFF7ED)
    val SeverityMedium = Color(0xFFF59E0B)
    val SeverityMediumBg = Color(0xFFFFFBEB)
    val SeverityLow = Color(0xFF64748B)
    val SeverityLowBg = Color(0xFFF1F5F9)
    val SeverityInformational = Color(0xFF2563EB)
    val SeverityInformationalBg = Color(0xFFEFF6FF)

    // Network / P2P
    val NetworkOnline = Color(0xFF10B981)
    val NetworkConnecting = Color(0xFFF59E0B)
    val NetworkOffline = Color(0xFF64748B)
    val NetworkPacket = Color(0xFF06B6D4)
    val MeshActive = Color(0xFF06B6D4)
    val MeshActiveBg = Color(0xFFECFEFF)
    val MeshNode = Color(0xFF0E7490)
    val MeshSending = Color(0xFF0891B2)
    val PeerConnected = Color(0xFF10B981)
    val PeerDisconnected = Color(0xFF64748B)

    // Categories
    val Emergency = Color(0xFFEF4444)
    val EmergencyBg = Color(0xFFFEF2F2)
    val Hazard = Color(0xFFF59E0B)
    val HazardBg = Color(0xFFFFFBEB)
    val Traffic = Color(0xFFF97316)
    val TrafficBg = Color(0xFFFFF7ED)
    val Transport = Color(0xFF2563EB)
    val TransportBg = Color(0xFFEFF6FF)
    val Infrastructure = Color(0xFF7C3AED)
    val InfrastructureBg = Color(0xFFF5F3FF)
    val Weather = Color(0xFF06B6D4)
    val WeatherBg = Color(0xFFECFEFF)
    val Crowd = Color(0xFF8B5CF6)
    val CrowdBg = Color(0xFFF5F3FF)
    val Services = Color(0xFF0F766E)
    val ServicesBg = Color(0xFFF0FDFA)

    // Resolved State
    val Resolved = Color(0xFF10B981)
    val ResolvedBg = Color(0xFFECFDF5)
    val ResolvedText = Color(0xFF047857)

    // Buttons
    val ButtonPrimaryBg = Color(0xFF2563EB)
    val ButtonPrimaryText = Color(0xFFFFFFFF)
    val ButtonPrimaryPressed = Color(0xFF1D4ED8)

    val ButtonSecondaryBg = Color(0xFFFFFFFF)
    val ButtonSecondaryBorder = Color(0xFF2563EB)
    val ButtonSecondaryText = Color(0xFF2563EB)

    val ButtonNeutralBg = Color(0xFFF1F5F9)
    val ButtonNeutralText = Color(0xFF334155)

    val ButtonDestructiveBg = Color(0xFFFEF2F2)
    val ButtonDestructiveText = Color(0xFFB91C1C)
    val ButtonDestructiveBorder = Color(0xFFFECACA)

    // Dark mode
    val DarkBackground = Color(0xFF0F172A)
    val DarkSurface = Color(0xFF1E293B)
    val DarkSurfaceSecondary = Color(0xFF334155)
    val DarkBorder = Color(0xFF475569)
    val DarkPrimaryBlue = Color(0xFF3B82F6)
    val DarkGreen = Color(0xFF34D399)
    val DarkOrange = Color(0xFFFBBF24)
    val DarkRed = Color(0xFFF87171)
    val DarkTextPrimary = Color(0xFFF8FAFC)
    val DarkTextSecondary = Color(0xFFCBD5E1)
}

// Brand colors (Aliased to CrowdWatchColors)
val PrimaryBlue = CrowdWatchColors.PrimaryBlue
val PrimaryBlueDark = CrowdWatchColors.PrimaryBlueDark
val PrimaryBlueLight = CrowdWatchColors.PrimaryBlueLight
val PrimaryContainerLight = CrowdWatchColors.PrimaryBlueBg
val PrimaryContainerDark = Color(0xFF1E3A8A)

val CyanAccent = CrowdWatchColors.NetworkPacket
val TealAccent = CrowdWatchColors.Services

// Semantic colors
val EmergencyRed = CrowdWatchColors.Emergency
val EmergencyRedBg = CrowdWatchColors.EmergencyBg
val HazardOrange = CrowdWatchColors.Hazard
val HazardOrangeBg = CrowdWatchColors.HazardBg
val DisruptionYellow = CrowdWatchColors.ConfidenceMedium
val DisruptionYellowBg = CrowdWatchColors.ConfidenceMediumBg
val TransportBlue = CrowdWatchColors.Transport
val TransportBlueBg = CrowdWatchColors.TransportBg
val InfrastructurePurple = CrowdWatchColors.Infrastructure
val InfrastructurePurpleBg = CrowdWatchColors.InfrastructureBg
val ResolvedGreen = CrowdWatchColors.Resolved
val ResolvedGreenBg = CrowdWatchColors.ResolvedBg

// Category specific colors matching global palette
val CategorySafetyBg = CrowdWatchColors.EmergencyBg
val CategorySafetyIcon = CrowdWatchColors.Emergency

val CategoryTrafficBg = CrowdWatchColors.TrafficBg
val CategoryTrafficIcon = CrowdWatchColors.Traffic

val CategoryWeatherBg = CrowdWatchColors.WeatherBg
val CategoryWeatherIcon = CrowdWatchColors.Weather

val CategoryInfraBg = CrowdWatchColors.InfrastructureBg
val CategoryInfraIcon = CrowdWatchColors.Infrastructure

val CategoryCrowdBg = CrowdWatchColors.CrowdBg
val CategoryCrowdIcon = CrowdWatchColors.Crowd

val CategoryTransportBg = CrowdWatchColors.TransportBg
val CategoryTransportIcon = CrowdWatchColors.Transport

val CategoryServicesBg = CrowdWatchColors.ServicesBg
val CategoryServicesIcon = CrowdWatchColors.Services

val CategoryMedicalBg = CrowdWatchColors.EmergencyBg
val CategoryMedicalIcon = CrowdWatchColors.Emergency

val CategoryUtilitiesBg = CrowdWatchColors.HazardBg
val CategoryUtilitiesIcon = CrowdWatchColors.Hazard

val CategoryOtherBg = CrowdWatchColors.SurfaceSecondary
val CategoryOtherIcon = CrowdWatchColors.TextTertiary

// Neutral tones - Light
val NeutralBgLight = CrowdWatchColors.Surface
val PageBgLight = CrowdWatchColors.Background
val CardBgLight = CrowdWatchColors.Surface
val CardBorderLight = CrowdWatchColors.Border
val TextPrimaryLight = CrowdWatchColors.TextPrimary
val TextSecondaryLight = CrowdWatchColors.TextSecondary
val TextMutedLight = CrowdWatchColors.TextTertiary

// Neutral tones - Dark
val NeutralBgDark = CrowdWatchColors.DarkBackground
val CardBgDark = CrowdWatchColors.DarkSurface
val CardBorderDark = CrowdWatchColors.DarkBorder
val TextPrimaryDark = CrowdWatchColors.DarkTextPrimary
val TextSecondaryDark = CrowdWatchColors.DarkTextSecondary
val TextMutedDark = Color(0xFF94A3B8)



