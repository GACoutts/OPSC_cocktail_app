# MyBar Rotating Alcohol Feature - Implementation Instructions

## Overview
This document provides step-by-step instructions to implement the rotating alcohol type feature in MyBar, where default cocktails are filtered by a rotating alcohol type (Vodka, Gin, Rum, Tequila, Whiskey, Brandy) that changes on each app launch.

---

## Status

✅ **Completed:**
- Created `MyBarRotatingHelper.kt` utility class
- Helper manages rotating alcohol types with SharedPreferences
- Cycles through 6 alcohol types automatically

⚠️ **Remaining:**
- Update `MyBar.kt` to use the helper class
- Ensure filtering works properly with selections
- Test rotation on multiple launches

---

## Implementation Steps

### Step 1: Verify Helper Class Exists

The helper class is already created at:
`app/src/main/java/com/example/mixmate/MyBarRotatingHelper.kt`

**What it does:**
- Stores launch count in SharedPreferences
- Returns next alcohol type in rotation
- Provides: `getRotatingAlcoholType(context)` method

---

### Step 2: Update MyBar.kt

#### A. Add Import at Top
Add this import after existing imports:

```kotlin
// At the top of MyBar.kt, add:
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```

#### B. Replace `loadDefaultSuggested()` Method

Find the `loadDefaultSuggested()` method (around line 209) and replace it with:

```kotlin
private suspend fun loadDefaultSuggested() {
    showLoading()
    try {
        // Get rotating alcohol type
        val alcoholType = MyBarRotatingHelper.getRotatingAlcoholType(this)
        android.util.Log.d("MyBar", "Loading default cocktails for: $alcoholType")

        // Load cocktails filtered by the rotating alcohol type
        loadSuggestedByIngredient(alcoholType)
    } catch (e: Exception) {
        android.util.Log.e("MyBar", "Error loading rotating suggestions, fallback to generic", e)
        // Fallback to generic cocktails
        val apiItems = CocktailApiRepository.fetchCocktails(limit = 50)
        val data = if (apiItems.isNotEmpty()) CocktailImageProvider.enrichWithImages(apiItems) else emptyList()
        
        // Load favorite states
        data.forEach { cocktail ->
            cocktail.cocktailId?.let { id ->
                val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
                favoriteStates[id] = isFav
                cocktail.isFavorite = isFav
            }
        }
        
        updateSuggestedList(data)
    }
}
```

#### C. Update `loadSuggestedByIngredient()` Method

Find the `loadSuggestedByIngredient()` method (around line 217) and replace it with:

```kotlin
private suspend fun loadSuggestedByIngredient(ingredient: String) {
    showLoading()
    val apiResponse = try {
        val api = com.example.mixmate.data.remote.CocktailApi.create()
        withContext(Dispatchers.IO) {
            api.filterByIngredient(ingredient)
        }
    } catch (e: Exception) {
        android.util.Log.e("MyBar", "Error filtering by ingredient: $ingredient", e)
        null
    }

    val cocktails = apiResponse?.drinks?.mapIndexed { index, drink ->
        val rating = 5.0 - (index * 0.1).coerceAtMost(2.0) // Generate ratings based on position
        SuggestedCocktail(
            name = drink.strDrink ?: "Unknown",
            rating = rating,
            category = ingredient,
            imageUrl = drink.strDrinkThumb,
            cocktailId = drink.idDrink,
            isFavorite = false
        )
    } ?: emptyList()

    android.util.Log.d("MyBar", "Loaded ${cocktails.size} cocktails for $ingredient")

    // Load favorite states
    cocktails.forEach { cocktail ->
        cocktail.cocktailId?.let { id ->
            val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
            favoriteStates[id] = isFav
            cocktail.isFavorite = isFav
        }
    }

    updateSuggestedList(cocktails)
}
```

#### D. Update `loadSuggestedByMultipleIngredients()` Method

Find the `loadSuggestedByMultipleIngredients()` method (around line 237) and update it:

