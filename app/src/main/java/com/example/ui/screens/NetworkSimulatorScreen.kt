package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NodeState
import com.example.p2p.MeshNetworkStats
import com.example.p2p.MeshPeerNode
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.ResolvedGreen
import com.example.ui.theme.ResolvedGreenBg

@Composable
fun NetworkSimulatorScreen(
    nodes: List<NodeState>,
    activeNodeId: String,
    logs: List<String>,
    connectedPeers: List<MeshPeerNode> = emptyList(),
    meshStats: MeshNetworkStats = MeshNetworkStats(),
    isNearbyMeshEnabled: Boolean = false,
    isLoopbackBridgeEnabled: Boolean = false,
    onToggleLoopbackBridge: (Boolean) -> Unit = {},
    onBack: () -> Unit,
    onSelectActiveNode: (String) -> Unit,
    onToggleNodeOnline: (String) -> Unit,
    onSyncAll: () -> Unit,
    onTriggerConsensusStep: (Int) -> Unit = {},
    onStartNearbyMesh: () -> Unit = {},
    onStopNearbyMesh: () -> Unit = {},
    onToggleMeshPeer: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Surface(
            color = CrowdWatchColors.Surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CrowdWatchColors.TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Decentralized P2P Mesh & Transports",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CrowdWatchColors.TextPrimary
                    )
                    Text(
                        text = "Modular PeerTransport • Nearby Radio + Internet WebSockets",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrowdWatchColors.TextSecondary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Nearby Connections Hardware Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNearbyMeshEnabled) CrowdWatchColors.MeshActiveBg else CrowdWatchColors.Surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isNearbyMeshEnabled) CrowdWatchColors.MeshActive else CrowdWatchColors.Border
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isNearbyMeshEnabled) CrowdWatchColors.MeshActive.copy(alpha = 0.2f) else CrowdWatchColors.SurfaceSecondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isNearbyMeshEnabled) Icons.Default.Radar else Icons.Default.BluetoothSearching,
                                        contentDescription = "P2P Mesh",
                                        tint = if (isNearbyMeshEnabled) CrowdWatchColors.MeshActive else CrowdWatchColors.TextTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Nearby Connections P2P",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isNearbyMeshEnabled) CrowdWatchColors.MeshNode else CrowdWatchColors.TextPrimary
                                    )
                                    Text(
                                        text = if (isNearbyMeshEnabled) "Gossip active (BLE + Wi-Fi Direct)" else "P2P radio suspended",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isNearbyMeshEnabled) CrowdWatchColors.MeshSending else CrowdWatchColors.TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isNearbyMeshEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) onStartNearbyMesh() else onStopNearbyMesh()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CrowdWatchColors.MeshActive
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Connected Peers",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrowdWatchColors.TextSecondary
                                )
                                Text(
                                    text = "${meshStats.activePeersCount} live",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (meshStats.activePeersCount > 0) CrowdWatchColors.PeerConnected else CrowdWatchColors.PeerDisconnected
                                )
                            }
                            Column {
                                Text(
                                    text = "Packets Sent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrowdWatchColors.TextSecondary
                                )
                                Text(
                                    text = "${meshStats.packetsTransmitted} pkts",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CrowdWatchColors.TextPrimary
                                )
                            }
                            Column {
                                Text(
                                    text = "Packets Received",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrowdWatchColors.TextSecondary
                                )
                                Text(
                                    text = "${meshStats.packetsReceived} pkts",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CrowdWatchColors.MeshSending
                                )
                            }
                        }

                        if (connectedPeers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Active Nearby Devices:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CrowdWatchColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            connectedPeers.forEach { peer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (peer.isConnected) CrowdWatchColors.PeerConnected else CrowdWatchColors.PeerDisconnected)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = peer.alias,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = CrowdWatchColors.TextPrimary
                                        )
                                    }
                                    val rssiColor = when {
                                        peer.rssiSignalDbm > -65 -> CrowdWatchColors.ConfidenceHigh
                                        peer.rssiSignalDbm > -80 -> CrowdWatchColors.ConfidenceMedium
                                        else -> CrowdWatchColors.ConfidenceLow
                                    }
                                    Text(
                                        text = "${peer.reportsInStore} reports • RSSI ${peer.rssiSignalDbm} dBm",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = rssiColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live Internet Cloud Mesh Relay Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = "Internet Cloud Sync",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Internet Cloud Mesh",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CrowdWatchColors.TextPrimary
                                    )
                                    Text(
                                        text = "Remote multi-device sync across Internet & Wi-Fi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CrowdWatchColors.TextSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CrowdWatchColors.ConfidenceHighBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(CrowdWatchColors.NetworkOnline)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Online",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CrowdWatchColors.ConfidenceHighText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Any reports or votes created on this phone are cryptographically signed with ECDSA and synchronized to all other devices connected to the internet in real-time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrowdWatchColors.TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onSyncAll,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("sync_cloud_mesh_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Cloud",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sync Cloud Mesh Now",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Topological Network Graph Canvas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mesh Topology",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = CrowdWatchColors.TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CrowdWatchColors.ConfidenceHighBg
                            ) {
                                Text(
                                    text = "${nodes.count { it.isOnline }}/${nodes.size} Nodes Online",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CrowdWatchColors.ConfidenceHighText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual interactive graph
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                        ) {
                            val w = size.width
                            val h = size.height

                            val positions = listOf(
                                Offset(w * 0.5f, h * 0.5f),  // Node A (Center)
                                Offset(w * 0.2f, h * 0.25f), // Node B
                                Offset(w * 0.8f, h * 0.25f), // Node C
                                Offset(w * 0.25f, h * 0.75f),// Node D
                                Offset(w * 0.75f, h * 0.75f) // Node E
                            )

                            // Draw mesh edges
                            val activeEdges = listOf(
                                Pair(0, 1), Pair(0, 2), Pair(0, 3), Pair(0, 4),
                                Pair(1, 3), Pair(2, 4), Pair(1, 2)
                            )

                            for ((from, to) in activeEdges) {
                                val n1 = nodes.getOrNull(from)
                                val n2 = nodes.getOrNull(to)
                                val isConnected = (n1?.isOnline == true) && (n2?.isOnline == true)
                                drawLine(
                                    color = if (isConnected) CrowdWatchColors.MeshActive.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.15f),
                                    start = positions[from],
                                    end = positions[to],
                                    strokeWidth = if (isConnected) 2.5f else 1.5f
                                )
                            }

                            // Draw Node points
                            nodes.forEachIndexed { idx, node ->
                                val pos = positions.getOrElse(idx) { Offset.Zero }
                                val isSelected = node.nodeId == activeNodeId

                                // Selection ring
                                if (isSelected) {
                                    drawCircle(
                                        color = CrowdWatchColors.PrimaryBlue.copy(alpha = 0.5f),
                                        radius = 22f,
                                        center = pos
                                    )
                                }

                                drawCircle(
                                    color = if (node.isOnline) CrowdWatchColors.PeerConnected else CrowdWatchColors.PeerDisconnected,
                                    radius = 14f,
                                    center = pos
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 5f,
                                    center = pos
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tap any node below to simulate the app from that device's perspective.",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrowdWatchColors.TextSecondary
                        )
                    }
                }
            }

            // Node Roster
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Peer Nodes in Mesh",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CrowdWatchColors.TextPrimary
                    )
                    OutlinedButton(
                        onClick = onSyncAll,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(14.dp), tint = CrowdWatchColors.PrimaryBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync All", fontSize = 12.sp, color = CrowdWatchColors.PrimaryBlue)
                    }
                }
            }

            // Single-Device Loopback Testing Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isLoopbackBridgeEnabled) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.Border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Single-Device Peer Sync Bridge",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = CrowdWatchColors.TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isLoopbackBridgeEnabled) CrowdWatchColors.ConfidenceHighBg else CrowdWatchColors.ConfidenceMediumBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isLoopbackBridgeEnabled) "ACTIVE" else "OFF",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLoopbackBridgeEnabled) CrowdWatchColors.ConfidenceHighText else CrowdWatchColors.ConfidenceMediumText
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enables virtual radio loopback on this handset so you can test peer discovery, cryptographic verification, and gossip propagation on a single device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrowdWatchColors.TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        androidx.compose.material3.Switch(
                            checked = isLoopbackBridgeEnabled,
                            onCheckedChange = onToggleLoopbackBridge,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CrowdWatchColors.PrimaryBlue
                            )
                        )
                    }
                }
            }

            if (nodes.isEmpty() && !isLoopbackBridgeEnabled) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = CrowdWatchColors.TextTertiary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Operating in pure multi-phone BLE mode",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = CrowdWatchColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "To test on a single phone without a 2nd physical device, turn on the Single-Device Peer Sync Bridge above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrowdWatchColors.TextTertiary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(nodes) { node ->
                val isActive = node.nodeId == activeNodeId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectActiveNode(node.nodeId) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) CrowdWatchColors.PrimaryBlueBg else CrowdWatchColors.Surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isActive) 2.dp else 1.dp,
                        if (isActive) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.Border
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (node.isOnline) CrowdWatchColors.ConfidenceHighBg else CrowdWatchColors.SurfaceSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (node.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = "Status",
                                    tint = if (node.isOnline) CrowdWatchColors.PeerConnected else CrowdWatchColors.PeerDisconnected,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = node.nodeName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CrowdWatchColors.TextPrimary
                                )
                                Text(
                                    text = "Reports: ${node.reportsHeld} • Pending sync: ${node.pendingSync} • ${node.lastSyncAgo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrowdWatchColors.TextSecondary
                                )
                            }
                        }

                        IconButton(onClick = { onToggleNodeOnline(node.nodeId) }) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Toggle Online",
                                tint = if (node.isOnline) CrowdWatchColors.ConfidenceHigh else CrowdWatchColors.ConfidenceLow
                            )
                        }
                    }
                }
            }
            }

            // Real-time Gossip Network Event Logs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Decentralized Gossip Event Log",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = CrowdWatchColors.MeshActive
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CrowdWatchColors.PeerConnected)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            logs.take(7).forEach { log ->
                                Text(
                                    text = "> $log",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
