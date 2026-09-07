# Memora

Memora is a mobile-first app for turning real-world information into actionable memories. It is built as **two fully native apps** that share no third-party cross-platform framework:

- **Android** — Kotlin + Jetpack Compose
- **iOS** — Swift + SwiftUI

Each OS uses its own native entry point; the repository is the single source, and the platform determines which native app is built and run.

```text
                 Memora (this repository)
                          |
         +----------------+----------------+
         |                                 |
   OS = Android                        OS = iOS
         |                                 |
  android/  (Kotlin)                 ios/  (Swift)
  MainActivity.kt  <-- entry -->     MemoraApp.swift
```

## Core Loop

```text
Capture -> Understand -> Identify -> Extract -> Confirm -> Remember -> Remind -> Act -> Complete
```

Both apps implement the same loop with shared behavior:

- Capture a new photo or select an existing image (native camera / photo picker).
- Review recognized text in an editable field.
- Extract a structured memory candidate from labels such as `EXP 08/27`.
- Confirm or edit the title, event type, and action date.
- Store confirmed memories locally on the device.
- Schedule a local reminder for actionable future dates.
- Sort active memories by urgency and show an attention summary.
- Track upcoming, due today, overdue, and completed states.
- Mark a memory complete (cancels its reminder) or delete it.

Recognized text is an editable field in this version. The extraction logic
(`MemoryExtractor`) is isolated so a native OCR engine can feed it later without
changing the confirmation or reminder flow.

## Project Structure

```text
android/                         Native Android app (Kotlin + Compose)
  settings.gradle.kts
  build.gradle.kts
  app/
    build.gradle.kts
    src/main/AndroidManifest.xml
    src/main/java/com/meruvakirankumar/memora/
      MainActivity.kt            Android entry point
      model/Memory.kt
      logic/MemoryExtractor.kt
      logic/MemoryStatusResolver.kt
      data/MemoryRepository.kt
      notifications/...          Channel, scheduler, receiver
      ui/...                     Compose screen + view model

ios/                             Native iOS app (Swift + SwiftUI)
  Memora.xcodeproj
  Memora/
    MemoraApp.swift              iOS entry point
    ContentView.swift            SwiftUI screen + native pickers
    MemoraViewModel.swift
    Models/Memory.swift
    Logic/MemoryExtractor.swift
    Logic/MemoryStatus.swift
    Data/MemoryStore.swift
    Notifications/ReminderScheduler.swift
```

## Build & Run

### Android (Windows, macOS, or Linux)

Requirements: Android Studio (Ladybug or newer), JDK 17, Android SDK 35.

1. Open the `android/` folder in Android Studio.
2. Let it sync (Android Studio generates the Gradle wrapper JAR automatically).
3. Run on an emulator or a connected device.

Command line (after the SDK and JDK 17 are installed and `gradle wrapper` has been generated):

```bash
cd android
./gradlew assembleDebug
```

### iOS (macOS only)

Requirements: macOS with Xcode 16 or newer.

1. Open `ios/Memora.xcodeproj` in Xcode.
2. Select a simulator or device and press Run.

iOS cannot be built on Windows or Linux — Apple's toolchain is macOS-only.

## Product Intent

See [INTENT.md](INTENT.md) for the full product philosophy and decision rule.