package com.example.mixmate

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
    val imageRes: Int = R.drawable.cosmopolitan, // default placeholder
    val imageUrl: String? = null
)

class SuggestedCocktailAdapter(
    private val items: MutableList<SuggestedCocktail>
) : RecyclerView.Adapter<SuggestedCocktailAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.img_photo)
        val name: TextView = itemView.findViewById(R.id.tv_name)
        val meta: TextView = itemView.findViewById(R.id.tv_meta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggested_cocktail, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        // Load imageUrl if present else fallback resource
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
        holder.name.text = item.name
        holder.meta.text = String.format(Locale.getDefault(), "%.1f • %s", item.rating, item.category)
    }

    override fun getItemCount(): Int = items.size

    fun replaceAll(newItems: List<SuggestedCocktail>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
