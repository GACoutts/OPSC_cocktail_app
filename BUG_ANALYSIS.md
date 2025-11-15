# MixMate Bug Analysis & Fix Plan

**Date:** Project Submission (Late -15%)
**Status:** Critical API Integration Issues

---

## 🔴 CRITICAL ISSUES IDENTIFIED

### 1. Missing Cocktail IDs in Discover Page
**Severity:** HIGH - Blocks core functionality

**Problem:**
- `CocktailApiRepository.fetchCocktails()` uses **API Ninjas** which returns cocktail names/ingredients but NO IDs
- `CocktailImageProvider.enrichWithImages()` fetches images from **TheCocktailDB** but doesn't capture the drink IDs
- Result: When users click recipes from Discover/MyBar/HomePage, `cocktailId` is **NULL**
- `RecipeDetailsActivity` receives no ID → cannot fetch full details → shows "Instructions not available"

**Root Cause:**
```kotlin
// In CocktailApiRepository.fetchCocktails() - Line 88
SuggestedCocktail(
    name = api.name!!.trim(),
    rating = randomRating(),
    category = baseSpirit,
    imageRes = R.drawable.cosmopolitan // ❌ NO cocktailId set!
)
```

**Impact:**
- ✅ Name and image display correctly
- ❌ No ingredients shown
- ❌ No instructions shown
- ❌ Cannot favorite properly (uses name.hashCode() as fake ID)
- ❌ Recipe details page is essentially empty

---

### 2. Two Different APIs Being Used Inconsistently

**Current Architecture:**
1. **API Ninjas** (`CocktailApiRepository`) - Used for initial data fetch
   - Endpoint: `https://api.api-ninjas.com/v1/cocktail`
   - Returns: name, ingredients list (no IDs, no instructions)
   
2. **TheCocktailDB** (`CocktailImageProvider` + `CocktailRepository`) - Used for images & details
   - Endpoints: 
     - `www.thecocktaildb.com/api/json/v1/1/search.php?s=name`
     - `www.thecocktaildb.com/api/json/v1/1/lookup.php?i=id`
   - Returns: complete drink data with IDs

**Problem:** API Ninjas data has no IDs, so we can't link it to TheCocktailDB details

---

### 3. Two Recipe Detail Activities (Confusing but Correct)

**Files:**
1. `RecipeDetailActivity.kt` - For **custom user-created recipes** (Room database)
2. `RecipeDetailsActivity.kt` - For **API cocktails** (TheCocktailDB)

