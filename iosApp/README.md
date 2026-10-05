# Tamatami iOS — build, run & deploy guide

This is the iOS half of the Kotlin Multiplatform migration. The shared business
logic + data layer live in `:shared` and compile for iOS; the SwiftUI app in
`iosApp/` consumes them through the `Shared` framework.

> **Current navigation (4-tab shell, see `RootView.swift`):**
> **Home** (Tama avatar + today trackers) · **Cycle & Training** (a segmented
> Cycle-guide/Nutrition + Training view, `GuideScreen`) · **Calendar** (month
> grid + day detail, `CalendarScreen`) · **Health** (Hormones + Medication).
> Settings is reached via the gear in the top bar (not a tab). Some setup notes
> below predate this layout and describe an earlier build order — follow the
> code, not the historical tab names, where they differ.

**What can and can't be done without a Mac + Xcode**

| Step | Needs Xcode? |
|---|---|
| Compile shared Kotlin for iOS (`compileKotlinIosSimulatorArm64`) | No (done in CI/this repo already) |
| Link the `Shared.framework` (`linkDebugFramework…`) | **Yes** — uses `xcodebuild` |
| Build/run the SwiftUI app, Simulator, device, TestFlight | **Yes** |

Everything below assumes **macOS with full Xcode installed** (not just Command
Line Tools). Verify with `xcodebuild -version` — if that errors, run
`sudo xcode-select -s /Applications/Xcode.app/Contents/Developer` first.

---

## 1. Build the shared framework

From the repo root:

```bash
# Simulator (Apple Silicon):
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
# Device:
./gradlew :shared:linkDebugFrameworkIosArm64
```

Output: `shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework`
(and the `iosArm64` equivalent for device).

> On an Intel Mac add an `iosX64()` target in `shared/build.gradle.kts` next to
> `iosSimulatorArm64()` and link that instead.

**Recommended: let Xcode build the framework automatically.** Add a *Run Script*
build phase to the app target (see step 3) so you never link by hand.

---

## 2. Create the Xcode project

The `iosApp/Tamatami/*.swift` files are ready; they need an Xcode project around
them.

1. Xcode → **File ▸ New ▸ Project ▸ iOS ▸ App**.
2. Product Name: **Tamatami**; Interface: **SwiftUI**; Language: **Swift**.
   Save it into the existing `iosApp/` folder (so the project sits at
   `iosApp/Tamatami.xcodeproj`).
3. Delete the auto-generated `ContentView.swift` and the `App` file — you'll use
   the ones already in `iosApp/Tamatami/`.
