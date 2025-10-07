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
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.Period

class SignUpPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var edtxName: EditText
    private lateinit var edtxSurname: EditText
    private lateinit var edtxEmail: EditText
    private lateinit var edtxPassword: EditText
    private lateinit var edtxDate: EditText
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

        auth = FirebaseAuth.getInstance()
        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        edtxName = findViewById(R.id.edtxName)
        edtxSurname = findViewById(R.id.edtxSurname)
        edtxEmail = findViewById(R.id.edtxEmail)
        edtxPassword = findViewById(R.id.edtxWantPassword)
        edtxDate = findViewById(R.id.edtxDate)
        btnSignUp = findViewById(R.id.btnSignUp)
    }

    private fun setupClickListeners() {
        btnSignUp.setOnClickListener { handleSignUp() }
    }

    private fun handleSignUp() {
        val name = edtxName.text.toString().trim()
        val surname = edtxSurname.text.toString().trim()
        val email = edtxEmail.text.toString().trim()
        val password = edtxPassword.text.toString().trim()
        val dateInput = edtxDate.text.toString().trim()

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.length < 6 || dateInput.isEmpty()) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        // --- AGE VALIDATION ---
        try {
            val birthDate = LocalDate.parse(dateInput) // expects "YYYY-MM-DD"
            val today = LocalDate.now()
            val age = Period.between(birthDate, today).years

            if (age < 18) {
                Toast.makeText(this, "You must be at least 18 years old to sign up.", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            return
        }

        // --- CREATE USER ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DiscoverPage::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
