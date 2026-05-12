package com.serene.mentor.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serene.mentor.models.AnalysisResult
import com.serene.mentor.utils.SpeechAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordingViewModel : ViewModel() {

    // ─── Recording State ──────────────────────────────────────

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _transcript = MutableLiveData("")
    val transcript: LiveData<String> = _transcript

    private val _durationSeconds = MutableLiveData(0L)
    val durationSeconds: LiveData<Long> = _durationSeconds

    private val _analysisState = MutableLiveData<UiState<AnalysisResult>>()
    val analysisState: LiveData<UiState<AnalysisResult>> = _analysisState

    // Internal transcript builder
    private val transcriptBuilder = StringBuilder()

    // ─── Transcript Management ────────────────────────────────

    fun appendTranscript(text: String) {
        transcriptBuilder.append(" ").append(text)
        _transcript.value = transcriptBuilder.toString().trim()
    }

    fun clearTranscript() {
        transcriptBuilder.clear()
        _transcript.value = ""
    }

    fun hasTranscript(): Boolean = transcriptBuilder.isNotBlank()

    // ─── Recording Control ────────────────────────────────────

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    fun setDuration(seconds: Long) {
        _durationSeconds.value = seconds
    }

    // ─── Analysis ─────────────────────────────────────────────

    fun analyzeTranscript(
        topic: String,
        topicCategory: String,
        difficulty: String,
        durationSeconds: Long
    ) {
        val text = transcriptBuilder.toString().trim()
        if (text.isBlank()) return

        _analysisState.value = UiState.Loading

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                SpeechAnalyzer.analyze(
                    transcript = text,
                    topic = topic,
                    topicCategory = topicCategory,
                    difficulty = difficulty,
                    durationSeconds = durationSeconds
                )
            }
            _analysisState.value = UiState.Success(result)
        }
    }

    // ─── Reset ────────────────────────────────────────────────

    fun reset() {
        clearTranscript()
        _isRecording.value = false
        _durationSeconds.value = 0L
        _analysisState.value = null
    }
}
