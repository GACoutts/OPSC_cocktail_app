package com.example.mixmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mixmate.data.local.FavoriteEntity

class FavoritesAdapter(
    private val onClick: (FavoriteEntity) -> Unit,
    private val onDelete: (FavoriteEntity) -> Unit
) : ListAdapter<FavoriteEntity, FavoritesAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<FavoriteEntity>() {
        override fun areItemsTheSame(o: FavoriteEntity, n: FavoriteEntity) = o.cocktailId == n.cocktailId
        override fun areContentsTheSame(o: FavoriteEntity, n: FavoriteEntity) = o == n
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.ivImage)
        val title: TextView = v.findViewById(R.id.tvTitle)
        val remove: ImageButton = v.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = getItem(position)

        h.title.text = item.name
        h.image.contentDescription = item.name

        // Ensure images are cached to disk so they show when offline
        Glide.with(h.itemView)
            .load(item.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(h.image)

        h.itemView.setOnClickListener {
            val pos = h.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onClick(getItem(pos))
        }
        h.remove.setOnClickListener {
            val pos = h.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onDelete(getItem(pos))
        }
    }

    // Good hygiene: clear Glide when a view is recycled
    override fun onViewRecycled(holder: VH) {
        Glide.with(holder.itemView).clear(holder.image)
        super.onViewRecycled(holder)
    }
}
