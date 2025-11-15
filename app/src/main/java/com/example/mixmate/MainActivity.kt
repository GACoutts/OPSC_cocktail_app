package com.example.mixmate

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.jvm.java
import androidx.credentials.*
import androidx.credentials.exceptions.*
import android.os.CancellationSignal


/**
 * Main login activity handling Firebase Authentication
 */
class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // UI Elements
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvSSOSignUp: TextView
    private lateinit var progressBar: ProgressBar

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if this is a logout scenario
        val isLogout = intent.getBooleanExtra("logout", false)
        if (isLogout) {
            // Force clear any remaining user data
            UserManager.clearUserData(this)
        }

        // Check if user is already logged in (unless this is a logout)
        if (!isLogout && UserManager.isLoggedIn(this)) {
            navigateToHome()
            return
        }

        setContentView(R.layout.activity_main)

        initializeViews()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupClickListeners()
    }

    private fun initializeViews() {
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvSSOSignUp = findViewById(R.id.tvSSOSignUp)
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

        tvSSOSignUp.setOnClickListener {
            signInWithGoogle()
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

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val cancellationSignal: android.os.CancellationSignal? = CancellationSignal()

        credentialManager.getCredentialAsync(
            context = this,
            request = request,
            cancellationSignal = cancellationSignal,
            executor = ContextCompat.getMainExecutor(this),
            callback = object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    val credential = result.credential

                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleToken.idToken)

                    } else {
                        Log.w(TAG, "Unexpected credential type: ${credential::class.java}")
                    }
                }

                override fun onError(e: GetCredentialException) {
                    Log.e(TAG, "Google Sign-In failed", e)
                }
            }
        )
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    Log.d(TAG, "Firebase auth success: ${user?.email}")

                    if (user != null) {
                        // 🔥 Load user data and persist it
                        UserManager.initializeAuthListener(this)

                        // 🔥 Navigate to home screen
                        navigateToHome()
                    }
                } else {
                    Log.w(TAG, "Firebase authentication failed", task.exception)
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
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
        startActivity(Intent(this, HomePage::class.java))
        finish()
    }
}
