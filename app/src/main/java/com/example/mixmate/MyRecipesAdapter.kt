package com.example.mixmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixmate.data.local.CustomRecipeEntity

class MyRecipesAdapter(
    private var recipes: List<CustomRecipeEntity> = emptyList(),
    private val onRecipeClick: (CustomRecipeEntity) -> Unit
) : RecyclerView.Adapter<MyRecipesAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgRecipe: ImageView = itemView.findViewById(R.id.img_recipe)
        val tvRecipeName: TextView = itemView.findViewById(R.id.tv_recipe_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        
        // Set recipe name
        holder.tvRecipeName.text = recipe.name
        
        // Load recipe image
        if (!recipe.imageUri.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(recipe.imageUri)
                .placeholder(R.drawable.ic_add_photo)
                .error(R.drawable.ic_add_photo)
                .centerCrop()
                .into(holder.imgRecipe)
        } else {
            // Use a default cocktail image or icon
            holder.imgRecipe.setImageResource(R.drawable.ic_local_bar)
        }
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onRecipeClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<CustomRecipeEntity>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}