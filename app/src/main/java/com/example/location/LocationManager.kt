package com.example.location

import android.content.Context
import com.example.service.LocationService
import com.example.ui.viewmodel.IncidentReportDraft
import kotlinx.coroutines.flow.Flow

/**
 * TaggedLocation alias pointing to canonical LocationService TaggedLocation
 */
typealias TaggedLocation = com.example.service.TaggedLocation

/**
 * LocationManager: Compatibility facade delegating to LocationService
 * using Google Play Services FusedLocationProviderClient to tag civic reports with real-time coordinates.
 */
class LocationManager(context: Context) {
    val service = LocationService(context)

    fun hasLocationPermission(): Boolean = service.hasLocationPermission()

    suspend fun getLastKnownLocation(): TaggedLocation? = service.getLastKnownLocation()

    suspend fun getCurrentLocation(timeoutMs: Long = 10000L): TaggedLocation? =
        service.getCurrentLocation(highAccuracy = true, timeoutMs = timeoutMs)

    fun getLocationUpdates(intervalMs: Long = 8000L): Flow<TaggedLocation> =
        service.observeLocationUpdates(intervalMs = intervalMs)

    fun tagReportDraft(draft: IncidentReportDraft, location: TaggedLocation): IncidentReportDraft =
        service.tagReportDraft(draft, location)

    fun reverseGeocode(lat: Double, lng: Double): String =
        service.reverseGeocode(lat, lng)
}
