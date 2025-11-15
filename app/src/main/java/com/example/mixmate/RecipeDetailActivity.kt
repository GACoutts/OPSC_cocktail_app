package com.example.mixmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixmate.data.local.CustomRecipeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailActivity : AppCompatActivity() {

    // Content views (match your existing layout IDs)
    private lateinit var ivPhoto: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvRecipeDescription: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvPrepTime: TextView
    private lateinit var rvIngredients: RecyclerView
    private lateinit var tvInstructions: TextView

    // Optional details views (keep if they exist in your layout, else they’re just never used)
    private lateinit var layoutOptionalDetails: LinearLayout
    private lateinit var layoutGlassware: LinearLayout
    private lateinit var tvGlassware: TextView
    private lateinit var layoutGarnish: LinearLayout
    private lateinit var tvGarnish: TextView

    // Adapter
    private lateinit var ingredientsAdapter: IngredientDetailAdapter

    // Recipe data
    private var recipe: CustomRecipeEntity? = null

    companion object {
        // Local (Room) custom recipe
        const val EXTRA_RECIPE_ID = "extra_recipe_id"

        // External cocktail (e.g., from Discover / Favourites)
        const val EXTRA_EXTERNAL_ID = "cocktail_id" // if you fetch later
        const val EXTRA_NAME = "recipe_name"
        const val EXTRA_IMAGE_URL = "recipe_image"

        fun launch(context: Context, localRecipeId: Long) {
            context.startActivity(
                Intent(context, RecipeDetailActivity::class.java)
                    .putExtra(EXTRA_RECIPE_ID, localRecipeId)
            )
        }

        fun launch(
            context: Context,
            name: String?,
            imageUrl: String?,
            externalId: String? = null
        ) {
            context.startActivity(
                Intent(context, RecipeDetailActivity::class.java)
                    .putExtra(EXTRA_NAME, name)
                    .putExtra(EXTRA_IMAGE_URL, imageUrl)
                    .putExtra(EXTRA_EXTERNAL_ID, externalId)
            )
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // keep the same layout you already use here
        setContentView(R.layout.activity_recipe_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        initializeViews()
        setupRecyclerView()
        resolveAndLoad()
    }

    private fun initializeViews() {
        try {
            // Bind to your existing IDs
            ivPhoto = findViewById(R.id.ivPhoto)
            tvName = findViewById(R.id.tvName)
            tvInstructions = findViewById(R.id.tvInstructions)

            // Optional views - use safe initialization
            tvRecipeDescription = findViewById<TextView?>(R.id.tv_recipe_description) ?: TextView(this).apply { visibility = View.GONE }
            tvDifficulty = findViewById<TextView?>(R.id.tv_difficulty) ?: TextView(this).apply { visibility = View.GONE }
            tvPrepTime = findViewById<TextView?>(R.id.tv_prep_time) ?: TextView(this).apply { visibility = View.GONE }
            rvIngredients = findViewById<RecyclerView?>(R.id.rv_ingredients) ?: RecyclerView(this).apply { visibility = View.GONE }

            // Optional blocks — keep only if these IDs exist in your XML
            layoutOptionalDetails = findViewById<LinearLayout?>(R.id.layout_optional_details) ?: LinearLayout(this).apply { visibility = View.GONE }
            layoutGlassware = findViewById<LinearLayout?>(R.id.layout_glassware) ?: LinearLayout(this).apply { visibility = View.GONE }
            tvGlassware = findViewById<TextView?>(R.id.tv_glassware) ?: TextView(this).apply { visibility = View.GONE }
            layoutGarnish = findViewById<LinearLayout?>(R.id.layout_garnish) ?: LinearLayout(this).apply { visibility = View.GONE }
            tvGarnish = findViewById<TextView?>(R.id.tv_garnish) ?: TextView(this).apply { visibility = View.GONE }

            // Setup back button
            findViewById<ImageButton?>(R.id.btn_back)?.setOnClickListener {
                finish()
            }
        } catch (e: Exception) {
            Log.e("RecipeDetailActivity", "Error initializing views", e)
            Toast.makeText(this, "Error loading recipe view", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        ingredientsAdapter = IngredientDetailAdapter()
        rvIngredients.apply {
            adapter = ingredientsAdapter
            layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
        }
    }

    /** Decide how to load based on the Intent we got. */
    private fun resolveAndLoad() {
        Log.d("RecipeDetailActivity", "resolveAndLoad called")
        val localId = intent.getLongExtra(EXTRA_RECIPE_ID, -1L)
        Log.d("RecipeDetailActivity", "Received EXTRA_RECIPE_ID: $localId")

        if (localId > 0L) {
            Log.d("RecipeDetailActivity", "Loading recipe from database with ID: $localId")
            loadRecipeFromDatabase(localId)
            return
        }

        val passedName = intent.getStringExtra(EXTRA_NAME)
        val passedImage = intent.getStringExtra(EXTRA_IMAGE_URL)
        Log.d("RecipeDetailActivity", "Received name: $passedName, image: $passedImage")

        if (!passedName.isNullOrBlank() || !passedImage.isNullOrBlank()) {
            Log.d("RecipeDetailActivity", "Populating from lightweight data")
            populateFromLightweight(passedName, passedImage)
        } else {
            Log.e("RecipeDetailActivity", "Missing recipe details - showing toast and finishing")
            Toast.makeText(this, "Missing recipe details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadRecipeFromDatabase(recipeId: Long) {
        Log.d("RecipeDetailActivity", "loadRecipeFromDatabase called with ID: $recipeId")
        lifecycleScope.launch {
            try {
                val userId = UserManager.getCurrentUserUid() ?: ""
                Log.d("RecipeDetailActivity", "Current userId: $userId")

                val loaded = withContext(Dispatchers.IO) {
                    MixMateApp.db.customRecipeDao().getCustomRecipeById(recipeId, userId)
                }

                if (loaded != null) {
                    Log.d("RecipeDetailActivity", "Recipe loaded successfully: ${loaded.name}")
                    Log.d("RecipeDetailActivity", "Recipe has ${loaded.ingredients.size} ingredients")
                    Log.d("RecipeDetailActivity", "Recipe description: ${loaded.description}")
                    Log.d("RecipeDetailActivity", "Recipe instructions: ${loaded.instructions}")
                    populateViews(loaded)
                } else {
                    Log.e("RecipeDetailActivity", "Recipe not found in database for ID: $recipeId, userId: $userId")
                    Toast.makeText(this@RecipeDetailActivity, "Recipe not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailActivity", "Error loading recipe from database", e)
                Toast.makeText(this@RecipeDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /** Populate UI when we only have basic info (name / image). */
    private fun populateFromLightweight(name: String?, imageUrl: String?) {
        recipe = null

        runCatching {
            tvName.text = name ?: getString(R.string.app_name)
            // If your layout doesn't have these, delete the assignments:
            tvRecipeDescription.text = ""
            tvDifficulty.text = getString(R.string.not_specified)
            tvPrepTime.text = getString(R.string.not_specified)
            tvInstructions.text = "No instructions available"

            // No ingredients → hide list if present
            ingredientsAdapter.updateIngredients(emptyList())

            // Hide optional sections if present
            layoutOptionalDetails.visibility = View.GONE
            layoutGlassware.visibility = View.GONE
            layoutGarnish.visibility = View.GONE

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_default_cocktail)
                    .error(R.drawable.ic_default_cocktail)
                    .centerCrop()
                    .into(ivPhoto)
            } else {
                ivPhoto.setImageResource(R.drawable.ic_default_cocktail)
                ivPhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        }.onFailure { e ->
            Log.e("RecipeDetailActivity", "Error populating lightweight view", e)
        }
    }

    /** Populate UI from a full Room entity. */
    private fun populateViews(recipe: CustomRecipeEntity) {
        Log.d("RecipeDetailActivity", "populateViews called for recipe: ${recipe.name}")
        this.recipe = recipe

        runCatching {
            Log.d("RecipeDetailActivity", "Setting recipe name: ${recipe.name}")
            tvName.text = recipe.name
            tvRecipeDescription.text = recipe.description.orEmpty()
            tvInstructions.text = recipe.instructions.orEmpty()
            tvDifficulty.text = recipe.difficulty ?: getString(R.string.not_specified)

            val prepTime = recipe.preparationTime
            tvPrepTime.text = if (prepTime != null && prepTime > 0) {
                getString(R.string.minutes_fmt, prepTime)
            } else {
                getString(R.string.not_specified)
            }
            Log.d("RecipeDetailActivity", "Basic fields populated successfully")
        }.onFailure { e ->
            Log.e("RecipeDetailActivity", "Error populating basic fields", e)
        }

        if (!recipe.imageUri.isNullOrEmpty()) {
            Log.d("RecipeDetailActivity", "Loading image from URI: ${recipe.imageUri}")
            Glide.with(this)
                .load(recipe.imageUri)
                .placeholder(R.drawable.ic_default_cocktail)
                .error(R.drawable.ic_default_cocktail)
                .centerCrop()
                .into(ivPhoto)
        } else {
            Log.d("RecipeDetailActivity", "No image URI, using default")
            ivPhoto.setImageResource(R.drawable.ic_default_cocktail)
            ivPhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        runCatching {
            Log.d("RecipeDetailActivity", "Updating ingredients adapter with ${recipe.ingredients.size} ingredients")
            ingredientsAdapter.updateIngredients(recipe.ingredients)
        }.onFailure { e ->
            Log.e("RecipeDetailActivity", "Error updating ingredients", e)
        }

        var hasOptional = false
        runCatching {
            if (!recipe.glassware.isNullOrEmpty()) {
                tvGlassware.text = recipe.glassware
                layoutGlassware.visibility = View.VISIBLE
                hasOptional = true
            } else layoutGlassware.visibility = View.GONE

            if (!recipe.garnish.isNullOrEmpty()) {
                tvGarnish.text = recipe.garnish
                layoutGarnish.visibility = View.VISIBLE
                hasOptional = true
            } else layoutGarnish.visibility = View.GONE

            layoutOptionalDetails.visibility = if (hasOptional) View.VISIBLE else View.GONE
        }
    }
}
