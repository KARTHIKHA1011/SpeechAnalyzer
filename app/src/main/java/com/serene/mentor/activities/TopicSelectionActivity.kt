package com.serene.mentor.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.serene.mentor.R
import com.serene.mentor.databinding.ActivityTopicSelectionBinding
import com.serene.mentor.models.Difficulty
import com.serene.mentor.models.TopicCategory
import com.serene.mentor.utils.TopicRepository

class TopicSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicSelectionBinding
    private var selectedCategory: TopicCategory = TopicCategory.TECHNOLOGY
    private var selectedDifficulty: Difficulty = Difficulty.BEGINNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Choose Your Topic"

        setupCategoryChips()
        setupDifficultyChips()
        setupStartButton()
    }

    private fun setupCategoryChips() {
        TopicCategory.values().forEachIndexed { index, category ->
            val chip = Chip(this).apply {
                text = category.displayName
                isCheckable = true
                isChecked = index == 0
                setChipBackgroundColorResource(R.color.chip_selector)
                setTextColor(getColorStateList(R.color.chip_text_selector))
                chipStrokeWidth = 2f
                setChipStrokeColorResource(R.color.primary)
                tag = category
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedCategory = category
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun setupDifficultyChips() {
        Difficulty.values().forEachIndexed { index, difficulty ->
            val chip = Chip(this).apply {
                text = difficulty.displayName
                isCheckable = true
                isChecked = index == 0
                setChipBackgroundColorResource(R.color.chip_selector)
                setTextColor(getColorStateList(R.color.chip_text_selector))
                chipStrokeWidth = 2f
                setChipStrokeColorResource(R.color.primary)
                tag = difficulty
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedDifficulty = difficulty
            }
            binding.chipGroupDifficulty.addView(chip)
        }
    }

    private fun setupStartButton() {
        binding.btnContinue.setOnClickListener {
            val topic = TopicRepository.getRandomTopic(selectedCategory, selectedDifficulty)
            if (topic != null) {
                val intent = Intent(this, PreparationActivity::class.java).apply {
                    putExtra(PreparationActivity.EXTRA_TOPIC_TITLE, topic.title)
                    putExtra(PreparationActivity.EXTRA_TOPIC_CATEGORY, topic.category.name)
                    putExtra(PreparationActivity.EXTRA_TOPIC_DIFFICULTY, topic.difficulty.name)
                    putExtra(PreparationActivity.EXTRA_TOPIC_DESCRIPTION, topic.description)
                    putExtra(PreparationActivity.EXTRA_TOPIC_FRAMEWORK, topic.suggestedFramework.name)
                    putExtra(PreparationActivity.EXTRA_TOPIC_OPENER, topic.sampleOpener)
                    putStringArrayListExtra(PreparationActivity.EXTRA_TOPIC_POINTS, ArrayList(topic.keyPoints))
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
