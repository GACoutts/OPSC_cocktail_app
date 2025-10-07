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

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var edtxEmail: EditText
    private lateinit var edtxPassword: EditText
    private lateinit var btnLogIn: Button
    private lateinit var btnSignUpPage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        edtxEmail = findViewById(R.id.edtxEmail)
        edtxPassword = findViewById(R.id.edtxPassword)
        btnLogIn = findViewById(R.id.btnLogIn)
        btnSignUpPage = findViewById(R.id.btnSignUp)

        btnLogIn.setOnClickListener { handleLogin() }

        btnSignUpPage.setOnClickListener {
            startActivity(Intent(this, SignUpPage::class.java))
        }

        // Auto-skip if user already logged in
        auth.currentUser?.let {
            startActivity(Intent(this, DiscoverPage::class.java))
            finish()
        }
    }

    private fun handleLogin() {
        val email = edtxEmail.text.toString().trim()
        val password = edtxPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DiscoverPage::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
