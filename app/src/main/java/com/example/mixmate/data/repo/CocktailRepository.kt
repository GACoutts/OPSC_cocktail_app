package com.example.mixmate.data.repo

import com.example.mixmate.data.remote.CocktailApi
import com.example.mixmate.data.remote.Drink

class CocktailRepository(
    private val api: CocktailApi = CocktailApi.create()
) {
    suspend fun getDrinkById(id: String): Drink? {
        return api.lookupById(id).drinks?.firstOrNull()
    }
}