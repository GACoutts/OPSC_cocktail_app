package com.example.mixmate

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mixmate.ui.discover.FilterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class FilterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FilterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FilterViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test initial state
    @Test
    fun testInitialState() {
        runTest {
            assertEquals(null, viewModel.selectedIngredient.value)
            assertEquals(null, viewModel.selectedRating.value)
            assertEquals(FilterViewModel.SortOrder.POPULAR, viewModel.selectedSort.value)
            assertEquals(emptyList<SuggestedCocktail>(), viewModel.filteredCocktails.value)
            assertEquals(false, viewModel.isLoading.value)
            assertEquals(null, viewModel.error.value)
        }
    }

    // Test setting initial cocktails
    @Test
    fun testSetInitialCocktails() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Martini", 4.5, "Classic"),
                SuggestedCocktail("Daiquiri", 4.0, "Classic"),
                SuggestedCocktail("Margarita", 4.2, "Tequila")
            )

            viewModel.setInitialCocktails(mockCocktails)
            advanceUntilIdle()

            assertEquals(mockCocktails, viewModel.filteredCocktails.value)
        }
    }

    // Test clear ingredient filter
    @Test
    fun testClearIngredientFilter() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Martini", 4.5, "Classic"),
                SuggestedCocktail("Daiquiri", 4.0, "Classic")
            )

            viewModel.setInitialCocktails(mockCocktails)
            viewModel.clearIngredientFilter()
            advanceUntilIdle()

            assertEquals(null, viewModel.selectedIngredient.value)
            assertEquals(mockCocktails, viewModel.filteredCocktails.value)
        }
    }

    // Test filter by rating
    @Test
    fun testFilterByRating() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Martini", 4.5, "Classic"),
                SuggestedCocktail("Daiquiri", 4.0, "Classic"),
                SuggestedCocktail("Margarita", 3.5, "Tequila"),
                SuggestedCocktail("Cosmopolitan", 3.0, "Vodka")
            )

            viewModel.setInitialCocktails(mockCocktails)
            viewModel.filterByRating(4.0)
            advanceUntilIdle()

            assertEquals(4.0, viewModel.selectedRating.value)
            val filtered = viewModel.filteredCocktails.value
            assertEquals(2, filtered.size)
            assertTrue(filtered.all { it.rating >= 4.0 })
        }
    }

    // Test clear rating filter
    @Test
    fun testClearRatingFilter() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Martini", 4.5, "Classic"),
                SuggestedCocktail("Daiquiri", 4.0, "Classic"),
                SuggestedCocktail("Margarita", 3.5, "Tequila")
            )

            viewModel.setInitialCocktails(mockCocktails)
            viewModel.filterByRating(4.0)
            advanceUntilIdle()

            viewModel.clearRatingFilter()
            advanceUntilIdle()

            assertEquals(null, viewModel.selectedRating.value)
            assertEquals(mockCocktails, viewModel.filteredCocktails.value)
        }
    }

    // Test set sort order - POPULAR
    @Test
    fun testSetSortOrderPopular() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Daiquiri", 4.0, "Classic"),
                SuggestedCocktail("Martini", 4.5, "Classic"),
                SuggestedCocktail("Margarita", 3.5, "Tequila")
            )

            viewModel.setInitialCocktails(mockCocktails)
            viewModel.setSortOrder(FilterViewModel.SortOrder.POPULAR)
            advanceUntilIdle()

            val filtered = viewModel.filteredCocktails.value
            assertEquals(3, filtered.size)
            assertEquals(4.5, filtered[0].rating, 0.0)
            assertEquals(4.0, filtered[1].rating, 0.0)
            assertEquals(3.5, filtered[2].rating, 0.0)
        }
    }

    // Test set sort order - NEWEST
    @Test
    fun testSetSortOrderNewest() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Daiquiri", 4.0, "Classic"),
                SuggestedCocktail("Martini", 4.5, "Classic"),
                SuggestedCocktail("Margarita", 3.5, "Tequila")
            )

            viewModel.setInitialCocktails(mockCocktails)
            viewModel.setSortOrder(FilterViewModel.SortOrder.NEWEST)
            advanceUntilIdle()

            val filtered = viewModel.filteredCocktails.value
            assertEquals(mockCocktails, filtered)
        }
    }

    // Test set sort order - TOP_RATED
    @Test
    fun testSetSortOrderTopRated() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Daiquiri", 4.0, "Classic"),
                SuggestedCocktail("Martini", 4.5, "Classic"),
                SuggestedCocktail("Margarita", 3.5, "Tequila")
            )

            viewModel.setInitialCocktails(mockCocktails)
            viewModel.setSortOrder(FilterViewModel.SortOrder.TOP_RATED)
            advanceUntilIdle()

            val filtered = viewModel.filteredCocktails.value
            assertEquals(3, filtered.size)
            assertEquals(4.5, filtered[0].rating, 0.0)
            assertEquals(4.0, filtered[1].rating, 0.0)
            assertEquals(3.5, filtered[2].rating, 0.0)
        }
    }

    // Test combined filters: rating + sort
    @Test
    fun testCombinedFilteringRatingAndSort() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Daiquiri", 4.0, "Classic"),
                SuggestedCocktail("Martini", 4.5, "Classic"),
                SuggestedCocktail("Margarita", 3.5, "Tequila"),
                SuggestedCocktail("Cosmopolitan", 4.2, "Vodka")
            )

            viewModel.setInitialCocktails(mockCocktails)
            viewModel.filterByRating(4.0)
            viewModel.setSortOrder(FilterViewModel.SortOrder.POPULAR)
            advanceUntilIdle()

            val filtered = viewModel.filteredCocktails.value
            assertEquals(3, filtered.size)
            assertTrue(filtered.all { it.rating >= 4.0 })
            assertEquals(4.5, filtered[0].rating, 0.0)
            assertEquals(4.2, filtered[1].rating, 0.0)
            assertEquals(4.0, filtered[2].rating, 0.0)
        }
    }

    // Test get filter description
    @Test
    fun testGetFilterDescription() {
        runTest {
            viewModel.clearIngredientFilter()
            viewModel.clearRatingFilter()
            viewModel.setSortOrder(FilterViewModel.SortOrder.POPULAR)
            advanceUntilIdle()

            var desc = viewModel.getFilterDescription()
            assertTrue(desc.contains("Sort: POPULAR"))
        }
    }

    // Test get filter description with rating
    @Test
    fun testGetFilterDescriptionWithRating() {
        runTest {
            viewModel.filterByRating(4.0)
            viewModel.setSortOrder(FilterViewModel.SortOrder.POPULAR)
            advanceUntilIdle()

            val desc = viewModel.getFilterDescription()
            assertTrue(desc.contains("Rating: 4.0+"))
            assertTrue(desc.contains("Sort: POPULAR"))
        }
    }

    // Test empty state
    @Test
    fun testEmptyState() {
        runTest {
            viewModel.setInitialCocktails(emptyList())
            advanceUntilIdle()

            assertEquals(emptyList<SuggestedCocktail>(), viewModel.filteredCocktails.value)
        }
    }

    // Test filter with no results
    @Test
    fun testFilterWithNoResults() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Daiquiri", 3.0, "Classic"),
                SuggestedCocktail("Martini", 3.5, "Classic")
            )

            viewModel.setInitialCocktails(mockCocktails)
            viewModel.filterByRating(4.5)
            advanceUntilIdle()

            val filtered = viewModel.filteredCocktails.value
            assertEquals(0, filtered.size)
        }
    }

    // Test sort order persists when filters change
    @Test
    fun testSortOrderPersistence() {
        runTest {
            val mockCocktails = listOf(
                SuggestedCocktail("Daiquiri", 4.0, "Classic"),
                SuggestedCocktail("Martini", 4.5, "Classic"),
                SuggestedCocktail("Margarita", 3.5, "Tequila")
            )

            viewModel.setInitialCocktails(mockCocktails)
            viewModel.setSortOrder(FilterViewModel.SortOrder.POPULAR)
            viewModel.filterByRating(3.0)
            advanceUntilIdle()

            assertEquals(FilterViewModel.SortOrder.POPULAR, viewModel.selectedSort.value)
            val filtered = viewModel.filteredCocktails.value
            assertEquals(4.5, filtered[0].rating, 0.0)
        }
    }
}
