package com.example.mixmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUpPage : AppCompatActivity() {

    private lateinit var edtxName: EditText
    private lateinit var edtxSurname: EditText
    private lateinit var edtxUsername: EditText
    private lateinit var edtxPassword: EditText
    private lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        edtxName = findViewById(R.id.edtxName)
        edtxSurname = findViewById(R.id.edtxSurname)
        edtxUsername = findViewById(R.id.edtxWantUsername)
        edtxPassword = findViewById(R.id.edtxWantPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
    }
    
    private fun setupClickListeners() {
        btnSignUp.setOnClickListener {
            handleSignUp()
        }
    }
    
    private fun handleSignUp() {
        val name = edtxName.text.toString().trim()
        val surname = edtxSurname.text.toString().trim()
        val username = edtxUsername.text.toString().trim()
        val password = edtxPassword.text.toString().trim()
        
        // Basic validation
        when {
            name.isEmpty() -> {
                edtxName.error = "Name is required"
                edtxName.requestFocus()
                return
            }
            surname.isEmpty() -> {
                edtxSurname.error = "Surname is required"
                edtxSurname.requestFocus()
                return
            }
            username.isEmpty() -> {
                edtxUsername.error = "Username is required"
                edtxUsername.requestFocus()
                return
            }
            password.length < 6 -> {
                edtxPassword.error = "Password must be at least 6 characters"
                edtxPassword.requestFocus()
                return
            }
        }
        
        // Save user data
        UserManager.saveUserData(this, name, surname, username)
        
        // Show success message
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
        
        // Navigate to main app (DiscoverPage)
        val intent = Intent(this, DiscoverPage::class.java)
        startActivity(intent)
        finish() // Close signup page
    }
}