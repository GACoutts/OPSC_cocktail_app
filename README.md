# 🍸 MixMate - Your Ultimate Cocktail Companion

<div align="center">

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![API Level](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-Integrated-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/license-Educational-blue.svg)]()

**A modern, offline-first Android cocktail discovery and management app built with Material Design 3**

[Features](#-key-features) • [Screenshots](#-screenshots) • [Installation](#-setup--installation) • [Architecture](#-architecture--design) • [Documentation](#-documentation)

</div>

---

## 👥 Team Members

- **Teejay Kamwaro** – ST10274142
- **Grant Coutts** – ST10258297  
- **Tristan Vries** – ST10380906
- **Kelvin Gravett** – ST10108660

**Repository:**  
https://github.com/IIEMSA/2025-nov-opsc6312-poe-part3-tjay-km

---

## 🎯 Purpose & Vision

### **The Problem We Solve**

Cocktail enthusiasts face several challenges:
- 📱 **Fragmented Information**: Recipes scattered across multiple apps and websites
- 🔌 **Connectivity Dependence**: Most apps require constant internet connection
- 🏠 **Ingredient Management**: No easy way to track what's in your bar
- 📝 **Recipe Organization**: Limited ability to save and customize personal recipes
- 🎨 **Poor UX**: Cluttered interfaces that make browsing tedious

### **Our Solution: MixMate**

MixMate is an **offline-first**, beautifully designed Android application that puts everything you need at your fingertips:

✨ **Discover** 150+ cocktails with intelligent filtering  
💾 **Work Offline** with comprehensive caching and local storage  
🍸 **Create** your own custom recipes with full metadata  
❤️ **Save** favorites that sync across devices  
🥃 **MyBar** finds cocktails based on ingredients you actually have  
🌐 **Multi-language** support (English & Afrikaans)  
🔥 **Firebase** integration for cloud backup and authentication

### **Target Audience**

- 🏠 **Home Bartenders**: Build and manage your cocktail repertoire
- 👨‍🍳 **Professional Bartenders**: Organize and access recipes quickly
- 🎉 **Party Hosts**: Impress guests with creative, well-documented drinks
- 📚 **Mixology Students**: Learn techniques, ratios, and flavor profiles
- 🍹 **Cocktail Enthusiasts**: Explore new recipes and track your favorites

---

## ✨ Key Features

### 🔍 **Advanced Discovery & Filtering**
- **Hybrid Data Sources**: Combines API Ninjas (150+ cocktails) with TheCocktailDB (images)
- **Multi-Criteria Filtering**: Filter by ingredient, category, and minimum rating
- **Client-Side Processing**: Instant results with no API delays
- **Smart Search**: Case-insensitive fuzzy matching for names and ingredients
- **Beautiful Grid Layout**: 2-column card design optimized for browsing

### 🥃 **MyBar - Intelligent Ingredient Matching**
- **Daily Rotating Suggestions**: Algorithm changes featured spirit each day
- **Multi-Ingredient Selection**: Filter by multiple ingredients (AND logic)
- **Toggle Categories**: Switch between "Alcohol Types" and "Other Ingredients"
- **Smart Recommendations**: Find cocktails you can make with available ingredients
- **Visual Feedback**: Interactive ingredient cards with selection states

### ❤️ **Unified Favorites System**
- **Cross-Page Synchronization**: Real-time updates across all screens
- **Offline Persistence**: Room database storage with user isolation
- **Instant Feedback**: Toast notifications and heart icon animations
- **Quick Navigation**: One-tap access to full recipe details
- **SharedViewModel**: Consistent state management throughout app

### 📝 **Custom Recipe Builder**
- **Complete Recipe Creation**: Name, description, instructions, ingredients
- **Dynamic Ingredient Forms**: Add unlimited ingredients with amounts and units
- **Metadata Support**: Difficulty, prep time, glassware, garnish
- **Image Integration**: Select photos from device gallery with preview
- **Dual Storage**: Room for offline + Firebase for cloud backup
- **User-Specific**: Recipes tied to authenticated user accounts

### 🔌 **Comprehensive Offline Mode** ⭐ *Key Innovation*
- **Cocktail Caching**: Browse 150+ cached cocktails without internet
- **Search & Filter Offline**: Full functionality on cached data
- **Custom Recipes Offline**: Create, edit, delete recipes without connection
- **Favorites Management**: Add/remove favorites offline with sync
- **Smart Cache**: 24-hour validity with automatic refresh
- **LRU Management**: Keeps 200 most recently accessed cocktails
- **Network Detection**: Automatic online/offline transitions
- **Background Sync**: Changes sync automatically when connection restored

### 👤 **Profile & Account Management**
- **Personal Dashboard**: View custom recipes and favorites in one place
- **Recipe Carousel**: Horizontal scroll through your created recipes
- **Favorites Grid**: 2-column layout of saved cocktails
- **Settings Access**: Direct navigation to preferences
- **User Statistics**: Display name, username, join date
- **Floating Action Button**: Quick access to recipe creation

### 🎨 **Modern UI/UX Design**
- **Material Design 3**: Google's latest design system
- **Dark Theme**: Elegant brown and gold color palette
- **Consistent Navigation**: Footer nav bar on all major pages
- **Edge-to-Edge**: Immersive full-screen experience
- **Grid Layouts**: Responsive 2-column grids with proper spacing
- **Smooth Animations**: Card transitions and state changes
- **Loading States**: Progress indicators for async operations
- **Empty States**: Helpful messages when no content available

### 🔥 **Firebase Integration**
- **Authentication**: Email/password with session management
- **Cloud Firestore**: Recipe backup and synchronization
- **Firebase Storage**: Image upload for custom recipes
- **User Isolation**: Secure per-user data storage
- **Real-time Sync**: Automatic cloud backup when online

### 🌐 **Multi-Language Support**
- **Languages**: English and Afrikaans
- **Persistent Settings**: Language preference saved across sessions
- **Complete Translation**: All UI elements, labels, and messages
- **LocaleHelper**: Seamless switching without app restart

### 📱 **Robust Recipe Details**
- **Hybrid Loading**: Supports both API cocktails and custom recipes
- **High-Quality Images**: Glide integration with placeholders
- **Comprehensive Info**: Ingredients, instructions, metadata
- **Interactive Elements**: Add/remove from favorites with feedback
- **Navigation**: Back button and footer nav for easy flow
- **Fallback Handling**: Graceful degradation for missing data

---



## 🏗️ Architecture & Design

### **Architectural Pattern: MVVM + Repository Pattern**

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │HomePage  │  │Discover  │  │  MyBar   │  │ Profile  │   │
│  │          │  │  Page    │  │          │  │          │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
└───────┼─────────────┼─────────────┼─────────────┼──────────┘
        │             │             │             │
        │             ▼             │             │
        │      ┌────────────┐      │             │
        │      │ViewModel   │      │             │
        │      │ Layer      │      │             │
        │      └─────┬──────┘      │             │
        │            │             │             │
        └────────────┼─────────────┴─────────────┘
                     │
        ┌────────────┴────────────────────────────┐
        │      Repository Layer                    │
        │  ┌────────────┐    ┌─────────────────┐ │
        │  │Recipe      │    │Cocktail         │ │
        │  │Repository  │    │Repository       │ │
        │  └─────┬──────┘    └────┬────────────┘ │
        └────────┼────────────────┼───────────────┘
                 │                │
     ┌───────────┴────────────────┴───────────┐
     │          Data Layer                     │
     │  ┌──────────┐  ┌──────────┐           │
     │  │  Room DB │  │ Firebase │           │
     │  │ (Local)  │  │ (Cloud)  │           │
     │  └──────────┘  └──────────┘           │
     │                                         │
     │  ┌──────────┐  ┌──────────┐           │
     │  │API Ninjas│  │CocktailDB│           │
     │  │  (Data)  │  │ (Images) │           │
     │  └──────────┘  └──────────┘           │
     └─────────────────────────────────────────┘
```

### **Design Principles**

#### 🌐 **Offline-First Architecture**

**Strategy**: Local database as single source of truth with background cloud sync

**Data Flow**:
1. **Write Operations**: Save to Room immediately → Sync to Firebase in background
2. **Read Operations**: Load from Room instantly → Update from API if needed
3. **Network Resilience**: App fully functional without internet
4. **Conflict Resolution**: Last-write-wins based on timestamps

**Benefits**:
- ⚡ Instant data access (no loading spinners)
- 🔌 Works in airplane mode
- 💾 Data persistence guaranteed
- 🔄 Automatic sync when online

#### 🎨 **Material Design 3 Implementation**

- **Color System**: Custom dark theme with brown/gold palette
- **Typography**: Clear hierarchy with Roboto font family
- **Components**: Cards, buttons, text fields follow MD3 specs
- **Motion**: Subtle transitions and state changes
- **Accessibility**: Proper touch targets and contrast ratios

#### 📊 **Repository Pattern**

**Purpose**: Abstract data sources from business logic

**Responsibilities**:
- Coordinate between local and remote data sources
- Handle caching and synchronization
- Provide clean API to ViewModels
- Manage errors and loading states

**Implementation**:
```kotlin
class CocktailRepository(context: Context, cacheDao: CocktailCacheDao) {
    // 1. Check network status
    // 2. Load from cache immediately
    // 3. Fetch from API if online
    // 4. Update cache with fresh data
    // 5. Return Flow<Result<List<Cocktail>>>
}
```

#### 🧩 **Modular Structure**

```
app/
├── data/
│   ├── local/          # Room database entities, DAOs
│   ├── remote/         # Firebase, API services
│   └── repository/     # Repository implementations
├── ui/
│   ├── discover/       # Discovery and filtering
│   ├── details/        # Recipe detail views
│   ├── favorites/      # Favorites management
│   └── profile/        # User profile
├── utils/              # Utilities (NetworkUtils, etc.)
└── notifications/      # Push notifications
```

### **Technology Stack**

#### **Languages & Frameworks**
- **Kotlin** 2.1.10 - Modern, concise Android development
- **Android SDK** 26+ (Target 36) - Latest Android features
- **Kotlin Coroutines** 1.8.1 - Async/concurrent operations
- **Kotlin Flow** - Reactive data streams

#### **UI/UX**
- **Material Design 3** - Google's design system
- **ViewBinding** - Type-safe view access
- **RecyclerView** - Efficient list rendering
- **ConstraintLayout** - Flexible, flat view hierarchies
- **Glide** 4.16.0 - Image loading and caching

#### **Architecture Components**
- **ViewModel** - UI state management
- **LiveData/Flow** - Observable data holders
- **Room** 2.6.1 - Local database (SQLite wrapper)
- **Navigation** - Fragment navigation (if applicable)

#### **Networking**
- **Retrofit** 2.11.0 - HTTP client for REST APIs
- **OkHttp** 4.12.0 - HTTP/2 client with connection pooling
- **Gson** 2.11.0 - JSON serialization/deserialization

#### **Backend & Cloud**
- **Firebase Firestore** - NoSQL cloud database
- **Firebase Auth** - User authentication
- **Firebase Storage** - Cloud file storage
- **Firebase Analytics** - Usage tracking

#### **APIs**
- **API Ninjas** - Cocktail data (150+ recipes with ingredients)
- **TheCocktailDB** - High-quality cocktail images

#### **Testing** (Framework in place)
- **JUnit** 4.13.2 - Unit testing
- **Espresso** - UI testing
- **MockWebServer** 4.12.0 - Network testing
- **Coroutines Test** 1.8.1 - Async testing

#### **Build Tools**
- **Gradle** 8.7 with Kotlin DSL
- **Android Gradle Plugin** 8.7.2
- **Kapt** - Annotation processing
- **Google Services** - Firebase integration

---

## 🔧 Setup & Installation

### **Prerequisites**

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Version 17 or higher
- **Android SDK**: API 26+ (Android 8.0) minimum
- **Git**: For cloning the repository

### **Quick Start**

#### 1. **Clone the Repository**

```bash
git clone https://github.com/IIEMSA/2025-nov-opsc6312-poe-part3-tjay-km.git
cd 2025-nov-opsc6312-poe-part3-tjay-km
```

#### 2. **Open in Android Studio**

- Launch Android Studio
- Select "Open an Existing Project"
- Navigate to the cloned directory
- Wait for Gradle sync to complete

#### 3. **Configure API Keys** (Optional but Recommended)

Create a `local.properties` file in the project root:

```properties
# API Ninjas Key (for cocktail data)
API_KEY=your_api_ninjas_key_here
```

**API Ninjas** (Primary cocktail data):
- Visit [API Ninjas](https://api-ninjas.com/)
- Sign up for a free account
- Copy your API key
- Add to `local.properties` as shown above

**TheCocktailDB** (Cocktail images):
- Free tier available at [TheCocktailDB](https://www.thecocktaildb.com/api.php)
- No API key required for basic usage
- App fetches images automatically

**Note**: App includes fallback data and works with limited API access.

#### 4. **Configure Firebase** (Optional for Full Features)

For cloud sync and authentication:

1. Visit [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing
3. Add Android app with package name: `com.example.mixmate`
4. Download `google-services.json`
5. Place in `app/` directory

Enable these services in Firebase Console:
- ✅ Authentication (Email/Password)
- ✅ Cloud Firestore
- ✅ Cloud Storage

**Note**: App works fully offline without Firebase.

#### 5. **Build & Run**

**Via Android Studio**:
- Click the "Run" button (green triangle)
- Select your device/emulator
- Wait for build and installation

**Via Command Line**:
```bash
# Debug build
./gradlew assembleDebug
./gradlew installDebug

# Release build (signed)
./gradlew assembleRelease
```

### **Troubleshooting**

**Gradle Sync Failed**:
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

**App Crashes on Launch**:
- Check `logcat` for errors
- Verify minimum API level (26+)
- Clear app data and reinstall

**Firebase Not Working**:
- Verify `google-services.json` is in `app/` folder
- Check package name matches (`com.example.mixmate`)
- Enable required services in Firebase Console

**API Rate Limiting**:
- App includes 100ms delay between image requests
- Most features work with cached data
- Free tier limits: Check API Ninjas documentation

---

## 📌 Current Functionality

### **✅ Fully Implemented Features**

#### **Home Page**
- Featured cocktail of the day with hero image
- Grid of suggested cocktails (changes daily)
- Quick navigation to all major sections
- Favorites heart icons with instant feedback
- Offline support with cached data

#### **Discover Page**
- Browse 150+ cocktails in responsive grid
- **Multi-Criteria Filtering**:
  - By ingredient (search in name or ingredient list)
  - By category (Cocktail, Shot, Ordinary Drink, etc.)
  - By minimum rating (1-5 stars)
  - Filters work in combination (AND logic)
- Real-time search with instant results
- Works completely offline with cached data
- Smooth scrolling with image loading optimization

#### **MyBar**
- **Daily Rotating Suggestions**: Featured alcohol type changes daily
- **Ingredient Selection**:
  - Toggle between "Alcohol Types" and "Other Ingredients"
  - Visual selection with interactive cards
  - Multi-select support (find drinks with ALL selected ingredients)
- **Smart Filtering**: Client-side filtering of 150+ cocktails
- Filter by single ingredient or multiple (AND logic)
- Works offline with cached data
- Displays matching cocktails in grid layout

#### **Recipe Details**
- **Hybrid Data Loading**:
  - Supports API cocktails (from Discover/Home/MyBar)
  - Supports custom recipes (from Profile)
  - Fallback mechanisms for missing data
- **Display Elements**:
  - Large hero image with Glide caching
  - Cocktail name and metadata
  - Complete ingredient list with measurements
  - Step-by-step instructions
  - Optional: Difficulty, prep time, glassware, garnish
- **Interactive Features**:
  - Add/remove favorites with instant feedback
  - Back navigation button
  - Footer navigation bar
  - Toast notifications for favorites

#### **Favorites**
- Add/remove cocktails from any page (Discover, Home, MyBar, Details)
- **Cross-Page Synchronization**: Heart icons update everywhere instantly
- **Offline Support**: Changes saved to Room database
- **User-Specific**: Isolated per userId
- View all favorites in Profile page
- Click to open full recipe details
- Grid layout with 2 columns
- Empty state with helpful message

#### **Custom Recipe Creation**
- **Comprehensive Form**:
  - Recipe name and description
  - Dynamic ingredient list (add/remove fields)
  - Step-by-step instructions
  - Difficulty level (Easy, Medium, Hard)
  - Preparation time (minutes)
  - Optional: Glassware and garnish
- **Image Support**:
  - Select from device gallery
  - Image preview before saving
  - Upload to Firebase Storage when online
- **Dual Storage**:
  - Saves to Room immediately (works offline)
  - Syncs to Firebase in background when online
- View custom recipes in Profile page
- Edit and delete functionality
- Works completely offline

#### **Profile Page**
- **My Recipes Section**:
  - Horizontal carousel of custom recipes
  - Image thumbnails with names
  - Click to view full details
  - Empty state when no recipes
- **Favorites Section**:
  - 2-column grid of favorited cocktails
  - Click to view details
  - Remove from favorites option
  - Empty state when no favorites
- **User Information**:
  - Display name
  - Username/handle
  - Join date
- **Quick Actions**:
  - Floating Action Button for creating recipes
  - Settings navigation
  - Footer navigation

#### **Offline Mode** ⭐
- **Cocktail Caching**:
  - 150+ cocktails cached in Room database
  - 24-hour cache validity with auto-refresh
  - LRU cache management (keeps 200 most recent)
  - Search and filter cached data offline
- **Custom Recipes**: Full CRUD operations offline
- **Favorites**: Add/remove offline with sync
- **Network Detection**: Automatic online/offline detection
- **Background Sync**: Changes sync when connection restored
- **Graceful Degradation**: Clear feedback when features need internet

#### **Settings**
- Language selection (English/Afrikaans)
- Persistent language preference
- Logout functionality
- Clear cache option
- App version information

#### **Authentication**
- Email/password registration
- Login with validation
- Session management
- User profile creation
- Logout with data clearing

---

## 📊 Database Schema

### **Room Database Tables**

#### **favorites** (User Favorites)
```sql
CREATE TABLE favorites (
    cocktailId TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    imageUrl TEXT,
    ingredients TEXT,
    instructions TEXT,
    userId TEXT NOT NULL,
    savedAt INTEGER
);
```

#### **custom_recipes** (User-Created Recipes)
```sql
CREATE TABLE custom_recipes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    instructions TEXT,
    ingredients TEXT,  -- JSON array
    glassware TEXT,
    garnish TEXT,
    preparationTime INTEGER,
    difficulty TEXT,
    imageUri TEXT,
    userId TEXT NOT NULL,
    createdAt INTEGER,
    updatedAt INTEGER
);
```

#### **cocktail_cache** (Offline Cocktail Data) 🆕
```sql
CREATE TABLE cocktail_cache (
    cocktailId TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    imageUrl TEXT,
    category TEXT,
    rating REAL,
    ingredients TEXT,  -- JSON array
    instructions TEXT,
    servings TEXT,
    cachedAt INTEGER,
    lastAccessedAt INTEGER
);
```

### **Firebase Collections**

#### **users** (User Profiles)
```javascript
{
  userId: "string",
  displayName: "string",
  email: "string",
  createdAt: timestamp
}
```

#### **recipes** (Cloud-Backed Custom Recipes)
```javascript
{
  recipeId: "string",
  userId: "string",
  name: "string",
  description: "string",
  instructions: "string",
  ingredients: [{
    name: "string",
    amount: "string",
    unit: "string"
  }],
  imageUrl: "string",  // Firebase Storage URL
  isPublic: boolean,
  createdAt: timestamp,
  updatedAt: timestamp
}
```

---

## 📚 Documentation

### **Available Documentation**

- **[RELEASE_NOTES_PART3.md](RELEASE_NOTES_PART3.md)** - Complete feature list and changelog
- **[OFFLINE_MODE_DOCUMENTATION.md](OFFLINE_MODE_DOCUMENTATION.md)** - Offline architecture and testing
- **[README.md](README.md)** - This file (project overview)

### **Code Documentation**

- KDoc comments on all public classes and functions
- Inline comments for complex logic
- Architecture decision records in commit messages

---

## 🧪 Testing

### **Testing Strategy**

#### **Unit Tests** (Framework in place)
- ViewModel logic testing
- Repository pattern testing
- Data transformation testing
- Network request mocking

#### **Integration Tests**
- Room database operations
- API integration testing
- Firebase operations (mocked)

#### **UI Tests** (Framework in place)
- Espresso for UI testing
- User flow testing
- Navigation testing

### **Manual Testing**

#### **Offline Mode Testing**
1. Launch app while online (builds cache)
2. Enable airplane mode
3. Test all features (browse, search, filter, favorites, custom recipes)
4. Disable airplane mode
5. Verify automatic sync

#### **Cross-Platform Testing**
- Physical devices (various Android versions)
- Emulators (API 26-34)
- Different screen sizes and densities

---

## 🚀 Future Enhancements

### **Planned Features**
- [ ] Social sharing of custom recipes
- [ ] Recipe ratings and reviews from community
- [ ] Cocktail of the day push notifications
- [ ] Advanced search with voice input
- [ ] Shopping list for ingredients
- [ ] Measurement unit conversion
- [ ] Dark/Light theme toggle
- [ ] Export recipes to PDF
- [ ] Integration with smart home devices
- [ ] AR cocktail preparation guide

### **Technical Improvements**
- [ ] Increase unit test coverage to 80%+
- [ ] Implement UI tests with Espresso
- [ ] Add CI/CD pipeline with GitHub Actions
- [ ] Performance monitoring with Firebase Performance
- [ ] Crash reporting with Crashlytics
- [ ] A/B testing for new features

---

## 🤝 Contributing

This is an educational project for OPSC coursework. Contributions are currently limited to team members.

### **Development Workflow**
1. Create feature branch from `master`
2. Implement feature with tests
3. Submit pull request with description
4. Code review by team members
5. Merge after approval

### **Commit Conventions**
We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: Add new feature
fix: Bug fix
docs: Documentation update
style: Code style change (no logic change)
refactor: Code refactoring
test: Add or update tests
chore: Build/tooling changes
```

