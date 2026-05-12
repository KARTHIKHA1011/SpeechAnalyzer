package com.serene.mentor.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serene.mentor.activities.AnalysisActivity
import com.serene.mentor.databinding.ItemSessionHistoryBinding
import com.serene.mentor.models.Session
import java.text.SimpleDateFormat
import java.util.Locale

class SessionHistoryAdapter(
    private val sessions: List<Session>
) : RecyclerView.Adapter<SessionHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSessionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSessionHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        with(holder.binding) {
            tvTopic.text = session.topic
            tvCategory.text = session.topicCategory.replace("_", " ")
            tvFluency.text = "${session.fluencyScore}%"
            tvConfidence.text = "${session.confidenceScore}%"
            tvDifficulty.text = session.difficulty

            session.timestamp?.let { ts ->
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvDate.text = sdf.format(ts.toDate())
            }

            val overallScore = (session.fluencyScore + session.confidenceScore) / 2
            progressOverall.progress = overallScore.toInt()
            tvOverallScore.text = "$overallScore%"

            val scoreColor = when {
                overallScore >= 75 -> com.serene.mentor.R.color.score_high
                overallScore >= 50 -> com.serene.mentor.R.color.score_mid
                else -> com.serene.mentor.R.color.score_low
            }
            tvOverallScore.setTextColor(root.context.getColor(scoreColor))

            root.setOnClickListener {
                val intent = Intent(root.context, AnalysisActivity::class.java).apply {
                    putExtra(AnalysisActivity.EXTRA_ANALYSIS_RESULT, session.toAnalysisResult())
                    putExtra("is_history", true)
                }
                root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = sessions.size
}
