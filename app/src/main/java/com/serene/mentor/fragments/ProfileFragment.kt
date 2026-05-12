package com.serene.mentor.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.serene.mentor.activities.LoginActivity
import com.serene.mentor.databinding.FragmentProfileBinding
import com.serene.mentor.firebase.FirebaseManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()
        setupClickListeners()
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            firebaseManager.getUserProfile().onSuccess { user ->
                binding.tvName.text = user.name
                binding.tvEmail.text = user.email
                binding.tvInitials.text = user.name.take(1).uppercase()
                binding.tvTotalSessions.text = user.totalSessions.toString()
                binding.tvAvgFluency.text = if (user.averageFluency > 0) "${user.averageFluency.toInt()}%" else "--"
                binding.tvAvgConfidence.text = if (user.averageConfidence > 0) "${user.averageConfidence.toInt()}%" else "--"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    firebaseManager.logout()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finishAffinity()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
