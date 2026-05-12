package com.serene.mentor.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serene.mentor.databinding.ItemNextStepBinding

class NextStepsAdapter(
    private val steps: List<String>
) : RecyclerView.Adapter<NextStepsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemNextStepBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNextStepBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            tvStepNumber.text = "${position + 1}"
            tvStepText.text = steps[position]
        }
    }

    override fun getItemCount() = steps.size
}
