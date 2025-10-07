package com.example.mixmate.ui.details

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mixmate.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.engine.DiskCacheStrategy

class RecipeDetailsActivity : ComponentActivity() {

    private val vm: RecipeDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        val ivPhoto = findViewById<ImageView>(R.id.ivPhoto)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvIngredients = findViewById<TextView>(R.id.tvIngredients)
        val tvInstructions = findViewById<TextView>(R.id.tvInstructions)
        val btnFav = findViewById<ImageButton>(R.id.btnFav)

        val id = intent.getStringExtra("cocktail_id")
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "Missing cocktail id", Toast.LENGTH_SHORT).show()

            finish()
            return
        }

        vm.load(id)

        lifecycleScope.launch {
            vm.ui.collectLatest { s ->
                tvName.text = s.name
                tvIngredients.text = s.ingredients
                tvInstructions.text = s.instructions

                if (s.imageUrl.isNotBlank()) {
                    Glide.with(this@RecipeDetailsActivity)
                        .load(s.imageUrl)
                        .into(ivPhoto)
                } else {
                    ivPhoto.setImageResource(R.drawable.ic_launcher_foreground)
                }

                btnFav.setImageResource(
                    if (s.isFavorited) R.drawable.ic_heart_filled
                    else R.drawable.ic_heart_outline
                )

                s.error?.let {
                    Toast.makeText(this@RecipeDetailsActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnFav.setOnClickListener { vm.toggleFavorite() }
    }
}
