package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.auth.AuthState
import com.example.crypto.CryptoNodeIdentity
import com.example.data.local.AppDatabase
import com.example.data.model.AlertItem
import com.example.data.model.EventType
import com.example.data.model.IncidentReport
import com.example.data.model.MediaType
import com.example.data.model.NodeState
import com.example.data.model.ReportCategory
import com.example.data.model.ReportCondition
import com.example.data.model.ReportEvent
import com.example.data.model.ReportSeverity
import com.example.data.model.ReportStatus
import com.example.data.model.UserProfile
import com.example.data.repository.ReportRepository
import com.example.engine.AIAnswer
import com.example.engine.AIService
import com.example.location.LocationManager
import com.example.location.TaggedLocation
import com.example.service.LocationService
import com.example.service.IncidentNotificationManager
import com.example.p2p.MeshNetworkStats
import com.example.p2p.MeshPeerNode
import com.example.p2p.model.NodeSyncEvent
import com.example.p2p.sync.PeerSyncEngine
import com.example.p2p.transport.InternetP2PTransport
import com.example.p2p.transport.LocalSimulationTransport
import com.example.p2p.transport.NearbyTransport
import com.example.service.CrowdWatchMeshService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavigationTab {
    LIVE,
    MAP,
    REPORT,
    ALERTS,
    PROFILE,
    NETWORK_SIMULATOR,
    ROUTE_INTELLIGENCE,
    AI_ASSISTANT
}

enum class MapViewMode {
    NEARBY,
    HEATMAP,
    CATEGORY
}

enum class ProximityFilter(val label: String, val maxMeters: Int) {
    ANY("All Distances", Int.MAX_VALUE),
    WITHIN_500M("< 500 m", 500),
    WITHIN_1KM("< 1 km", 1000),
    WITHIN_3KM("< 3 km", 3000),
    WITHIN_5KM("< 5 km", 5000),
    WITHIN_10KM("< 10 km", 10000)
}

enum class VerificationStatusFilter(val label: String) {
    ALL("All Statuses"),
    VERIFIED_ONLY("Verified"),
    ACTIVE_DEVELOPING("Active & Developing"),
    RESOLVED("Resolved"),
    DISPUTED("Disputed")
}

data class IncidentFilterCriteria(
    val query: String,
    val category: ReportCategory?,
    val proximity: ProximityFilter,
    val verification: VerificationStatusFilter,
    val selectedId: String?
)

data class IncidentReportDraft(
    val category: ReportCategory = ReportCategory.TRAFFIC_ROADS,
    val subcategory: String = "Road obstruction",
    val title: String = "Road obstruction",
    val description: String = "",
    val condition: ReportCondition = ReportCondition.HAPPENING_NOW,
    val evidenceType: MediaType = MediaType.PHOTO,
    val evidenceCount: Int = 1,
    val evidenceSampleType: String = "photo",
    val localMediaUri: String? = null,
    val evidenceExifMeta: String? = null,
    val areaName: String = "Current Location",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracyMeters: Float = 0f,
    val severity: ReportSeverity = ReportSeverity.HIGH,
    val initialConfidence: Int = 54
)

