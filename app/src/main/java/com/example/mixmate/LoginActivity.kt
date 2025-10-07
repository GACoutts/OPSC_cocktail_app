package com.example.mixmate

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

/**
 * Login activity handling Firebase Authentication
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    
    // UI Elements
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        initializeViews()
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupClickListeners()
        
        // Check if user is already logged in
        if (UserManager.isLoggedIn(this)) {
            navigateToHome()
        }
    }
    
    private fun initializeViews() {
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            attemptLogin()
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        tvForgotPassword.setOnClickListener {
            // Navigate to forgot password activity or show dialog
            // For now, just show toast
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (!validateInput(email, password)) {
            return
        }

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                
                if (task.isSuccessful) {
                    // Login success
                    val user = auth.currentUser
                    user?.let {
                        // Initialize auth listener to sync user data
                        UserManager.initializeAuthListener(this)
                        
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    }
                } else {
                    // Login failed
                    val errorMessage = when {
                        task.exception?.message?.contains("password") == true -> 
                            "Invalid password. Please try again."
                        task.exception?.message?.contains("user") == true -> 
                            "No account found with this email."
                        task.exception?.message?.contains("network") == true -> 
                            "Network error. Please check your connection."
                        else -> 
                            "Login failed. Please try again."
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            tilEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Please enter a valid email"
            isValid = false
        } else {
            tilEmail.error = null
        }

        // Validate password
        if (password.isEmpty()) {
            tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        tvSignUp.isEnabled = !show
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}