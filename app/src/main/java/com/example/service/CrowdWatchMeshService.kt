package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.crypto.CryptoNodeIdentity
import com.example.data.local.AppDatabase
import com.example.data.local.toModel
import com.example.p2p.sync.PeerSyncEngine
import com.example.p2p.transport.InternetP2PTransport
import com.example.p2p.transport.LocalSimulationTransport
import com.example.p2p.transport.NearbyTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * CrowdWatchMeshService: Background foreground service.
 * Manages active peer discovery, BLE/Wi-Fi Direct sockets, and WebSocket signaling when the screen is locked
 * or when the application is placed into the background.
 * Prevents Android Doze mode and background execution limits from severing critical emergency civic alerts.
 */
class CrowdWatchMeshService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    lateinit var syncEngine: PeerSyncEngine
        private set

    lateinit var nearbyTransport: NearbyTransport
        private set

    lateinit var internetTransport: InternetP2PTransport
        private set

    lateinit var simulationTransport: LocalSimulationTransport
        private set

    inner class LocalBinder : Binder() {
        fun getService(): CrowdWatchMeshService = this@CrowdWatchMeshService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(applicationContext)
        val myNodeId = CryptoNodeIdentity.getNodeIdentityId()

        syncEngine = PeerSyncEngine(
            reportDao = db.reportDao(),
            eventDao = db.eventDao(),
            alertDao = db.alertDao(),
            myNodeId = myNodeId
        )

        nearbyTransport = NearbyTransport(applicationContext, myNodeId)
        internetTransport = InternetP2PTransport(myNodeId)
        simulationTransport = LocalSimulationTransport(myNodeId)

        syncEngine.registerTransport(nearbyTransport)
        syncEngine.registerTransport(internetTransport)
        syncEngine.registerTransport(simulationTransport)

        val notificationManager = IncidentNotificationManager(applicationContext)

        serviceScope.launch {
            nearbyTransport.start()
            internetTransport.start()
            simulationTransport.start()

            // Monitor active civic reports in background to alert user of urgent hazards
            db.reportDao().getAllReports().collect { reportEntities ->
                val critical = reportEntities.firstOrNull {
                    it.severity == com.example.data.model.ReportSeverity.CRITICAL
                }
                if (critical != null) {
                    val domainReport = critical.toModel()
                    notificationManager.evaluateAndNotify(domainReport, null)
                }
            }
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("CrowdWatch Mesh Radio Active • Monitoring emergency civic alerts"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.launch {
            nearbyTransport.stop()
            internetTransport.stop()
            simulationTransport.stop()
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CrowdWatch Distributed Mesh",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps local P2P mesh network synchronized in background"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CrowdWatch Node Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "crowdwatch_mesh_channel"
        private const val NOTIFICATION_ID = 4041

        fun start(context: Context) {
            val intent = Intent(context, CrowdWatchMeshService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CrowdWatchMeshService::class.java)
            context.stopService(intent)
        }
    }
}
