# FloatWatch - Modern Floating Stopwatch for Android

FloatWatch is a lightweight, responsive native Android stopwatch app built with **Kotlin** and **Jetpack Compose**. Its primary feature is drawing a high-precision, floating, draggable, and collapsible stopwatch HUD over any running application (games, study sessions, workouts, productivity tools).

---

## 🌟 Key Features

### 1. Floating Window Overlay (`SYSTEM_ALERT_WINDOW`)
- **Draggable Anywhere:** Move the floating bubble freely across your screen.
- **Compact HUD Mode:** Toggle between expanded controls (Play/Pause, Lap, Reset) and a minimalist mini-pill counter.
- **Accurate Background Timekeeping:** Runs in a persistent Android Foreground Service driven by `SystemClock.elapsedRealtime()`, preventing background timer drift and OS battery throttling.

### 2. Freemium UI & Customization
- **Free Tier:**
  - Full centisecond accuracy.
  - Obsidian Dark & Clean White skins.
  - Essential floating controls & up to 10 laps recorded per session.
  - Haptic touch feedback.
- **Pro Tier ($2.99 Lifetime):**
  - **Exclusive Skins:** Cyberpunk Neon, Frosted Glass (Glassmorphism), and OLED Stealth Red.
  - Unlimited laps with split time calculation.
  - Fine-grained transparency / opacity slider (30% to 100%).
  - 100% ad-free experience.

---

## 🛠 Tech Stack
- **Language:** Kotlin 2.0
- **UI:** Jetpack Compose + Material 3 (including `ComposeView` embedded directly inside the Android `WindowManager`)
- **Architecture:** MVI / Clean Architecture with Kotlin Coroutines & `StateFlow`
- **Persistence:** Android Jetpack DataStore Preferences
- **Build System:** Gradle (Kotlin DSL) with Version Catalogs (`libs.versions.toml`)

---

## 🚀 Building & Running

### Requirements
- Android SDK 35 (minSdk 26)
- JDK 17 or JDK 21 (bundled with Android Studio JBR)

### Build Debug APK:
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Install on Device via ADB:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
