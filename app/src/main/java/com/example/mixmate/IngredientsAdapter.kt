package com.example.mixmate

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class IngredientsAdapter(
    private val ingredients: MutableList<Ingredient>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    // Common units for cocktail ingredients
    private val commonUnits = listOf(
        "oz", "ml", "cl", "dash", "splash", "tsp", "tbsp", "cup",
        "drop", "pinch", "slice", "wedge", "twist", "sprig", "wheel",
        "cube", "garnish", "rim", "float", "top", "layer"
    )

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tilIngredientName: TextInputLayout = itemView.findViewById(R.id.til_ingredient_name)
        private val etIngredientName: TextInputEditText = itemView.findViewById(R.id.et_ingredient_name)
        private val tilIngredientAmount: TextInputLayout = itemView.findViewById(R.id.til_ingredient_amount)
        private val etIngredientAmount: TextInputEditText = itemView.findViewById(R.id.et_ingredient_amount)
        private val tilIngredientUnit: TextInputLayout = itemView.findViewById(R.id.til_ingredient_unit)
        private val actvIngredientUnit: AutoCompleteTextView = itemView.findViewById(R.id.actv_ingredient_unit)
        private val btnRemoveIngredient: ImageButton = itemView.findViewById(R.id.btn_remove_ingredient)

        fun bind(ingredient: Ingredient, position: Int) {
            // Remove existing text watchers to prevent infinite loops
            etIngredientName.tag?.let { watcher ->
                etIngredientName.removeTextChangedListener(watcher as TextWatcher)
            }
            etIngredientAmount.tag?.let { watcher ->
                etIngredientAmount.removeTextChangedListener(watcher as TextWatcher)
            }
            actvIngredientUnit.tag?.let { watcher ->
                actvIngredientUnit.removeTextChangedListener(watcher as TextWatcher)
            }

            // Set current values
            etIngredientName.setText(ingredient.name)
            etIngredientAmount.setText(ingredient.amount)
            actvIngredientUnit.setText(ingredient.unit)

            // Setup unit dropdown
            val unitAdapter = ArrayAdapter(
                itemView.context,
                android.R.layout.simple_dropdown_item_1line,
                commonUnits
            )
            actvIngredientUnit.setAdapter(unitAdapter)

            // Create new text watchers
            val nameWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    ingredient.name = s?.toString() ?: ""
                    validateField(tilIngredientName, ingredient.name.trim().isNotEmpty())
                }
            }

            val amountWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    ingredient.amount = s?.toString() ?: ""
                    validateField(tilIngredientAmount, ingredient.amount.trim().isNotEmpty())
                }
            }

            val unitWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    ingredient.unit = s?.toString() ?: ""
                    validateField(tilIngredientUnit, ingredient.unit.trim().isNotEmpty())
                }
            }

            // Add text watchers and store them as tags
            etIngredientName.addTextChangedListener(nameWatcher)
            etIngredientName.tag = nameWatcher

            etIngredientAmount.addTextChangedListener(amountWatcher)
            etIngredientAmount.tag = amountWatcher

            actvIngredientUnit.addTextChangedListener(unitWatcher)
            actvIngredientUnit.tag = unitWatcher

            // Handle remove button
            btnRemoveIngredient.setOnClickListener {
                onRemoveClick(position)
            }

            // Show/hide remove button based on list size
            btnRemoveIngredient.visibility = if (ingredients.size > 1) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

            // Clear any existing errors on bind (don't validate empty fields initially)
            tilIngredientName.error = null
            tilIngredientAmount.error = null
            tilIngredientUnit.error = null
        }

        private fun validateField(textInputLayout: TextInputLayout, isValid: Boolean) {
            // Only show validation errors if the field is not empty or currently focused
            if (textInputLayout.editText?.hasFocus() == true) {
                // Clear errors while user is typing
                textInputLayout.error = null
            } else if (!isValid && !textInputLayout.editText?.text.isNullOrBlank()) {
                // Show error only for non-empty invalid fields that aren't focused
                textInputLayout.error = when (textInputLayout.id) {
                    R.id.til_ingredient_name -> "Ingredient name required"
                    R.id.til_ingredient_amount -> "Amount required"
                    R.id.til_ingredient_unit -> "Unit required"
                    else -> "Required"
                }
            } else {
                textInputLayout.error = null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position], position)
    }

    override fun getItemCount(): Int = ingredients.size

    /**
     * Returns list of valid ingredients (all fields filled)
     */
    fun getValidIngredients(): List<Ingredient> {
        return ingredients.filter { ingredient ->
            ingredient.name.trim().isNotEmpty() &&
            ingredient.amount.trim().isNotEmpty() &&
            ingredient.unit.trim().isNotEmpty()
        }
    }

    /**
     * Checks if adapter has any content in ingredients
     */
    fun hasContent(): Boolean {
        return ingredients.any { ingredient ->
            ingredient.name.trim().isNotEmpty() ||
            ingredient.amount.trim().isNotEmpty() ||
            ingredient.unit.trim().isNotEmpty()
        }
    }

    /**
     * Validates all ingredients and returns true if at least one is complete
     */
    fun validateIngredients(): Boolean {
        return getValidIngredients().isNotEmpty()
    }

    /**
     * Clears all ingredient data
     */
    fun clearAll() {
        ingredients.clear()
        ingredients.add(Ingredient("", "", "")) // Keep at least one empty ingredient
        notifyDataSetChanged()
    }

    /**
     * Gets all ingredients data for submission
     */
    fun getAllIngredients(): List<Ingredient> {
        return ingredients.toList()
    }
}