**Used By:**
- `RecipeDetailActivity` ← ProfileActivity (user's custom recipes) ✅ CORRECT
- `RecipeDetailsActivity` ← DiscoverPage, MyBar, HomePage, FavouritesActivity ✅ CORRECT

**Verdict:** This is actually fine - they serve different purposes. Keep both.

---

### 4. Favorites Not Working Properly

**Problem:**
- When cocktailId is null, favorites uses `name.hashCode()` as a fake ID
- This breaks synchronization with TheCocktailDB
- Favorited items can't be looked up properly

**Code Location:** `DiscoverPage.kt` Line 307
```kotlin
val cocktailId = cocktail.cocktailId ?: cocktail.name.hashCode().toString() // ❌ Bad fallback
```

---

## ✅ SOLUTION PLAN

### Phase 1: Switch to TheCocktailDB Exclusively (PRIORITY)

**Goal:** Fetch cocktails WITH IDs from TheCocktailDB instead of API Ninjas

**Implementation:**

1. **Create new `TheCocktailDBRepository.kt`:**
```kotlin
suspend fun fetchRandomCocktails(limit: Int = 50): List<SuggestedCocktail>
```
- Use multiple API calls to TheCocktailDB:
  - `/random.php` (gets 1 random drink)
  - `/filter.php?a=Alcoholic` (gets list with IDs)
  - `/search.php?f=a` (search by first letter)
  
2. **Update `CocktailImageProvider`:**
   - Return `Pair<String?, String?>` (imageUrl, cocktailId)
   - Or better: return enriched `SuggestedCocktail` with BOTH

3. **Update DiscoverPage data loading:**
   - Replace `CocktailApiRepository.fetchCocktails()` 
   - Use new TheCocktailDB-based repository
   - Ensure ALL items have `cocktailId` populated

### Phase 2: Fix Data Flow

**Files to Update:**

1. **`DiscoverPage.kt`** (Line 228-245)
   - Replace API Ninjas fetch with TheCocktailDB fetch
   - Verify every SuggestedCocktail has cocktailId

2. **`MyBar.kt`** 
   - Same fix as DiscoverPage

3. **`HomePage.kt`**
   - Same fix as DiscoverPage

4. **`SuggestedCocktailAdapter.kt`** (Line 96)
   - Already passes ID correctly ✅
   - Just needs data to have IDs

5. **`RecipeDetailsViewModel.kt`** (Line 57-60)
   - Already handles missing data gracefully ✅
   - Shows name/image even without API data ✅

### Phase 3: Fix Favorites

1. **Remove hashCode fallback** in `DiscoverPage.kt` Line 307
2. **Only allow favoriting** when cocktailId exists
3. **Update UI** to show "Sync required" for items without IDs

---

## 🔧 IMMEDIATE FIX (Quick & Dirty)

If we need something working RIGHT NOW:

### Option A: Use TheCocktailDB Search-by-Name as Fallback

**In `RecipeDetailsActivity.onCreate()`:**
```kotlin
val cocktailName = intent.getStringExtra("cocktail_name").orEmpty()
val cocktailId = intent.getStringExtra("cocktail_id")

if (cocktailId.isNullOrBlank() && cocktailName.isNotBlank()) {
    // Try to find ID by searching name
    vm.findByNameThenLoad(cocktailName)
} else if (!cocktailId.isNullOrBlank()) {
    vm.load(cocktailId)
}
```

**Add to `RecipeDetailsViewModel`:**
```kotlin
suspend fun findByNameThenLoad(name: String) {
    val api = CocktailApi.create()
    val result = api.searchByName(name)
    val drink = result.drinks?.firstOrNull()
    if (drink?.idDrink != null) {
        load(drink.idDrink)
    } else {
        // Show error or partial data
    }
}
```

### Option B: Enrich Data On-Click

**In `SuggestedCocktailAdapter` click handler:**
```kotlin
holder.itemView.setOnClickListener {
    lifecycleScope.launch {
        val id = if (item.cocktailId != null) {
            item.cocktailId
        } else {
            // Fetch ID by name
            fetchIdByName(item.name)
        }
        
        val intent = Intent(ctx, RecipeDetailsActivity::class.java)
        intent.putExtra("cocktail_name", item.name)
        intent.putExtra("cocktail_image", item.imageUrl)
        id?.let { intent.putExtra("cocktail_id", it) }
        ctx.startActivity(intent)
    }
}
```

---

## 📋 TESTING CHECKLIST

After implementing fixes:

- [ ] DiscoverPage loads cocktails with IDs
- [ ] Clicking any cocktail shows full details (ingredients + instructions)
- [ ] Favorites save/load correctly
- [ ] MyBar shows cocktails with working details
- [ ] Search functionality returns items with IDs
- [ ] Recipe details gracefully handles missing data
- [ ] No crashes when ID is missing
- [ ] Add Recipe (custom) still works with RecipeDetailActivity

---

## 🎯 RECOMMENDED APPROACH

**Best Solution:** Implement Phase 1 properly
- Switch to TheCocktailDB completely
- Fetch data with IDs from the start
- Clean, maintainable solution

**Quick Fix:** Implement Option A from Immediate Fix
- Add search-by-name fallback in RecipeDetailsViewModel
- Gets working functionality quickly
- Can refactor later

---

## 📝 API ENDPOINTS REFERENCE

### TheCocktailDB (Use This One!)
```
Base: https://www.thecocktaildb.com/api/json/v1/1/

GET /lookup.php?i={id}          # Get drink by ID
GET /search.php?s={name}        # Search by name
GET /search.php?f={letter}      # Search by first letter
GET /filter.php?i={ingredient}  # Filter by ingredient
GET /filter.php?a=Alcoholic     # Get alcoholic drinks
GET /random.php                 # Get random drink
```

### API Ninjas (Currently Using - PROBLEMATIC)
```
Base: https://api.api-ninjas.com/v1/

GET /cocktail?name={name}       # ❌ Returns no IDs!
```

---

## 🚨 PRIORITY ACTIONS

1. **TODAY:** Implement search-by-name fallback (Option A) - 30 mins
2. **TODAY:** Test end-to-end flow - 15 mins
3. **TOMORROW:** Replace API Ninjas with TheCocktailDB repository - 2 hours
4. **TOMORROW:** Full regression testing - 1 hour

---

## 💡 NOTES

- API Ninjas requires API key (currently blank check exists)
- TheCocktailDB is free and doesn't require API key
- Current code already has good error handling
- The adapter already passes data correctly
- Main issue is data source doesn't provide IDs

---

**Status:** Ready to implement fixes
**Next Step:** Choose between Quick Fix (Option A) or Proper Fix (Phase 1)