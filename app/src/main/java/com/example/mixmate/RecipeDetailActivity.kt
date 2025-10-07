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

    // Header views
    private lateinit var btnBack: ImageButton
    private lateinit var btnEdit: ImageButton

    // Content views
    private lateinit var imgRecipeDetail: ImageView
    private lateinit var tvRecipeName: TextView
    private lateinit var tvRecipeDescription: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvPrepTime: TextView
    private lateinit var rvIngredients: RecyclerView
    private lateinit var tvInstructions: TextView

    // Optional details views
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
        const val EXTRA_EXTERNAL_ID = "cocktail_id"      // string id if you decide to fetch later
        const val EXTRA_NAME = "recipe_name"
        const val EXTRA_IMAGE_URL = "recipe_image"

        /** Launch using a LOCAL custom recipe id (Room). */
        fun launch(context: Context, localRecipeId: Long) {
            context.startActivity(
                Intent(context, RecipeDetailActivity::class.java)
                    .putExtra(EXTRA_RECIPE_ID, localRecipeId)
            )
        }

        /** Launch using minimal EXTERNAL info (name/image/optional id). */
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recipe_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        resolveAndLoad()
    }

    private fun initializeViews() {
        // Header
        btnBack = findViewById(R.id.btn_back)
        btnEdit = findViewById(R.id.btn_edit)

        // Content
        imgRecipeDetail = findViewById(R.id.img_recipe_detail)
        tvRecipeName = findViewById(R.id.tv_recipe_name)
        tvRecipeDescription = findViewById(R.id.tv_recipe_description)
        tvDifficulty = findViewById(R.id.tv_difficulty)
        tvPrepTime = findViewById(R.id.tv_prep_time)
        rvIngredients = findViewById(R.id.rv_ingredients)
        tvInstructions = findViewById(R.id.tv_instructions)

        // Optional
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

    private fun setupClickListeners() {
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btnEdit.setOnClickListener {
            // If you have an edit flow, wire it here. For now, navigate to submit with prefill later.
            startActivity(Intent(this, SubmitRecipeActivity::class.java))
        }
    }

    /** Decide how to load based on the Intent we got. */
    private fun resolveAndLoad() {
        val localId = intent.getLongExtra(EXTRA_RECIPE_ID, -1L)
        if (localId > 0L) {
            // Load from Room (custom recipe created by user)
            loadRecipeFromDatabase(localId)
            return
        }

        // Otherwise, show lightweight details passed in from elsewhere
        val passedName = intent.getStringExtra(EXTRA_NAME)
        val passedImage = intent.getStringExtra(EXTRA_IMAGE_URL)
        val externalId = intent.getStringExtra(EXTRA_EXTERNAL_ID) // reserved if you later fetch from API

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
                val recipe = withContext(Dispatchers.IO) {
                    val userId = UserManager.getCurrentUserUid() ?: ""
                    MixMateApp.db.customRecipeDao().getCustomRecipeById(recipeId, userId)
                val loaded = withContext(Dispatchers.IO) {
                    MixMateApp.db.customRecipeDao().getCustomRecipeById(recipeId)
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

        tvRecipeName.text = name ?: getString(R.string.app_name)
        tvRecipeDescription.text = ""          // unknown
        tvInstructions.text = ""               // unknown
        tvDifficulty.text = getString(R.string.not_specified)
        tvPrepTime.text = getString(R.string.not_specified)

        // No ingredients → hide list
        ingredientsAdapter.updateIngredients(emptyList())

        // Hide optional sections
        layoutOptionalDetails.visibility = View.GONE
        layoutGlassware.visibility = View.GONE
        layoutGarnish.visibility = View.GONE

        // Image
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_default_cocktail)
                .error(R.drawable.ic_default_cocktail)
                .centerCrop()
                .into(imgRecipeDetail)
        } else {
            imgRecipeDetail.setImageResource(R.drawable.ic_default_cocktail)
            imgRecipeDetail.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        // If lightweight, you probably don’t want edit
        btnEdit.visibility = View.GONE
    }

    /** Populate UI from a full Room entity. */
    private fun populateViews(recipe: CustomRecipeEntity) {
        this.recipe = recipe

        tvRecipeName.text = recipe.name
        tvRecipeDescription.text = recipe.description.orEmpty()
        tvInstructions.text = recipe.instructions.orEmpty()

        tvDifficulty.text = recipe.difficulty ?: getString(R.string.not_specified)

        val prepTime = recipe.preparationTime
        tvPrepTime.text = if (prepTime != null && prepTime > 0) {
            getString(R.string.minutes_fmt, prepTime)
        } else {
            getString(R.string.not_specified)
        }

        if (!recipe.imageUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(recipe.imageUri)
                .placeholder(R.drawable.ic_default_cocktail)
                .error(R.drawable.ic_default_cocktail)
                .centerCrop()
                .into(imgRecipeDetail)
        } else {
            imgRecipeDetail.setImageResource(R.drawable.ic_default_cocktail)
            imgRecipeDetail.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        ingredientsAdapter.updateIngredients(recipe.ingredients)

        var hasOptional = false
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