data class CrowdWatchUiState(
    val currentTab: NavigationTab = NavigationTab.LIVE,
    val reports: List<IncidentReport> = emptyList(),
    val filteredReports: List<IncidentReport> = emptyList(),
    val userReports: List<IncidentReport> = emptyList(),
    val alerts: List<AlertItem> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val simulatedNodes: List<NodeState> = emptyList(),
    val networkLogs: List<String> = emptyList(),
    val activeSimulationNodeId: String = "local-node",
    val connectedPeers: List<MeshPeerNode> = emptyList(),
    val meshStats: MeshNetworkStats = MeshNetworkStats(),
    val meshLogs: List<String> = emptyList(),
    val isNearbyEnabled: Boolean = true,
    val selectedReport: IncidentReport? = null,
    val searchQuery: String = "",
    val selectedCategoryFilter: ReportCategory? = null,
    val proximityFilter: ProximityFilter = ProximityFilter.ANY,
    val verificationFilter: VerificationStatusFilter = VerificationStatusFilter.ALL,
    val showOnboarding: Boolean = false,
    val onboardingStep: Int = 0,
    val reportCreationStep: Int = 1,
    val reportDraft: IncidentReportDraft = IncidentReportDraft(),
    val aiQuery: String = "",
    val aiAnswer: AIAnswer? = null,
    val isAiThinking: Boolean = false,
    val mapViewMode: MapViewMode = MapViewMode.NEARBY,
    val selectedMapReportId: String? = null,
    val routeFrom: String = "",
    val routeTo: String = "",
    val authState: AuthState = AuthState(),
    val currentTaggedLocation: TaggedLocation? = null,
    val isLoopbackPeerEnabled: Boolean = false
) {
    val activeFiltersCount: Int
        get() = (if (selectedCategoryFilter != null) 1 else 0) +
                (if (proximityFilter != ProximityFilter.ANY) 1 else 0) +
                (if (verificationFilter != VerificationStatusFilter.ALL) 1 else 0)
}

