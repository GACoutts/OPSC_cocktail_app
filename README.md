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

**MixMate** is a modern Android cocktail application that brings the art of mixology to your fingertips. Whether you're a professional bartender or cocktail enthusiast, MixMate provides everything you need to discover, create, and perfect your cocktail recipes.

[![Build Status](https://github.com/GACoutts/OPSC_cocktail_app/actions/workflows/android.yml/badge.svg)](https://github.com/GACoutts/OPSC_cocktail_app/actions)
[![API Level](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-Integrated-orange.svg)](https://firebase.google.com)

---

## 🎯 Purpose & Vision

### **App Purpose**
MixMate was designed to solve the common challenges faced by cocktail enthusiasts:
- **Recipe Discovery**: Finding new and exciting cocktail recipes
- **Personal Collection**: Creating and managing custom recipes
- **Ingredient Management**: Tracking available ingredients and suggesting cocktails
- **Learning Platform**: Understanding cocktail techniques and flavor profiles

### **Target Audience**
- 🏠 **Home Bartenders**: Cocktail enthusiasts who want to expand their repertoire
- 👨‍🍳 **Professional Bartenders**: Industry professionals seeking recipe management tools
- 🎉 **Party Hosts**: People who want to impress guests with creative drinks
- 📚 **Cocktail Students**: Learners exploring the art of mixology

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

### ⭐ **Favourites**
- Mark and organize your favorite cocktails
- Smart categorization of favorites
- Quick access to your most-used recipes
- Sync favorites across devices

### 🔥 **Firebase Integration**
- User authentication and secure data storage
- Real-time database synchronization
- Analytics for usage patterns
- Cloud storage for recipe images

---

## 🏗️ Architecture & Design Considerations

### **Architectural Pattern: MVVM + Repository Pattern**

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Layer      │    │  Repository      │    │   Data Sources  │
│                 │    │                  │    │                 │
│ • Activities    │◄──►│ • RecipeRepo     │◄──►│ • Room DB       │
│ • Fragments     │    │ • UserManager    │    │ • Firebase      │
│ • Adapters      │    │ • CocktailApi    │    │ • CocktailDB    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
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

<div align="center">

*MixMate - Crafting Perfect Cocktails, One Sip at a Time* 🍸

</div>
