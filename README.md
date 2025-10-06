# 🍸 MixMate - Your Ultimate Cocktail Companion

![App Banner](https://via.placeholder.com/800x200/8B4513/FFFFFF?text=MixMate+Cocktail+App)

**MixMate** is a modern Android cocktail application that brings the art of mixology to your fingertips. Whether you're a professional bartender or cocktail enthusiast, MixMate provides everything you need to discover, create, and perfect your cocktail recipes.

[![Build Status](https://github.com/GACoutts/OPSC_cocktail_app/actions/workflows/android.yml/badge.svg)](https://github.com/GACoutts/OPSC_cocktail_app/actions)
[![API Level](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-Integrated-orange.svg)](https://firebase.google.com)

## 📱 Screenshots

*Add your app screenshots here*

| Home Screen | Recipe Creation | Profile & Recipes | Recipe Details |
|-------------|----------------|-------------------|----------------|
| ![Home](https://via.placeholder.com/200x350/8B4513/FFFFFF?text=Home) | ![Create](https://via.placeholder.com/200x350/8B4513/FFFFFF?text=Create) | ![Profile](https://via.placeholder.com/200x350/8B4513/FFFFFF?text=Profile) | ![Details](https://via.placeholder.com/200x350/8B4513/FFFFFF?text=Details) |

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

### 🔍 **Discovery & Search**
- Browse curated cocktail collections
- Search by name, ingredient, or difficulty
- Filter by available ingredients in your bar
- Discover trending and seasonal cocktails

### 📝 **Custom Recipe Creation**
- **Hybrid Storage System**: Offline-first with cloud synchronization
- Create detailed recipes with ingredients, instructions, and photos
- Specify glassware, garnish, preparation time, and difficulty
- Rich image support with beautiful default cocktail icons

### 👤 **Profile Management**
- Personal recipe collection
- Favorites system for quick access
- User preferences and settings
- Join date and profile customization

### 🥃 **My Bar Management**
- Track your available ingredients
- Get personalized cocktail suggestions
- Shopping list generation for missing ingredients
- Inventory management with expiration tracking

### 🔄 **Offline-First Architecture**
- **Local Database**: Room database for instant access
- **Cloud Sync**: Firebase Firestore for backup and sharing
- **Graceful Degradation**: Works perfectly without internet
- **Real-time Updates**: Seamless synchronization across devices

---

## 🏗️ Architecture & Design Considerations

### **Architectural Pattern: MVVM + Repository Pattern**

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Layer      │    │  Repository      │    │   Data Sources  │
│                 │    │                  │    │                 │
│ • Activities    │◄──►│ • RecipeRepo     │◄──►│ • Room DB       │
│ • Fragments     │    │ • UserManager    │    │ • Firebase      │
│ • Adapters      │    │ • CocktailApi    │    │ • API Ninjas    │
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
- **API Integration**: Retrofit 2.11.0 with API Ninjas

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

### **Integration Testing**
- **Framework**: AndroidX Test with Espresso 3.7.0
- **Database Testing**: Room in-memory database
- **Network Testing**: MockWebServer for API calls
- **UI Testing**: Espresso with Idling Resources

### **Test Coverage Report**
```bash
./gradlew jacocoTestReport
# Coverage reports generated in: build/reports/jacoco/jacocoTestReport/
```

---

## 🔧 Setup & Installation

### **Prerequisites**
- Android Studio Ladybug or newer
- Java 17+ (required for AGP 8.12.3)
- Android SDK API 26+ (minSdk) and API 36 (targetSdk)
- Git for version control

### **Firebase Setup**
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add Android app with package name: `com.example.mixmate`
3. Download `google-services.json` and place in `app/` directory
4. Enable Firestore Database and Authentication

### **API Configuration**
1. Get API key from [API Ninjas](https://api.api-ninjas.com/)
2. Add to `local.properties`:
```properties
API_KEY=your_api_ninjas_key_here
```

### **Build & Run**
```bash
# Clone the repository
git clone https://github.com/GACoutts/OPSC_cocktail_app.git
cd OPSC_cocktail_app

# Build debug APK
./gradlew :app:assembleDebug

# Install on connected device
./gradlew :app:installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

---

## 🚀 GitHub & CI/CD Integration

### **GitHub Repository Structure**
```
OPSC_cocktail_app/
├── .github/
│   └── workflows/
│       └── android.yml          # CI/CD pipeline
├── app/
│   ├── src/
│   │   ├── main/                # Main application code
│   │   ├── test/                # Unit tests
│   │   └── androidTest/         # Integration tests
│   └── build.gradle.kts         # App-level build configuration
├── gradle/
│   └── libs.versions.toml       # Dependency version catalog
├── README.md                    # Project documentation
└── WARP.md                      # Development guidelines
```

### **GitHub Actions Workflow**

Our CI/CD pipeline automatically:
- ✅ **Builds** the app on every push and PR
- ✅ **Runs** unit and integration tests
- ✅ **Generates** code coverage reports
- ✅ **Validates** code quality with linting
- ✅ **Deploys** APKs to release artifacts

#### **Workflow Configuration** (`.github/workflows/android.yml`)
```yaml
name: Android CI/CD
on:
  push:
    branches: [ master, develop ]
  pull_request:
    branches: [ master ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
      
      - name: Run unit tests
        run: ./gradlew test
      
      - name: Generate test coverage
        run: ./gradlew jacocoTestReport
      
      - name: Build debug APK
        run: ./gradlew assembleDebug
```

### **Branch Strategy**
- **`master`**: Production-ready code, protected branch
- **`develop`**: Integration branch for features
- **`feature/*`**: Individual feature development
- **`hotfix/*`**: Critical bug fixes

### **Commit Convention**
We follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` New features
- `fix:` Bug fixes
- `docs:` Documentation updates
- `test:` Adding or updating tests
- `refactor:` Code refactoring
- `style:` Code formatting changes

---

## 📸 Development Progress - Commit Screenshots

### **Feature Development Timeline**

#### **Initial Project Setup**
*Paste screenshot of initial commits here*

![Initial Setup](https://via.placeholder.com/800x200/4CAF50/FFFFFF?text=Initial+Project+Setup+Commits)

---

#### **UI Development & Styling**
*Paste screenshot of UI-related commits here*

![UI Development](https://via.placeholder.com/800x200/2196F3/FFFFFF?text=UI+Development+Commits)

---

#### **Database Integration**
*Paste screenshot of database setup commits here*

![Database Integration](https://via.placeholder.com/800x200/FF9800/FFFFFF?text=Database+Integration+Commits)

---

#### **Firebase Integration**
*Paste screenshot of Firebase setup commits here*

![Firebase Integration](https://via.placeholder.com/800x200/F44336/FFFFFF?text=Firebase+Integration+Commits)

---

#### **Recipe Management System**
*Paste screenshot of recipe feature commits here*

![Recipe Management](https://via.placeholder.com/800x200/9C27B0/FFFFFF?text=Recipe+Management+Commits)

---

#### **Testing & Quality Assurance**
*Paste screenshot of testing commits here*

![Testing Implementation](https://via.placeholder.com/800x200/607D8B/FFFFFF?text=Testing+Implementation+Commits)

---

#### **Final Integration & Bug Fixes**
*Paste screenshot of final commits here*

![Final Integration](https://via.placeholder.com/800x200/795548/FFFFFF?text=Final+Integration+Commits)

---

## 📊 GitHub Actions Build History

### **Build Status Overview**
*Paste screenshot of GitHub Actions overview here*

![GitHub Actions](https://via.placeholder.com/800x300/1976D2/FFFFFF?text=GitHub+Actions+Build+History)

---

### **Test Coverage Reports**
*Paste screenshot of test coverage results here*

![Test Coverage](https://via.placeholder.com/800x300/43A047/FFFFFF?text=Test+Coverage+Reports)

---

## 👥 Team & Contributions

### **Development Team**
- **Lead Developer**: [Your Name]
- **UI/UX Design**: [Team Member]
- **Backend Integration**: [Team Member]
- **Testing & QA**: [Team Member]

### **Contribution Guidelines**
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📈 Future Roadmap

### **Phase 1 - Core Features** ✅
- [x] Basic cocktail discovery
- [x] Custom recipe creation
- [x] Profile management
- [x] Offline-first architecture

### **Phase 2 - Enhanced Features** 🚧
- [ ] Social sharing capabilities
- [ ] Recipe ratings and reviews
- [ ] Advanced search filters
- [ ] Cocktail recommendations AI

### **Phase 3 - Premium Features** 📅
- [ ] Professional bartender tools
- [ ] Inventory cost tracking
- [ ] Recipe analytics
- [ ] Export functionality

---

## 🔗 Links & Resources

- **GitHub Repository**: [OPSC_cocktail_app](https://github.com/GACoutts/OPSC_cocktail_app)
- **Firebase Console**: [Project Dashboard](https://console.firebase.google.com/)
- **API Documentation**: [API Ninjas Cocktail API](https://api.api-ninjas.com/v1/cocktail)
- **Design System**: [Material Design 3](https://m3.material.io/)

---

## 📄 License

This project is developed for educational purposes as part of the OPSC course curriculum.

---

## 🤝 Support

For support, questions, or contributions:
- 📧 **Email**: [your.email@domain.com]
- 🐛 **Issues**: [GitHub Issues](https://github.com/GACoutts/OPSC_cocktail_app/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/GACoutts/OPSC_cocktail_app/discussions)

---

<div align="center">

**Built with ❤️ using Android & Firebase**

*MixMate - Crafting Perfect Cocktails, One Sip at a Time* 🍸

</div>