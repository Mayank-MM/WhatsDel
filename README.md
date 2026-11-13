# WhatsDel 🛡️

**WhatsDel** is a modern, privacy-focused Android application built with **Jetpack Compose** and **Material 3** that captures WhatsApp notifications, backs up media files, and preserves complete message history locally.

It silently records incoming WhatsApp text messages, photos, videos, voice notes, and documents. When supported notification updates indicate that a message has been deleted or edited, WhatsDel preserves the original content for offline viewing with zero cloud or server dependencies.

> **Disclaimer:** WhatsDel is an independent project and is not affiliated with, endorsed by, or sponsored by WhatsApp or Meta.

---

## 🚀 Key Features

* **Deleted Message Recovery**: Automatically detects deleted WhatsApp messages and alerts you via high-priority notifications with recovered original text and media.
* **Edited Message Timeline**: Preserves both original and edited message versions with edit timestamps and comparison cards.
* **Conversation-Wise Chat History**: Organizes messages into contact and group conversation threads with circular avatar initials/group icons, last message previews, timestamps, date separators, and rich chat bubbles.
* **Real-Time Media Backup Engine**: Background `FileObserver` engine backing up:
  - **Photos & Images** (`.jpg`, `.jpeg`, `.png`, `.webp`)
  - **Videos** (`.mp4`, `.3gp`, `.mkv`)
  - **Voice Notes & Audio** (`.opus`, `.ogg`, `.m4a`, `.aac`, `.mp3`)
  - **Document Files** (`.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`, `.ppt`, `.pptx`, `.txt`, `.zip`, `.rar`, `.apk`, `.csv`)
* **Gallery & Public Media Export**: Download backed-up media files directly to your phone's public Gallery (`Pictures/WhatsDel Downloads/` & `Movies/WhatsDel Downloads/`) or Downloads folder with instant OS indexing (`MediaScannerConnection`).
* **Export & Clear Conversation**: Export formatted plain-text conversation logs (`.txt`) via system share sheet (`ACTION_SEND`) or clear captured history with confirmation dialogs.
* **Group Title Sanitization**: Strips WhatsApp notification count suffixes (e.g. `" (4 messages)"` -> `"Vikings⚔️ Club"`) and merges duplicate entries.
* **Material 3 Light & Dark Themes**: Interactive Theme Selector dialog (**System Default**, **Light Theme** `#F8F9FA`, **Dark Theme** `#121417`) with custom WhatsApp Teal palette.
* **Live Overview Dashboard**: Real-time 2x2 statistics grid (**Conversations**, **Total Messages**, **Edited**, **Media Files**) and recent captured activity feed.
* **Storage Cleanup**: Individual file deletion and bulk **Clear All Media** controls with confirmation dialogs.

---

## 🛠️ Tech Stack & Architecture

* **Language:** Kotlin 2.0
* **UI Framework:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles
* **Dependency Injection:** Dagger Hilt
* **Database:** Room (Database Schema v8 with KSP migrations)
* **Navigation:** Navigation Compose (5-destination Bottom Navigation Bar)
* **Background Monitoring:** `NotificationListenerService` & `FileObserver`
* **Reactive Programming:** Kotlin Coroutines & StateFlow

---

## 🏗️ Project Structure

```text
app/src/main/java/com/example/whatsdel/
 ├── data/
 │    ├── dao/           # ConversationDao, MessageDao, EditHistoryDao
 │    ├── database/      # WhatsDelDatabase (v8 & Migrations)
 │    ├── entity/        # MessageEntity, ConversationEntity, MessageEditHistoryEntity
 │    └── repository/    # MessageRepositoryImpl
 ├── di/                 # Hilt Database & Repository Modules
 ├── domain/             # MessageRepository Interface & Domain Models
 ├── navigation/         # NavGraph, Screen routes, BottomNavItem
 ├── service/            # WhatsDelNotificationListenerService
 ├── ui/
 │    ├── components/    # StatCard, ConversationItem, MessageItem, MediaGridItem, etc.
 │    ├── screens/       # Dashboard, Conversations, Detail, Messages, Deleted, Media, Settings
 │    └── theme/         # Material 3 Color, Theme, Type
 └── utils/              # MediaDownloadHelper, WhatsAppMediaObserver, WhatsAppMediaBackupManager
```

---

## ⚙️ How to Build and Run

### Android Studio

1. Open Android Studio (2024.1+ / Jellyfish / Ladybug).
2. Open the project directory.
3. Wait for Gradle Sync to complete.
4. Connect a physical Android device (Android 7.0+ / API 24+) or start an emulator.
5. Click **Run 'app'** (`Shift + F10`).

### Command Line

```bash
# Build Debug APK
./gradlew assembleDebug

# Install on connected device
adb install -r -t app/build/outputs/apk/debug/app-debug.apk

# Launch App
adb shell am start -n "com.example.whatsdel/.MainActivity"
```

---

## 🔒 Required Permissions

* **Notification Access**: Required to capture incoming WhatsApp notifications in the background.
* **Storage Access**: Required to observe WhatsApp media folders and backup media files.
* **Ignore Battery Optimization**: Recommended to prevent Android from killing the background monitoring service.

---

## 🔒 Privacy & Security

WhatsDel is designed with privacy as its highest priority:
- All captured messages, media backups, and database records are **strictly stored on your device only**.
- Zero cloud servers, analytics trackers, or external network requests.
- Does not modify or access WhatsApp's internal database or encrypted storage.

---

## 📄 License

This project is intended for educational, research, and personal use.
