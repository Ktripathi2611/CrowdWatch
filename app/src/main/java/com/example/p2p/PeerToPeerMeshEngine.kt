package com.example.p2p

import com.example.crypto.CryptoNodeIdentity
import com.example.data.model.IncidentReport
import com.example.data.model.ReportEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class MeshTransportType {
    BLUETOOTH_LE,
    WIFI_DIRECT,
    LOCAL_P2P_SOCKET,
    GOSSIP_BROADCAST
}

data class MeshPeerNode(
    val nodeId: String,
    val alias: String,
    val transport: MeshTransportType,
    val rssiSignalDbm: Int,
    val lastSeenTimestamp: Long,
    val reportsInStore: Int,
    val isConnected: Boolean = true
)

data class MeshPacket(
    val packetId: String = UUID.randomUUID().toString(),
    val senderNodeId: String,
    val originNodeId: String,
    val hops: Int = 0,
    val maxHops: Int = 4,
    val reportPayload: IncidentReport? = null,
    val eventPayload: ReportEvent? = null,
    val signature: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MeshNetworkStats(
    val isAdvertising: Boolean = true,
    val isDiscovering: Boolean = true,
    val activePeersCount: Int = 4,
    val packetsTransmitted: Int = 0,
    val packetsReceived: Int = 0,
    val bytesTransferred: Long = 0L,
    val meshGossipIntervalSeconds: Int = 8
)

class PeerToPeerMeshEngine(
    private val myNodeId: String = CryptoNodeIdentity.getNodeIdentityId()
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var gossipJob: Job? = null

    private val _connectedPeers = MutableStateFlow<List<MeshPeerNode>>(emptyList())
    val connectedPeers: StateFlow<List<MeshPeerNode>> = _connectedPeers.asStateFlow()

    private val _meshStats = MutableStateFlow(MeshNetworkStats())
    val meshStats: StateFlow<MeshNetworkStats> = _meshStats.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    val incomingPackets: SharedFlow<MeshPacket> = _incomingPackets.asSharedFlow()

    private val _meshLogEvents = MutableStateFlow<List<String>>(emptyList())
    val meshLogEvents: StateFlow<List<String>> = _meshLogEvents.asStateFlow()

    private val seenPacketIds = mutableSetOf<String>()

    init {
        initializeMeshNeighbors()
        startMeshHeartbeatAndGossip()
    }

    private fun initializeMeshNeighbors() {
        _connectedPeers.value = listOf(
            MeshPeerNode("node-mira-w", "Peer Mira West (B)", MeshTransportType.WIFI_DIRECT, -62, System.currentTimeMillis() - 4000, 8),
            MeshPeerNode("node-stn-hub", "Peer Station Hub (C)", MeshTransportType.BLUETOOTH_LE, -74, System.currentTimeMillis() - 11000, 7),
            MeshPeerNode("node-hwy-9", "Peer Highway Patrol (D)", MeshTransportType.WIFI_DIRECT, -58, System.currentTimeMillis() - 2000, 8),
            MeshPeerNode("node-bypass-e", "Peer East Bypass (E)", MeshTransportType.BLUETOOTH_LE, -85, System.currentTimeMillis() - 45000, 5)
        )
        logEvent("P2P Mesh stack initialized for Node $myNodeId. 4 active peer routes discovered.")
    }

    private fun startMeshHeartbeatAndGossip() {
        gossipJob?.cancel()
        gossipJob = scope.launch {
            while (isActive) {
                delay(8000L)
                // Random RSSI jitter and heartbeat ping
                _connectedPeers.value = _connectedPeers.value.map { peer ->
                    val jitter = (-2..2).random()
                    val newRssi = (peer.rssiSignalDbm + jitter).coerceIn(-95, -45)
                    peer.copy(rssiSignalDbm = newRssi, lastSeenTimestamp = System.currentTimeMillis())
                }
                _meshStats.value = _meshStats.value.copy(
                    packetsTransmitted = _meshStats.value.packetsTransmitted + 1,
                    bytesTransferred = _meshStats.value.bytesTransferred + 420
                )
            }
        }
    }

    fun broadcastNewIncident(report: IncidentReport) {
        val payloadToSign = "${report.id}:${report.createdAt}:${report.confidenceScore}:${report.reporterNodeId}"
        val signature = CryptoNodeIdentity.signPayload(payloadToSign)

        val packet = MeshPacket(
            senderNodeId = myNodeId,
            originNodeId = myNodeId,
            hops = 0,
            reportPayload = report,
            signature = signature
        )
        seenPacketIds.add(packet.packetId)

        _meshStats.value = _meshStats.value.copy(
            packetsTransmitted = _meshStats.value.packetsTransmitted + _connectedPeers.value.size,
            bytesTransferred = _meshStats.value.bytesTransferred + (1850 * _connectedPeers.value.size)
        )
        logEvent("Gossip broadcast: '${report.title}' transmitted to ${_connectedPeers.value.size} peer nodes with ECDSA signature.")
    }

    fun broadcastEvent(event: ReportEvent) {
        val signature = CryptoNodeIdentity.signPayload("${event.eventId}:${event.type}:${event.timestamp}")
        val packet = MeshPacket(
            senderNodeId = myNodeId,
            originNodeId = myNodeId,
            hops = 0,
            eventPayload = event,
            signature = signature
        )
        seenPacketIds.add(packet.packetId)

        _meshStats.value = _meshStats.value.copy(
            packetsTransmitted = _meshStats.value.packetsTransmitted + _connectedPeers.value.size,
            bytesTransferred = _meshStats.value.bytesTransferred + (650 * _connectedPeers.value.size)
        )
        logEvent("Gossip event: '${event.type}' (${event.details}) signed & fanned out across local mesh.")
    }

    fun injectSimulatedPeerPacket(packet: MeshPacket) {
        if (seenPacketIds.contains(packet.packetId)) return
        seenPacketIds.add(packet.packetId)

        _meshStats.value = _meshStats.value.copy(
            packetsReceived = _meshStats.value.packetsReceived + 1,
            bytesTransferred = _meshStats.value.bytesTransferred + 1200
        )
        logEvent("Received P2P packet from ${packet.senderNodeId} (hops: ${packet.hops}). Validating payload.")
        _incomingPackets.tryEmit(packet)
    }

    fun logEvent(message: String) {
        _meshLogEvents.value = (listOf(message) + _meshLogEvents.value).take(50)
    }

    fun togglePeerOnline(peerId: String) {
        _connectedPeers.value = _connectedPeers.value.map {
            if (it.nodeId == peerId) it.copy(isConnected = !it.isConnected) else it
        }
        val target = _connectedPeers.value.firstOrNull { it.nodeId == peerId }
        val state = if (target?.isConnected == true) "connected" else "disconnected"
        logEvent("Peer $peerId $state from local mesh routing table.")
    }
}
