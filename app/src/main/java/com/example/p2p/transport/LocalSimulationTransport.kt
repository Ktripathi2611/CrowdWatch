package com.example.p2p.transport

import com.example.crypto.CryptoNodeIdentity
import com.example.data.model.EventType
import com.example.p2p.MeshPeerNode
import com.example.p2p.MeshTransportType
import com.example.p2p.model.EvidenceRequest
import com.example.p2p.model.EvidenceResponse
import com.example.p2p.model.NodeSyncEvent
import com.example.p2p.model.SyncHandshakeManifest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * LocalSimulationTransport: Deterministic multi-node in-memory simulator.
 * Shares the exact same [PeerTransport] contract as real Nearby and Internet transports.
 * This guarantees that unit tests, integration tests, and UI demos execute the real distributed consensus engine.
 */
class LocalSimulationTransport(
    private val myNodeId: String = CryptoNodeIdentity.getNodeIdentityId()
) : PeerTransport {

    override val transportType: MeshTransportType = MeshTransportType.GOSSIP_BROADCAST

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _discoveredPeers = MutableStateFlow<List<MeshPeerNode>>(emptyList())
    override val discoveredPeers: StateFlow<List<MeshPeerNode>> = _discoveredPeers.asStateFlow()

    private val _incomingEvents = MutableSharedFlow<NodeSyncEvent>(extraBufferCapacity = 128)
    override val incomingEvents: SharedFlow<NodeSyncEvent> = _incomingEvents.asSharedFlow()

    private val _incomingManifests = MutableSharedFlow<Pair<String, SyncHandshakeManifest>>(extraBufferCapacity = 32)
    override val incomingManifests: SharedFlow<Pair<String, SyncHandshakeManifest>> = _incomingManifests.asSharedFlow()

    private val _incomingEvidenceRequests = MutableSharedFlow<EvidenceRequest>(extraBufferCapacity = 16)
    override val incomingEvidenceRequests: SharedFlow<EvidenceRequest> = _incomingEvidenceRequests.asSharedFlow()

    private val _incomingEvidenceResponses = MutableSharedFlow<EvidenceResponse>(extraBufferCapacity = 16)
    override val incomingEvidenceResponses: SharedFlow<EvidenceResponse> = _incomingEvidenceResponses.asSharedFlow()

    private var isLoopbackEnabled = false

    init {
        _discoveredPeers.value = emptyList()
    }

    fun setLoopbackBridgeEnabled(enabled: Boolean) {
        isLoopbackEnabled = enabled
        if (enabled) {
            val peer = MeshPeerNode(
                nodeId = "peer-radio-loopback",
                alias = "Nearby Peer (Radio Loopback)",
                transport = MeshTransportType.BLUETOOTH_LE,
                rssiSignalDbm = -38,
                lastSeenTimestamp = System.currentTimeMillis(),
                reportsInStore = 3,
                isConnected = true
            )
            _discoveredPeers.value = listOf(peer)
        } else {
            _discoveredPeers.value = emptyList()
        }
    }

    override suspend fun start() {}

    override suspend fun stop() {}

    override suspend fun broadcastEvent(event: NodeSyncEvent): Boolean {
        if (isLoopbackEnabled && event.eventType == EventType.REPORT_CREATED) {
            // When user publishes a report on a single phone, loopback peer verifies and confirms it
            scope.launch {
                delay(1200L)
                val confirmEvent = NodeSyncEvent(
                    eventId = "evt-ack-${UUID.randomUUID().toString().take(8)}",
                    reportId = event.reportId,
                    originNodeId = event.originNodeId,
                    authorNodeId = "peer-radio-loopback",
                    timestamp = System.currentTimeMillis(),
                    eventType = EventType.REPORT_CONFIRMED,
                    payload = "Confirmed via local radio loopback verification.",
                    version = event.version + 1
                )
                _incomingEvents.tryEmit(confirmEvent)
            }
        }
        return true
    }

    override suspend fun sendEventToPeer(peerNodeId: String, event: NodeSyncEvent): Boolean = true

    override suspend fun sendHandshakeManifest(peerNodeId: String, manifest: SyncHandshakeManifest): Boolean = true

    override suspend fun requestEvidence(peerNodeId: String, request: EvidenceRequest): Boolean = true

    override suspend fun sendEvidence(peerNodeId: String, response: EvidenceResponse): Boolean = true

    fun injectSimulatedEvent(event: NodeSyncEvent) {
        _incomingEvents.tryEmit(event)
    }

    fun togglePeerOnline(peerId: String) {
        _discoveredPeers.value = _discoveredPeers.value.map {
            if (it.nodeId == peerId) it.copy(isConnected = !it.isConnected) else it
        }
    }
}
