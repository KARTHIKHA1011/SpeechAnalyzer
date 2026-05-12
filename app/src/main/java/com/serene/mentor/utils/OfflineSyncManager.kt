package com.serene.mentor.utils

import android.content.Context
import android.util.Log
import com.serene.mentor.firebase.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles syncing offline sessions to Firestore when connectivity is restored.
 * Attach a NetworkMonitor observer and call syncPendingSessions() when online.
 */
object OfflineSyncManager {

    private const val TAG = "OfflineSyncManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun syncPendingSessions(context: Context) {
        val prefs = PreferencesManager.getInstance(context)
        if (!prefs.hasOfflineSessions()) return
        if (!context.isNetworkAvailable()) return

        scope.launch {
            val firebaseManager = FirebaseManager.getInstance()
            if (!firebaseManager.isLoggedIn()) return@launch

            val offlineSessions = prefs.getOfflineSessions()
            Log.d(TAG, "Syncing ${offlineSessions.size} offline session(s)...")

            var syncedCount = 0
            for (session in offlineSessions) {
                val result = firebaseManager.saveSession(session)
                result.onSuccess { syncedCount++ }
                    .onFailure { e -> Log.e(TAG, "Failed to sync session: ${e.message}") }
            }

            if (syncedCount == offlineSessions.size) {
                prefs.clearOfflineSessions()
                Log.d(TAG, "All $syncedCount session(s) synced and cleared.")
            } else {
                Log.w(TAG, "Only $syncedCount/${offlineSessions.size} sessions synced.")
            }
        }
    }
}
