import SwiftUI
import Lottie
import Shared

/// The domain `TamagotchiState` (mood/accessories/bounceHz/expression) is bridged
/// as `TamagotchiState_` — Kotlin/Native appended `_` because the SQLDelight row
/// type `db.TamagotchiState` claimed the unsuffixed Swift name. Alias it for
/// readability.
typealias TamaState = TamagotchiState_

/// The Tamagotchi mascot, driven by the shared `TamagotchiMoodEngine` (via
/// `tamagotchi.observe`). `state.expression` selects one of five looping Lottie
/// animations bundled under `Animations/`. When the expression changes, SwiftUI
/// swaps the `LottieView` (keyed by expression) and cross-fades.
struct TamagotchiAvatar: View {
    let state: TamaState

    var body: some View {
        VStack(spacing: 8) {
            LottieView(animation: .named(assetName(state.expression)))
                .resizable()
                .looping()
                .id(state.expression)                       // re-create on change
                .frame(width: 180, height: 180)
                .transition(.opacity)
                .animation(.easeInOut(duration: 0.42), value: state.expression)

            Text(moodLabel(state.mood))
                .font(.caption).foregroundStyle(.secondary)
        }
    }

    /// Maps the bridged `TamaExpression` to a bundled animation name (no
    /// extension — Lottie resolves `<name>.json`/`.lottie` in the bundle). Keep
    /// in sync with `Animations/` and the Android `assetFor()` map.
    private func assetName(_ expression: TamaExpression) -> String {
        switch expression {
        case .greeting: return "greeting"
        case .happy: return "happy"
        case .sadTired: return "sad_tired"
        case .moody: return "moody"
        case .romantic: return "romantic"
        default: return "greeting"
        }
    }

    private func moodLabel(_ mood: TamagotchiMood) -> String {
        mood.name.capitalized
    }
}

@MainActor
final class TamagotchiModel: ObservableObject {
    // nil until the first shared emission — avoids guessing the bridged name of
    // the Kotlin companion's default (TamagotchiState.Companion.Idle).
    @Published var state: TamaState?

    private var watcher: FlowWatcher<TamaState>?

    func start(sdk: TamatamiSdk) {
        watcher = FlowWatcher<TamaState>({
            FlowObserver(flow: sdk.tamagotchi.observe(today: sdk.today()))
        }) { [weak self] s in self?.state = s }
    }
    func stop() { watcher?.cancel(); watcher = nil }
}
