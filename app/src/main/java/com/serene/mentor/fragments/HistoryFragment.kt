package com.serene.mentor.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.serene.mentor.adapters.SessionHistoryAdapter
import com.serene.mentor.databinding.FragmentHistoryBinding
import com.serene.mentor.firebase.FirebaseManager
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSessions()
    }

    private fun loadSessions() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            firebaseManager.getSessions().onSuccess { sessions ->
                binding.progressBar.visibility = View.GONE
                if (sessions.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvSessions.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvSessions.visibility = View.VISIBLE
                    binding.rvSessions.adapter = SessionHistoryAdapter(sessions)
                }
            }.onFailure {
                binding.progressBar.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
