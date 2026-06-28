# WhatsDel

WhatsDel is a modern Android application that recovers deleted WhatsApp messages. It silently captures incoming WhatsApp notifications and stores them locally. When the sender deletes a message, WhatsDel preserves the original content — completely offline, with no cloud or server dependency.

## 🚀 Features
- **Deleted Message Recovery**: Automatically detects when WhatsApp replaces a message with "This message was deleted" and preserves the original text.
- **Real-time Notification Monitoring**: Background service captures every incoming WhatsApp text message via `NotificationListenerService`.
- **Live Dashboard**: Displays total captured messages and deleted messages count, updated in real-time.
- **Messages Feed**: Browse all captured messages with sender, chat name, preview, timestamp, and a "Deleted" badge for recovered messages.
- **Deleted Messages Screen**: Dedicated view showing only recovered messages with original text, capture time, deletion time, search, and sort.
- **Search**: Full-text search across sender, chat name, and message content on both Messages and Deleted screens.
- **Smart Deduplication**: Prevents duplicate entries when WhatsApp pushes the same notification multiple times.
- **Group Chat Support**: Correctly separates sender name from group chat name.
- **Permission Management**: Guides the user through enabling Notification Access, Storage, and Battery Optimization with direct links to system settings.
- **Material 3 Design**: Modern UI with dynamic colors, dark mode support, and responsive layouts.

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
 ├── service/         # NotificationListenerService for message capture & deletion detection
 ├── ui/              
 │    ├── components/ # Reusable Compose widgets (MessageItem, DeletedMessageItem, StatCard, etc.)
 │    ├── screens/    # Main screens and ViewModels (Dashboard, Messages, Deleted, Settings)
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
- **Notification Access**: To capture incoming WhatsApp messages and detect deletions.
- **Storage Access**: To save data safely.
- **Ignore Battery Optimization**: To keep the background monitoring service running reliably.

## 🗺️ Project Roadmap
- **Phase 1 (Completed)**: Project foundation, UI architecture, Jetpack Compose screens, and Room database setup.
- **Phase 2 (Completed)**: Real-time notification monitoring and message capture via `NotificationListenerService`.
- **Phase 3 (Completed)**: Deleted message detection, recovery, search, sort, and Settings with DB statistics.
