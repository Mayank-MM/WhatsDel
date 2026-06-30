# WhatsDel

WhatsDel is a modern Android application that captures WhatsApp notifications and preserves message history locally. It silently records incoming WhatsApp notifications and stores them on the device. When supported notification updates indicate that a message has been deleted or edited, WhatsDel preserves the original content for later viewing—all completely offline, with no cloud or server dependency.

> **Disclaimer:** WhatsDel is an independent project and is not affiliated with, endorsed by, or sponsored by WhatsApp or Meta.

---

# 🚀 Features

* **Deleted Message Recovery**: Detects supported WhatsApp notification updates indicating a deleted message and preserves the original text captured earlier.
* **Edited Message History**: Preserves both the original and edited versions of supported text messages along with their edit history.
* **Real-time Notification Monitoring**: Background service captures incoming WhatsApp notifications using `NotificationListenerService`.
* **Live Dashboard**: Displays real-time statistics for captured, deleted, edited, and supported media messages.
* **Messages Feed**: Browse captured messages with sender, chat name, preview, timestamp, and status badges.
* **Deleted Messages Screen**: Dedicated screen showing recovered deleted messages.
* **Edited Messages Screen**: Compare original and edited versions with complete edit history.
* **Media Detection**: Detects supported media notifications and stores available metadata.
* **Search & Filtering**: Search across sender, chat name, and message content.
* **Smart Deduplication**: Prevents duplicate entries caused by repeated notification updates.
* **Group Chat Support**: Correctly separates sender and group information.
* **Permission Management**: Guides users through enabling Notification Access and other required permissions.
* **Material 3 Design**: Modern UI with dynamic colors, dark mode, and responsive layouts.

---

# 🛠️ Tech Stack

* **Language:** Kotlin 2.0
* **UI Framework:** Jetpack Compose
* **Architecture:** MVVM (Model-View-ViewModel)
* **Dependency Injection:** Dagger Hilt
* **Database:** Room (KSP)
* **Navigation:** Navigation Compose
* **Reactive Programming:** Kotlin Coroutines & StateFlow
* **Build System:** Gradle Kotlin DSL with Version Catalogs

---

# 🏗️ Project Structure

```text
app/src/main/java/com/example/whatsdel/
 ├── data/
 ├── di/
 ├── domain/
 ├── navigation/
 ├── service/
 ├── ui/
 │    ├── components/
 │    ├── screens/
 │    └── theme/
 └── utils/
```

---

# ⚙️ How to Build and Run

## Android Studio

1. Open Android Studio.
2. Open the project.
3. Wait for Gradle Sync.
4. Connect a physical device or start an emulator.
5. Run the application.

## Command Line

```bash
./gradlew assembleDebug

adb install -r -t app/build/outputs/apk/debug/app-debug.apk

adb shell am start -n "com.example.whatsdel/.MainActivity"
```

---

# 🔒 Required Permissions

* Notification Access
* Storage Access (where applicable)
* Ignore Battery Optimization (recommended)

---

# ⚠️ Current Limitations

WhatsDel is built entirely on Android's public APIs. Because of Android's security model and WhatsApp's end-to-end encryption, there are important limitations.

### Notification-Based Architecture

WhatsDel captures information **only from WhatsApp notifications**.

It does **not** access WhatsApp's private database or encrypted message storage.

---

### Deleted Message Recovery

Deleted messages can only be recovered if:

* The original notification was received.
* WhatsDel was running with Notification Access enabled.
* The message was captured before it was deleted.

Messages that were never shown in a notification cannot be recovered.

---

### Edited Message Detection

Edited message history depends on WhatsApp issuing a notification update.

If WhatsApp does not update the notification, the edit cannot be detected.

---

### Media Recovery

Media support depends on what Android exposes through notifications.

While WhatsDel can detect media notifications and capture available metadata, Android does not generally provide direct access to WhatsApp's private media files.

As a result:

* Images, videos, voice notes, stickers, and documents cannot always be recovered automatically.
* Recovery depends on the information made available by Android and WhatsApp.
* Some media types may only have metadata available rather than the original file.

---

### Notification Dependency

WhatsDel may not capture messages if:

* Notification Access is disabled.
* WhatsApp notifications are turned off.
* The message does not generate a notification.
* Android suppresses notifications under certain conditions.

---

### Android Security

WhatsDel does **not**:

* Read WhatsApp's encrypted database.
* Bypass Android sandbox restrictions.
* Circumvent WhatsApp security mechanisms.
* Access private application storage without permission.

The application intentionally relies only on Android-supported mechanisms.

---

# 🗺️ Roadmap

## ✅ Phase 1

* Project foundation
* MVVM architecture
* Room database
* Material 3 UI

## ✅ Phase 2

* Notification monitoring
* Message capture
* Smart deduplication

## ✅ Phase 3

* Deleted message recovery
* Search
* Sort
* Dashboard
* Settings

## ✅ Phase 4

* Edited message detection
* Edit history timeline
* Edited messages screen
* Dashboard analytics

## 🚧 Planned

* Advanced global search
* Chat-based organization
* Timeline view
* Local backup & restore
* Database encryption
* PIN/Biometric lock
* Export (JSON, CSV, PDF)
* Performance optimizations
* Additional UI enhancements

---

# 📄 License

This project is intended for educational, research, and personal use.

Users are responsible for complying with applicable laws, platform policies, and the privacy of anyone whose data may be processed by the application.
