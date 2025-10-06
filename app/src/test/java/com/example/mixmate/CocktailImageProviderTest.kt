package com.example.mixmate

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// We duplicate minimal interface to inject a test Retrofit pointing to MockWebServer.
private interface TestCocktailDbService {
    @GET("search.php")
    suspend fun searchByName(@Query("s") name: String): TestResponse

    @GET("search.php")
    suspend fun searchByFirstLetter(@Query("f") letter: String): TestResponse
}

private data class TestResponse(val drinks: List<TestDrink>?)
private data class TestDrink(val strDrink: String?, val strDrinkThumb: String?)

class CocktailImageProviderTest {
    private lateinit var server: MockWebServer
    private val gson = Gson()

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        CocktailImageProvider.replaceServiceForTests(server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `enrichWithImages attaches imageUrl when API returns match`() = runBlocking {
        val responseJson = gson.toJson(TestResponse(listOf(TestDrink("Margarita", "https://img/margarita.png"))))
        server.enqueue(MockResponse().setBody(responseJson).setResponseCode(200))

        val list = listOf(SuggestedCocktail("Margarita", 4.5, "Tequila"))
        val enriched = CocktailImageProvider.enrichWithImages(list)
        assertEquals("https://img/margarita.png", enriched.first().imageUrl)
    }

    @Test
    fun `enrichWithImages leaves placeholder when no drinks returned`() = runBlocking {
        val emptyJson = gson.toJson(TestResponse(null))
        server.enqueue(MockResponse().setBody(emptyJson).setResponseCode(200))

        val list = listOf(SuggestedCocktail("Unknown Drink", 4.0, "General"))
        val enriched = CocktailImageProvider.enrichWithImages(list)
        assertTrue(enriched.first().imageUrl == null)
    }

    @Test
    fun `fuzzy search picks closest match`() = runBlocking {
        // First direct search fails (empty), second letter-based search returns similar names.
        val emptyJson = gson.toJson(TestResponse(null))
        val fuzzyJson = gson.toJson(TestResponse(listOf(
            TestDrink("Margaritaa", "https://img/fuzzy1.png"),
            TestDrink("Margarita", "https://img/fuzzy_correct.png")
        )))
        server.enqueue(MockResponse().setBody(emptyJson).setResponseCode(200)) // direct search
        server.enqueue(MockResponse().setBody(fuzzyJson).setResponseCode(200)) // first-letter search

        val list = listOf(SuggestedCocktail("Margarita", 4.5, "Tequila"))
        val enriched = CocktailImageProvider.enrichWithImages(list)
        assertEquals("https://img/fuzzy_correct.png", enriched.first().imageUrl)
    }
}
