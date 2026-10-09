package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class CrowdWatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:109876543210:android:crowdwatchcivicnode")
                    .setApiKey("AIzaSyFakeKeyForCrowdWatchCivicNodeAuth123")
                    .setProjectId("crowdwatch-local")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            Log.w("CrowdWatchApp", "FirebaseApp fallback initialization: ${e.message}")
        }
    }
}
