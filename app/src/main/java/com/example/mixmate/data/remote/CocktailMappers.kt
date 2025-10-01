package com.example.mixmate.data.remote


fun formatIngredients(drink: Drink): String {
    val pairs = listOf(
        drink.strMeasure1 to drink.strIngredient1,
        drink.strMeasure2 to drink.strIngredient2,
        drink.strMeasure3 to drink.strIngredient3,
        drink.strMeasure4 to drink.strIngredient4,
        drink.strMeasure5 to drink.strIngredient5,
        drink.strMeasure6 to drink.strIngredient6,
        drink.strMeasure7 to drink.strIngredient7,
        drink.strMeasure8 to drink.strIngredient8,
        drink.strMeasure9 to drink.strIngredient9,
        drink.strMeasure10 to drink.strIngredient10,
        drink.strMeasure11 to drink.strIngredient11,
        drink.strMeasure12 to drink.strIngredient12,
        drink.strMeasure13 to drink.strIngredient13,
        drink.strMeasure14 to drink.strIngredient14,
        drink.strMeasure15 to drink.strIngredient15
    )

    return pairs.mapNotNull { (m, i) ->
        val ing = i?.trim().orEmpty()
        if (ing.isEmpty()) null
        else {
            val meas = m?.trim().orEmpty()
            if (meas.isEmpty()) ing else "$meas $ing"
        }
    }.joinToString("\n")
}