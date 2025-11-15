package com.example.mixmate.ui.details

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.mixmate.R
import com.example.mixmate.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipeDetailsActivity : ComponentActivity() {

    private lateinit var vm: RecipeDetailsViewModel

    // keep your locale hook
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(com.example.mixmate.LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        // views present in activity_recipe_details.xml
        val ivPhoto = findViewById<ImageView>(R.id.ivPhoto)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvIngredients = findViewById<TextView>(R.id.tvIngredients)
        val tvInstructions = findViewById<TextView>(R.id.tvInstructions)
        val btnFav = findViewById<ImageButton>(R.id.btnFav)

        // VM
        val userId = UserManager.getCurrentUserUid() ?: "default_user"
        vm = ViewModelProvider(
            this,
            RecipeDetailsViewModelFactory(userId)
        )[RecipeDetailsViewModel::class.java]

        // always set initial (from adapter extras)
        val cocktailName = intent.getStringExtra("cocktail_name").orEmpty()
        val cocktailImage = intent.getStringExtra("cocktail_image").orEmpty()
        vm.setInitial(cocktailName, cocktailImage)

        // then load full details if we have an id, or search by name as fallback
        val cocktailId = intent.getStringExtra("cocktail_id")
        if (!cocktailId.isNullOrBlank()) {
            vm.load(cocktailId)
        } else if (cocktailName.isNotBlank()) {
            // Fallback: Try to find the cocktail by name
            vm.findByNameThenLoad(cocktailName)
        }

        // observe UI state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.ui.collectLatest { s ->
                    tvName.text = s.name
                    tvIngredients.text = s.ingredients
                    tvInstructions.text = s.instructions

                    if (s.imageUrl.isNotBlank()) {
                        Glide.with(this@RecipeDetailsActivity)
                            .load(s.imageUrl)
                            .placeholder(R.drawable.ic_default_cocktail)
                            .error(R.drawable.ic_default_cocktail)
                            .centerCrop()
                            .into(ivPhoto)
                    } else {
                        ivPhoto.setImageResource(R.drawable.ic_default_cocktail)
                    }

                    btnFav.setImageResource(
                        if (s.isFavorited) R.drawable.ic_heart_filled
                        else R.drawable.ic_heart_outline
                    )

                    s.error?.let {
                        if (it.isNotBlank()) {
                            Toast.makeText(this@RecipeDetailsActivity, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        btnFav.setOnClickListener { vm.toggleFavorite() }
    }
}

class RecipeDetailsViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailsViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
