# WhatsDel

WhatsDel is a modern Android application designed to safely capture, manage, and recover deleted messages and media from notifications. Built with Kotlin and Jetpack Compose, it features a robust, reactive architecture.

## 🚀 Features
- **Modern Dashboard**: High-level overview of message statistics (total, deleted, edited, and media).
- **Messages & Deleted Views**: Dedicated screens for viewing captured notifications and recovered content.
- **Settings & Permissions Hub**: Centralized system for managing essential app permissions (Notification Access, Storage, Battery Optimization) directly linked to system settings.
- **Material 3 Design**: Fully custom, responsive, and dynamic UI using the latest Material Design 3 guidelines.

## 🛠️ Tech Stack
- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles
- **Dependency Injection**: Dagger Hilt
- **Database**: Room (with Kotlin Symbol Processing - KSP)
- **Navigation**: Navigation Compose
- **Reactive Programming**: Kotlin Coroutines & StateFlow
- **Build System**: Gradle Kotlin DSL with Version Catalogs (`libs.versions.toml`)

## 🏗️ Project Structure
```text
app/src/main/java/com/example/whatsdel/
 ├── data/            # Room Database, Entities, DAOs, and Repository implementations
 ├── di/              # Hilt Modules for Dependency Injection
 ├── domain/          # Domain Models and Repository Interfaces
 ├── navigation/      # NavGraph and Screen route definitions
 ├── service/         # Background services (NotificationListenerService placeholder)
 ├── ui/              
 │    ├── components/ # Reusable Compose UI widgets
 │    ├── screens/    # Main application screens and ViewModels
 │    └── theme/      # Material 3 colors, typography, and theme definitions
 └── utils/           # Helper classes (Permissions, Date formatting)
```

## ⚙️ How to Build and Run
This project requires **Java 17** and **Android Gradle Plugin 8.9.1**.

### Option 1: Android Studio (Recommended)
1. Open Android Studio.
2. Select **File > Open** and point to the `WhatDel` folder.
3. Wait for the initial Gradle sync to complete.
4. Connect an Android device with USB Debugging enabled, or start an Emulator.
5. Click the green **Run** button.

### Option 2: Command Line (ADB)
```bash
# Build the APK
./gradlew assembleDebug

# Install on connected device
adb install -r -t app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n "com.example.whatsdel/.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```

## 🔒 Permissions Required
To function correctly, WhatsDel requires:
- **Notification Access**: To capture incoming messages.
- **Storage Access**: To save media files safely.
- **Ignore Battery Optimization**: To ensure the background monitoring service is not killed by the Android system.

## 🗺️ Project Roadmap
- **Phase 1 (Completed)**: Project foundation, UI architecture, Jetpack Compose screens, and Room database setup.
- **Phase 2 (Upcoming)**: Core notification monitoring (`NotificationListenerService`) and deleted message recovery logic.
