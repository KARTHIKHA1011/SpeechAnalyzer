package com.serene.mentor.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

/**
 * NOTE: Firestore stores integers as Long and decimals as Double.
 * Using Int or Float in these data classes can cause toObject() to fail.
 */

// ─── User Model ───────────────────────────────────────────────
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Timestamp? = null,
    val totalSessions: Long = 0,
    val averageFluency: Double = 0.0,
    val averageConfidence: Double = 0.0
)

// ─── Session Model ────────────────────────────────────────────
data class Session(
    val sessionId: String = "",
    val userId: String = "",
    val topic: String = "",
    val topicCategory: String = "",
    val difficulty: String = "",
    val transcript: String = "",
    val fluencyScore: Long = 0,
    val confidenceScore: Long = 0,
    val grammarScore: Long = 0,
    val durationSeconds: Long = 0,
    val wordCount: Long = 0,
    val wordsPerMinute: Long = 0,
    val fillerWords: Map<String, Long> = emptyMap(),
    val grammarFeedback: List<GrammarFeedback> = emptyList(),
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val nextSteps: List<String> = emptyList(),
    val timestamp: Timestamp? = null
) {
    fun toAnalysisResult(): AnalysisResult {
        return AnalysisResult(
            transcript = transcript,
            topic = topic,
            topicCategory = topicCategory,
            difficulty = difficulty,
            fluencyScore = fluencyScore.toInt(),
            confidenceScore = confidenceScore.toInt(),
            grammarScore = grammarScore.toInt(),
            durationSeconds = durationSeconds,
            wordCount = wordCount.toInt(),
            wordsPerMinute = wordsPerMinute.toInt(),
            fillerWords = HashMap(fillerWords.mapValues { it.value.toInt() }),
            grammarFeedback = ArrayList(grammarFeedback),
            strengths = ArrayList(strengths),
            weaknesses = ArrayList(weaknesses),
            nextSteps = ArrayList(nextSteps)
        )
    }
}

// ─── Grammar Feedback ─────────────────────────────────────────
@Parcelize
data class GrammarFeedback(
    val original: String = "",
    val suggestion: String = "",
    val explanation: String = "",
    val type: String = "" // REPETITION, TENSE, FRAGMENT, FILLER
) : Parcelable

// ─── Topic Model ──────────────────────────────────────────────
data class Topic(
    val title: String,
    val category: TopicCategory,
    val difficulty: Difficulty,
    val description: String,
    val keyPoints: List<String>,
    val suggestedFramework: GDFramework,
    val sampleOpener: String
)

// ─── Analysis Result ──────────────────────────────────────────
@Parcelize
data class AnalysisResult(
    val transcript: String,
    val topic: String,
    val topicCategory: String,
    val difficulty: String,
    val fluencyScore: Int,
    val confidenceScore: Int,
    val grammarScore: Int,
    val durationSeconds: Long,
    val wordCount: Int,
    val wordsPerMinute: Int,
    val fillerWords: HashMap<String, Int>,
    val grammarFeedback: ArrayList<GrammarFeedback>,
    val strengths: ArrayList<String>,
    val weaknesses: ArrayList<String>,
    val nextSteps: ArrayList<String>
) : Parcelable

// ─── Enums ────────────────────────────────────────────────────
enum class TopicCategory(val displayName: String) {
    TECHNOLOGY("Technology"),
    ECONOMY("Economy"),
    SOCIAL_ISSUES("Social Issues"),
    ABSTRACT("Abstract Topics")
}

enum class Difficulty(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

enum class GDFramework(val displayName: String, val description: String) {
    PREP(
        "PREP Framework",
        "Point → Reason → Example → Point\nState your point, explain why, give an example, restate your point."
    ),
    STAR(
        "STAR Framework",
        "Situation → Task → Action → Result\nDescribe context, your role, what you did, and the outcome."
    ),
    THREE_POINT(
        "3-Point Structure",
        "Introduction → 3 Main Points → Conclusion\nSimple, clear, and effective for most GD topics."
    ),
    PROS_CONS(
        "Pros & Cons Analysis",
        "Advantages → Disadvantages → Balanced Verdict\nPresent both sides objectively before concluding."
    ),
    CAUSE_EFFECT(
        "Cause-Effect-Solution",
        "Identify Causes → Analyze Effects → Propose Solutions\nIdeal for problem-based and social topics."
    )
}

// ─── QuickStat for Dashboard ──────────────────────────────────
data class QuickStat(
    val label: String,
    val value: String,
    val iconRes: Int,
    val colorRes: Int
)
