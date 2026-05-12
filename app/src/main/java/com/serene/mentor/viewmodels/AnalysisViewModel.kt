package com.serene.mentor.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.serene.mentor.firebase.FirebaseManager
import com.serene.mentor.models.AnalysisResult
import com.serene.mentor.utils.OfflineSyncManager
import com.serene.mentor.utils.PreferencesManager
import com.serene.mentor.utils.isNetworkAvailable
import kotlinx.coroutines.launch

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Saved(val sessionId: String) : SaveState()
    object SavedOffline : SaveState()
    data class Error(val message: String) : SaveState()
}

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseManager = FirebaseManager.getInstance()
    private val prefsManager = PreferencesManager.getInstance(application)

    private val _saveState = MutableLiveData<SaveState>(SaveState.Idle)
    val saveState: LiveData<SaveState> = _saveState

    /**
     * Save session to Firestore, or locally if offline.
     */
    fun saveSession(result: AnalysisResult) {
        if (_saveState.value is SaveState.Saved || _saveState.value is SaveState.SavedOffline) return

        _saveState.value = SaveState.Saving

        viewModelScope.launch {
            val context = getApplication<Application>()

            if (!context.isNetworkAvailable()) {
                // Save offline and queue for sync
                prefsManager.saveOfflineSession(result)
                _saveState.value = SaveState.SavedOffline
                return@launch
            }

            firebaseManager.saveSession(result)
                .onSuccess { sessionId ->
                    _saveState.value = SaveState.Saved(sessionId)
                }
                .onFailure { e ->
                    // Firebase failed, fall back to offline storage
                    prefsManager.saveOfflineSession(result)
                    _saveState.value = SaveState.SavedOffline
                }
        }
    }

    fun syncOfflineIfNeeded() {
        val context = getApplication<Application>()
        if (prefsManager.hasOfflineSessions() && context.isNetworkAvailable()) {
            OfflineSyncManager.syncPendingSessions(context)
        }
    }
}
