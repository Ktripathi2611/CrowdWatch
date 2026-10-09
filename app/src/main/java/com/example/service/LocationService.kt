package com.example.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.model.IncidentReport
import com.example.ui.viewmodel.IncidentReportDraft
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Accuracy tier classification for the intelligence platform.
 */
enum class LocationAccuracyTier {
    HIGH_PRECISION,   // <= 15m
    MEDIUM_PRECISION, // 16m - 50m
    LOW_PRECISION     // > 50m
}

/**
 * Real-time geographical location data model for tagging reports in the intelligence platform.
 */
data class TaggedLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 5.0f,
    val altitudeMeters: Double? = null,
    val speedMps: Float? = null,
    val bearingDegrees: Float? = null,
    val areaName: String = "Locating...",
    val streetAddress: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRealTime: Boolean = true,
    val provider: String = "gps_fused"
) {
    val accuracyTier: LocationAccuracyTier
        get() = when {
            accuracyMeters <= 15f -> LocationAccuracyTier.HIGH_PRECISION
            accuracyMeters <= 50f -> LocationAccuracyTier.MEDIUM_PRECISION
            else -> LocationAccuracyTier.LOW_PRECISION
        }

    fun toFormattedCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lngDir = if (longitude >= 0) "E" else "W"
        return String.format(
            Locale.US,
            "%.5f° %s, %.5f° %s (±%.0fm)",
            kotlin.math.abs(latitude),
            latDir,
            kotlin.math.abs(longitude),
            lngDir,
            accuracyMeters
        )
    }

    fun toDecimalString(): String {
        return String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
    }

    fun getAccuracyClassification(): String {
        return when (accuracyTier) {
            LocationAccuracyTier.HIGH_PRECISION -> "High Precision (±${accuracyMeters.toInt()}m)"
            LocationAccuracyTier.MEDIUM_PRECISION -> "Medium Precision (±${accuracyMeters.toInt()}m)"
            LocationAccuracyTier.LOW_PRECISION -> "Approximate (±${accuracyMeters.toInt()}m)"
        }
    }

    fun getConfidenceBoost(): Int {
        return when (accuracyTier) {
            LocationAccuracyTier.HIGH_PRECISION -> 15
            LocationAccuracyTier.MEDIUM_PRECISION -> 10
            LocationAccuracyTier.LOW_PRECISION -> 5
        }
    }
}

/**
 * Backward compatibility alias for existing callers
 */
typealias DeviceLocation = TaggedLocation

/**
 * LocationService:
 * Core location service utilizing Google Play Services FusedLocationProviderClient
 * to tag civic incident reports with high-accuracy real-time geographical coordinates,
 * reverse-geocoded landmarks, and intelligence metrics for the CrowdWatch intelligence platform.
 */
