package com.serene.mentor.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.serene.mentor.R
import com.serene.mentor.adapters.GrammarFeedbackAdapter
import com.serene.mentor.adapters.NextStepsAdapter
import com.serene.mentor.databinding.ActivityAnalysisBinding
import com.serene.mentor.firebase.FirebaseManager
import com.serene.mentor.models.AnalysisResult
import kotlinx.coroutines.launch

class AnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisBinding
    private val firebaseManager = FirebaseManager.getInstance()

    companion object {
        const val EXTRA_ANALYSIS_RESULT = "analysis_result"
        const val EXTRA_IS_HISTORY = "is_history"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Your Results"

        val result = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_ANALYSIS_RESULT, AnalysisResult::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_ANALYSIS_RESULT)
        }

        val isHistory = intent.getBooleanExtra(EXTRA_IS_HISTORY, false)

        result?.let {
            displayResults(it)
            if (!isHistory) {
                saveSession(it)
            } else {
                binding.tvSaved.visibility = View.GONE
            }
        }

        binding.btnHome.setOnClickListener {
            if (isHistory) finish() else navigateToHome()
        }
    }

    private fun displayResults(result: AnalysisResult) {
        // Topic info
        binding.tvTopicTitle.text = result.topic
        binding.chipDifficulty.text = result.difficulty

        // Scores
        animateScore(binding.progressFluency, result.fluencyScore)
        animateScore(binding.progressConfidence, result.confidenceScore)
        animateScore(binding.progressGrammar, result.grammarScore)

        binding.tvFluencyScore.text = "${result.fluencyScore}%"
        binding.tvConfidenceScore.text = "${result.confidenceScore}%"
        binding.tvGrammarScore.text = "${result.grammarScore}%"

        // Stats
        val minutes = result.durationSeconds / 60
        val seconds = result.durationSeconds % 60
        binding.tvDuration.text = String.format("%02d:%02d", minutes, seconds)
        binding.tvWordCount.text = "${result.wordCount} words"
        binding.tvWpm.text = "${result.wordsPerMinute} WPM"

        // Transcript
        binding.tvTranscript.text = result.transcript.ifBlank { "No transcript available." }

        // Filler words
        if (result.fillerWords.isEmpty()) {
            binding.tvNoFillers.visibility = View.VISIBLE
            binding.chipGroupFillers.visibility = View.GONE
        } else {
            binding.tvNoFillers.visibility = View.GONE
            binding.chipGroupFillers.visibility = View.VISIBLE
            result.fillerWords.forEach { (word, count) ->
                val chip = Chip(this).apply {
                    text = "$word ($count)"
                    setChipBackgroundColorResource(R.color.filler_chip_bg)
                    setTextColor(getColor(R.color.filler_chip_text))
                    isClickable = false
                }
                binding.chipGroupFillers.addView(chip)
            }
        }

        // Grammar feedback
        if (result.grammarFeedback.isEmpty()) {
            binding.rvGrammar.visibility = View.GONE
            binding.tvNoGrammarIssues.visibility = View.VISIBLE
        } else {
            binding.rvGrammar.visibility = View.VISIBLE
            binding.tvNoGrammarIssues.visibility = View.GONE
            binding.rvGrammar.adapter = GrammarFeedbackAdapter(result.grammarFeedback)
        }

        // Next steps
        binding.rvNextSteps.adapter = NextStepsAdapter(result.nextSteps)

        // Strengths
        binding.llStrengths.removeAllViews()
        result.strengths.forEach { strength ->
            val chip = Chip(this).apply {
                text = strength
                setChipBackgroundColorResource(R.color.strength_chip_bg)
                setTextColor(getColor(R.color.strength_chip_text))
                isClickable = false
                chipIcon = getDrawable(R.drawable.ic_check_circle)
            }
            binding.llStrengths.addView(chip)
        }
    }

    private fun animateScore(progressBar: com.mikhaellopez.circularprogressbar.CircularProgressBar, score: Int) {
        progressBar.apply {
            progressMax = 100f
            setProgressWithAnimation(score.toFloat(), 1200)
        }
    }

    private fun saveSession(result: AnalysisResult) {
        lifecycleScope.launch {
            val saveResult = firebaseManager.saveSession(result)
            saveResult.onSuccess {
                binding.tvSaved.visibility = View.VISIBLE
            }.onFailure {
                Snackbar.make(binding.root, "Could not save session. Check your Firestore rules.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (intent.getBooleanExtra(EXTRA_IS_HISTORY, false)) {
            finish()
        } else {
            navigateToHome()
        }
        return true
    }
}
