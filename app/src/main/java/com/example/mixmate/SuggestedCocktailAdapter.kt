package com.example.mixmate

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

data class SuggestedCocktail(
    val name: String,
    val rating: Double,
    val category: String,
    val imageRes: Int = R.drawable.cosmopolitan,   // local fallback
    val imageUrl: String? = null,                  // remote image (optional)
    val cocktailId: String? = null,                // for details/favourites
    var isFavorite: Boolean = false,               // fallback favourite state
    val ingredients: List<String>? = null          // ingredient list for filtering
)

class SuggestedCocktailAdapter(
    internal val items: MutableList<SuggestedCocktail>,
    private val onItemClick: ((SuggestedCocktail) -> Unit)? = null,
    private val onFavoriteClick: ((SuggestedCocktail, Boolean) -> Unit)? = null,
    // Return true if the given cocktailId is currently a favourite (Room/Repo)
    private val getFavoriteState: ((String) -> Boolean)? = null
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
        val placeholder = item.imageRes
        if (item.imageUrl.isNullOrBlank()) {
            holder.photo.setImageResource(placeholder)
        } else {
            Glide.with(holder.photo.context)
                .load(item.imageUrl)
                .placeholder(placeholder)
                .error(placeholder)
                .centerCrop()
                .into(holder.photo)
        }

        // Text
        holder.name.text = capitalizeWords(item.name)
        holder.meta.text = String.format(Locale.getDefault(), "%.1f • %s", item.rating, item.category)

        // Favourite icon (query repo if we have an id; otherwise fall back to item.isFavorite)
        val currentFav = item.cocktailId?.let { id -> getFavoriteState?.invoke(id) } ?: item.isFavorite
        renderFavIcon(holder.favoriteIcon, currentFav)

        holder.favoriteIcon.setOnClickListener {
            // Re-read current state (in case repo changed)
            val nowFav = item.cocktailId?.let { id -> getFavoriteState?.invoke(id) } ?: item.isFavorite
            val newState = !nowFav
            item.isFavorite = newState // keep in-memory in sync for quick UI
            renderFavIcon(holder.favoriteIcon, newState)
            onFavoriteClick?.invoke(item, newState)
        }

        // Open details – delegate if provided, else default to RecipeDetailsActivity
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item) ?: run {
                val ctx = holder.itemView.context
                val intent = Intent(ctx, com.example.mixmate.ui.details.RecipeDetailsActivity::class.java)
                // Always pass name + image so details can render even without API fetch
                intent.putExtra("cocktail_name", item.name)
                intent.putExtra("cocktail_image", item.imageUrl)
                // Pass id if available so details can fetch full description/ingredients
                item.cocktailId?.let { id -> intent.putExtra("cocktail_id", id) }
                ctx.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun replaceAll(newItems: List<SuggestedCocktail>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun renderFavIcon(icon: ImageView, isFav: Boolean) {
        icon.setImageResource(if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
    }
}

/* ---------- text helpers ---------- */

fun capitalizeWords(raw: String): String = raw.trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .joinToString(" ") { token ->
        // Handle hyphenated words and simple possessives gracefully
        token.split('-').joinToString("-") { segment ->
            if (segment.isBlank()) ""
            else capitalizePossessiveSegment(segment)
        }
    }

private fun capitalizePossessiveSegment(segment: String): String {
    val parts = segment.split("'")
    if (parts.size == 1) {
        val p = parts[0]
        return p.lowercase().replaceFirstChar { c ->
            if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
        }
    }
    return parts.mapIndexed { idx, part ->
        if (part.isBlank()) "" else when {
            idx == 0 -> part.lowercase().replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()

            }

            part.length == 1 -> part.lowercase() // the "'s"
            else -> part.lowercase().replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else c.toString()
            }
        }
    }.joinToString("'")
}