class LocationService(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Verifies if location permissions (FINE or COARSE) are granted.
     */
    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    /**
     * Fetches the cached last known location from Google Play Services.
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): TaggedLocation? = suspendCancellableCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        try {
            fusedClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        cont.resume(mapAndroidLocationToTagged(location))
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        } catch (e: Exception) {
            cont.resume(null)
        }
    }

    /**
     * Requests a fresh high-accuracy real-time location fix from Google Play Services.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(
        highAccuracy: Boolean = true,
        timeoutMs: Long = 10000L
    ): TaggedLocation? = suspendCancellableCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        val priority = if (highAccuracy) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        val cancellationTokenSource = CancellationTokenSource()

        cont.invokeOnCancellation {
            cancellationTokenSource.cancel()
        }

        try {
            fusedClient.getCurrentLocation(priority, cancellationTokenSource.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        cont.resume(mapAndroidLocationToTagged(location))
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        } catch (e: Exception) {
            cont.resume(null)
        }
    }

    /**
     * Returns a real-time Flow of continuous location updates from Google Play Services.
     */
    @SuppressLint("MissingPermission")
    fun observeLocationUpdates(
        intervalMs: Long = 8000L,
        fastestIntervalMs: Long = 4000L,
        minDistanceMeters: Float = 5f
    ): Flow<TaggedLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(fastestIntervalMs)
            .setMinUpdateDistanceMeters(minDistanceMeters)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(mapAndroidLocationToTagged(loc))
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    /**
     * Tags an IncidentReportDraft with real-time GPS coordinates, area name, and accuracy.
     */
    fun tagReportDraft(draft: IncidentReportDraft, location: TaggedLocation): IncidentReportDraft {
        return draft.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracyMeters,
            areaName = location.areaName,
            initialConfidence = (draft.initialConfidence + location.getConfidenceBoost()).coerceIn(0, 99)
        )
    }

    /**
     * Tags an IncidentReport with real-time GPS coordinates, area name, and accuracy.
     */
    fun tagIncidentReport(report: IncidentReport, location: TaggedLocation): IncidentReport {
        return report.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracyMeters,
            areaName = location.areaName
        )
    }

    /**
     * Maps an Android framework Location to a domain TaggedLocation with reverse-geocoded area name.
     */
    private fun mapAndroidLocationToTagged(location: Location): TaggedLocation {
        val area = reverseGeocode(location.latitude, location.longitude)
        return TaggedLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else 8.0f,
            altitudeMeters = if (location.hasAltitude()) location.altitude else null,
            speedMps = if (location.hasSpeed()) location.speed else null,
            bearingDegrees = if (location.hasBearing()) location.bearing else null,
            areaName = area,
            timestamp = if (location.time > 0) location.time else System.currentTimeMillis(),
            isRealTime = true,
            provider = location.provider ?: "gps_fused"
        )
    }

    /**
     * Reverse geocodes coordinates into a human-readable civic street and locality name.
     */
    fun reverseGeocode(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                formatAddress(addr, lat, lng)
            } else {
                formatDefaultCoordinate(lat, lng)
            }
        } catch (e: Exception) {
            formatDefaultCoordinate(lat, lng)
        }
    }

    private fun formatAddress(addr: Address, lat: Double, lng: Double): String {
        val thoroughfare = addr.thoroughfare
        val subLocality = addr.subLocality
        val locality = addr.locality ?: addr.subAdminArea

        return when {
            thoroughfare != null && subLocality != null -> "$thoroughfare, $subLocality"
            subLocality != null && locality != null && subLocality != locality -> "$subLocality, $locality"
            thoroughfare != null -> thoroughfare
            subLocality != null -> subLocality
            locality != null -> locality
            addr.featureName != null -> addr.featureName
            else -> formatDefaultCoordinate(lat, lng)
        }
    }

    private fun formatDefaultCoordinate(lat: Double, lng: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lngDir = if (lng >= 0) "E" else "W"
        return String.format(Locale.US, "%.4f° %s, %.4f° %s", kotlin.math.abs(lat), latDir, kotlin.math.abs(lng), lngDir)
    }

    companion object {
        /**
         * Computes geodesic distance in meters between two geographical points.
         */
        fun computeDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
            val results = FloatArray(1)
            Location.distanceBetween(lat1, lon1, lat2, lon2, results)
            return results[0].toInt()
        }

        /**
         * Calculates initial bearing in degrees from point 1 to point 2.
         */
        fun computeBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            val results = FloatArray(2)
            Location.distanceBetween(lat1, lon1, lat2, lon2, results)
            return if (results.size > 1) results[1] else 0f
        }

        /**
         * Converts bearing angle in degrees to compass cardinal direction string (e.g. N, NE, E, SE, S, SW, W, NW).
         */
        fun getBearingDirection(bearing: Float): String {
            val normalized = (bearing % 360 + 360) % 360
            return when {
                normalized >= 337.5 || normalized < 22.5 -> "N"
                normalized < 67.5 -> "NE"
                normalized < 112.5 -> "E"
                normalized < 157.5 -> "SE"
                normalized < 202.5 -> "S"
                normalized < 247.5 -> "SW"
                normalized < 292.5 -> "W"
                else -> "NW"
            }
        }

        /**
         * Checks if point 2 is within radiusMeters of point 1.
         */
        fun isWithinRadius(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double,
            radiusMeters: Double
        ): Boolean {
            return computeDistanceMeters(lat1, lon1, lat2, lon2) <= radiusMeters
        }

        /**
         * Filters reports that fall within a specified geographic radius for spatial intelligence.
         */
        fun filterReportsByIntelligenceRadius(
            reports: List<IncidentReport>,
            centerLat: Double,
            centerLon: Double,
            radiusMeters: Double = 1500.0
        ): List<IncidentReport> {
            return reports.filter { report ->
                isWithinRadius(centerLat, centerLon, report.latitude, report.longitude, radiusMeters)
            }.sortedBy { report ->
                computeDistanceMeters(centerLat, centerLon, report.latitude, report.longitude)
            }
        }

        /**
         * Generates a simple GeoHash representation for spatial indexing in the intelligence platform.
         */
        fun generateGeohash(lat: Double, lon: Double, precision: Int = 7): String {
            val base32 = "0123456789bcdefghjkmnpqrstuvwxyz"
            var minLat = -90.0
            var maxLat = 90.0
            var minLon = -180.0
            var maxLon = 180.0
            var isEven = true
            var bit = 0
            var ch = 0
            val geohash = StringBuilder()

            while (geohash.length < precision) {
                if (isEven) {
                    val mid = (minLon + maxLon) / 2
                    if (lon > mid) {
                        ch = ch or (1 shl (4 - bit))
                        minLon = mid
                    } else {
                        maxLon = mid
                    }
                } else {
                    val mid = (minLat + maxLat) / 2
                    if (lat > mid) {
                        ch = ch or (1 shl (4 - bit))
                        minLat = mid
                    } else {
                        maxLat = mid
                    }
                }

                isEven = !isEven
                if (bit < 4) {
                    bit++
                } else {
                    geohash.append(base32[ch])
                    bit = 0
                    ch = 0
                }
            }
            return geohash.toString()
        }
    }
}
