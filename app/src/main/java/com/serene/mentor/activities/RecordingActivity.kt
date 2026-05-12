package com.serene.mentor.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.serene.mentor.R
import com.serene.mentor.databinding.ActivityRecordingBinding
import com.serene.mentor.utils.SpeechAnalyzer
import java.util.Locale

class RecordingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordingBinding
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var isRecording = false
    private var startTime = 0L
    private val fullTranscript = StringBuilder()

    companion object {
        const val EXTRA_TOPIC_TITLE = "topic_title"
        const val EXTRA_TOPIC_CATEGORY = "topic_category"
        const val EXTRA_TOPIC_DIFFICULTY = "topic_difficulty"
        const val PERMISSION_REQUEST_AUDIO = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topicTitle = intent.getStringExtra(EXTRA_TOPIC_TITLE) ?: "Unknown Topic"
        binding.tvTopicName.text = topicTitle

        setupSpeechRecognizer()
        setupClickListeners()
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                binding.tvStatus.text = "Listening..."
            }

            override fun onBeginningOfSpeech() {
                binding.waveformView.visibility = View.VISIBLE
            }

            override fun onRmsChanged(rmsdB: Float) {
                binding.waveformView.setLevel(rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                binding.tvStatus.text = "Processing..."
            }

            override fun onError(error: Int) {
                if (isRecording) {
                    // Avoid infinite loop on certain errors, but generally restart for continuous listening
                    if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        speechRecognizer?.startListening(recognizerIntent)
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    if (fullTranscript.isNotEmpty()) fullTranscript.append(" ")
                    fullTranscript.append(text)
                    binding.tvTranscript.text = fullTranscript.toString().trim()
                    binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
                }

                if (isRecording) {
                    speechRecognizer?.startListening(recognizerIntent)
                } else {
                    checkAndShowAnalyzeButton()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partial.isNullOrEmpty()) {
                    val currentText = fullTranscript.toString().trim()
                    val partialText = partial[0]
                    val displayText = if (currentText.isEmpty()) partialText else "$currentText $partialText"
                    binding.tvTranscript.text = displayText
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    private fun setupClickListeners() {
        binding.btnRecord.setOnClickListener {
            if (isRecording) stopRecording() else checkPermissionAndStart()
        }

        binding.btnAnalyze.setOnClickListener {
            val transcript = binding.tvTranscript.text.toString().trim()
            if (transcript.isNotBlank()) {
                navigateToAnalysis(transcript)
            } else {
                showNoTranscriptDialog()
            }
        }

        binding.btnDiscard.setOnClickListener {
            showDiscardDialog()
        }
    }

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_AUDIO
            )
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        isRecording = true
        fullTranscript.setLength(0) // Clear previous transcript
        binding.tvTranscript.text = ""
        startTime = SystemClock.elapsedRealtime()
        binding.chronometer.base = startTime
        binding.chronometer.start()

        updateUI(recording = true)
        speechRecognizer?.startListening(recognizerIntent)
    }

    private fun stopRecording() {
        isRecording = false
        binding.chronometer.stop()
        speechRecognizer?.stopListening()

        updateUI(recording = false)
        
        // Final check for analyze button visibility after a small delay to let onResults finish
        binding.root.postDelayed({
            checkAndShowAnalyzeButton()
        }, 500)
    }

    private fun checkAndShowAnalyzeButton() {
        if (binding.tvTranscript.text.isNotBlank()) {
            binding.btnAnalyze.visibility = View.VISIBLE
            binding.tvHint.text = "Review your transcript and tap Analyze when ready"
        }
    }

    private fun updateUI(recording: Boolean) {
        if (recording) {
            binding.btnRecord.setImageResource(R.drawable.ic_stop)
            binding.btnRecord.backgroundTintList = ContextCompat.getColorStateList(this, R.color.error)
            binding.tvStatus.text = "Listening..."
            binding.pulseAnimation.visibility = View.VISIBLE
            binding.pulseAnimation.playAnimation()
            binding.cardTranscript.visibility = View.VISIBLE
            binding.btnAnalyze.visibility = View.GONE
        } else {
            binding.btnRecord.setImageResource(R.drawable.ic_mic)
            binding.btnRecord.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary)
            binding.tvStatus.text = "Recording stopped"
            binding.pulseAnimation.pauseAnimation()
            binding.pulseAnimation.visibility = View.INVISIBLE
            binding.waveformView.visibility = View.INVISIBLE
        }
    }

    private fun navigateToAnalysis(transcript: String) {
        val elapsed = (SystemClock.elapsedRealtime() - startTime) / 1000
        val topicTitle = intent.getStringExtra(EXTRA_TOPIC_TITLE) ?: ""
        val topicCategory = intent.getStringExtra(EXTRA_TOPIC_CATEGORY) ?: ""
        val topicDifficulty = intent.getStringExtra(EXTRA_TOPIC_DIFFICULTY) ?: ""

        val analysisResult = SpeechAnalyzer.analyze(
            transcript = transcript,
            topic = topicTitle,
            topicCategory = topicCategory,
            difficulty = topicDifficulty,
            durationSeconds = elapsed
        )

        val intent = Intent(this, AnalysisActivity::class.java).apply {
            putExtra(AnalysisActivity.EXTRA_ANALYSIS_RESULT, analysisResult)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun showNoTranscriptDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("No Speech Detected")
            .setMessage("Please record yourself speaking before analyzing.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDiscardDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Discard Recording?")
            .setMessage("Your current recording will be lost.")
            .setPositiveButton("Discard") { _, _ -> finish() }
            .setNegativeButton("Keep", null)
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_AUDIO && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}