4. **Add the existing sources:** right-click the group ▸ *Add Files to
   "Tamatami"…* ▸ select every `.swift` in `iosApp/Tamatami/`:
   - `TamatamiApp.swift` (entry + `AppState`)
   - `RootView.swift` (onboarding-vs-tabs gate + tab shell)
   - `OnboardingFlow.swift`
   - `HomeScreen.swift`, `CalendarScreen.swift`, `TrainingScreen.swift`,
     `CycleInfoScreen.swift`, `MedicationScreen.swift`,
     `HormonesScreen.swift`, `SettingsScreen.swift`, `NutritionScreen.swift`
   - `TamagotchiAvatar.swift`
   - `AddMedicationSheet.swift`, `LogWorkoutSheet.swift`, `AddHormoneSheet.swift`
   - `FlowWatcher.swift`, `NotificationScheduler.swift`
   Uncheck "Copy items if needed" (they're already in place).
5. Set **minimum deployment** to iOS 16 (the Swift uses `NavigationStack` /
   `.task`).

---

## 3. Link the `Shared` framework

**Option A — Run Script (recommended, always up to date):**

1. Target **Tamatami** ▸ *Build Phases* ▸ **+** ▸ *New Run Script Phase*, drag it
   above *Compile Sources*. Script:
   ```bash
   cd "$SRCROOT/.."
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```
   The `embedAndSignAppleFrameworkForXcode` task is provided by the Kotlin
   plugin; it builds the right framework for the current
   `PLATFORM_NAME`/`ARCHS`/`CONFIGURATION` and embeds it.
2. Target ▸ *Build Settings* ▸ add **Framework Search Paths**:
   `$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
3. Target ▸ *Build Phases* ▸ *Link Binary With Libraries* ▸ **+** ▸ add
   `Shared.framework` (browse to the path above after one build).
4. *Build Settings* ▸ **Other Linker Flags**: add `-framework Shared`.

**Option B — manual:** run the `linkDebugFramework…` task from step 1, then drag
the resulting `Shared.framework` into the project and set *Embed & Sign*.

After a build, Xcode generates the Objective-C header for the framework. **Import
it in Swift with `import Shared`** (already done in the source files).

---

## 4. Run in the Simulator

1. Select the **Tamatami** scheme and an iOS 16+ Simulator (e.g. iPhone 15).
2. **⌘R**. First build runs Gradle to produce the framework — expect a minute.
3. You should see the tab bar (Home / Meds). Tap **+** on Meds to insert a
   sample medication; the row + adherence update live from the shared SQLDelight
   DB. Kill and relaunch — the data persists (proves the native SQLite driver).

---

## 5. Notifications

`NotificationScheduler.swift` requests authorization on launch and schedules via
`UNUserNotificationCenter`, driven by the **shared** `ReminderEvaluator`. For
real periodic evaluation in the background, register a `BGAppRefreshTask`
(Signing & Capabilities ▸ **Background Modes** ▸ *Background fetch* /
*Background processing*) and call
`await appState.notifications.evaluateAndSchedule(sdk:)` from its handler. The
scaffold calls it on demand only.

---

## 6. Device + TestFlight

- **Device run:** Signing & Capabilities ▸ select your **Team** (a free Apple ID
  works for on-device debugging). Plug in an iPhone, pick it as the run target,
  ⌘R.
- **TestFlight / App Store:** requires a **paid Apple Developer Program**
  membership ($99/yr). Then: set a real bundle id, Product ▸ *Archive* ▸
  *Distribute App* ▸ *App Store Connect*.

---

## 7. Known adjustment points (KMP ↔ Swift bridging)

The Swift files use the conventional names Kotlin/Native gives bridged symbols.
Confirm these against the generated header (Xcode ▸ jump-to-definition on
`import Shared`) after the first successful build and tweak if needed:

- **kotlinx-datetime types** appear as `Kotlinx_datetimeLocalDate` /
  `Kotlinx_datetimeLocalTime` (that module prefix can vary by version). The SDK
  exposes `sdk.today()` / `sdk.now()` so app code mostly avoids constructing
  these directly.
- **Enum cases** bridge to lowerCamelCase: `TimeOfDay.MORNING` → `.morning`,
  `CyclePhase.MENSTRUAL` → `.menstrual`. Adjust if your Kotlin version emits a
  different casing.
- **`List<T>`** bridges to `NSArray`; `FlowWatcher<NSArray>` then casts to
  `[MedicationToday]`. Single-value flows (e.g. `CycleSnapshot`) use
  `FlowWatcher<CycleSnapshot>` directly.
- **`Int`** (Kotlin) ↔ `Int32` (Swift); `Long` ↔ `Int64`. `daysUntilNextPeriod`
  is a boxed `KotlinInt?` → read `.intValue`.
- **suspend functions** bridge to Swift `async throws` — hence the
  `try? await` calls.

If a symbol name differs, it's a rename only — the shape and behavior are what
the shared code (already compiled for iOS) guarantees.

---

## What's implemented vs. stubbed

**Shared (done, compiles for iOS):** all domain logic, all 7 repositories over
SQLDelight, the `TamatamiSdk` facade (repos + `today()/now()/localDate()`,
`currentCycle()`, `observeOnboardingComplete()`, `completeOnboarding()`,
`updateSettings()`), `FlowObserver`.

**iOS app (scaffold — written, not yet built in Xcode):**
- Entry + SDK bootstrap + notification permission.
- **Onboarding flow** → `completeOnboarding`; the root view gates on
  `observeOnboardingComplete()` and switches to the tabs automatically.
- Screens wired to shared repos: **Home** (Tamagotchi avatar + cycle snapshot),
  **Calendar** (full 6×7 month grid via the shared `MonthBuilder`, phase-colored
  cells with today/period/predicted markers, month nav, tap-a-day detail that
  logs water/flow), **Training** (shared recommendation + recent workouts +
  **log sheet**), **Cycle info** (pure `guideFor` per phase), **Nutrition**
  (pure `nutritionFor` per phase), **Medication** (observe/toggle + **add
  sheet**), **Hormones** (recent + **add-reading sheet**), **Settings**
  (load/edit/`updateSettings`).
- **Tamagotchi avatar** on Home: emoji-based mood face + accessory badges +
  `bounceHz`-driven animation, driven by the shared `TamagotchiMoodEngine` via
  `tamagotchi.observe`. (Emoji stand-in for the Android vector art — swap for a
  Canvas/asset when desired.)
- Real input sheets: `AddMedicationSheet`, `LogWorkoutSheet`, `AddHormoneSheet`.
- Notification permission + shared-logic-driven scheduler.

**Not yet built:** richer Tamagotchi art (currently emoji); a HealthKit step
source (Android's `StepDataSource` is Health-Connect-only, no shared
equivalent); `BGTaskScheduler` registration for background reminders;
delete/edit affordances on logged rows.

> **First-build reality check:** none of the Swift has been compiled (no Xcode
> here). Expect to fix a handful of bridged-symbol names on the first build —
> see §7. In particular verify: `TrainingRecommender.shared` / `ReminderEvaluator.shared`
> (Kotlin `object` → `.shared`), `PhaseGuideKt.guideFor` (top-level fun →
> `<File>Kt`), `HormoneLog.value_`, enum case casing, and `KotlinInt`/`KotlinBoolean`
> boxing on `daysUntilNextPeriod` / the onboarding-gate flow.
