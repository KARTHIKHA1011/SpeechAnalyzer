package com.serene.mentor.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.serene.mentor.R
import com.serene.mentor.databinding.ActivityPreparationBinding
import com.serene.mentor.models.GDFramework

class PreparationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreparationBinding

    companion object {
        const val EXTRA_TOPIC_TITLE = "topic_title"
        const val EXTRA_TOPIC_CATEGORY = "topic_category"
        const val EXTRA_TOPIC_DIFFICULTY = "topic_difficulty"
        const val EXTRA_TOPIC_DESCRIPTION = "topic_description"
        const val EXTRA_TOPIC_FRAMEWORK = "topic_framework"
        const val EXTRA_TOPIC_OPENER = "topic_opener"
        const val EXTRA_TOPIC_POINTS = "topic_points"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreparationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Preparation"

        loadTopicData()
        setupStartButton()
    }

    private fun loadTopicData() {
        val title = intent.getStringExtra(EXTRA_TOPIC_TITLE) ?: ""
        val description = intent.getStringExtra(EXTRA_TOPIC_DESCRIPTION) ?: ""
        val frameworkName = intent.getStringExtra(EXTRA_TOPIC_FRAMEWORK) ?: ""
        val opener = intent.getStringExtra(EXTRA_TOPIC_OPENER) ?: ""
        val points = intent.getStringArrayListExtra(EXTRA_TOPIC_POINTS) ?: arrayListOf()
        val difficulty = intent.getStringExtra(EXTRA_TOPIC_DIFFICULTY) ?: ""
        val category = intent.getStringExtra(EXTRA_TOPIC_CATEGORY) ?: ""

        // Topic Header
        binding.tvTopicTitle.text = title
        binding.tvTopicDescription.text = description
        binding.chipDifficulty.text = difficulty
        binding.chipCategory.text = category.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }

        // Framework
        val framework = try { GDFramework.valueOf(frameworkName) } catch (e: Exception) { GDFramework.PREP }
        binding.tvFrameworkTitle.text = framework.displayName
        binding.tvFrameworkDescription.text = framework.description

        // Key Points
        binding.llKeyPoints.removeAllViews()
        points.forEachIndexed { index, point ->
            val tv = TextView(this).apply {
                text = "${index + 1}. $point"
                setTextAppearance(R.style.TextAppearance_SereneMentor_BodyMedium)
                setPadding(0, 8, 0, 8)
            }
            binding.llKeyPoints.addView(tv)
        }

        // Sample Opener
        binding.tvSampleOpener.text = "\"$opener\""
    }

    private fun setupStartButton() {
        binding.btnStartRecording.setOnClickListener {
            val intent = Intent(this, RecordingActivity::class.java).apply {
                putExtra(RecordingActivity.EXTRA_TOPIC_TITLE, intent.getStringExtra(EXTRA_TOPIC_TITLE))
                putExtra(RecordingActivity.EXTRA_TOPIC_CATEGORY, intent.getStringExtra(EXTRA_TOPIC_CATEGORY))
                putExtra(RecordingActivity.EXTRA_TOPIC_DIFFICULTY, intent.getStringExtra(EXTRA_TOPIC_DIFFICULTY))
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
