package com.example.mixmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.mixmate.data.local.FavoriteEntity
import java.util.Locale

data class SuggestedCocktail(
    val name: String,
    val rating: Double,
    val category: String,
    val imageRes: Int = R.drawable.cosmopolitan,   // local fallback
    val imageUrl: String? = null,                  // remote image (optional)
    val cocktailId: String? = null,                // for favorites functionality
    var isFavorite: Boolean = false                // favorite status
)

class SuggestedCocktailAdapter(
    private val items: MutableList<SuggestedCocktail>,
    private val onItemClick: ((SuggestedCocktail) -> Unit)? = null,
    private val onFavoriteClick: ((SuggestedCocktail, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<SuggestedCocktailAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.img_photo)
        val name: TextView = itemView.findViewById(R.id.tv_name)
        val meta: TextView = itemView.findViewById(R.id.tv_meta)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.iv_favorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggested_cocktail, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        // Image
        holder.photo.scaleType = ImageView.ScaleType.CENTER_CROP
        val ph = item.imageRes
        if (item.imageUrl.isNullOrBlank()) {
            holder.photo.setImageResource(ph)
        } else {
            Glide.with(holder.photo.context)
                .load(item.imageUrl)
                .placeholder(ph)
                .error(ph)
                .centerCrop()
                .into(holder.photo)
        }

        // Text
        holder.name.text = capitalizeWords(item.name)
        holder.meta.text = String.format(Locale.getDefault(), "%.1f • %s", item.rating, item.category)

        // Favorite icon
        updateFavoriteIcon(holder.favoriteIcon, item.isFavorite)
        holder.favoriteIcon.setOnClickListener {
            item.isFavorite = !item.isFavorite
            updateFavoriteIcon(holder.favoriteIcon, item.isFavorite)
            onFavoriteClick?.invoke(item, item.isFavorite)
        }

        // Click → open details (either delegate to lambda or default to RecipeDetailActivity)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item) ?: run {
                RecipeDetailActivity.launch(
                    context = holder.itemView.context,
                    name = item.name,
                    imageUrl = item.imageUrl,
                    externalId = null
                )
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun replaceAll(newItems: List<SuggestedCocktail>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun updateFavoriteIcon(iconView: ImageView, isFavorite: Boolean) {
        if (isFavorite) {
            iconView.setImageResource(R.drawable.ic_heart_filled)
        } else {
            iconView.setImageResource(R.drawable.ic_heart_outline)
        }
    }
}

/* -------- helpers -------- */

fun capitalizeWords(raw: String): String = raw.trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .joinToString(" ") { token ->
        // Process hyphenated segments separately
        token.split('-').joinToString("-") { segment ->
            if (segment.isBlank()) "" else capitalizePossessiveSegment(segment)
        }
    }

private fun capitalizePossessiveSegment(segment: String): String {
    val parts = segment.split("'")
    if (parts.size == 1) {
        return parts[0].lowercase().replaceFirstChar { c ->
            if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
        }
    }
    return parts.mapIndexed { index, part ->
        if (part.isBlank()) "" else when {
            index == 0 -> part.lowercase().replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
            }
            part.length == 1 -> part.lowercase() // possessive 's
            else -> part.lowercase().replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
            }
        }
    }.joinToString("'")
}
