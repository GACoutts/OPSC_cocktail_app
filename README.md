# 🍸 MixMate - Your Ultimate Cocktail Companion

![MixMate Logo](mixmate_logo.png)

## Team Members:
Teejay Kamwaro – ST10274142  
Grant Coutts - ST10258297  
Tristan Vries - ST10380906  
Kelvin Gravett - ST10108660  

**Github Link:**  
https://github.com/IIEMSA/opsc6312-poe-part-2-tjay-km

---

**MixMate** is a modern Android cocktail app that makes discovery, creation, and curation effortless. From quick “what can I make with what I have?” checks to saving your own recipes, MixMate keeps everything fast, tidy, and offline-friendly.

[![Build Status](https://github.com/GACoutts/OPSC_cocktail_app/actions/workflows/android.yml/badge.svg)](https://github.com/GACoutts/OPSC_cocktail_app/actions)
[![API Level](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-Integrated-orange.svg)](https://firebase.google.com)

---

## 🎯 Purpose & Vision

### **App Purpose**
MixMate was designed to solve the common challenges faced by cocktail enthusiasts:
- **Recipe Discovery**: Finding new and exciting cocktail recipes that’s actually browsable on mobile.
- **Personal Collection**: Creating and managing recipes for your own custom cocktails.
- **Ingredient Management**: Tracking available ingredients and suggesting cocktails so you can filter by what’s already in your bar.
- **Learning Platform**: Understanding cocktail techniques and flavor profiles
- **Clear steps & structure**: Effecient prep time, difficulty estimation, glasswareneed, and garnishing.

### **Target Audience**
- 🏠 **Home Bartenders**: Cocktail enthusiasts who want to expand their repertoire
- 👨‍🍳 **Professional Bartenders**: Industry professionals seeking recipe management tools
- 🎉 **Party Hosts**: People who want to impress guests with creative drinks and need reliable crowd-pleasers
- 📚 **Cocktail Students**: Learners exploring the art of mixology and are learning techniques and ratios

---

## ✨ Key Features

### 🎨 **Card Design**
- Beautiful card-based UI for recipes and ingredients
- Interactive cards with swipe, tap, and long-press actions
- Consistent design language throughout the app
- Optimized for readability and usability

### 👤 **Profile Management**
- Personal recipe collection
- Favorites system for quick access
- User preferences and settings
- Join date and profile customization

### 🔄 **Offline-First Architecture**
- **Local Database**: Room database for instant access
- **Cloud Sync**: Firebase Firestore for backup and sharing
- **Graceful Degradation**: Works perfectly without internet
- **Real-time Updates**: Seamless synchronization across devices
- **Efficient Searching**: Filter and search by name/ingredient/difficulty
- **Glide** caching for images to keep lists snappy

### ⭐ **Favourites (offline)**
- Mark and organize your favorite cocktails
- Smart categorization of favorites
- Quick access to your most-used recipes
- Sync favorites across devices
- Saved locally with Room (works without internet)
- Each favourite stores: name, image URL, folded ingredients string, instructions, timestamp
- Seamless click-through back to Recipe Details

### 🔥 **Firebase Integration**
- User authentication and secure data storage
- Real-time database synchronization
- Analytics for usage patterns
- Cloud storage for recipe images

### 🥃 **My Bar**

- Track the ingredients you actually have
- Get suggestions that match your shelf

### 📝 **Custom Recipes**

- Create local recipes with name, image, ingredients, steps
- Optional fields: glassware, garnish, difficulty, prep time

---

## 🏗️ Architecture & Design Considerations

### **Architectural Pattern: MVVM + Repository Pattern**

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Layer      │    │  Repository      │    │   Data Sources  │
│                 │    │                  │    │                 │
│ • Activities    │◄──►│ • RecipeRepo     │◄──►│ • Room DB       │
│ • Fragments     │    │ • UserManager    │    │ • Firebase      │
│ • Adapters      │    │ • CocktailApi    │    │ • CocktailDB    |
|                 │    |                  |    | • Firestore     |
└─────────────────┘    └──────────────────┘    └─────────────────┘

• Networking: Retrofit + OkHttp
• Images: Glide
• DB: Room (SQLite)
• Cloud: Firebase Firestore (NoSQL)


```

### **Design Principles**

#### 🌐 **Offline-First Strategy**
- **Local Storage Priority**: Room database serves as single source of truth
- **Background Synchronization**: Firebase updates happen transparently
- **Network Resilience**: App functions fully without internet connectivity
- **Conflict Resolution**: Smart merging of local and cloud data

#### 🎨 **UI/UX Design**
- **Dark Theme**: Elegant brown and gold color palette
- **Material Design 3**: Modern Android design guidelines
- **Responsive Layouts**: Optimized for various screen sizes
- **Intuitive Navigation**: Clear user flow and consistent interactions

#### 📊 **Data Management**
- **Hybrid Repository Pattern**: Combines local and remote data sources
- **Type Converters**: Seamless serialization of complex objects
- **Migration Support**: Smooth database schema updates

### **Technology Stack**

#### **Frontend**
- **Language**: Kotlin 2.1.10
- **UI Framework**: Android Views with Material Design 3
- **Architecture**: MVVM + Repository Pattern
- **Navigation**: Intent-based with proper lifecycle management

#### **Backend & Data**
- **Local Database**: Room 2.8.1 with SQLite
- **Cloud Database**: Firebase Firestore
- **Authentication**: Firebase Auth (planned)
- **Image Storage**: Firebase Storage
- **API Integration**: Retrofit 2.11.0 with TheCocktailDB API

#### **Development Tools**
- **Build System**: Gradle with Kotlin DSL
- **Testing**: JUnit 4, Espresso, MockWebServer
- **Code Coverage**: Jacoco
- **Version Control**: Git with conventional commits

---

## 🧪 Testing Strategy

### **Unit Testing**
- **Framework**: JUnit 4.13.2
- **Mocking**: MockWebServer 4.12.0
- **Coroutines**: Kotlinx Coroutines Test 1.8.1
- **Coverage Target**: 80%+ code coverage

---

### 🔧 **Setup & Installation**

**Requirements**

- Android Studio Ladybug (or newer)
- JDK 17
- minSdk 26, targetSdk 36

**Clone & Build**

git clone https://github.com/GACoutts/OPSC_cocktail_app.git
cd OPSC_cocktail_app
./gradlew :app:assembleDebug
./gradlew :app:installDebug


**Firebase (optional, for cloud features)**

1. Create a project at https://console.firebase.google.com/
2. Add Android app com.example.mixmate
3. Place google-services.json in app/
4. Enable Cloud Firestore

**TheCocktailDB**

Free tier works for development: https://www.thecocktaildb.com/api.php

---

### 📌 **Current Functionality**

**Discover**

Pulls cocktails from TheCocktailDB and renders them as image-forward cards in the dark theme. Grid spacing is tuned for scan-ability. Tapping a card opens Recipe Details.


**Recipe Details**

Large hero image, bold name, compact meta (difficulty + prep time), clean ingredient list, and readable instructions. If the drink came from the API, we display what we have; if it’s a local custom recipe, we also show glassware and garnish. A heart in the header toggles favourites instantly (offline too).


**Favourites (offline)**

Stored via Room with the fields we need offline: name, image URL (Glide-cached), folded ingredients, instructions, timestamp. Smooth scrolling; each item opens back into Recipe Details. When online, local and cloud (Firestore) can reconcile in the background.


**My Bar**

Maintain a list of ingredients you own and use it to filter discovery results.


**Submit / Custom Recipes**

Create your own recipes (name, image, ingredients, steps) with optional meta (glassware, garnish, difficulty, prep time). Saved locally for instant retrieval.


---

### 🔭 **Roadmap**

- Advanced search & filters
- Ratings / reviews
- Sharable recipe links
- Export options

---

### 📄 **License**

Educational project for OPSC coursework.


<div align="center">

*MixMate - Crafting Perfect Cocktails, One Sip at a Time* 🍸

</div>
