package com.serene.mentor.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serene.mentor.R
import com.serene.mentor.databinding.ItemGrammarFeedbackBinding
import com.serene.mentor.models.GrammarFeedback

class GrammarFeedbackAdapter(
    private val items: List<GrammarFeedback>
) : RecyclerView.Adapter<GrammarFeedbackAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemGrammarFeedbackBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGrammarFeedbackBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvOriginal.text = item.original
            tvSuggestion.text = item.suggestion
            tvExplanation.text = item.explanation

            val (bgColor, iconRes) = when (item.type) {
                "TENSE" -> Pair(R.color.tense_bg, R.drawable.ic_grammar_tense)
                "REPETITION" -> Pair(R.color.repetition_bg, R.drawable.ic_grammar_repeat)
                "FRAGMENT" -> Pair(R.color.fragment_bg, R.drawable.ic_grammar_fragment)
                else -> Pair(R.color.filler_bg, R.drawable.ic_grammar_filler)
            }
            cardGrammar.setCardBackgroundColor(root.context.getColor(bgColor))
            ivGrammarIcon.setImageResource(iconRes)
            chipType.text = item.type.replace("_", " ")
        }
    }

    override fun getItemCount() = items.size
}
