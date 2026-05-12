package com.serene.mentor.utils

import com.serene.mentor.models.AnalysisResult
import com.serene.mentor.models.GrammarFeedback
import kotlin.math.roundToInt

/**
 * Core speech analysis engine.
 * Performs rule-based analysis on transcribed speech text.
 */
object SpeechAnalyzer {

    // ─── Filler Words Dictionary ──────────────────────────────
    private val FILLER_WORDS = setOf(
        "uh", "um", "uhh", "umm", "er", "err", "ah", "ahh",
        "like", "you know", "actually", "basically", "literally",
        "kind of", "sort of", "kinda", "sorta",
        "right", "okay so", "so yeah", "i mean",
        "well", "anyway", "and uh", "and um"
    )

    // ─── Common Grammar Errors ────────────────────────────────
    private val GRAMMAR_RULES = mapOf(
        "he don't" to Pair("he doesn't", "Subject-verb agreement: use 'doesn't' with he/she/it"),
        "she don't" to Pair("she doesn't", "Subject-verb agreement: use 'doesn't' with he/she/it"),
        "it don't" to Pair("it doesn't", "Subject-verb agreement: use 'doesn't' with he/she/it"),
        "they was" to Pair("they were", "Subject-verb agreement: use 'were' with plural subjects"),
        "we was" to Pair("we were", "Subject-verb agreement: use 'were' with plural subjects"),
        "i done" to Pair("i did", "Use simple past 'did' instead of 'done' without auxiliary"),
        "i seen" to Pair("i saw", "Use simple past 'saw' instead of 'seen' without auxiliary"),
        "i been" to Pair("i have been", "Use 'have been' as perfect auxiliary is required"),
        "it were" to Pair("it was", "Subject-verb agreement: use 'was' with singular subjects"),
        "could of" to Pair("could have", "'Could of' is incorrect; use 'could have'"),
        "should of" to Pair("should have", "'Should of' is incorrect; use 'should have'"),
        "would of" to Pair("would have", "'Would of' is incorrect; use 'would have'"),
        "me and" to Pair("... and I", "Use 'I' as a subject, e.g., 'My friend and I'"),
        "more better" to Pair("better", "'Better' is already comparative; don't use 'more'"),
        "most best" to Pair("best", "'Best' is already superlative; don't use 'most'")
    )

    // ─── Hesitation Indicators ────────────────────────────────
    private val HESITATION_PATTERNS = listOf(
        "i think maybe", "i'm not sure if", "i don't know if",
        "probably", "might be", "could be",
        "something like", "what do you call it"
    )

    // ─── Main Analysis Method ─────────────────────────────────
    fun analyze(
        transcript: String,
        topic: String,
        topicCategory: String,
        difficulty: String,
        durationSeconds: Long
    ): AnalysisResult {

        val cleaned = transcript.trim().lowercase()
        val words = cleaned.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val wordCount = words.size
        val sentences = splitIntoSentences(transcript)

        // Analysis components
        val fillerMap = detectFillerWords(cleaned)
        val grammarFeedback = detectGrammarIssues(transcript)
        val (fluencyScore, wpm) = calculateFluencyScore(wordCount, durationSeconds, fillerMap)
        val confidenceScore = calculateConfidenceScore(cleaned, sentences, fillerMap)
        val grammarScore = calculateGrammarScore(grammarFeedback, sentences.size)

        val strengths = generateStrengths(fluencyScore, confidenceScore, grammarScore, fillerMap, wordCount)
        val weaknesses = generateWeaknesses(fluencyScore, confidenceScore, grammarScore, fillerMap)
        val nextSteps = generateNextSteps(fluencyScore, confidenceScore, grammarScore, fillerMap, difficulty)

        return AnalysisResult(
            transcript = transcript,
            topic = topic,
            topicCategory = topicCategory,
            difficulty = difficulty,
            fluencyScore = fluencyScore,
            confidenceScore = confidenceScore,
            grammarScore = grammarScore,
            durationSeconds = durationSeconds,
            wordCount = wordCount,
            wordsPerMinute = wpm,
            fillerWords = HashMap(fillerMap),
            grammarFeedback = ArrayList(grammarFeedback),
            strengths = ArrayList(strengths),
            weaknesses = ArrayList(weaknesses),
            nextSteps = ArrayList(nextSteps)
        )
    }

