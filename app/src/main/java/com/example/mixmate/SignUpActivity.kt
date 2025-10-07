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
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * Sign up activity handling Firebase Authentication and user registration
 */
class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    
    // UI Elements
    private lateinit var tilName: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var tilSurname: TextInputLayout
    private lateinit var etSurname: TextInputEditText
    private lateinit var tilUsername: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignUp: Button
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        
        initializeViews()
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupClickListeners()
    }
    
    private fun initializeViews() {
        tilName = findViewById(R.id.tilName)
        etName = findViewById(R.id.etName)
        tilSurname = findViewById(R.id.tilSurname)
        etSurname = findViewById(R.id.etSurname)
        tilUsername = findViewById(R.id.tilUsername)
        etUsername = findViewById(R.id.etUsername)
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvLogin = findViewById(R.id.tvLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnSignUp.setOnClickListener {
            attemptSignUp()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptSignUp() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (!validateInput(name, surname, username, email, password, confirmPassword)) {
            return
        }

        showLoading(true)

        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration success, update profile
                    val user = auth.currentUser
                    user?.let {
                        updateUserProfile(it, name, surname, username)
                    }
                } else {
                    showLoading(false)
                    // Registration failed
                    val errorMessage = when {
                        task.exception?.message?.contains("already in use") == true -> 
                            "This email is already registered. Please use a different email."
                        task.exception?.message?.contains("weak-password") == true -> 
                            "Password is too weak. Please use a stronger password."
                        task.exception?.message?.contains("malformed") == true -> 
                            "Please enter a valid email address."
                        task.exception?.message?.contains("network") == true -> 
                            "Network error. Please check your connection."
                        else -> 
                            "Registration failed. Please try again."
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updateUserProfile(user: com.google.firebase.auth.FirebaseUser, name: String, surname: String, username: String) {
        val displayName = "$name $surname"
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { profileTask ->
                showLoading(false)
                
                if (profileTask.isSuccessful) {
                    // Save additional user data locally
                    UserManager.saveUserData(this, name, surname, username)
                    
                    // Initialize auth listener
                    UserManager.initializeAuthListener(this)
                    
                    Toast.makeText(this, "Account created successfully! Welcome to MixMate!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Profile update failed, but account was created.", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
            }
    }

    private fun validateInput(
        name: String, 
        surname: String, 
        username: String, 
        email: String, 
        password: String, 
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Validate name
        if (name.isEmpty()) {
            tilName.error = "Name is required"
            isValid = false
        } else if (name.length < 2) {
            tilName.error = "Name must be at least 2 characters"
            isValid = false
        } else {
            tilName.error = null
        }

        // Validate surname
        if (surname.isEmpty()) {
            tilSurname.error = "Surname is required"
            isValid = false
        } else if (surname.length < 2) {
            tilSurname.error = "Surname must be at least 2 characters"
            isValid = false
        } else {
            tilSurname.error = null
        }

        // Validate username
        if (username.isEmpty()) {
            tilUsername.error = "Username is required"
            isValid = false
        } else if (username.length < 3) {
            tilUsername.error = "Username must be at least 3 characters"
            isValid = false
        } else if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            tilUsername.error = "Username can only contain letters, numbers, and underscores"
            isValid = false
        } else {
            tilUsername.error = null
        }

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

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSignUp.isEnabled = !show
        tvLogin.isEnabled = !show
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}