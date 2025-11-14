package com.example.mixmate.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CocktailApi {

    // https://www.thecocktaildb.com/api/json/v1/1/lookup.php?i=17196
    @GET("lookup.php")
    suspend fun lookupById(@Query("i") id: String): CocktailResponse

    // https://www.thecocktaildb.com/api/json/v1/1/filter.php?i=Vodka
    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): CocktailResponse

    companion object {
        private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

        fun create(): CocktailApi =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CocktailApi::class.java)
    }
}