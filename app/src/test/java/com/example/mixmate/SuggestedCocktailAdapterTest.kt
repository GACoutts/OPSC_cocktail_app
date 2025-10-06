package com.example.mixmate

import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestedCocktailAdapterTest {
    @Test
    fun `capitalizeWords capitalizes each word`() {
        assertEquals("Margarita", capitalizeWords("margarita"))
        assertEquals("Old Fashioned", capitalizeWords("old fashioned"))
        assertEquals("Gin Fizz", capitalizeWords("GIN fizz"))
    }

    @Test
    fun `capitalizeWords trims and collapses spaces`() {
        assertEquals("Dry Martini", capitalizeWords("  dry   martini  "))
    }

    @Test
    fun `capitalizeWords leaves internal punctuation intact`() {
        assertEquals("Mary's Special", capitalizeWords("mary's special"))
    }
}

