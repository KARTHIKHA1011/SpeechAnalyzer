package com.serene.mentor.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.serene.mentor.activities.MainActivity
import com.serene.mentor.databinding.FragmentDashboardBinding
import com.serene.mentor.firebase.FirebaseManager
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
        setupClickListeners()
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            firebaseManager.getUserProfile().onSuccess { user ->
                binding.tvWelcome.text = "Welcome back,\n${user.name.split(" ")[0]} 👋"
                val fluency = user.averageFluency.toInt()
                val confidence = user.averageConfidence.toInt()
                binding.tvFluencyStat.text = if (fluency > 0) "$fluency%" else "--"
                binding.tvConfidenceStat.text = if (confidence > 0) "$confidence%" else "--"
                binding.tvSessionsStat.text = user.totalSessions.toString()
            }.onFailure {
                binding.tvWelcome.text = "Welcome back! 👋"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnStartPractice.setOnClickListener {
            (activity as? MainActivity)?.navigateToTopicSelection()
        }
        binding.btnViewHistory.setOnClickListener {
            (activity as? MainActivity)?.setSelectedTab(com.serene.mentor.R.id.nav_history)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
