<div align="center">

# 🌸 tamatami

### A period tracker that grows with you — literally.

**Log your cycle. Care for your Tama. Watch them bloom across menstrual, follicular, ovulatory, and luteal phases.**

<p>
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android%20%7C%20iOS-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Kotlin Multiplatform" src="https://img.shields.io/badge/Kotlin%20Multiplatform-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img alt="SwiftUI" src="https://img.shields.io/badge/SwiftUI-0071E3?style=for-the-badge&logo=swift&logoColor=white" />
  <img alt="Material 3" src="https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white" />
</p>

<sub>Period tracking that doesn't feel clinical. A tiny companion that reacts to where you are in your cycle.</sub>

</div>

---

## ✨ Why tamatami?

Most period trackers feel like a spreadsheet with a pink border. **tamatami** treats your cycle as something you live *with*, not just log. Your Tama — a Tamagotchi-style companion — shifts its mood, glow, and energy as your hormones do. Logging a period day, recording a mood, finishing a training session, all of it ripples back into the little creature on the home screen.

It's still a real period tracker underneath: predictions, calendar marks, phase-aware nutrition and training suggestions. It's just one you might actually open every day.

---

## 📱 Screenshots

<!--
  To replace the placeholders below with real screenshots:
  1. Drop PNG files into  docs/screenshots/  (create the folder)
  2. Suggested names: home.png, calendar.png, training.png, cycle.png, nutrition.png
  3. Swap the via.placeholder.com URLs for  docs/screenshots/<name>.png
  Recommended capture size: 1080×2400 (any Pixel emulator at 420 dpi).
-->

<div align="center">

| Home & your Tama | Calendar | Training |
| :---: | :---: | :---: |
| <img src="https://via.placeholder.com/260x520/E4527A/FFFFFF?text=Home+%E2%80%A2+Tama" width="220" alt="Home — your Tama reacts to today's phase" /> | <img src="https://via.placeholder.com/260x520/FA7E1E/FFFFFF?text=Calendar" width="220" alt="Phase-tinted calendar with logged days and predictions" /> | <img src="https://via.placeholder.com/260x520/FEDA75/333333?text=Training" width="220" alt="Phase-aware workout suggestions" /> |
| **Cycle info** | **Nutrition** | **Onboarding** |
| <img src="https://via.placeholder.com/260x520/962FBF/FFFFFF?text=Cycle" width="220" alt="Cycle insights and hormone overview" /> | <img src="https://via.placeholder.com/260x520/B58CB0/FFFFFF?text=Nutrition" width="220" alt="Foods that match where you are in your cycle" /> | <img src="https://via.placeholder.com/260x520/E4527A/FFFFFF?text=Name+your+Tama" width="220" alt="Onboarding — name your Tama and log your last period" /> |

</div>

> _Demo GIF coming soon. Drop a recording at `docs/screenshots/demo.gif` and replace this line with:_
> `<p align="center"><img src="docs/screenshots/demo.gif" width="280" alt="tamatami in motion" /></p>`

---

## 🎨 The four phases, four moods

Each cycle phase has its own color in the app — they tint the calendar, the home backdrop, and your Tama's glow.

<div align="center">

| Phase | Color | What you'll feel in-app |
| :--- | :---: | :--- |
| **Menstrual** | <kbd>&nbsp;&nbsp;</kbd> `#E4527A` | Soft, restful UI. Your Tama curls up and asks for kindness. |
| **Follicular** | <kbd>&nbsp;&nbsp;</kbd> `#FEDA75` | Bright, sunny tint. Energy returning — training suggestions ramp up. |
| **Ovulatory** | <kbd>&nbsp;&nbsp;</kbd> `#FA7E1E` | Warm orange. Peak-window cues; Tama is bouncy. |
| **Luteal** | <kbd>&nbsp;&nbsp;</kbd> `#962FBF` | Deep magenta. The app gets cozier; nutrition leans grounding. |

</div>

---

## 🧩 What's inside

