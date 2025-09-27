# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

# MixMate Android (OPSC Cocktail App)

A practical guide for building, testing, and contributing to MixMate - an Android cocktail application built with Kotlin.

## Quick Start

1. Open the project in Android Studio and let Gradle sync
2. Start an emulator from AVD Manager or connect a device
3. Build and install debug:
   - **Windows**: `gradlew.bat :app:installDebug`
   - **macOS/Linux**: `./gradlew :app:installDebug`

## Prerequisites

- **Android Studio**: Latest stable version
- **Java**: Version 17 (required for AGP 8.9.2)
- **Android SDK**: API 35 (compileSdk) and API 26+ (minSdk)
- **Gradle**: Uses wrapper (8.11.1) - no separate installation needed

## Project Structure

**Single Module Architecture**
- `app/` - Main application module containing all source code
- `app/src/main/java/com/example/mixmate/` - Kotlin source files
- `app/src/main/res/` - Resources (layouts, drawables, values)
- `app/src/test/` - Unit tests (JUnit 4)
- `app/src/androidTest/` - Instrumented tests (AndroidJUnit4)

**Key Configuration**
- **Application ID**: `com.example.mixmate`
- **Kotlin**: 1.9.24
- **AGP**: 8.9.2
- **Target/Compile SDK**: 35
- **Min SDK**: 26

## Build Commands

### Basic Operations
```bash
# Windows
gradlew.bat clean
gradlew.bat :app:assembleDebug
gradlew.bat :app:installDebug

# macOS/Linux  
./gradlew clean
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

### Release Builds
```bash
# Windows
gradlew.bat :app:assembleRelease
gradlew.bat :app:bundleRelease

# macOS/Linux
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

## Testing

### Unit Tests
```bash
# Run all unit tests
gradlew.bat :app:testDebugUnitTest          # Windows
./gradlew :app:testDebugUnitTest            # macOS/Linux

# Run specific test class
gradlew.bat :app:testDebugUnitTest --tests com.example.mixmate.ExampleUnitTest
```

### Instrumented Tests
**Prerequisites**: Ensure an emulator is running or device is connected (`adb devices`)

```bash
# Run instrumented tests
gradlew.bat :app:connectedDebugAndroidTest   # Windows
./gradlew :app:connectedDebugAndroidTest     # macOS/Linux
```

**Test Runner**: `androidx.test.runner.AndroidJUnitRunner`

## Linting and Code Quality

### Android Lint
```bash
gradlew.bat :app:lintDebug                  # Windows
./gradlew :app:lintDebug                    # macOS/Linux
```
**Reports Location**: `app/build/reports/lint/`

### Additional Tools (Optional)
The following tools are not currently configured but can be added:
- **ktlint**: For Kotlin code formatting
- **detekt**: For static code analysis  
- **spotless**: For code formatting

## Emulator and ADB Commands

```bash
# List connected devices
adb devices

# View app logs  
adb logcat | findstr "MixMate"              # Windows
adb logcat | grep "MixMate"                 # macOS/Linux

# Clear app data
adb shell pm clear com.example.mixmate

# Uninstall app
adb uninstall com.example.mixmate
```

## Architecture Overview

**UI Layer**: Traditional XML-based layouts with Activities
- `MainActivity` - Login screen (entry point)
- `SignUpPage` - User registration  
- `HomePage` - Main app interface after login

**Navigation**: Simple Intent-based navigation between activities

**Core Libraries**:
- AndroidX Core KTX 1.15.0
- Material Design Components 1.12.0
- ConstraintLayout 2.2.1
- AppCompat 1.7.0

**Build Configuration**: Uses Gradle Version Catalogs (`gradle/libs.versions.toml`)

## Development Workflow

### Git Guidelines
- **Branching**: Use feature branches from main
- **Commits**: Follow Conventional Commits (e.g., `feat:`, `fix:`, `docs:`)
- **Git Pager Rule**: Use `--no-pager` only for `git log`, `git diff`, and `git show`

```bash
# Correct usage
git log --no-pager --oneline --graph
git diff --no-pager
git show --no-pager

# Don't use --no-pager for these
git add
git commit  
git push
git status
```

### Server Management
Run any long-running servers in a separate PowerShell or Command Prompt window outside of this chat environment to keep them always on.

### Code Review Checklist
- [ ] Build passes locally
- [ ] Tests updated or added
- [ ] Lint checks pass
- [ ] Screenshots included for UI changes

## CI/CD

The project uses **GitHub Actions** with Super Linter:
- Workflow file: `.github/workflows/superlinter.yml`
- Triggers on push to any branch
- Runs comprehensive linting across the codebase

For local CI simulation:
```bash
# Ensure build passes
gradlew.bat build                           # Windows
./gradlew build                             # macOS/Linux
```

## Troubleshooting

### Common Issues

**Gradle Sync Failures**
- Invalidate caches and restart Android Studio
- Check Java version: `java -version` (should be 17+)
- Ensure ANDROID_HOME is set or Android Studio manages SDK

**Emulator Issues**  
- Cold boot the emulator or create a new AVD
- Check available system images in SDK Manager

**Build Errors**
- Clean and rebuild: `gradlew.bat clean build`
- Check for conflicting dependencies in `gradle/libs.versions.toml`

### Useful Commands
```bash
# List all available Gradle tasks
gradlew.bat tasks                           # Windows
./gradlew tasks                             # macOS/Linux

# Get Gradle version info
gradlew.bat --version                       # Windows
./gradlew --version                         # macOS/Linux
```

## File Locations

**Build Outputs**: `app/build/`
- APKs: `app/build/outputs/apk/`
- AABs: `app/build/outputs/bundle/`
- Test Results: `app/build/reports/tests/`
- Lint Reports: `app/build/reports/lint/`

**Configuration Files**:
- Version catalog: `gradle/libs.versions.toml`
- App config: `app/build.gradle.kts`
- Manifest: `app/src/main/AndroidManifest.xml`

---

*This WARP.md file should be updated when AGP, Kotlin, or SDK versions change, or when new modules/features are added.*