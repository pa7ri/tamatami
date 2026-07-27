# Creating the Tamatami iOS Xcode project — click by click

You've already got: Xcode 26.6 active, the shared framework linking, and all 17
Swift files typechecking against it (0 errors). This is the last mile — wrapping
those `.swift` files in an Xcode app target and pressing ⌘R.

Every path/name below is from *this* repo, verified — not generic boilerplate.

---

## 1. Create the app target

1. Open **Xcode ▸ File ▸ New ▸ Project…**
2. Choose **iOS ▸ App**, click **Next**.
3. Fill in:
   - **Product Name:** `Tamatami`
   - **Team:** your Apple ID team (needed later for device; fine to leave for Simulator)
   - **Organization Identifier:** `com.mobile` (so the bundle id is `com.mobile.Tamatami`)
   - **Interface:** **SwiftUI**
   - **Language:** **Swift**
   - Storage: **None**; leave tests unchecked.
4. Click **Next**. In the save dialog, navigate into the repo's **`iosApp/`**
   folder and **Create** there. You'll get `iosApp/Tamatami.xcodeproj` sitting
   next to the existing `iosApp/Tamatami/` sources.

## 2. Swap in the existing source files

Xcode generated its own `TamatamiApp.swift` + `ContentView.swift` inside a new
`Tamatami/` group — but our real sources are already in `iosApp/Tamatami/`.

1. In the Project navigator, select Xcode's generated `ContentView.swift` and
   the generated `TamatamiApp.swift`, **Delete ▸ Move to Trash**.
2. Right-click the **Tamatami** group ▸ **Add Files to "Tamatami"…**
3. Select all **17** `.swift` files in `iosApp/Tamatami/`:
   `TamatamiApp, RootView, OnboardingFlow, HomeScreen, CalendarScreen,
   TrainingScreen, CycleInfoScreen, NutritionScreen, MedicationScreen,
   HormonesScreen, SettingsScreen, TamagotchiAvatar, AddMedicationSheet,
   LogWorkoutSheet, AddHormoneSheet, FlowWatcher, NotificationScheduler`.
4. **Uncheck** "Copy items if needed" (they're already in place). Ensure
   **"Add to target: Tamatami"** is checked. **Add.**

## 3. Set the deployment target

Select the **Tamatami** project ▸ **Tamatami** target ▸ **General** ▸
**Minimum Deployments = iOS 16.0** (the Swift uses `NavigationStack` / `.task`).

## 4. Link the shared framework via a build-phase script

This makes Gradle rebuild + embed the right `Shared.framework` slice for
whatever you're building (simulator/device, debug/release) — no manual copying.

1. Target **Tamatami** ▸ **Build Phases**.
2. Click **+ ▸ New Run Script Phase**. Drag it so it runs **before**
   "Compile Sources".
3. Paste this script (uses the real task name in this repo). It passes
   `KOTLIN_FRAMEWORK_BUILD_TYPE` explicitly — the AGP-9 KMP-library plugin
   otherwise errors with *"Unable to detect Kotlin framework build type"*:
   ```bash
   cd "$SRCROOT/.."
   if [ "$CONFIGURATION" = "Release" ]; then KFBT=release; else KFBT=debug; fi
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode -PKOTLIN_FRAMEWORK_BUILD_TYPE=$KFBT
   ```
4. In that phase, **uncheck** "Based on dependency analysis" (so it always runs).
5. Still on the target ▸ **Build Settings** (toggle **All**):
   - **Framework Search Paths** → add:
     `$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
   - **Other Linker Flags** → add: `-framework Shared`

> ⚠️ **Path caveat (unverified from CLI):** the embed task writes the framework
> under `shared/build/xcode-frameworks/…`, but I could not confirm the exact
> subfolder outside Xcode — the task needs Xcode's full build environment
> (`BUILT_PRODUCTS_DIR`, `EXPANDED_CODE_SIGN_IDENTITY`, etc.) to run, which a
> plain `./gradlew` call doesn't supply. After your **first build attempt in
> Xcode**, run `find shared/build/xcode-frameworks -name Shared.framework` and,
> if the folder differs from the search path above, correct **Framework Search
> Paths** to the real parent directory. This is a one-time adjustment.

## 5. Build & run

1. Scheme: **Tamatami**; destination: an **iOS 16+ Simulator** (e.g. iPhone 15).
2. **⌘R**. The first build runs Gradle (a minute) to produce + embed the
   framework, then compiles the Swift.
3. Expected: the **onboarding form** appears (name, last-period date, cycle
   sliders). Fill it and tap **Get started** → the tab bar (Home / Calendar /
   Training / Cycle / Food / Meds) appears. This mirrors exactly what we saw
   working on Android.

## 6. Sanity checks once it launches

- **Meds tab ▸ +** → add "Vitamin D", Morning+Evening → the card appears; tap a
  slot chip → it toggles. Kill & relaunch → it persists (native SQLite driver).
- **Home** shows the Tamagotchi (emoji) + current cycle phase.
- **Calendar** shows the month grid with phase colors; tap a day → log water/flow.

## If the build fails

The Swift already typechecks against the framework, so failures here are almost
always **project wiring**, not code:

- **"No such module 'Shared'"** → the Run Script hasn't produced the framework
  yet (build once to run it), or the **Framework Search Paths** value is wrong.
  After a build attempt, run
  `find shared/build/xcode-frameworks -name Shared.framework` and point the
  search path at the real parent folder.
- **"Unable to detect Kotlin framework build type"** → the run-script isn't
  passing `-PKOTLIN_FRAMEWORK_BUILD_TYPE` — use the script in step 4 verbatim.
- **Gradle "command not found"** in the script → use an absolute path to
  `./gradlew`, or ensure the Run Script's shell can find it (the `cd "$SRCROOT/.."`
  should land at the repo root).
- **Signing error on device** → Target ▸ Signing & Capabilities ▸ pick your Team.
  (Simulator needs no signing.)
- For notifications later: Signing & Capabilities ▸ **+ Background Modes** ▸
  *Background fetch* / *Background processing*, then call
  `appState.notifications.evaluateAndSchedule(sdk:)` from a `BGAppRefreshTask`.

---

Once it runs, the two genuinely-unbuilt natives remain: **HealthKit** step source
(Android's is Health-Connect-only, no shared equivalent) and **BGTaskScheduler**
registration for background reminders. Everything else has an on-device-verified
Android counterpart and a typecheck-verified iOS one.
