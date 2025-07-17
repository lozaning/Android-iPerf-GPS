# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a new Android application project using Kotlin, set up with modern Android development practices and Android SDK 34.

## Development Commands

### Building the Project
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.example.androidproject.YourTestClass"
```

### Code Quality and Linting
```bash
# Run lint checks
./gradlew lint

# Generate lint report
./gradlew lintDebug
```

### Development Server
```bash
# No development server needed for Android - use Android Studio or command line builds
```

## Project Structure

### Key Directories
- `app/src/main/java/com/example/androidproject/` - Main Kotlin source code
- `app/src/main/res/` - Android resources (layouts, strings, drawables)
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumented tests
- `app/build/` - Build outputs (APKs, reports)

### Important Files
- `app/build.gradle` - App-level build configuration and dependencies
- `build.gradle` - Project-level build configuration
- `settings.gradle` - Project settings and module declarations
- `app/src/main/AndroidManifest.xml` - App manifest with permissions and components
- `app/src/main/res/layout/activity_main.xml` - Main activity layout
- `app/src/main/res/values/strings.xml` - String resources

## Architecture Notes

### Current Setup
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Build Tools**: Gradle 8.0.0
- **Architecture**: Single Activity with basic setup
- **View System**: View Binding enabled

### Key Dependencies
- AndroidX Core KTX
- AppCompat
- Material Design Components
- ConstraintLayout
- JUnit for testing
- Espresso for UI testing

## Development Notes

### Adding New Features
1. Create new activities/fragments in `app/src/main/java/com/example/androidproject/`
2. Add corresponding layouts in `app/src/main/res/layout/`
3. Update `AndroidManifest.xml` if adding new activities
4. Add string resources to `strings.xml`
5. Update `app/build.gradle` for new dependencies

### Testing Approach
- Unit tests go in `app/src/test/`
- Instrumented tests go in `app/src/androidTest/`
- Use JUnit 4 for unit tests
- Use Espresso for UI tests

### Common Patterns
- Use View Binding for accessing views
- Follow Material Design guidelines
- Use AndroidX libraries
- Implement proper lifecycle management
- Handle configuration changes appropriately