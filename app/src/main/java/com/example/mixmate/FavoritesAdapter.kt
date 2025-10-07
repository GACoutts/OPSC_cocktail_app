package com.example.mixmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mixmate.data.local.FavoriteEntity

class FavoritesAdapter(
    private var favorites: List<FavoriteEntity> = emptyList(),
    private val onFavoriteClick: (FavoriteEntity) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgRecipe: ImageView = itemView.findViewById(R.id.img_recipe)
        val tvRecipeName: TextView = itemView.findViewById(R.id.tv_recipe_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = favorites[position]
        
        // Set recipe name
        holder.tvRecipeName.text = favorite.name
        
        // Load recipe image
        if (favorite.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(favorite.imageUrl)
                .placeholder(R.drawable.ic_default_cocktail)
                .error(R.drawable.ic_default_cocktail)
                .centerCrop()
                .into(holder.imgRecipe)
        } else {
            // Use a default cocktail image
            holder.imgRecipe.setImageResource(R.drawable.ic_default_cocktail)
            holder.imgRecipe.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onFavoriteClick(favorite)
        }
    }

    override fun getItemCount(): Int = favorites.size

    fun updateFavorites(newFavorites: List<FavoriteEntity>) {
        favorites = newFavorites
        notifyDataSetChanged()
    }
}