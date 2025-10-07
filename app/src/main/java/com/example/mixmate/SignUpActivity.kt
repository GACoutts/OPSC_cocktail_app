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
import java.time.LocalDate
import java.time.Period
import android.text.TextWatcher
import android.text.Editable

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
    private lateinit var tilDateOfBirth: TextInputLayout
    private lateinit var etDateOfBirth: TextInputEditText
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
        tilDateOfBirth = findViewById(R.id.tilDateOfBirth)
        etDateOfBirth = findViewById(R.id.etDateOfBirth)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvLogin = findViewById(R.id.tvLogin)
        progressBar = findViewById(R.id.progressBar)
        
        // Set up date formatting helper
        setupDateFormatting()
    }

    private fun setupClickListeners() {
        btnSignUp.setOnClickListener {
            attemptSignUp()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
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
        val dateOfBirth = etDateOfBirth.text.toString().trim()

        if (!validateInput(name, surname, username, email, password, confirmPassword, dateOfBirth)) {
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
                if (profileTask.isSuccessful) {
                    // Save username to Firestore before signing out
                    UserManager.saveUsernameToFirestore(
                        userId = user.uid,
                        username = username,
                        onSuccess = {
                            showLoading(false)
                            // Sign out the user and redirect to login
                            auth.signOut()
                            Toast.makeText(this, "Account created successfully! Please sign in to continue.", Toast.LENGTH_LONG).show()
                            navigateToLogin()
                        },
                        onFailure = { exception ->
                            showLoading(false)
                            // Sign out even if Firestore save failed
                            auth.signOut()
                            Toast.makeText(this, "Account created, but there was an issue saving your profile. Please sign in.", Toast.LENGTH_LONG).show()
                            navigateToLogin()
                        }
                    )
                } else {
                    showLoading(false)
                    // Sign out even if profile update failed
                    auth.signOut()
                    Toast.makeText(this, "Account created, but profile setup had issues. Please sign in.", Toast.LENGTH_LONG).show()
                    navigateToLogin()
                }
            }
    }

    private fun validateInput(
        name: String, 
        surname: String, 
        username: String, 
        email: String, 
        password: String, 
        confirmPassword: String,
        dateOfBirth: String
    ): Boolean {
        var isValid = true

        // Validate name
        if (name.isEmpty()) {
            tilName.error = "First name is required"
            isValid = false
        } else if (name.length < 2) {
            tilName.error = "First name must be at least 2 characters"
            isValid = false
        } else if (!name.matches(Regex("^[a-zA-Z]+(\\s+[a-zA-Z]+)*$"))) {
            tilName.error = "First name can only contain letters and spaces"
            isValid = false
        } else {
            tilName.error = null
        }

        // Validate surname
        if (surname.isEmpty()) {
            tilSurname.error = "Last name is required"
            isValid = false
        } else if (surname.length < 2) {
            tilSurname.error = "Last name must be at least 2 characters"
            isValid = false
        } else if (!surname.matches(Regex("^[a-zA-Z]+(\\s+[a-zA-Z]+)*$"))) {
            tilSurname.error = "Last name can only contain letters and spaces"
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
        } else if (username.length > 20) {
            tilUsername.error = "Username must be less than 20 characters"
            isValid = false
        } else if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            tilUsername.error = "Username can only contain letters, numbers, and underscores"
            isValid = false
        } else if (username.startsWith("_") || username.endsWith("_")) {
            tilUsername.error = "Username cannot start or end with an underscore"
            isValid = false
        } else if (username.contains("__")) {
            tilUsername.error = "Username cannot contain consecutive underscores"
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

        // Validate date of birth and age
        if (dateOfBirth.isEmpty()) {
            tilDateOfBirth.error = "Date of birth is required"
            isValid = false
        } else {
            // Check basic format first
            if (!dateOfBirth.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                tilDateOfBirth.error = "Date must be in YYYY-MM-DD format (e.g., 2000-01-15)"
                isValid = false
            } else {
                try {
                    val birthDate = LocalDate.parse(dateOfBirth)
                    val today = LocalDate.now()
                    val age = Period.between(birthDate, today).years
                    
                    // Check if date is in the future
                    if (birthDate.isAfter(today)) {
                        tilDateOfBirth.error = "Date of birth cannot be in the future"
                        isValid = false
                    } else if (age < 18) {
                        tilDateOfBirth.error = "You must be at least 18 years old to sign up"
                        isValid = false
                    } else if (age > 120) {
                        tilDateOfBirth.error = "Please enter a valid date of birth"
                        isValid = false
                    } else {
                        tilDateOfBirth.error = null
                    }
                } catch (e: Exception) {
                    tilDateOfBirth.error = when {
                        e.message?.contains("Invalid value for MonthOfYear") == true -> "Invalid month (must be 01-12)"
                        e.message?.contains("Invalid value for DayOfMonth") == true -> "Invalid day for this month"
                        else -> "Invalid date. Please use YYYY-MM-DD format"
                    }
                    isValid = false
                }
            }
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSignUp.isEnabled = !show
        tvLogin.isEnabled = !show
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun setupDateFormatting() {
        etDateOfBirth.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val pattern = "####-##-##"
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                
                val input = s.toString().replace("-", "")
                if (input.length <= 8) {
                    isUpdating = true
                    
                    val formatted = StringBuilder()
                    for (i in input.indices) {
                        if (i == 4 || i == 6) {
                            formatted.append("-")
                        }
                        formatted.append(input[i])
                    }
                    
                    etDateOfBirth.setText(formatted.toString())
                    etDateOfBirth.setSelection(formatted.length)
                    
                    isUpdating = false
                }
            }
        })
    }
}
