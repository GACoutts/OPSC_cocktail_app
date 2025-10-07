package com.example.mixmate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.mixmate.data.local.CustomRecipeEntity
import com.example.mixmate.data.local.CustomIngredient
import com.example.mixmate.data.remote.FirebaseRecipeRepository
import com.example.mixmate.data.repository.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubmitRecipeActivity : AppCompatActivity() {

    // Repository and coroutine scope
    private lateinit var recipeRepository: RecipeRepository
    private val activityScope = CoroutineScope(Dispatchers.Main)

    // Header views
    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: Button
    
    // Image upload views
    private lateinit var cardImageUpload: MaterialCardView
    private lateinit var imgRecipePhoto: ImageView
    private lateinit var btnRemoveImage: ImageButton
    
    // Form views
    private lateinit var tilRecipeName: TextInputLayout
    private lateinit var etRecipeName: TextInputEditText
    private lateinit var tilDescription: TextInputLayout
    private lateinit var etDescription: TextInputEditText
    
    // Ingredients section
    private lateinit var rvIngredients: RecyclerView
    private lateinit var btnAddIngredient: Button
    private lateinit var ingredientsAdapter: IngredientsAdapter
    private val ingredientsList = mutableListOf<Ingredient>()
    
    // Instructions section
    private lateinit var tilInstructions: TextInputLayout
    private lateinit var etInstructions: TextInputEditText
    
    // Optional fields
    private lateinit var tilGlassware: TextInputLayout
    private lateinit var etGlassware: TextInputEditText
    private lateinit var tilGarnish: TextInputLayout
    private lateinit var etGarnish: TextInputEditText
    
    // Additional fields
    private lateinit var tilPreparationTime: TextInputLayout
    private lateinit var etPreparationTime: TextInputEditText
    private lateinit var tilDifficulty: TextInputLayout
    private lateinit var etDifficulty: AutoCompleteTextView
    
    // Image handling
    private var selectedImageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                updateImagePreview()
            }
        }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle camera result
            updateImagePreview()
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showImageSourceDialog()
        } else {
            Toast.makeText(this, "Permission required to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submit_recipe)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupDifficultyDropdown()
        initializeRepository()
        
        // Add first ingredient by default
        addIngredient()
    }

    private fun initializeViews() {
        // Header
        btnBack = findViewById(R.id.btn_back)
        btnSave = findViewById(R.id.btn_save)
        
        // Image upload
        cardImageUpload = findViewById(R.id.card_image_upload)
        imgRecipePhoto = findViewById(R.id.img_recipe_photo)
        btnRemoveImage = findViewById(R.id.btn_remove_image)
        
        // Form fields
        tilRecipeName = findViewById(R.id.til_recipe_name)
        etRecipeName = findViewById(R.id.et_recipe_name)
        tilDescription = findViewById(R.id.til_description)
        etDescription = findViewById(R.id.et_description)
        
        // Ingredients
        rvIngredients = findViewById(R.id.rv_ingredients)
        btnAddIngredient = findViewById(R.id.btn_add_ingredient)
        
        // Instructions
        tilInstructions = findViewById(R.id.til_instructions)
        etInstructions = findViewById(R.id.et_instructions)
        
        // Optional fields
        tilGlassware = findViewById(R.id.til_glassware)
        etGlassware = findViewById(R.id.et_glassware)
        tilGarnish = findViewById(R.id.til_garnish)
        etGarnish = findViewById(R.id.et_garnish)
        
        // Additional fields
        tilPreparationTime = findViewById(R.id.til_preparation_time)
        etPreparationTime = findViewById(R.id.et_preparation_time)
        tilDifficulty = findViewById(R.id.til_difficulty)
        etDifficulty = findViewById(R.id.et_difficulty)
    }
    
    private fun setupRecyclerView() {
        ingredientsAdapter = IngredientsAdapter(
            ingredients = ingredientsList,
            onRemoveClick = { position ->
                if (ingredientsList.size > 1) { // Keep at least one ingredient
                    ingredientsList.removeAt(position)
                    ingredientsAdapter.notifyItemRemoved(position)
                }
            }
        )
        rvIngredients.adapter = ingredientsAdapter
        rvIngredients.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressed()
        }
        
        btnSave.setOnClickListener {
            validateAndSubmitRecipe()
        }
        
        cardImageUpload.setOnClickListener {
            handleImageUpload()
        }
        
        btnRemoveImage.setOnClickListener {
            removeImage()
        }
        
        btnAddIngredient.setOnClickListener {
            addIngredient()
        }
    }
    
    private fun handleImageUpload() {
        // For modern Android versions (API 33+), we don't need READ_EXTERNAL_STORAGE
        // for picking images from gallery
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            showImageSourceDialog()
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    this, 
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showImageSourceDialog()
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }
    
    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Select Image")
            .setItems(arrayOf("Camera", "Gallery")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }
    
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    
    private fun updateImagePreview() {
        selectedImageUri?.let { uri ->
            imgRecipePhoto.setImageURI(uri)
            imgRecipePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
            btnRemoveImage.visibility = android.view.View.VISIBLE
        }
    }
    
    private fun removeImage() {
        selectedImageUri = null
        imgRecipePhoto.setImageResource(R.drawable.ic_add_photo) // Reset to add photo icon
        imgRecipePhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
        btnRemoveImage.visibility = android.view.View.GONE
    }
    
    private fun addIngredient() {
        ingredientsList.add(Ingredient("", "", ""))
        ingredientsAdapter.notifyItemInserted(ingredientsList.size - 1)
    }
    
    private fun validateAndSubmitRecipe() {
        var isValid = true
        
        // Validate required fields
        if (etRecipeName.text.isNullOrBlank()) {
            tilRecipeName.error = "Recipe name is required"
            isValid = false
        } else {
            tilRecipeName.error = null
        }
        
        if (etDescription.text.isNullOrBlank()) {
            tilDescription.error = "Description is required"
            isValid = false
        } else {
            tilDescription.error = null
        }
        
        if (etInstructions.text.isNullOrBlank()) {
            tilInstructions.error = "Instructions are required"
            isValid = false
        } else {
            tilInstructions.error = null
        }
        
        // Validate ingredients
        val validIngredients = ingredientsAdapter.getValidIngredients()
        if (validIngredients.isEmpty()) {
            Toast.makeText(this, "At least one ingredient is required", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        if (isValid) {
            submitRecipe()
        }
    }
    
    private fun setupDifficultyDropdown() {
        val difficultyOptions = arrayOf("Easy", "Medium", "Hard", "Expert")
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            difficultyOptions
        )
        etDifficulty.setAdapter(adapter)
    }
    
    private fun initializeRepository() {
        val customRecipeDao = MixMateApp.db.customRecipeDao()
        val firebaseRepository = FirebaseRecipeRepository()
        recipeRepository = RecipeRepository(customRecipeDao, firebaseRepository, activityScope)
    }
    
    private fun submitRecipe() {
        activityScope.launch {
            try {
                // Convert form data to CustomRecipeEntity
                val recipe = createRecipeFromForm()
                
                // Get current user ID (you'll need to implement user authentication)
                val userId = getCurrentUserId() ?: "anonymous_user"
                
                // Save using hybrid repository (offline-first with Firebase sync)
                val result = recipeRepository.saveRecipe(recipe, userId, isPublic = false)
                
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(
                            this@SubmitRecipeActivity, 
                            "Recipe saved successfully! (Syncing to cloud...)", 
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@SubmitRecipeActivity, 
                            "Failed to save recipe: ${result.exceptionOrNull()?.message}", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SubmitRecipeActivity, 
                        "Error saving recipe: ${e.message}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun createRecipeFromForm(): CustomRecipeEntity {
        val ingredients = ingredientsAdapter.getValidIngredients().map { ingredient ->
            CustomIngredient(
                name = ingredient.name,
                amount = ingredient.amount,
                unit = ingredient.unit
            )
        }
        
        return CustomRecipeEntity(
            name = etRecipeName.text.toString().trim(),
            description = etDescription.text.toString().trim(),
            instructions = etInstructions.text.toString().trim(),
            ingredients = ingredients,
            glassware = etGlassware.text.toString().trim().ifEmpty { null },
            garnish = etGarnish.text.toString().trim().ifEmpty { null },
            preparationTime = etPreparationTime.text.toString().trim().toIntOrNull(),
            difficulty = etDifficulty.text.toString().trim().ifEmpty { null },
            imageUri = selectedImageUri?.toString(),
            userId = getCurrentUserId() ?: "" // Include current user ID
        )
    }
    
    private fun getCurrentUserId(): String? {
        return try {
            if (UserManager.isLoggedIn(this)) {
                // Use Firebase UID as primary identifier, fallback to username if not available
                UserManager.getCurrentUserUid() ?: UserManager.getUsername(this)
            } else {
                null // Don't allow anonymous recipe creation
            }
        } catch (e: Exception) {
            null // Return null if there's an error - user should be logged in
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (hasUnsavedChanges()) {
            AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to leave?")
                .setPositiveButton("Discard") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
    
    private fun hasUnsavedChanges(): Boolean {
        return !etRecipeName.text.isNullOrBlank() ||
                !etDescription.text.isNullOrBlank() ||
                !etInstructions.text.isNullOrBlank() ||
                selectedImageUri != null ||
                ingredientsAdapter.hasContent()
    }
}

// Data class for ingredients
data class Ingredient(
    var name: String,
    var amount: String,
    var unit: String
)