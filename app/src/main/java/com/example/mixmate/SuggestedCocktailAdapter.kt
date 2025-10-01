package com.example.mixmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

data class SuggestedCocktail(
    val name: String,
    val rating: Double,
    val category: String,
    val imageRes: Int
)

class SuggestedCocktailAdapter(
    private val items: List<SuggestedCocktail>
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
        holder.photo.setImageResource(item.imageRes)
        holder.name.text = item.name
        holder.meta.text = String.format(Locale.getDefault(), "%.1f • %s", item.rating, item.category)
    }

    override fun getItemCount(): Int = items.size
}