```kotlin
private suspend fun loadSuggestedByMultipleIngredients(ingredients: List<String>) {
    showLoading()
    try {
        val api = com.example.mixmate.data.remote.CocktailApi.create()
        val allCocktails = mutableSetOf<SuggestedCocktail>()

        android.util.Log.d("MyBar", "Loading cocktails for ${ingredients.size} ingredients: $ingredients")

        // Fetch cocktails for each selected ingredient
        for (ingredient in ingredients) {
            try {
                val apiResponse = withContext(Dispatchers.IO) {
                    api.filterByIngredient(ingredient)
                }
                apiResponse.drinks?.forEachIndexed { index, drink ->
                    if (drink.idDrink != null && drink.strDrink != null) {
                        // Calculate rating based on position (popularity)
                        val rating = 5.0 - (index * 0.1).coerceAtMost(2.0)

                        allCocktails.add(
                            SuggestedCocktail(
                                name = drink.strDrink,
                                rating = rating,
                                category = ingredient,
                                imageUrl = drink.strDrinkThumb,
                                cocktailId = drink.idDrink,
                                isFavorite = false
                            )
                        )
                    }
                }
                android.util.Log.d("MyBar", "Added cocktails for $ingredient, total now: ${allCocktails.size}")
            } catch (e: Exception) {
                android.util.Log.e("MyBar", "Error loading ingredient: $ingredient", e)
                // Continue with next ingredient if one fails
                continue
            }
        }

        android.util.Log.d("MyBar", "Total unique cocktails loaded: ${allCocktails.size}")

        // Load favorite states
        allCocktails.forEach { cocktail ->
            cocktail.cocktailId?.let { id ->
                val isFav = favoritesViewModel.isFavorite(id).firstOrNull() ?: false
                favoriteStates[id] = isFav
                cocktail.isFavorite = isFav
            }
        }

        updateSuggestedList(allCocktails.toList())
    } catch (e: Exception) {
        android.util.Log.e("MyBar", "Error in loadSuggestedByMultipleIngredients", e)
        updateSuggestedList(emptyList())
    }
}
```

---

## How It Works

### Rotation Logic

1. **First Launch**: Shows Vodka cocktails
2. **Second Launch**: Shows Gin cocktails
3. **Third Launch**: Shows Rum cocktails
4. **Fourth Launch**: Shows Tequila cocktails
5. **Fifth Launch**: Shows Whiskey cocktails
6. **Sixth Launch**: Shows Brandy cocktails
7. **Seventh Launch**: Cycles back to Vodka

### With Filtering

- **No selection**: Shows rotating alcohol type cocktails
- **User selects alcohol/ingredient**: Shows selected items only
- **User deselects all**: Returns to rotating alcohol type

---

## Testing Checklist

### Basic Rotation
- [ ] Open MyBar - should show one alcohol type (e.g., Vodka)
- [ ] Note which alcohol type is shown
- [ ] Close app completely
- [ ] Reopen MyBar - should show NEXT alcohol type (e.g., Gin)
- [ ] Repeat 6 times to see full rotation

### With Filtering
- [ ] Open MyBar (shows rotating type)
- [ ] Click "Alcohol" tab
- [ ] Select "Rum"
- [ ] Should show only Rum cocktails
- [ ] Deselect "Rum"
- [ ] Should return to rotating type

### Edge Cases
- [ ] Network failure - should show empty state with message
- [ ] No cocktails found - should show empty state
- [ ] Rapid opening/closing - counter should increment correctly

---

## Debugging

### Check Logs
Filter logcat for "MyBar" to see:
```
D/MyBarRotating: Launch #0 -> Showing: Vodka
D/MyBar: Loading default cocktails for: Vodka
D/MyBar: Loaded 25 cocktails for Vodka
```

### Verify Launch Count
The launch count is stored in SharedPreferences:
```
Name: MyBarPrefs
Key: launch_count
Value: (increments on each launch)
```

### Reset Counter (For Testing)
You can reset the counter programmatically:
```kotlin
MyBarRotatingHelper.resetLaunchCounter(this)
```

Or clear app data:
```
Settings -> Apps -> MixMate -> Storage -> Clear Data
```

---

## Troubleshooting

### Problem: Same alcohol type every time
**Solution:** Check that `getRotatingAlcoholType()` is being called in `loadDefaultSuggested()`

### Problem: No cocktails showing
**Solution:** 
1. Check network connection
2. Verify API is working
3. Check logcat for API errors
4. Ensure `updateSuggestedList()` is being called

### Problem: Filtering not working
**Solution:**
1. Verify `withContext(Dispatchers.IO)` is used for API calls
2. Check that `updateSuggestions()` calls correct method
3. Ensure favorite states are loaded

### Problem: App crashes on launch
**Solution:**
1. Check all imports are correct
2. Verify no syntax errors
3. Ensure `MyBarRotatingHelper` exists
4. Check logcat for stack trace

---

## Future Enhancements

### Possible Improvements:
1. **User Preference**: Let user set favorite alcohol type
2. **Smart Rotation**: Show alcohol types user favorites most
3. **Time-Based**: Different alcohol types for different times of day
4. **Location-Based**: Popular drinks in user's region
5. **Seasonal**: Seasonal cocktails (summer vs winter)

---

## Related Files

- `app/src/main/java/com/example/mixmate/MyBar.kt` - Main activity
- `app/src/main/java/com/example/mixmate/MyBarRotatingHelper.kt` - Helper class
- `app/src/main/java/com/example/mixmate/CocktailApi.kt` - API interface
- `app/src/main/java/com/example/mixmate/SuggestedCocktail.kt` - Data model

---

## Summary

After implementing these changes:

✅ MyBar will show different alcohol types on each launch
✅ Cycles through 6 alcohol types automatically
✅ Filtering still works when user makes selections
✅ Returns to rotation when all filters cleared
✅ Provides variety in default cocktail display
✅ Encourages users to explore different drink types

This creates a more dynamic and engaging user experience!