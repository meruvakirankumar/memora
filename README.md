# Memora

Memora turns real-world information into actionable memories: capture a label,
bill, or document; Memora reads it, understands what matters, and reminds you at
the right time — so you don't have to.

> **Capture it. Understand it. Remember it. Act on it.**

**Android** is the primary, feature-complete app (Kotlin + Jetpack Compose,
clean architecture). A native **iOS** app (Swift + SwiftUI) exists as an earlier
prototype and will be rebuilt on the same domain contract later.

## The Loop

```text
Capture → Understand → Identify → Extract → Confirm → Remember → Remind → Act → Complete
```

- **Capture** — take a photo or pick an image (native camera / Android Photo Picker).
- **Understand** — on-device OCR (ML Kit) reads the text.
- **Identify** — the actionable date is selected (e.g. `EXP` over `MFG`/`BATCH`).
- **Extract** — a structured candidate is produced (title, event type, date, confidence).
- **Confirm** — you review and correct; ambiguous dates ask you to resolve them.
- **Remember** — the memory + its reminder are stored locally (Room).
- **Remind** — a local notification fires from the lead-up window through overdue days.
- **Act / Complete** — mark it done (reminders stop) or edit/reschedule it.

## Features (Android)

- Camera and gallery capture with a temporary-image privacy lifecycle (images are
  never stored permanently).
- On-device OCR + a deterministic, testable extractor that **never fabricates a date**.
- Multi-format date parsing: ISO, textual months (`31 Aug 2027`, `AUG-27`),
  numeric, and separatorless; ambiguous day/month order is resolved by the user.
- Confirm screen with a Material date picker, event-type chips, and confidence guidance.
- Reminder engine: configurable lead time (1–10 days), status window
  (Upcoming / Reminder active / Due today / Overdue), and an overdue reminder window.
- Notifications via AlarmManager, rescheduled after reboot.
- Home: search, urgency-sorted list, attention summary, detail/edit, and
  undo (snackbar) on complete and delete.

## Architecture

Clean, layered, one-way dependencies. The UI never touches persistence, OCR, or
notifications directly.

```text
presentation  →  domain  →  data / platform
   (Compose)     (models,     (Room)   (capture, ML Kit OCR,
    ViewModels    use cases,            notifications, AlarmManager)
                  contracts)
```

- **domain** is Android-free (no Compose/Room/ML Kit) and unit-tested.
- **platform** implements the domain contracts (`TextExtractor`, `Notifier`,
  `ReminderScheduler`, `TempImageStore`).

## Project Structure

```text
android/
  gradle/libs.versions.toml           Version catalog
  app/build.gradle.kts                AGP, Room schema export, R8, signing
  app/src/main/java/com/meruvakirankumar/memora/
    MemoraApplication.kt              Hilt application
    core/                             error model, AppResult, Clock, DI
    domain/                           models, repository/extraction/scheduling contracts,
                                      services, use cases (all Android-free)
    data/                             Room entities, DAOs, database, mappers, repos, DI
    platform/                         ML Kit OCR, notifications, AlarmManager, temp image
    presentation/                     MainActivity, theme, home/capture/add/detail
  app/src/test/                       JVM unit tests
  app/src/androidTest/                Instrumented Room tests

ios/                                  Native iOS prototype (Swift + SwiftUI)
INTENT.md                             Product philosophy and decision rule
```

## Development Stages

```text
S0 Foundation   [x]   S5 Reminder engine     [x]
S1 Architecture [x]   S6 Notifications       [x]
S2 Capture      [x]   S7 Home UX + search    [x]
S3 Extraction   [x]   S8 Hardening           [x] (partial)
S4 Confirmation [x]   S9 Release engineering [x] (code side)
                      S10 Native iOS         [ ] (deferred; prototype only)
```

> Note: the code is complete through Stage 9 but has **not yet been compiled or
> run** in the current environment. Build it in Android Studio to verify.

## Build & Run (Android)

Requirements: **Android Studio Ladybug (2024.2)+**, **JDK 21** (bundled with
recent Android Studio), **Android SDK 35**, and internet access for Gradle/Maven.

1. Open the `android/` folder in Android Studio (it installs SDK 35 + JDK 21 and
   generates the Gradle wrapper).
2. Run on an emulator (API >= 26) or a connected device.

Command line (with JDK 21 + SDK on PATH, wrapper generated):

```bash
cd android
./gradlew testDebugUnitTest     # unit tests
./gradlew assembleDebug         # debug APK
```

### Signed release build

Create a keystore, then a `keystore.properties` at the repo root (never committed):

```properties
storeFile=/absolute/path/to/memora.jks
storePassword=...
keyAlias=...
keyPassword=...
```

```bash
cd android
./gradlew bundleRelease         # signed AAB (minified, resource-shrunk)
```

## iOS (prototype)

Requires macOS with Xcode 16+. Open `ios/Memora.xcodeproj` and run. iOS cannot be
built on Windows/Linux. It is a prototype and not aligned to the Android
architecture yet.

## Tech Stack

Kotlin 2.0 · Jetpack Compose (Material 3) · Hilt · Room · Coroutines/Flow ·
Navigation Compose · ML Kit Text Recognition · AlarmManager · JDK 21 target.

## Product Intent

See [INTENT.md](INTENT.md) for the full product philosophy and decision rule.
