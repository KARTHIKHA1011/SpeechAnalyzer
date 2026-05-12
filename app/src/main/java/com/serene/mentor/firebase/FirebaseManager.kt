package com.serene.mentor.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.serene.mentor.models.AnalysisResult
import com.serene.mentor.models.Session
import com.serene.mentor.models.User
import kotlinx.coroutines.tasks.await

class FirebaseManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val USERS_COLLECTION = "users"
        const val SESSIONS_COLLECTION = "sessions"
        private const val TAG = "FirebaseManager"

        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseManager().also { INSTANCE = it }
            }
        }
    }

    // ─── Auth Methods ─────────────────────────────────────────

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            createUserProfile(user.uid, name, email)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up error", e)
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Result.failure(e)
        }
    }

    fun logout() = auth.signOut()

    // ─── User Profile ─────────────────────────────────────────

    private suspend fun createUserProfile(uid: String, name: String, email: String) {
        val user = hashMapOf(
            "userId" to uid,
            "name" to name,
            "email" to email,
            "createdAt" to Timestamp.now(),
            "totalSessions" to 0,
            "averageFluency" to 0f,
            "averageConfidence" to 0f
        )
        db.collection(USERS_COLLECTION).document(uid).set(user).await()
    }

    suspend fun getUserProfile(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val doc = db.collection(USERS_COLLECTION).document(uid).get().await()
            
            val user = if (doc.exists()) {
                doc.toObject(User::class.java) ?: User(userId = uid)
            } else {
                // If doc doesn't exist, create it if we have basic info
                val currentUser = auth.currentUser
                val newUser = User(
                    userId = uid,
                    name = currentUser?.displayName ?: "User",
                    email = currentUser?.email ?: ""
                )
                // We don't block here, just return the local object
                Result.success(newUser)
                return Result.success(newUser)
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Get user profile error", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserStats(fluency: Int, confidence: Int) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val docRef = db.collection(USERS_COLLECTION).document(uid)
            
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                
                if (!snapshot.exists()) {
                    val user = auth.currentUser
                    transaction.set(docRef, hashMapOf(
                        "userId" to uid,
                        "name" to (user?.displayName ?: "User"),
                        "email" to (user?.email ?: ""),
                        "createdAt" to Timestamp.now(),
                        "totalSessions" to 1,
                        "averageFluency" to fluency.toFloat(),
                        "averageConfidence" to confidence.toFloat()
                    ))
                } else {
                    val total = snapshot.getLong("totalSessions") ?: 0
                    val avgFluency = snapshot.getDouble("averageFluency") ?: 0.0
                    val avgConf = snapshot.getDouble("averageConfidence") ?: 0.0

                    val newTotal = total + 1
                    val newFluency = ((avgFluency * total) + fluency) / newTotal
                    val newConf = ((avgConf * total) + confidence) / newTotal

                    transaction.update(docRef, mapOf(
                        "totalSessions" to newTotal,
                        "averageFluency" to newFluency.toFloat(),
                        "averageConfidence" to newConf.toFloat()
                    ))
                }
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Update user stats error", e)
        }
    }

    // ─── Sessions ─────────────────────────────────────────────

    suspend fun saveSession(result: AnalysisResult): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

            val grammarList = result.grammarFeedback.map { gf ->
                mapOf(
                    "original" to gf.original,
                    "suggestion" to gf.suggestion,
                    "explanation" to gf.explanation,
                    "type" to gf.type
                )
            }

            val sessionData = hashMapOf(
                "userId" to uid,
                "topic" to result.topic,
                "topicCategory" to result.topicCategory,
                "difficulty" to result.difficulty,
                "transcript" to result.transcript,
                "fluencyScore" to result.fluencyScore,
                "confidenceScore" to result.confidenceScore,
                "grammarScore" to result.grammarScore,
                "durationSeconds" to result.durationSeconds,
                "wordCount" to result.wordCount,
                "wordsPerMinute" to result.wordsPerMinute,
                "fillerWords" to result.fillerWords,
                "grammarFeedback" to grammarList,
                "strengths" to result.strengths,
                "weaknesses" to result.weaknesses,
                "nextSteps" to result.nextSteps,
                "timestamp" to Timestamp.now()
            )

            val docRef = db.collection(SESSIONS_COLLECTION).add(sessionData).await()
            updateUserStats(result.fluencyScore, result.confidenceScore)
            Log.d(TAG, "Session saved successfully: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Save session error", e)
            Result.failure(e)
        }
    }

    suspend fun getSessions(): Result<List<Session>> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val snapshot = db.collection(SESSIONS_COLLECTION)
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val sessions = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Session::class.java)?.copy(sessionId = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping session document", e)
                    null
                }
            }
            Result.success(sessions)
        } catch (e: Exception) {
            Log.e(TAG, "Get sessions error", e)
            Result.failure(e)
        }
    }

    suspend fun getRecentSessions(limit: Int = 5): Result<List<Session>> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val snapshot = db.collection(SESSIONS_COLLECTION)
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val sessions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Session::class.java)?.copy(sessionId = doc.id)
            }
            Result.success(sessions)
        } catch (e: Exception) {
            Log.e(TAG, "Get recent sessions error", e)
            Result.failure(e)
        }
    }
}
