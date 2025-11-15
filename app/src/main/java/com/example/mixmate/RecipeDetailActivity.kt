package com.example.mixmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
        // Bind to your existing IDs
        ivPhoto = findViewById(R.id.ivPhoto)
        tvName = findViewById(R.id.tvName)
        tvRecipeDescription = findViewById(R.id.tv_recipe_description) // if this doesn’t exist, remove this line
        tvDifficulty = findViewById(R.id.tv_difficulty)               // if not in layout, remove
        tvPrepTime = findViewById(R.id.tv_prep_time)                   // if not in layout, remove
        rvIngredients = findViewById(R.id.rv_ingredients)              // if not in layout, remove
        tvInstructions = findViewById(R.id.tvInstructions)

        // Optional blocks — keep only if these IDs exist in your XML
        layoutOptionalDetails = findViewById(R.id.layout_optional_details)
        layoutGlassware = findViewById(R.id.layout_glassware)
        tvGlassware = findViewById(R.id.tv_glassware)
        layoutGarnish = findViewById(R.id.layout_garnish)
        tvGarnish = findViewById(R.id.tv_garnish)
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
        val localId = intent.getLongExtra(EXTRA_RECIPE_ID, -1L)
        if (localId > 0L) {
            loadRecipeFromDatabase(localId)
            return
        }

        val passedName = intent.getStringExtra(EXTRA_NAME)
        val passedImage = intent.getStringExtra(EXTRA_IMAGE_URL)

        if (!passedName.isNullOrBlank() || !passedImage.isNullOrBlank()) {
            populateFromLightweight(passedName, passedImage)
        } else {
            Toast.makeText(this, "Missing recipe details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadRecipeFromDatabase(recipeId: Long) {
        lifecycleScope.launch {
            try {
                val loaded = withContext(Dispatchers.IO) {
                    val userId = UserManager.getCurrentUserUid() ?: ""
                    MixMateApp.db.customRecipeDao().getCustomRecipeById(recipeId, userId)
                }
                if (loaded != null) {
                    populateViews(loaded)
                } else {
                    Toast.makeText(this@RecipeDetailActivity, "Recipe not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailActivity", "Error loading recipe", e)
                Toast.makeText(this@RecipeDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /** Populate UI when we only have basic info (name / image). */
    private fun populateFromLightweight(name: String?, imageUrl: String?) {
        recipe = null

        tvName.text = name ?: getString(R.string.app_name)
        // If your layout doesn’t have these, delete the assignments:
        tvRecipeDescription.text = ""
        tvDifficulty.text = getString(R.string.not_specified)
        tvPrepTime.text = getString(R.string.not_specified)

        // No ingredients → hide list if present
        runCatching { ingredientsAdapter.updateIngredients(emptyList()) }

        // Hide optional sections if present
        runCatching {
            layoutOptionalDetails.visibility = View.GONE
            layoutGlassware.visibility = View.GONE
            layoutGarnish.visibility = View.GONE
        }

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
    }

    /** Populate UI from a full Room entity. */
    private fun populateViews(recipe: CustomRecipeEntity) {
        this.recipe = recipe

        tvName.text = recipe.name
        runCatching { tvRecipeDescription.text = recipe.description.orEmpty() }
        tvInstructions.text = recipe.instructions.orEmpty()
        runCatching { tvDifficulty.text = recipe.difficulty ?: getString(R.string.not_specified) }

        val prepTime = recipe.preparationTime
        runCatching {
            tvPrepTime.text = if (prepTime != null && prepTime > 0) {
                getString(R.string.minutes_fmt, prepTime)
            } else {
                getString(R.string.not_specified)
            }
        }

        if (!recipe.imageUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(recipe.imageUri)
                .placeholder(R.drawable.ic_default_cocktail)
                .error(R.drawable.ic_default_cocktail)
                .centerCrop()
                .into(ivPhoto)
        } else {
            ivPhoto.setImageResource(R.drawable.ic_default_cocktail)
            ivPhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        runCatching { ingredientsAdapter.updateIngredients(recipe.ingredients) }

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
