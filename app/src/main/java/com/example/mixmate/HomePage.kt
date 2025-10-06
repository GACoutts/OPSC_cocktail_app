package com.example.mixmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomePage : AppCompatActivity() {

   private lateinit var btnLogOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //btnLogOut = findViewById(R.id.btnL)

        btnLogOut.setOnClickListener {
            // Navigate to main app (DiscoverPage)
            val intent = Intent(this, DiscoverPage::class.java)
            startActivity(intent)
            finish()
        }
    }
}