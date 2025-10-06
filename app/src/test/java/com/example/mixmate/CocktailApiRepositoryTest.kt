package com.example.mixmate

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CocktailApiRepositoryTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        // point repository to mock backend & allow blank key for unit tests
        CocktailApiRepository.replaceBaseUrlForTests(server.url("/").toString())
        CocktailApiRepository.enableBlankApiKeyForTests()
    }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun `fetchCocktails respects limit`() = runBlocking {
        val body = """[
            {"name":"A1","ingredients":["1 oz Gin"],"instructions":"Mix","servings":1},
            {"name":"A2","ingredients":["1 oz Vodka"],"instructions":"Mix","servings":1},
            {"name":"A3","ingredients":["1 oz Rum"],"instructions":"Mix","servings":1},
            {"name":"A4","ingredients":["1 oz Tequila"],"instructions":"Mix","servings":1}
        ]""".trimIndent()
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))
        val list = CocktailApiRepository.fetchCocktails(limit = 2)
        assertEquals(2, list.size)
    }

    @Test
    fun `fetchCocktails returns empty on network error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        val list = CocktailApiRepository.fetchCocktails(limit = 5)
        assertTrue(list.isEmpty())
    }
}

