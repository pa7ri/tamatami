# Shared framework → Swift bridging notes

How the Kotlin `Shared` framework surfaces in Swift, and the non-obvious cases
the SwiftUI layer depends on. Regenerate the header after any `commonMain` /
`iosMain` change and re-check this file:

```
./gradlew :shared:embedAndSignAppleFrameworkForXcode        # what Xcode runs
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64       # quick local link
# header:
shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework/Headers/Shared.h
```

## Naming: unprefixed Swift names, prefixed ObjC names

Every class gets an ObjC name `Shared<Name>` **and** an
`__attribute__((swift_name("<Name>")))`, so **Swift sees the unprefixed name**.
Use `TamatamiSdk`, `DailySnapshot`, `PeriodFlow`, `FlowObserver`, … directly —
never the `Shared` prefix. (If a future name genuinely collides with a Swift/
stdlib symbol, prefix that one reference with `Shared`, e.g. `SharedDailySnapshot`.)

## Deliberate names / aliases

| Kotlin | Swift-visible | Notes |
|---|---|---|
| `com.mobile.tamatami.ios.FlowCancellable` | `FlowCancellable` | Renamed from `Cancellable` so it never clashes with `Combine.Cancellable` (SwiftUI re-exports Combine). `FlowObserver.watch` returns it. |
| `domain.model.TamagotchiState` | `TamagotchiState_` | Trailing `_` because the SQLDelight row type `db.TamagotchiState` claimed the unsuffixed name. `TamagotchiAvatar.swift` aliases it: `typealias TamaState = TamagotchiState_`. |
| SQLDelight `db.PeriodDay` vs domain `PeriodDay` | `PeriodDay` / `PeriodDay_` | Same collision pattern; not currently referenced from Swift. |

## Numeric / boxed wrappers (Kotlin*, not Shared*)

Kotlin/Native runtime boxes primitives with `Kotlin`-prefixed names:
`KotlinBoolean`, `KotlinInt`, `KotlinLong`, `KotlinFloat`, `KotlinArray<T>`,
`KotlinEnum`. Unbox with `.boolValue` / `.intValue` / `.floatValue` etc.

- `Flow<Boolean>` → callback value is `KotlinBoolean`; `done.boolValue`.
- Nullable value-type properties box, e.g. `CycleSnapshot.daysUntilNextPeriod`
  is `KotlinInt?` → `days.intValue`.
- `DailySnapshot.energy` is `KotlinInt?`.

## Collections over the Flow bridge

`Flow<List<T>>` arrives in the `FlowWatcher` callback as an `NSArray`
(bridged to `[Any]?`). Instantiate `FlowWatcher<NSArray>` and cast inside
`onEach`: `(arr as? [CalendarDay]) ?? []`. Used by Calendar, Medication,
Hormones, Training.

`Set<TimeOfDay>` bridges to `NSSet<SharedTimeOfDay *>`; `Adherence` /
`MedicationToday` expose it as a Swift `Set<TimeOfDay>` usably via `.contains`.

## Enums

Kotlin enums bridge as classes with `.<case>` class properties (lowercased
first segment: `WALK` → `.walk`, `THYROID_TSH` → `.thyroidTsh`) plus `entries`
and a bridged `name: String`. They are **not** exhaustively switchable from
Swift — every `switch` over one needs a `default:`.

- `PeriodFlow` has **no** label property → labeled inline in `HomeScreen.swift`
  (`DayDetail.flowLabel`).
- `HormoneMarker` and `TimeOfDay` **do** expose a real Kotlin `displayName`
  property — use it directly (`marker.displayName`, `slot.displayName`).
- `WorkoutType` / `WorkoutIntensity` / `TamagotchiMood` use `.name.capitalized`
  (valid: `name` is bridged; uppercase Kotlin names capitalize cleanly).

## `HormoneMarker.expectedRange`

Typed `ClosedFloatingPointRange<Float>`, bridges to `KotlinClosedRange` whose
`start` / `endInclusive` are `Any` (boxed `KotlinFloat`). `AddHormoneSheet.swift`
adds `rangeLow` / `rangeHigh` extensions:
`(expectedRange.start as? KotlinFloat)?.floatValue ?? 0`.

## Objects (singletons) & companions

Kotlin `object` → Swift class with a `.shared` singleton:
`ReminderEvaluator.shared.evaluate(...)`, `TrainingRecommender.shared.recommend(...)`.
Top-level functions in a file `Foo.kt` → `FooKt` class with static methods:
`NutritionGuideKt.nutritionFor(phase:)`, `PhaseGuideKt.guideFor(phase:)`.
Companion objects → `.companion`.

## suspend & Flow

`suspend fun` → Swift `async` throwing (completion-handler under the hood):
`try? await sdk.user.getProfile()`. `Flow` is not directly consumable — always
go through `FlowObserver(flow:).watch { }` wrapped by `FlowWatcher`.

## Generated column quirks

SQLDelight row property `value` bridges as `value_` (trailing underscore) —
`HormoneLog.value_`.

## Tamagotchi avatar → Lottie (`TamaExpression`)

`TamagotchiState` carries a derived `expression: TamaExpression` (7 cases). It
bridges **unsuffixed** as Swift enum `TamaExpression` with camelCased cases
(`.greeting`, `.happy`, `.sadTired`, `.moody`, `.romantic`, `.sleepy`, `.thirsty`).
`TamagotchiAvatar.swift` maps each to a bundled Lottie file (`Animations/<name>.json`)
via `assetName(_:)` and renders `LottieView(animation: .named(name)).resizable().looping()`
(note: `.playing()` plays **once** — use `.looping()`). Keep the filename map in
sync with Android's `assetFor()` and the files under `iosApp/Tamatami/Animations/`.
The current files are placeholders — replace them in place with the real exports.
