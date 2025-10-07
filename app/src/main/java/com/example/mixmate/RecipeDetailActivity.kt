package com.example.mixmate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixmate.data.local.CustomRecipeEntity

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
        const val EXTRA_RECIPE_ID = "extra_recipe_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadRecipeData()
    }

    private fun initializeViews() {
        // Header views
        btnBack = findViewById(R.id.btn_back)
        btnEdit = findViewById(R.id.btn_edit)
        
        // Content views
        imgRecipeDetail = findViewById(R.id.img_recipe_detail)
        tvRecipeName = findViewById(R.id.tv_recipe_name)
        tvRecipeDescription = findViewById(R.id.tv_recipe_description)
        tvDifficulty = findViewById(R.id.tv_difficulty)
        tvPrepTime = findViewById(R.id.tv_prep_time)
        rvIngredients = findViewById(R.id.rv_ingredients)
        tvInstructions = findViewById(R.id.tv_instructions)
        
        // Optional details views
        layoutOptionalDetails = findViewById(R.id.layout_optional_details)
        layoutGlassware = findViewById(R.id.layout_glassware)
        tvGlassware = findViewById(R.id.tv_glassware)
        layoutGarnish = findViewById(R.id.layout_garnish)
        tvGarnish = findViewById(R.id.tv_garnish)
    }

    private fun setupRecyclerView() {
        ingredientsAdapter = IngredientDetailAdapter()
        rvIngredients.adapter = ingredientsAdapter
        rvIngredients.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressed()
        }
        
        btnEdit.setOnClickListener {
            // TODO: Navigate to edit recipe activity
            // For now, we can navigate back to SubmitRecipeActivity with the recipe data
            val intent = Intent(this, SubmitRecipeActivity::class.java)
            // You could pass the recipe data here to pre-fill the form
            startActivity(intent)
        }
    }

    private fun loadRecipeData() {
        val recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1)
        if (recipeId != -1L) {
            // Load recipe from database
            // For now, we'll simulate this. In a real app, you'd query the database
            loadRecipeFromDatabase(recipeId)
        } else {
            // Handle error - no recipe ID provided
            finish()
        }
    }

    private fun loadRecipeFromDatabase(recipeId: Long) {
        // Launch coroutine to load recipe from database
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val recipe = withContext(Dispatchers.IO) {
                    val userId = UserManager.getCurrentUserUid() ?: ""
                    MixMateApp.db.customRecipeDao().getCustomRecipeById(recipeId, userId)
                }
                
                if (recipe != null) {
                    populateViews(recipe)
                } else {
                    // Recipe not found
                    Toast.makeText(this@RecipeDetailActivity, "Recipe not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailActivity", "Error loading recipe", e)
                Toast.makeText(this@RecipeDetailActivity, "Error loading recipe: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun populateViews(recipe: CustomRecipeEntity) {
        this.recipe = recipe
        
        // Set basic info
        tvRecipeName.text = recipe.name
        tvRecipeDescription.text = recipe.description
        tvInstructions.text = recipe.instructions
        
        // Set difficulty
        tvDifficulty.text = recipe.difficulty ?: "Not specified"
        
        // Set preparation time
        val prepTime = recipe.preparationTime
        tvPrepTime.text = if (prepTime != null && prepTime > 0) {
            "$prepTime mins"
        } else {
            "Not specified"
        }
        
        // Load image
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
        
        // Set ingredients
        ingredientsAdapter.updateIngredients(recipe.ingredients)
        
        // Handle optional details
        var hasOptionalDetails = false
        
        // Glassware
        if (!recipe.glassware.isNullOrEmpty()) {
            tvGlassware.text = recipe.glassware
            layoutGlassware.visibility = View.VISIBLE
            hasOptionalDetails = true
        } else {
            layoutGlassware.visibility = View.GONE
        }
        
        // Garnish
        if (!recipe.garnish.isNullOrEmpty()) {
            tvGarnish.text = recipe.garnish
            layoutGarnish.visibility = View.VISIBLE
            hasOptionalDetails = true
        } else {
            layoutGarnish.visibility = View.GONE
        }
        
        // Show/hide optional details section
        layoutOptionalDetails.visibility = if (hasOptionalDetails) View.VISIBLE else View.GONE
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}