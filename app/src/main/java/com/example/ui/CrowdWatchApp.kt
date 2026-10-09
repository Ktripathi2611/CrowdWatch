package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.CrowdWatchBottomNav
import com.example.ui.screens.AIAssistantScreen
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.NetworkSimulatorScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReportCreationScreen
import com.example.ui.screens.ReportDetailScreen
import com.example.ui.screens.RouteIntelligenceScreen
import com.example.ui.viewmodel.CrowdWatchViewModel
import com.example.ui.viewmodel.NavigationTab

object CrowdWatchRoutes {
    const val FEED = "feed"
    const val REPORT_SUBMISSION = "report_submission"
    const val USER_PROFILE = "profile"
    const val CAMERA_CAPTURE = "camera_capture"
    const val MAP = "map"
    const val ALERTS = "alerts"
    const val REPORT_DETAIL = "report_detail"
    const val NETWORK_SIMULATOR = "network_simulator"
    const val ROUTE_INTELLIGENCE = "route_intelligence"
    const val AI_ASSISTANT = "ai_assistant"
}

@Composable
fun CrowdWatchApp(
    viewModel: CrowdWatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: CrowdWatchRoutes.FEED

    // Map current route to navigation tab for bottom nav selection state
    val activeNavTab = when (currentRoute) {
        CrowdWatchRoutes.FEED -> NavigationTab.LIVE
        CrowdWatchRoutes.MAP -> NavigationTab.MAP
        CrowdWatchRoutes.REPORT_SUBMISSION -> NavigationTab.REPORT
        CrowdWatchRoutes.ALERTS -> NavigationTab.ALERTS
        CrowdWatchRoutes.USER_PROFILE -> NavigationTab.PROFILE
        CrowdWatchRoutes.NETWORK_SIMULATOR -> NavigationTab.NETWORK_SIMULATOR
        CrowdWatchRoutes.ROUTE_INTELLIGENCE -> NavigationTab.ROUTE_INTELLIGENCE
        CrowdWatchRoutes.AI_ASSISTANT -> NavigationTab.AI_ASSISTANT
        else -> NavigationTab.LIVE
    }

    // Top-level back handler
    BackHandler(enabled = currentRoute != CrowdWatchRoutes.FEED || uiState.showOnboarding) {
        when {
            uiState.showOnboarding -> viewModel.setShowOnboarding(false)
            navController.previousBackStackEntry != null -> navController.popBackStack()
            else -> navController.navigate(CrowdWatchRoutes.FEED) {
                popUpTo(CrowdWatchRoutes.FEED) { inclusive = true }
            }
        }
    }

    if (uiState.showOnboarding) {
        OnboardingScreen(
            currentStep = uiState.onboardingStep,
            onNextStep = { viewModel.nextOnboardingStep() },
            onPrevStep = { viewModel.prevOnboardingStep() },
            onDismiss = { viewModel.setShowOnboarding(false) }
        )
        return
    }

    val showBottomBar = currentRoute in listOf(
        CrowdWatchRoutes.FEED,
        CrowdWatchRoutes.MAP,
        CrowdWatchRoutes.ALERTS,
        CrowdWatchRoutes.USER_PROFILE
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CrowdWatchBottomNav(
                    currentTab = activeNavTab,
                    onTabSelected = { tab ->
                        val targetRoute = when (tab) {
                            NavigationTab.LIVE -> CrowdWatchRoutes.FEED
                            NavigationTab.MAP -> CrowdWatchRoutes.MAP
                            NavigationTab.REPORT -> CrowdWatchRoutes.REPORT_SUBMISSION
                            NavigationTab.ALERTS -> CrowdWatchRoutes.ALERTS
                            NavigationTab.PROFILE -> CrowdWatchRoutes.USER_PROFILE
                            NavigationTab.NETWORK_SIMULATOR -> CrowdWatchRoutes.NETWORK_SIMULATOR
                            NavigationTab.ROUTE_INTELLIGENCE -> CrowdWatchRoutes.ROUTE_INTELLIGENCE
                            NavigationTab.AI_ASSISTANT -> CrowdWatchRoutes.AI_ASSISTANT
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(CrowdWatchRoutes.FEED) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onReportClick = {
                        viewModel.startReportCreation()
                        navController.navigate(CrowdWatchRoutes.REPORT_SUBMISSION)
                    },
                    unreadAlertsCount = uiState.alerts.count { !it.isRead }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = CrowdWatchRoutes.FEED,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Report Feed Screen (Home)
                composable(CrowdWatchRoutes.FEED) {
                    HomeScreen(
                        reports = uiState.filteredReports,
                        searchQuery = uiState.searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        selectedCategory = uiState.selectedCategoryFilter,
                        onCategorySelected = { viewModel.setCategoryFilter(it) },
                        proximityFilter = uiState.proximityFilter,
                        onProximitySelected = { viewModel.setProximityFilter(it) },
                        verificationFilter = uiState.verificationFilter,
                        onVerificationSelected = { viewModel.setVerificationFilter(it) },
                        onResetFilters = { viewModel.resetFilters() },
                        onReportClick = { rep ->
                            viewModel.selectReport(rep)
                            navController.navigate(CrowdWatchRoutes.REPORT_DETAIL)
                        },
                        onNavigateTab = { tab ->
                            when (tab) {
                                NavigationTab.LIVE -> navController.navigate(CrowdWatchRoutes.FEED)
                                NavigationTab.MAP -> navController.navigate(CrowdWatchRoutes.MAP)
                                NavigationTab.REPORT -> {
                                    viewModel.startReportCreation()
                                    navController.navigate(CrowdWatchRoutes.REPORT_SUBMISSION)
                                }
                                NavigationTab.ALERTS -> navController.navigate(CrowdWatchRoutes.ALERTS)
                                NavigationTab.PROFILE -> navController.navigate(CrowdWatchRoutes.USER_PROFILE)
                                NavigationTab.NETWORK_SIMULATOR -> navController.navigate(CrowdWatchRoutes.NETWORK_SIMULATOR)
                                NavigationTab.ROUTE_INTELLIGENCE -> navController.navigate(CrowdWatchRoutes.ROUTE_INTELLIGENCE)
                                NavigationTab.AI_ASSISTANT -> navController.navigate(CrowdWatchRoutes.AI_ASSISTANT)
                            }
                        },
                        onAlertsClick = { navController.navigate(CrowdWatchRoutes.ALERTS) },
                        onProfileClick = { navController.navigate(CrowdWatchRoutes.USER_PROFILE) },
                        onNetworkClick = { navController.navigate(CrowdWatchRoutes.NETWORK_SIMULATOR) },
                        activePeerCount = uiState.connectedPeers.size,
                        isNearbyActive = uiState.isNearbyEnabled,
                        areaName = currentLocation?.areaName ?: "Local Vicinity",
                        hasUnreadAlerts = uiState.alerts.any { !it.isRead }
                    )
                }

                // 2. Report Submission Form Screen
                composable(CrowdWatchRoutes.REPORT_SUBMISSION) {
                    ReportCreationScreen(
                        currentStep = uiState.reportCreationStep,
                        draft = uiState.reportDraft,
                        onCategorySelected = { viewModel.updateDraftCategory(it) },
                        onSubcategorySelected = { viewModel.updateDraftSubcategory(it) },
                        onEvidenceUpdated = { media, sample, desc, area ->
                            viewModel.updateDraftEvidence(media, sample, desc, area)
                        },
                        onLocationTagged = { location ->
                            viewModel.tagDraftLocation(location)
                        },
                        onConditionSelected = { viewModel.updateDraftCondition(it) },
                        onStepChange = { viewModel.setReportCreationStep(it) },
                        onPublish = {
                            viewModel.publishDraftReport()
                            navController.navigate(CrowdWatchRoutes.FEED) {
                                popUpTo(CrowdWatchRoutes.FEED) { inclusive = true }
                            }
                        },
                        onCancel = {
                            if (!navController.popBackStack()) {
                                navController.navigate(CrowdWatchRoutes.FEED)
                            }
                        }
                    )
                }

                // 3. User Profile Screen
                composable(CrowdWatchRoutes.USER_PROFILE) {
                    ProfileScreen(
                        profile = uiState.userProfile,
                        authState = authState,
                        userReports = uiState.userReports,
                        onReportClick = { repId ->
                            viewModel.selectReport(repId)
                            navController.navigate(CrowdWatchRoutes.REPORT_DETAIL)
                        },
                        onCreateReportClick = {
                            viewModel.startReportCreation()
                            navController.navigate(CrowdWatchRoutes.REPORT_SUBMISSION)
                        },
                        onSignInGoogle = { viewModel.signInWithGoogle() },
                        onSignInAnonymous = { viewModel.signInAnonymously() },
                        onSignInEmail = { email, pass -> viewModel.signInWithEmail(email, pass) },
                        onSignUpEmail = { email, pass, name -> viewModel.signUpWithEmail(email, pass, name) },
                        onSignOut = { viewModel.signOut() },
                        onShowOnboarding = { viewModel.setShowOnboarding(true) },
                        onOpenNetworkSimulator = { navController.navigate(CrowdWatchRoutes.NETWORK_SIMULATOR) },
                        onClearCache = { viewModel.clearLocalCache() },
                        onUpdatePrivacy = { prec, share, days ->
                            viewModel.updatePrivacySettings(prec, share, days)
                        }
                    )
                }

                // 4. CameraX Capture Screen
                composable(CrowdWatchRoutes.CAMERA_CAPTURE) {
                    CameraCaptureScreen(
                        onMediaCaptured = { uri, isVideo ->
                            viewModel.attachCapturedMedia(uri.toString(), isVideo)
                            navController.popBackStack()
                        },
                        onClose = { navController.popBackStack() }
                    )
                }

                // 5. Interactive Map Screen
                composable(CrowdWatchRoutes.MAP) {
                    MapScreen(
                        reports = uiState.filteredReports,
                        selectedReportId = uiState.selectedMapReportId,
                        onSelectReport = { viewModel.selectMapReport(it) },
                        onViewReportDetails = { rep ->
                            viewModel.selectReport(rep)
                            navController.navigate(CrowdWatchRoutes.REPORT_DETAIL)
                        },
                        onConfirmReport = { viewModel.confirmReport(it) },
                        mapViewMode = uiState.mapViewMode,
                        onMapViewModeChange = { viewModel.setMapViewMode(it) },
                        selectedCategory = uiState.selectedCategoryFilter,
                        onCategorySelected = { viewModel.setCategoryFilter(it) },
                        userLocation = currentLocation,
                        onRefreshLocation = { viewModel.refreshCurrentLocation() }
                    )
                }

                // 6. Community Alerts Screen
                composable(CrowdWatchRoutes.ALERTS) {
                    AlertsScreen(
                        alerts = uiState.alerts,
                        onAlertClick = { repId ->
                            viewModel.markAlertAsRead(repId)
                            viewModel.selectReport(repId)
                            navController.navigate(CrowdWatchRoutes.REPORT_DETAIL)
                        }
                    )
                }

                // 7. Incident Report Detail Screen
                composable(CrowdWatchRoutes.REPORT_DETAIL) {
                    val report = uiState.selectedReport
                    val reportEvents by viewModel.selectedReportEvents.collectAsState()

                    if (report != null) {
                        ReportDetailScreen(
                            report = report,
                            events = reportEvents,
                            onBack = {
                                viewModel.clearSelectedReport()
                                navController.popBackStack()
                            },
                            onConfirm = { viewModel.confirmReport(report.id) },
                            onDispute = { reason -> viewModel.disputeReport(report.id, reason) },
                            onStillHappening = { viewModel.markStillHappening(report.id) },
                            onResolve = { viewModel.resolveReport(report.id) },
                            onAddEvidence = { media, label -> viewModel.addEvidence(report.id, media, label) }
                        )
                    } else {
                        navController.popBackStack()
                    }
                }

                // 8. P2P Mesh Network Simulator Screen
                composable(CrowdWatchRoutes.NETWORK_SIMULATOR) {
                    NetworkSimulatorScreen(
                        nodes = uiState.simulatedNodes,
                        activeNodeId = uiState.activeSimulationNodeId,
                        logs = if (uiState.meshLogs.isNotEmpty()) uiState.meshLogs else uiState.networkLogs,
                        connectedPeers = uiState.connectedPeers,
                        meshStats = uiState.meshStats,
                        isNearbyMeshEnabled = uiState.isNearbyEnabled,
                        isLoopbackBridgeEnabled = uiState.isLoopbackPeerEnabled,
                        onToggleLoopbackBridge = { viewModel.toggleLoopbackPeer(it) },
                        onBack = { navController.popBackStack() },
                        onSelectActiveNode = { viewModel.switchSimulationNode(it) },
                        onToggleNodeOnline = { viewModel.toggleNodeOnline(it) },
                        onSyncAll = { viewModel.broadcastSyncRequest() },
                        onStartNearbyMesh = { viewModel.startNearbyMesh() },
                        onStopNearbyMesh = { viewModel.stopNearbyMesh() },
                        onToggleMeshPeer = { viewModel.toggleMeshPeer(it) }
                    )
                }

                // 9. Route Intelligence Screen
                composable(CrowdWatchRoutes.ROUTE_INTELLIGENCE) {
                    RouteIntelligenceScreen(
                        fromLocation = uiState.routeFrom,
                        toLocation = uiState.routeTo,
                        onLocationsChange = { from, to -> viewModel.setRoutes(from, to) },
                        reports = uiState.reports,
                        onReportClick = { rep ->
                            viewModel.selectReport(rep)
                            navController.navigate(CrowdWatchRoutes.REPORT_DETAIL)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // 10. AI Assistant Screen
                composable(CrowdWatchRoutes.AI_ASSISTANT) {
                    AIAssistantScreen(
                        aiAnswer = uiState.aiAnswer,
                        isThinking = uiState.isAiThinking,
                        onAsk = { viewModel.askAi(it) },
                        onReportClick = { rep ->
                            viewModel.selectReport(rep)
                            navController.navigate(CrowdWatchRoutes.REPORT_DETAIL)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
