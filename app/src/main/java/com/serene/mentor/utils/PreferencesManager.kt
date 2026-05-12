package com.serene.mentor.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.serene.mentor.models.AnalysisResult

/**
 * Manages local SharedPreferences storage.
 * Used for offline fallback and caching user preferences.
 */
class PreferencesManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "serene_mentor_prefs"

        // Keys
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_OFFLINE_SESSIONS = "offline_sessions"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_TOTAL_SESSIONS = "total_sessions_local"
        private const val KEY_AVG_FLUENCY = "avg_fluency_local"
        private const val KEY_AVG_CONFIDENCE = "avg_confidence_local"
        private const val KEY_NOTIFICATION_ENABLED = "notifications_enabled"
        private const val KEY_DAILY_REMINDER_HOUR = "reminder_hour"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // ─── User Info ────────────────────────────────────────────

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userId: String
        get() = prefs.getString(KEY_USER_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    // ─── App Settings ─────────────────────────────────────────

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var isOnboarded: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDED, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, value).apply()

    var dailyReminderHour: Int
        get() = prefs.getInt(KEY_DAILY_REMINDER_HOUR, 9)
        set(value) = prefs.edit().putInt(KEY_DAILY_REMINDER_HOUR, value).apply()

    // ─── Local Stats Cache ────────────────────────────────────

    var totalSessionsLocal: Int
        get() = prefs.getInt(KEY_TOTAL_SESSIONS, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_SESSIONS, value).apply()

    var avgFluencyLocal: Float
        get() = prefs.getFloat(KEY_AVG_FLUENCY, 0f)
        set(value) = prefs.edit().putFloat(KEY_AVG_FLUENCY, value).apply()

    var avgConfidenceLocal: Float
        get() = prefs.getFloat(KEY_AVG_CONFIDENCE, 0f)
        set(value) = prefs.edit().putFloat(KEY_AVG_CONFIDENCE, value).apply()

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()

    // ─── Offline Session Queue ────────────────────────────────

    /**
     * Save a session locally when Firebase is unavailable.
     * These are synced to Firestore on next successful connection.
     */
    fun saveOfflineSession(result: AnalysisResult) {
        val sessions = getOfflineSessions().toMutableList()
        sessions.add(0, result) // prepend newest
        val json = gson.toJson(sessions.take(20)) // keep max 20 offline
        prefs.edit().putString(KEY_OFFLINE_SESSIONS, json).apply()

        // Update local stats immediately
        val newTotal = totalSessionsLocal + 1
        avgFluencyLocal = ((avgFluencyLocal * totalSessionsLocal) + result.fluencyScore) / newTotal
        avgConfidenceLocal = ((avgConfidenceLocal * totalSessionsLocal) + result.confidenceScore) / newTotal
        totalSessionsLocal = newTotal
    }

    fun getOfflineSessions(): List<AnalysisResult> {
        val json = prefs.getString(KEY_OFFLINE_SESSIONS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<AnalysisResult>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearOfflineSessions() {
        prefs.edit().remove(KEY_OFFLINE_SESSIONS).apply()
    }

    fun hasOfflineSessions(): Boolean = getOfflineSessions().isNotEmpty()

    // ─── User Cache ───────────────────────────────────────────

    fun cacheUserProfile(name: String, email: String, userId: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun updateLocalStats(fluency: Float, confidence: Float, sessions: Int) {
        prefs.edit()
            .putFloat(KEY_AVG_FLUENCY, fluency)
            .putFloat(KEY_AVG_CONFIDENCE, confidence)
            .putInt(KEY_TOTAL_SESSIONS, sessions)
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }

    // ─── Clear All ────────────────────────────────────────────

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun clearUserData() {
        prefs.edit()
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ID)
            .remove(KEY_OFFLINE_SESSIONS)
            .remove(KEY_TOTAL_SESSIONS)
            .remove(KEY_AVG_FLUENCY)
            .remove(KEY_AVG_CONFIDENCE)
            .remove(KEY_LAST_SYNC)
            .apply()
    }
}