    // ─── 1. Filler Word Detection ─────────────────────────────
    fun detectFillerWords(text: String): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val lowerText = text.lowercase()

        for (filler in FILLER_WORDS) {
            val regex = "\\b$filler\\b".toRegex()
            val matches = regex.findAll(lowerText).count()
            if (matches > 0) {
                result[filler] = matches
            }
        }
        return result
    }

    // ─── 2. Grammar Analysis ──────────────────────────────────
    fun detectGrammarIssues(transcript: String): List<GrammarFeedback> {
        val feedback = mutableListOf<GrammarFeedback>()
        val sentences = splitIntoSentences(transcript)
        val lowerText = transcript.lowercase()

        // Rule-based errors
        for ((error, correction) in GRAMMAR_RULES) {
            val regex = "\\b$error\\b".toRegex()
            if (regex.containsMatchIn(lowerText)) {
                val originalSentence = sentences.firstOrNull {
                    it.lowercase().contains(error)
                } ?: error
                feedback.add(GrammarFeedback(
                    original = originalSentence.trim(),
                    suggestion = originalSentence.replace(error, correction.first, ignoreCase = true).trim(),
                    explanation = correction.second,
                    type = "GRAMMAR"
                ))
            }
        }

        // Repeated words detection
        val words = transcript.split("\\s+".toRegex()).filter { it.isNotBlank() }
        for (i in 0 until words.size - 1) {
            val w1 = words[i].lowercase().replace(Regex("[^a-z]"), "")
            val w2 = words[i + 1].lowercase().replace(Regex("[^a-z]"), "")
            if (w1 == w2 && w1.length > 2) {
                val context = words.subList(maxOf(0, i - 2), minOf(words.size, i + 4)).joinToString(" ")
                feedback.add(GrammarFeedback(
                    original = context,
                    suggestion = context.replace("$w1 $w1", w1, ignoreCase = true),
                    explanation = "Repeated word '$w1' detected. Avoid consecutive repetition.",
                    type = "REPETITION"
                ))
            }
        }

        // Fragment detection (very short sentences)
        sentences.forEach { sentence ->
            val sWords = sentence.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
            if (sWords.size in 1..3 && sentence.length > 4) {
                feedback.add(GrammarFeedback(
                    original = sentence.trim(),
                    suggestion = "[Try expanding this thought]",
                    explanation = "This sentence is very short. Elaborate to sound more fluent.",
                    type = "FRAGMENT"
                ))
            }
        }

        return feedback.distinctBy { it.suggestion }.take(8)
    }

    // ─── 3. Fluency Score Calculation ─────────────────────────
    private fun calculateFluencyScore(
        wordCount: Int,
        durationSeconds: Long,
        fillerMap: Map<String, Int>
    ): Pair<Int, Int> {
        if (durationSeconds == 0L || wordCount == 0) return Pair(50, 0)

        val minutes = durationSeconds / 60.0
        val wpm = if (minutes > 0) (wordCount / minutes).roundToInt() else 0

        // Ideal WPM range for GD: 100-140
        var wpmScore = when {
            wpm < 60 -> 40
            wpm < 90 -> 65
            wpm in 90..150 -> 95
            wpm in 151..180 -> 75
            else -> 50
        }

        // Filler word penalty
        val totalFillers = fillerMap.values.sum()
        val fillerRate = (totalFillers.toFloat() / wordCount) * 100
        val fillerPenalty = when {
            fillerRate < 2 -> 0
            fillerRate < 5 -> 10
            fillerRate < 10 -> 20
            else -> 35
        }

        val score = (wpmScore - fillerPenalty).coerceIn(10, 100)
        return Pair(score, wpm)
    }

    // ─── 4. Confidence Score Calculation ─────────────────────
    private fun calculateConfidenceScore(
        text: String,
        sentences: List<String>,
        fillerMap: Map<String, Int>
    ): Int {
        var score = 80 // base score

        // Hesitation penalty
        val hesitationCount = HESITATION_PATTERNS.count { text.contains(it) }
        score -= hesitationCount * 6

        // Filler word impact on confidence
        val totalFillers = fillerMap.values.sum()
        score -= (totalFillers * 2)

        // Sentence length variety (sign of confidence)
        val longSentences = sentences.count { s ->
            s.trim().split("\\s+".toRegex()).size >= 8
        }
        score += (longSentences * 2)

        // Positive language patterns
        val assertivePatterns = listOf(
            "i believe", "clearly", "therefore", "consequently",
            "specifically", "for instance", "in conclusion"
        )
        val assertiveCount = assertivePatterns.count { text.contains(it) }
        score += assertiveCount * 4

        return score.coerceIn(10, 100)
    }

    // ─── 5. Grammar Score ─────────────────────────────────────
    private fun calculateGrammarScore(
        grammarFeedback: List<GrammarFeedback>,
        sentenceCount: Int
    ): Int {
        if (sentenceCount == 0) return 100
        val errorCount = grammarFeedback.size
        
        var score = 100
        score -= (errorCount * 12)
        
        return score.coerceIn(10, 100)
    }

    // ─── 6. Feedback Generation ───────────────────────────────
    private fun generateStrengths(
        fluency: Int,
        confidence: Int,
        grammar: Int,
        fillerMap: Map<String, Int>,
        wordCount: Int
    ): List<String> {
        val strengths = mutableListOf<String>()
        if (fluency >= 75) strengths.add("Great speaking rhythm and pace")
        if (confidence >= 75) strengths.add("Strong, assertive delivery")
        if (grammar >= 85) strengths.add("Excellent grammatical accuracy")
        if (fillerMap.isEmpty()) strengths.add("Zero filler words used!")
        else if (fillerMap.values.sum() <= 2) strengths.add("Very few filler words")
        if (wordCount >= 80) strengths.add("Detailed and thorough response")
        
        if (strengths.isEmpty()) strengths.add("Completed the session successfully")
        return strengths
    }

    private fun generateWeaknesses(
        fluency: Int,
        confidence: Int,
        grammar: Int,
        fillerMap: Map<String, Int>
    ): List<String> {
        val weaknesses = mutableListOf<String>()
        if (fluency < 60) weaknesses.add("Speaking pace was a bit inconsistent")
        if (confidence < 60) weaknesses.add("Could sound more confident with fewer hesitations")
        if (grammar < 70) weaknesses.add("Some common grammar errors were detected")
        
        val topFiller = fillerMap.maxByOrNull { it.value }
        if (topFiller != null && topFiller.value >= 3) {
            weaknesses.add("Overused the word '${topFiller.key}'")
        }
        return weaknesses
    }

    private fun generateNextSteps(
        fluency: Int,
        confidence: Int,
        grammar: Int,
        fillerMap: Map<String, Int>,
        difficulty: String
    ): List<String> {
        val steps = mutableListOf<String>()

        if (fillerMap.isNotEmpty()) {
            steps.add("Try to replace '${fillerMap.keys.first()}' with a short pause")
        }
        if (grammar < 80) {
            steps.add("Review the suggested corrections to improve accuracy")
        }
        if (confidence < 70) {
            steps.add("Use power words like 'Definitely' and 'Absolutely' to sound more sure")
        }
        
        steps.add("Practice the PREP framework for better structure")
        steps.add("Record another session on a similar topic to track progress")

        return steps.take(4)
    }

    // ─── Helpers ──────────────────────────────────────────────
    private fun splitIntoSentences(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return text.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
    }
}
