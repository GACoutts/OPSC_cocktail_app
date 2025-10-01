package com.example.mixmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.os.Build
import android.widget.ArrayAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import android.content.Intent

class DiscoverPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_discover_page)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Footer navigation wiring
        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navDiscover = findViewById<ImageView>(R.id.nav_discover)
        val navList = findViewById<ImageView>(R.id.nav_list)
        val navFav = findViewById<ImageView>(R.id.nav_favourites)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)

        navDiscover?.isSelected = true

        navHome?.setOnClickListener {
            startActivity(Intent(this, HomePage::class.java))
            finish()
        }
        navDiscover?.setOnClickListener { /* already here */ }
        navList?.setOnClickListener { Toast.makeText(this, "List coming soon", Toast.LENGTH_SHORT).show() }
        navFav?.setOnClickListener { Toast.makeText(this, "Favourites coming soon", Toast.LENGTH_SHORT).show() }
        navProfile?.setOnClickListener { Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show() }

        // Setup dropdown adapters
        val ingredientView = findViewById<MaterialAutoCompleteTextView>(R.id.ac_filter_ingredient)
        val alcoholView = findViewById<MaterialAutoCompleteTextView>(R.id.ac_filter_alcohol)
        val ratingView = findViewById<MaterialAutoCompleteTextView>(R.id.ac_filter_rating)

        fun adapterFromArray(arrayId: Int) = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(arrayId)
        )

        ingredientView?.setAdapter(adapterFromArray(R.array.ingredient_options))
        alcoholView?.setAdapter(adapterFromArray(R.array.alcohol_type_options))
        ratingView?.setAdapter(adapterFromArray(R.array.rating_options))

        // Set default display text (first item in each array)
        ingredientView?.setText(resources.getStringArray(R.array.ingredient_options).first(), false)
        alcoholView?.setText(resources.getStringArray(R.array.alcohol_type_options).first(), false)
        ratingView?.setText(resources.getStringArray(R.array.rating_options).first(), false)

        // Ensure tapping the field opens the menu
        ingredientView?.setOnClickListener { ingredientView.showDropDown() }
        alcoholView?.setOnClickListener { alcoholView.showDropDown() }
        ratingView?.setOnClickListener { ratingView.showDropDown() }
    }
}