---

## 📄 License

This project is developed for educational purposes as part of the OPSC6312 course at The IIE's Varsity College.

**Copyright © 2024 MixMate Team. All rights reserved.**

---

## 🙏 Acknowledgments

### **APIs & Services**
- [API Ninjas](https://api-ninjas.com/) - Comprehensive cocktail data
- [TheCocktailDB](https://www.thecocktaildb.com/) - High-quality cocktail images
- [Firebase](https://firebase.google.com/) - Backend services and authentication

### **Libraries & Tools**
- [Glide](https://github.com/bumptech/glide) - Image loading
- [Retrofit](https://square.github.io/retrofit/) - HTTP client
- [Room](https://developer.android.com/training/data-storage/room) - Local database
- [Material Design](https://m3.material.io/) - Design system

### **Inspiration**
- Professional bartending apps and mixology resources
- Android development best practices and design patterns
- Open source community contributions

---

## 📞 Support & Contact

### **Reporting Issues**
- Check existing issues first
- Provide detailed description with logs
- Include device info and Android version
- Steps to reproduce the issue

### **For Course-Related Questions**
Contact team members via Varsity College email addresses.

---

## 📈 Project Statistics

- **Lines of Code**: ~15,000+ (Kotlin)
- **Database Tables**: 3 Room + 2 Firebase Collections
- **API Integrations**: 2 (API Ninjas, TheCocktailDB)
- **Features**: 10+ major features
- **Offline Capability**: 95%+ of features work offline
- **Supported Languages**: 2 (English, Afrikaans)
- **Minimum Android Version**: 8.0 (API 26)
- **Target Android Version**: 14 (API 34)

---

## 🎓 Educational Context

**Course**: OPSC6312 - Open Source Coding  
**Institution**: The IIE's Varsity College  
**Semester**: 2024, Semester 2  
**Project**: Portfolio of Evidence (POE) Part 3  

### **Learning Outcomes Demonstrated**
✅ Android application development with Kotlin  
✅ MVVM architecture pattern implementation  
✅ Room database and data persistence  
✅ Firebase cloud services integration  
✅ RESTful API consumption with Retrofit  
✅ Material Design 3 implementation  
✅ Offline-first architecture design  
✅ Version control with Git  
✅ Collaborative development workflows  
✅ Technical documentation practices  

---

<div align="center">

### 🍸 MixMate - Crafting Perfect Cocktails, One Sip at a Time

**Made with ❤️ by the MixMate Team**

[⬆ Back to Top](#-mixmate---your-ultimate-cocktail-companion)

</div>