@OptIn(ExperimentalCoroutinesApi::class)
class CrowdWatchViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val myNodeId = CryptoNodeIdentity.getNodeIdentityId()

    // Location & Authentication managers
    val locationService = LocationService(application)
    val locationManager = LocationManager(application)
    val incidentNotificationManager = IncidentNotificationManager(application)
    val authManager = AuthManager(application)
    val authState: StateFlow<AuthState> = authManager.authState
    private val _currentLocation = MutableStateFlow<TaggedLocation?>(null)
    val currentLocation: StateFlow<TaggedLocation?> = _currentLocation.asStateFlow()

    // Distributed synchronization engine & transports
    val syncEngine = PeerSyncEngine(
        reportDao = database.reportDao(),
        eventDao = database.eventDao(),
        alertDao = database.alertDao(),
        myNodeId = myNodeId
    )

    val nearbyTransport = NearbyTransport(application, myNodeId)
    val internetTransport = InternetP2PTransport(myNodeId)
    val simulationTransport = LocalSimulationTransport(myNodeId)

    private val repository = ReportRepository(
        database.reportDao(),
        database.eventDao(),
        database.alertDao()
    )

    private val _currentTab = MutableStateFlow(NavigationTab.LIVE)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryFilter = MutableStateFlow<ReportCategory?>(null)
    private val _proximityFilter = MutableStateFlow(ProximityFilter.ANY)
    private val _verificationFilter = MutableStateFlow(VerificationStatusFilter.ALL)
    private val _selectedReportId = MutableStateFlow<String?>(null)
    private val _showOnboarding = MutableStateFlow(false)
    private val _onboardingStep = MutableStateFlow(0)
    private val _reportCreationStep = MutableStateFlow(1)
    private val _reportDraft = MutableStateFlow(IncidentReportDraft())
    private val _aiQuery = MutableStateFlow("Can I travel through this area?")
    private val _aiAnswer = MutableStateFlow<AIAnswer?>(null)
    private val _isAiThinking = MutableStateFlow(false)
    private val _mapViewMode = MutableStateFlow(MapViewMode.NEARBY)
    private val _selectedMapReportId = MutableStateFlow<String?>(null)
    private val _routeFrom = MutableStateFlow("")
    private val _routeTo = MutableStateFlow("")
    private val _isLoopbackPeerEnabled = MutableStateFlow(false)

    private val _filterParams = combine(
        _searchQuery,
        _selectedCategoryFilter,
        _proximityFilter,
        _verificationFilter,
        _selectedReportId
    ) { q, cat, prox, verif, selId ->
        IncidentFilterCriteria(q, cat, prox, verif, selId)
    }

    private val _reportsAndAlerts = combine(repository.allReports, repository.allAlerts) { rep, al ->
        Pair(rep, al)
    }

    private val _meshState = combine(
        repository.simulatedNodes,
        repository.networkEventLogs,
        repository.userProfile,
        syncEngine.connectedPeers,
        syncEngine.meshStats
    ) { simNodes, simLogs, profile, peers, stats ->
        object {
            val simNodes = simNodes
            val simLogs = simLogs
            val profile = profile
            val peers = peers
            val stats = stats
        }
    }

    val uiState: StateFlow<CrowdWatchUiState> = combine(
        _reportsAndAlerts,
        _meshState,
        syncEngine.meshLogEvents,
        _filterParams,
        _currentTab
    ) { (reports, alerts), meshInfo, meshLogs, criteria, tab ->
        val userLoc = _currentLocation.value
        val filtered = reports.filter { rep ->
            val matchesQuery = criteria.query.isBlank() ||
                rep.title.contains(criteria.query, ignoreCase = true) ||
                rep.description.contains(criteria.query, ignoreCase = true) ||
                rep.areaName.contains(criteria.query, ignoreCase = true) ||
                rep.subcategory.contains(criteria.query, ignoreCase = true)

            val matchesCat = criteria.category == null || rep.category == criteria.category

            val matchesProximity = when (criteria.proximity) {
                ProximityFilter.ANY -> true
                else -> {
                    val dist = if (userLoc != null && userLoc.latitude != 0.0 && userLoc.longitude != 0.0) {
                        calculateDistanceMeters(userLoc.latitude, userLoc.longitude, rep.latitude, rep.longitude)
                    } else {
                        rep.distanceMeters.toDouble()
                    }
                    dist <= criteria.proximity.maxMeters
                }
            }

            val matchesVerification = when (criteria.verification) {
                VerificationStatusFilter.ALL -> true
                VerificationStatusFilter.VERIFIED_ONLY -> rep.status == ReportStatus.VERIFIED || rep.confidenceScore >= 70 || rep.verificationsCount >= 2
                VerificationStatusFilter.ACTIVE_DEVELOPING -> rep.status == ReportStatus.ACTIVE || rep.status == ReportStatus.DEVELOPING
                VerificationStatusFilter.RESOLVED -> rep.status == ReportStatus.RESOLVED
                VerificationStatusFilter.DISPUTED -> rep.status == ReportStatus.DISPUTED
            }

            matchesQuery && matchesCat && matchesProximity && matchesVerification
        }
        val selReport = reports.find { it.id == criteria.selectedId }

        val currentUid = authManager.authState.value.uid
        val userRepList = reports.filter { rep ->
            rep.reporterNodeId == myNodeId ||
            rep.reporterNodeId == meshInfo.profile.nodeId ||
            (currentUid.isNotBlank() && rep.reporterNodeId == currentUid) ||
            rep.reporterNodeId == "local-node"
        }

        CrowdWatchUiState(
            currentTab = tab,
            reports = reports,
            filteredReports = filtered,
            userReports = userRepList,
            alerts = alerts,
            userProfile = meshInfo.profile,
            simulatedNodes = meshInfo.simNodes,
            networkLogs = meshInfo.simLogs,
            activeSimulationNodeId = repository.activeSimulationNodeId.value,
            connectedPeers = meshInfo.peers,
            meshStats = meshInfo.stats,
            meshLogs = meshLogs,
            isNearbyEnabled = meshInfo.stats.isAdvertising || meshInfo.stats.isDiscovering,
            selectedReport = selReport,
            searchQuery = criteria.query,
            selectedCategoryFilter = criteria.category,
            proximityFilter = criteria.proximity,
            verificationFilter = criteria.verification,
            showOnboarding = _showOnboarding.value,
            onboardingStep = _onboardingStep.value,
            reportCreationStep = _reportCreationStep.value,
            reportDraft = _reportDraft.value,
            aiQuery = _aiQuery.value,
            aiAnswer = _aiAnswer.value,
            isAiThinking = _isAiThinking.value,
            mapViewMode = _mapViewMode.value,
            selectedMapReportId = _selectedMapReportId.value,
            routeFrom = _routeFrom.value,
            routeTo = _routeTo.value,
            authState = authManager.authState.value,
            currentTaggedLocation = _currentLocation.value,
            isLoopbackPeerEnabled = _isLoopbackPeerEnabled.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CrowdWatchUiState()
    )

    val selectedReportEvents: StateFlow<List<ReportEvent>> = _selectedReportId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getEventsForReport(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Register transports into the central distributed engine
        syncEngine.registerTransport(nearbyTransport)
        syncEngine.registerTransport(internetTransport)
        syncEngine.registerTransport(simulationTransport)

        // Launch transports
        viewModelScope.launch {
            nearbyTransport.start()
            internetTransport.start()
            simulationTransport.start()
        }

        // Continuously stream location from LocationService to keep current node coordinates fresh
        viewModelScope.launch {
            try {
                val lastLoc = locationService.getLastKnownLocation()
                if (lastLoc != null) {
                    _currentLocation.value = lastLoc
                }
                locationService.observeLocationUpdates(intervalMs = 6000L).collect { loc ->
                    _currentLocation.value = loc
                }
            } catch (e: Exception) {
                // Handled gracefully if location permission is not yet granted
            }
        }

        // Vicinity alerts notification evaluation for incoming and synced high-priority reports
        viewModelScope.launch {
            repository.allReports.collect { list ->
                if (_aiAnswer.value == null && list.isNotEmpty()) {
                    _aiAnswer.value = AIService.answerQuery("Can I travel through this area?", list)
                }
                // Check high priority vicinity reports
                list.forEach { rep ->
                    incidentNotificationManager.evaluateAndNotify(rep, _currentLocation.value)
                }
            }
        }

        // Start background Mesh Foreground Service to guarantee survivability when app is backgrounded
        try {
            CrowdWatchMeshService.start(application)
        } catch (e: Exception) {
            // Service startup fallback
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: ReportCategory?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == category) null else category
    }

    fun setProximityFilter(proximity: ProximityFilter) {
        _proximityFilter.value = proximity
    }

    fun setVerificationFilter(verification: VerificationStatusFilter) {
        _verificationFilter.value = verification
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategoryFilter.value = null
        _proximityFilter.value = ProximityFilter.ANY
        _verificationFilter.value = VerificationStatusFilter.ALL
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    fun selectReport(reportId: String) {
        _selectedReportId.value = reportId
    }

    fun clearSelectedReport() {
        _selectedReportId.value = null
    }

    fun selectMapReport(reportId: String?) {
        _selectedMapReportId.value = reportId
    }

    fun setMapViewMode(mode: MapViewMode) {
        _mapViewMode.value = mode
    }

    fun setRouteEndpoints(from: String, to: String) {
        _routeFrom.value = from
        _routeTo.value = to
    }

    fun setShowOnboarding(show: Boolean) {
        _showOnboarding.value = show
    }

    fun setOnboardingStep(step: Int) {
        _onboardingStep.value = step
    }

    fun nextOnboardingStep() {
        _onboardingStep.value = (_onboardingStep.value + 1).coerceAtMost(3)
    }

    fun prevOnboardingStep() {
        _onboardingStep.value = (_onboardingStep.value - 1).coerceAtLeast(0)
    }

    fun startReportCreation() {
        val loc = _currentLocation.value
        _reportDraft.value = if (loc != null) {
            IncidentReportDraft(
                areaName = loc.areaName,
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = loc.accuracyMeters
            )
        } else {
            IncidentReportDraft()
        }
        viewModelScope.launch {
            val freshLoc = locationService.getCurrentLocation() ?: locationService.getLastKnownLocation()
            if (freshLoc != null) {
                _currentLocation.value = freshLoc
                _reportDraft.value = _reportDraft.value.copy(
                    areaName = freshLoc.areaName,
                    latitude = freshLoc.latitude,
                    longitude = freshLoc.longitude,
                    accuracyMeters = freshLoc.accuracyMeters
                )
            }
        }
        _reportCreationStep.value = 1
        _currentTab.value = NavigationTab.REPORT
    }

    fun setRoutes(from: String, to: String) {
        _routeFrom.value = from
        _routeTo.value = to
    }

    // Report creation wizard actions
    fun updateDraftCategory(category: ReportCategory) {
        val defaultSubcategory = when (category) {
            ReportCategory.SAFETY -> "Hazard / Unsafe condition"
            ReportCategory.TRAFFIC_ROADS -> "Road obstruction"
            ReportCategory.WEATHER_ENV -> "Flooding / Waterlogging"
            ReportCategory.INFRASTRUCTURE -> "Broken street light"
            ReportCategory.CROWD_ACTIVITY -> "Heavy gathering"
            ReportCategory.PUBLIC_TRANSPORT -> "Transit delay"
            ReportCategory.LOCAL_SERVICES -> "Water supply disruption"
            ReportCategory.MEDICAL -> "Medical assistance needed"
            ReportCategory.UTILITIES -> "Power outage"
            ReportCategory.OTHER -> "General incident"
        }
        _reportDraft.value = _reportDraft.value.copy(
            category = category,
            subcategory = defaultSubcategory,
            title = defaultSubcategory
        )
        _reportCreationStep.value = 2
    }

    fun updateDraftSubcategory(subcategory: String) {
        _reportDraft.value = _reportDraft.value.copy(
            subcategory = subcategory,
            title = subcategory
        )
        _reportCreationStep.value = 3
    }

    fun updateDraftEvidence(
        mediaType: MediaType,
        sampleType: String,
        description: String,
        areaName: String,
        latitude: Double? = null,
        longitude: Double? = null,
        accuracyMeters: Float? = null,
        localUri: String? = null,
        exifMeta: String? = null
    ) {
        _reportDraft.value = _reportDraft.value.copy(
            evidenceType = mediaType,
            evidenceSampleType = sampleType,
            description = description,
            areaName = areaName,
            latitude = latitude ?: _reportDraft.value.latitude,
            longitude = longitude ?: _reportDraft.value.longitude,
            accuracyMeters = accuracyMeters ?: _reportDraft.value.accuracyMeters,
            localMediaUri = localUri ?: _reportDraft.value.localMediaUri,
            evidenceExifMeta = exifMeta ?: _reportDraft.value.evidenceExifMeta
        )
        _reportCreationStep.value = 4
    }

    fun tagDraftLocation(location: TaggedLocation) {
        _currentLocation.value = location
        _reportDraft.value = locationService.tagReportDraft(_reportDraft.value, location)
    }

    fun updateDraftCondition(condition: ReportCondition) {
        _reportDraft.value = _reportDraft.value.copy(
            condition = condition,
            initialConfidence = if (condition == ReportCondition.HAPPENING_NOW) 54 else 42
        )
        _reportCreationStep.value = 5
    }

    fun setReportCreationStep(step: Int) {
        _reportCreationStep.value = step.coerceIn(1, 5)
    }

    fun publishDraftReport() {
        viewModelScope.launch {
            val draft = _reportDraft.value
            val now = System.currentTimeMillis()
            val newReportId = "rep-${UUID.randomUUID().toString().take(6)}"

            val creationEvent = NodeSyncEvent(
                eventId = "evt-${UUID.randomUUID().toString().take(8)}",
                reportId = newReportId,
                originNodeId = myNodeId,
                authorNodeId = myNodeId,
                timestamp = now,
                eventType = EventType.REPORT_CREATED,
                payload = draft.description.ifBlank { "${draft.subcategory} observed near ${draft.areaName}." },
                evidenceUri = draft.localMediaUri,
                version = 1,
                title = draft.title,
                description = draft.description,
                category = draft.category,
                subcategory = draft.subcategory,
                latitude = draft.latitude,
                longitude = draft.longitude,
                accuracyMeters = draft.accuracyMeters,
                areaName = draft.areaName,
                severity = draft.severity,
                condition = draft.condition,
                evidenceType = draft.evidenceType,
                evidenceSampleType = draft.evidenceSampleType
            )

            syncEngine.publishLocalEvent(creationEvent)

            // Evaluate if this new report warrants a vicinity alert notification
            val createdReport = IncidentReport(
                id = newReportId,
                category = draft.category,
                subcategory = draft.subcategory,
                title = draft.title,
                description = draft.description,
                latitude = draft.latitude,
                longitude = draft.longitude,
                accuracyMeters = draft.accuracyMeters,
                areaName = draft.areaName,
                createdAt = now,
                updatedAt = now,
                status = ReportStatus.ACTIVE,
                severity = draft.severity,
                confidenceScore = draft.initialConfidence,
                reporterNodeId = myNodeId,
                evidenceCount = draft.evidenceCount,
                evidenceType = draft.evidenceType,
                evidenceSampleType = draft.evidenceSampleType,
                condition = draft.condition
            )
            incidentNotificationManager.evaluateAndNotify(createdReport, _currentLocation.value)

            _selectedReportId.value = newReportId
            _currentTab.value = NavigationTab.LIVE
            _reportCreationStep.value = 1
        }
    }

    /**
     * Tags the current report draft with real-time geographical data using LocationService.
     */
    fun tagDraftWithRealTimeLocation(onLocationTagged: ((TaggedLocation) -> Unit)? = null) {
        viewModelScope.launch {
            val loc = locationService.getCurrentLocation() ?: locationService.getLastKnownLocation()
            if (loc != null) {
                _currentLocation.value = loc
                _reportDraft.value = locationService.tagReportDraft(_reportDraft.value, loc)
                onLocationTagged?.invoke(loc)
            }
        }
    }

    /**
     * Re-queries LocationService to refresh the device's current location coordinates.
     */
    fun refreshCurrentLocation(onRefreshed: ((TaggedLocation) -> Unit)? = null) {
        viewModelScope.launch {
            val loc = locationService.getCurrentLocation() ?: locationService.getLastKnownLocation()
            if (loc != null) {
                _currentLocation.value = loc
                onRefreshed?.invoke(loc)
            }
        }
    }

    /**
     * Attaches photos or videos captured from CameraCaptureScreen to the report draft.
     */
    fun attachCapturedMedia(uri: String, isVideo: Boolean, exifMeta: String? = null) {
        val mediaType = if (isVideo) MediaType.VIDEO else MediaType.PHOTO
        val sampleType = if (isVideo) "video" else "photo"
        _reportDraft.value = _reportDraft.value.copy(
            evidenceType = mediaType,
            evidenceSampleType = sampleType,
            localMediaUri = uri,
            evidenceExifMeta = exifMeta ?: "CameraX Attached (${if (isVideo) "MP4 Video" else "High-Res JPEG"})"
        )
    }

    /**
     * Authenticates the user with Google via Credential Manager & Firebase Auth.
     */
    fun signInWithGoogle(webClientId: String? = null) {
        viewModelScope.launch {
            authManager.signInWithGoogle(webClientId)
        }
    }

    /**
     * Authenticates with Email & Password.
     */
    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            authManager.signInWithEmail(email, pass)
        }
    }

    /**
     * Registers a new account with Email & Password.
     */
    fun signUpWithEmail(email: String, pass: String, displayName: String) {
        viewModelScope.launch {
            authManager.signUpWithEmail(email, pass, displayName)
        }
    }

    /**
     * Authenticates as an anonymous node via Firebase Auth.
     */
    fun signInAnonymously(customDisplayName: String? = null) {
        viewModelScope.launch {
            authManager.signInAnonymously(customDisplayName)
        }
    }

    /**
     * Signs out the user from Firebase Auth and clears Credential Manager credentials.
     */
    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
        }
    }

    // Verification actions
    fun confirmReport(reportId: String) {
        viewModelScope.launch {
            val event = NodeSyncEvent(
                eventId = "evt-${UUID.randomUUID().toString().take(8)}",
                reportId = reportId,
                originNodeId = reportId,
                authorNodeId = myNodeId,
                eventType = EventType.REPORT_CONFIRMED,
                timestamp = System.currentTimeMillis(),
                payload = "Report confirmed by local node via P2P mesh",
                version = 2
            )
            syncEngine.publishLocalEvent(event)
        }
    }

    fun disputeReport(reportId: String, reason: String = "Inaccurate or no longer present") {
        viewModelScope.launch {
            val event = NodeSyncEvent(
                eventId = "evt-${UUID.randomUUID().toString().take(8)}",
                reportId = reportId,
                originNodeId = reportId,
                authorNodeId = myNodeId,
                eventType = EventType.REPORT_DISPUTED,
                timestamp = System.currentTimeMillis(),
                payload = "Disputed: $reason",
                version = 2
            )
            syncEngine.publishLocalEvent(event)
        }
    }

    fun markStillHappening(reportId: String) {
        viewModelScope.launch {
            val event = NodeSyncEvent(
                eventId = "evt-${UUID.randomUUID().toString().take(8)}",
                reportId = reportId,
                originNodeId = reportId,
                authorNodeId = myNodeId,
                eventType = EventType.STILL_HAPPENING,
                timestamp = System.currentTimeMillis(),
                payload = "Still happening confirmed by node observation",
                version = 2
            )
            syncEngine.publishLocalEvent(event)
        }
    }

    fun resolveReport(reportId: String) {
        viewModelScope.launch {
            val event = NodeSyncEvent(
                eventId = "evt-${UUID.randomUUID().toString().take(8)}",
                reportId = reportId,
                originNodeId = reportId,
                authorNodeId = myNodeId,
                eventType = EventType.REPORT_RESOLVED,
                timestamp = System.currentTimeMillis(),
                payload = "Issue cleared from physical site",
                version = 3
            )
            syncEngine.publishLocalEvent(event)
        }
    }

    fun addEvidence(reportId: String, mediaType: MediaType = MediaType.PHOTO, label: String = "Photo evidence") {
        viewModelScope.launch {
            val event = NodeSyncEvent(
                eventId = "evt-${UUID.randomUUID().toString().take(8)}",
                reportId = reportId,
                originNodeId = reportId,
                authorNodeId = myNodeId,
                eventType = EventType.EVIDENCE_ADDED,
                timestamp = System.currentTimeMillis(),
                payload = "Evidence ($label) attached by local observing node",
                evidenceType = mediaType,
                evidenceSampleType = "photo",
                version = 2
            )
            syncEngine.publishLocalEvent(event)
        }
    }

    fun markAlertAsRead(alertId: String) {
        viewModelScope.launch {
            repository.markAlertAsRead(alertId)
        }
    }

    // P2P Nearby Mesh Controls
    fun startNearbyMesh() {
        viewModelScope.launch {
            nearbyTransport.start()
        }
    }

    fun stopNearbyMesh() {
        viewModelScope.launch {
            nearbyTransport.stop()
        }
    }

    fun toggleMeshPeer(nodeId: String) {
        syncEngine.togglePeerOnline(nodeId)
    }

    fun broadcastSyncRequest() {
        viewModelScope.launch {
            syncEngine.requestFullSync()
            repository.syncAllNodes()
        }
    }

    fun toggleLoopbackPeer(enabled: Boolean) {
        _isLoopbackPeerEnabled.value = enabled
        simulationTransport.setLoopbackBridgeEnabled(enabled)
    }

    // AI reasoning
    fun askAi(query: String) {
        _aiQuery.value = query
        _isAiThinking.value = true
        viewModelScope.launch {
            val answer = AIService.answerQuery(query, uiState.value.reports)
            _aiAnswer.value = answer
            _isAiThinking.value = false
        }
    }

    // Network Simulator
    fun switchSimulationNode(nodeId: String) {
        repository.setActiveSimulationNode(nodeId)
    }

    fun toggleNodeOnline(nodeId: String) {
        repository.toggleNodeOnline(nodeId)
    }

    fun syncAllNodes() {
        viewModelScope.launch {
            syncEngine.requestFullSync()
            syncEngine.requestInternetSync()
            repository.syncAllNodes()
        }
    }

    fun syncCloudMesh() {
        viewModelScope.launch {
            syncEngine.requestInternetSync()
        }
    }

    fun updatePrivacySettings(precision: String, shareEvidence: Boolean, retentionDays: Int) {
        repository.updatePrivacySettings(precision, shareEvidence, retentionDays)
    }

    fun clearLocalCache() {
        viewModelScope.launch {
            repository.clearAllLocalData()
        }
    }
}