- 🏠 **Home** — your Tama, today's mood, and quick trackers (water, mood, activity) at a glance.
- ❤️ **Cycle & Training** — one tab with three views: phase summary and what to expect
  (*Cycle info*), phase-aware food suggestions (*Nutrition*), and phase-aware workout
  recommendations (*Training* — gentler moves in menstrual, push days in follicular/ovulatory,
  mobility in luteal).
- 📅 **Calendar** — its own tab: a month grid tinted by predicted phase, with logged-period dots
  whose darkness encodes flow (spotting → heavy), prediction rings for next period and ovulation,
  and a today ring. Tap any day to log flow and mood.
- 🩺 **Health** — *Hormones* (estrogen / progesterone / LH rhythms across the cycle) and
  *Medication* tracking.
- ⚙️ **Settings** — reached via the gear in the top bar of every screen; profile, cycle length,
  daily goals, and reminders.
- 👋 **Onboarding** — name your Tama, log your last period, set cycle length. You're tracking
  inside two minutes.

---

## 🛠 Built with

**Kotlin Multiplatform** — the domain, data, and business logic (cycle prediction, the Tamagotchi
mood engine, phase guides, nutrition and training recommendations) live in a shared `:shared`
module and are consumed by both apps.

- **Android** — Jetpack Compose + Material 3, Navigation Compose (type-safe `@Serializable` routes),
  edge-to-edge insets.
- **iOS** — SwiftUI, calling directly into the shared KMP framework (no logic reimplemented in
  Swift); Lottie via `lottie-ios`.
- **Room** (Android) / **SQLDelight** (shared) for local persistence — your cycle never leaves your device
- **Coroutines / Flow** end to end, bridged to SwiftUI on iOS
- The mascot renders as a **Lottie** animation that plays once, rests ~1 minute, then replays — a
  calm heartbeat rather than a restless loop — and cross-fades gently between moods.

Targets: Android 12+ (min SDK 31, compile SDK 36) and iOS 16+.

---

## 🚀 Getting started

```bash
# clone
git clone https://github.com/<your-org>/tamatami.git
cd tamatami

# build and install on a connected device or emulator
./gradlew :app:installDebug

# or open in Android Studio (Iguana+) and hit ▶︎
```

For iOS, generate and open the Xcode project (requires [XcodeGen](https://github.com/yonaskolb/XcodeGen)):

```bash
cd iosApp
xcodegen generate
open Tamatami.xcodeproj   # then pick an iPhone simulator and hit ▶︎
```

Run the unit tests:

```bash
./gradlew :app:testDebugUnitTest   # Android
./gradlew :shared:jvmTest          # shared domain (mood engine, guides, predictions)
```

---

## 🗺 Project layout

```
shared/src/commonMain/kotlin/com/mobile/tamatami/
├── domain/        # CyclePhase, Mood, PeriodFlow, TamagotchiMoodEngine, guides…
└── data/          # SQLDelight-backed repositories (shared by both apps)

app/src/main/java/com/mobile/tamatami/   # Android
├── data/          # Room entities, DAOs, repositories
├── di/            # AppContainer — hand-rolled DI
└── ui/
    ├── components/   # Scaffold, BottomBar, SettingsAction, shared UI
    ├── nav/          # TamatamiNavHost + type-safe routes
    ├── screens/      # home, cycleinfo (Cycle & Training), calendar, nutrition,
    │                 # training, health, hormones, pills, onboarding, settings
    └── theme/        # Phase + mood color tokens, gradients

iosApp/Tamatami/                          # iOS (SwiftUI)
├── RootView.swift     # onboarding gate + TabView (Home · Cycle & Training · Calendar · Health)
├── *Screen.swift      # one view per tab/screen, calling into the shared framework
└── TamagotchiAvatar.swift  # Lottie mascot driven by the shared mood engine
```

---

## 💖 A note on the name

**tama** (玉) — a small, precious thing. **tamagotchi** — the tiny digital pet that taught a generation what daily care feels like. **tamatami** is both: a little life you tend to, one cycle at a time.

---

<div align="center">
<sub>Made with care. Period tracking should feel like that, too.</sub>
</div>
