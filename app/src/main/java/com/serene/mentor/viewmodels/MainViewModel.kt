package com.serene.mentor.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.serene.mentor.firebase.FirebaseManager
import com.serene.mentor.models.Session
import com.serene.mentor.models.User
import com.serene.mentor.utils.NetworkMonitor
import com.serene.mentor.utils.OfflineSyncManager
import com.serene.mentor.utils.PreferencesManager
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseManager = FirebaseManager.getInstance()
    private val prefsManager = PreferencesManager.getInstance(application)

    // ─── Network ──────────────────────────────────────────────
    val networkState: NetworkMonitor = NetworkMonitor(application)

    // ─── User ─────────────────────────────────────────────────
    private val _userState = MutableLiveData<UiState<User>>()
    val userState: LiveData<UiState<User>> = _userState

    // ─── Sessions ─────────────────────────────────────────────
    private val _sessionsState = MutableLiveData<UiState<List<Session>>>()
    val sessionsState: LiveData<UiState<List<Session>>> = _sessionsState

    // ─── Offline Banner ───────────────────────────────────────
    private val _showOfflineBanner = MutableLiveData<Boolean>()
    val showOfflineBanner: LiveData<Boolean> = _showOfflineBanner

    init {
        loadUserProfile()
        observeNetwork()
    }

    // ─── User Profile ─────────────────────────────────────────

    fun loadUserProfile() {
        _userState.value = UiState.Loading
        viewModelScope.launch {
            firebaseManager.getUserProfile()
                .onSuccess { user ->
                    _userState.value = UiState.Success(user)
                    prefsManager.updateLocalStats(
                        fluency = user.averageFluency.toFloat(),
                        confidence = user.averageConfidence.toFloat(),
                        sessions = user.totalSessions.toInt()
                    )
                    prefsManager.cacheUserProfile(user.name, user.email, user.userId)
                }
                .onFailure { _ ->
                    // Fall back to cached local stats
                    val cachedUser = User(
                        name = prefsManager.userName,
                        email = prefsManager.userEmail,
                        userId = prefsManager.userId,
                        totalSessions = prefsManager.totalSessionsLocal.toLong(),
                        averageFluency = prefsManager.avgFluencyLocal.toDouble(),
                        averageConfidence = prefsManager.avgConfidenceLocal.toDouble()
                    )
                    _userState.value = UiState.Success(cachedUser)
                }
        }
    }

    // ─── Sessions ─────────────────────────────────────────────

    fun loadSessions() {
        _sessionsState.value = UiState.Loading
        viewModelScope.launch {
            firebaseManager.getSessions()
                .onSuccess { sessions ->
                    _sessionsState.value = UiState.Success(sessions)
                }
                .onFailure { e ->
                    _sessionsState.value = UiState.Error(
                        e.message ?: "Failed to load sessions"
                    )
                }
        }
    }

    fun loadRecentSessions(limit: Int = 5) {
        viewModelScope.launch {
            firebaseManager.getRecentSessions(limit)
                .onSuccess { sessions ->
                    _sessionsState.value = UiState.Success(sessions)
                }
                .onFailure { e ->
                    _sessionsState.value = UiState.Error(
                        e.message ?: "Failed to load recent sessions"
                    )
                }
        }
    }

    // ─── Network Observation ──────────────────────────────────

    private fun observeNetwork() {
        networkState.observeForever { isOnline ->
            _showOfflineBanner.value = !isOnline
            if (isOnline) {
                // Trigger offline sync when connectivity is restored
                OfflineSyncManager.syncPendingSessions(getApplication())
            }
        }
    }

    // ─── Auth ─────────────────────────────────────────────────

    fun logout() {
        firebaseManager.logout()
        prefsManager.clearUserData()
    }

    fun isLoggedIn(): Boolean = firebaseManager.isLoggedIn()

    override fun onCleared() {
        super.onCleared()
    }
}
