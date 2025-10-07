package com.example.mixmate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class EditProfileActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: Button
    private lateinit var imgProfile: ShapeableImageView
    private lateinit var btnChangePhoto: TextView
    private lateinit var etDisplayName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText

    private var selectedImageUri: Uri? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            loadImageIntoView(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
        loadCurrentProfileData()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btn_back)
        btnSave = findViewById(R.id.btn_save)
        imgProfile = findViewById(R.id.img_profile)
        btnChangePhoto = findViewById(R.id.btn_change_photo)
        etDisplayName = findViewById(R.id.et_display_name)
        etUsername = findViewById(R.id.et_username)
        etEmail = findViewById(R.id.et_email)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnSave.setOnClickListener {
            saveProfileChanges()
        }

        btnChangePhoto.setOnClickListener {
            openImagePicker()
        }

        imgProfile.setOnClickListener {
            openImagePicker()
        }
    }

    private fun loadCurrentProfileData() {
        // Load current user data from UserManager
        etDisplayName.setText(UserManager.getDisplayName(this))
        etUsername.setText(UserManager.getUsername(this).removePrefix("@"))
        etEmail.setText(UserManager.getCurrentUserEmail(this) ?: "")

        // Load profile picture if available
        val profilePictureUri = UserManager.getProfilePictureUri(this)
        if (!profilePictureUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(profilePictureUri)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .centerCrop()
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun openImagePicker() {
        try {
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e("EditProfileActivity", "Error opening image picker", e)
            Toast.makeText(this, "Error opening image picker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImageIntoView(uri: Uri) {
        try {
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .centerCrop()
                .into(imgProfile)
        } catch (e: Exception) {
            Log.e("EditProfileActivity", "Error loading selected image", e)
            Toast.makeText(this, "Error loading selected image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfileChanges() {
        val displayName = etDisplayName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()

        // Validate input
        if (displayName.isEmpty()) {
            etDisplayName.error = "Display name is required"
            etDisplayName.requestFocus()
            return
        }

        if (username.isEmpty()) {
            etUsername.error = "Username is required"
            etUsername.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email address"
            etEmail.requestFocus()
            return
        }

        try {
            // Parse display name to get first name and surname
            val parts = displayName.split(" ", limit = 2)
            val firstName = parts.getOrElse(0) { displayName }
            val lastName = parts.getOrElse(1) { "" }
            
            // Save user data using UserManager
            UserManager.saveUserData(this, firstName, lastName, username)
            
            // Save profile picture URI if selected
            selectedImageUri?.let {
                UserManager.saveProfilePictureUri(this, it.toString())
            }
            
            // Save username to Firestore if user is logged in
            UserManager.getCurrentUserUid()?.let { userId ->
                UserManager.saveUsernameToFirestore(
                    userId, 
                    username,
                    onSuccess = {
                        Log.d("EditProfileActivity", "Username saved to Firestore")
                    },
                    onFailure = { exception ->
                        Log.w("EditProfileActivity", "Failed to save username to Firestore", exception)
                    }
                )
            }

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            Log.d("EditProfileActivity", "Profile updated successfully")

            // Go back to previous screen
            finish()
        } catch (e: Exception) {
            Log.e("EditProfileActivity", "Error saving profile changes", e)
            Toast.makeText(this, "Error saving profile changes", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}