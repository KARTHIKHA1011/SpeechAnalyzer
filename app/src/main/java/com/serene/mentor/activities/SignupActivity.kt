package com.serene.mentor.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.serene.mentor.R
import com.serene.mentor.databinding.ActivitySignupBinding
import com.serene.mentor.firebase.FirebaseManager
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInputs(name, email, password, confirmPassword)) {
                performSignup(name, email, password)
            }
        }

        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isEmpty() || name.length < 2) {
            binding.tilName.error = "Enter your full name"
            isValid = false
        } else binding.tilName.error = null

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            isValid = false
        } else binding.tilEmail.error = null

        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else binding.tilPassword.error = null

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else binding.tilConfirmPassword.error = null

        return isValid
    }

    private fun performSignup(name: String, email: String, password: String) {
        setLoading(true)
        lifecycleScope.launch {
            val result = firebaseManager.signUp(name, email, password)
            setLoading(false)
            result.onSuccess {
                navigateToMain()
            }.onFailure { e ->
                showError(e.message ?: "Signup failed. Please try again.")
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSignup.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSignup.text = if (loading) "" else getString(R.string.create_account)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
