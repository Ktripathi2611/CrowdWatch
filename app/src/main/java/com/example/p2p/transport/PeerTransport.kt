package com.example.p2p.transport

import com.example.p2p.MeshPeerNode
import com.example.p2p.MeshTransportType
import com.example.p2p.model.EvidenceRequest
import com.example.p2p.model.EvidenceResponse
import com.example.p2p.model.NodeSyncEvent
import com.example.p2p.model.SyncHandshakeManifest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Modular PeerTransport abstraction.
 * Decouples the application, storage, and consensus layer from the physical or network carrier.
 * Allows seamless switching or concurrent operation of:
 *  - NearbyTransport (Google Play Services Nearby Connections BLE / Wi-Fi Direct)
 *  - InternetP2PTransport (WebSocket signaling, WebRTC DataChannel, STUN / TURN fallback)
 *  - LocalSimulationTransport (Deterministic multi-node simulation for validation and testing)
 */
interface PeerTransport {
    val transportType: MeshTransportType

    /** Active peer nodes discovered or connected via this transport */
    val discoveredPeers: StateFlow<List<MeshPeerNode>>

    /** Flow of incoming verified synchronization events */
    val incomingEvents: SharedFlow<NodeSyncEvent>

    /** Flow of incoming sync manifests from peers */
    val incomingManifests: SharedFlow<Pair<String, SyncHandshakeManifest>> // (peerNodeId, manifest)

    /** Flow of incoming peer evidence requests */
    val incomingEvidenceRequests: SharedFlow<EvidenceRequest>

    /** Flow of incoming peer evidence responses (raw photo/video payload) */
    val incomingEvidenceResponses: SharedFlow<EvidenceResponse>

    /** Start advertising presence and listening for peers */
    suspend fun start()

    /** Stop transport activities */
    suspend fun stop()

    /** Broadcast an immutable synchronization event to reachable peers */
    suspend fun broadcastEvent(event: NodeSyncEvent): Boolean

    /** Send an event to a specific target peer node */
    suspend fun sendEventToPeer(peerNodeId: String, event: NodeSyncEvent): Boolean

    /** Exchange compact manifest for handshake sync */
    suspend fun sendHandshakeManifest(peerNodeId: String, manifest: SyncHandshakeManifest): Boolean

    /** Request missing evidence directly from observing peer */
    suspend fun requestEvidence(peerNodeId: String, request: EvidenceRequest): Boolean

    /** Respond with local evidence bytes */
    suspend fun sendEvidence(peerNodeId: String, response: EvidenceResponse): Boolean
}
