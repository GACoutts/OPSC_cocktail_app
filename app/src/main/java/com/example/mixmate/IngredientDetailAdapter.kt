package com.example.mixmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mixmate.data.local.CustomIngredientRealm

class IngredientDetailAdapter(
    private var ingredients: List<CustomIngredientRealm> = emptyList()
) : RecyclerView.Adapter<IngredientDetailAdapter.IngredientViewHolder>() {

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIngredient: TextView = itemView.findViewById(R.id.tv_ingredient)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_detail, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        
        // Format ingredient text: "amount unit name"
        val ingredientText = buildString {
            if (ingredient.amount.isNotEmpty()) {
                append(ingredient.amount)
                if (ingredient.unit.isNotEmpty()) {
                    append(" ")
                    append(ingredient.unit)
                }
                append(" ")
            }
            append(ingredient.name)
        }
        
        holder.tvIngredient.text = ingredientText
    }

    override fun getItemCount(): Int = ingredients.size

    fun updateIngredients(newIngredients: List<CustomIngredientRealm>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }
}