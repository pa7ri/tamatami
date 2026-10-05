import SwiftUI
import Lottie
import Shared

/// The domain `TamagotchiState` (mood/accessories/bounceHz/expression) is bridged
/// as `TamagotchiState_` — Kotlin/Native appended `_` because the SQLDelight row
/// type `db.TamagotchiState` claimed the unsuffixed Swift name. Alias it for
/// readability.
typealias TamaState = TamagotchiState_

/// The Tamagotchi mascot, driven by the shared `TamagotchiMoodEngine` (via
/// `tamagotchi.observe`). `state.expression` selects one of five Lottie
/// animations bundled under `Animations/`. Rather than looping forever, the clip
/// plays through **once**, rests for ~1 minute, then replays — a calm heartbeat
/// instead of a restless loop. When the expression changes, SwiftUI swaps the
/// `LottieView` (keyed by expression) and cross-fades gently.
struct TamagotchiAvatar: View {
    let state: TamaState

    /// How long to idle between one-shot plays. Mirrors Android's
    /// `IDLE_BETWEEN_PLAYS_MS` (60 s) in `TamagotchiAvatar.kt`.
    private static let idleBetweenPlays: Duration = .seconds(60)
    /// Gentle cross-fade; mirrors Android's `EXPRESSION_CROSSFADE_MS` (1.2 s).
    private static let crossfade: Double = 1.2

    // Bumping this restarts playback. It also keys the LottieView via `.id`, so
    // each tick recreates the view and plays the clip from the start once.
    @State private var playTick = 0

    var body: some View {
        VStack(spacing: 8) {
            LottieView(animation: .named(assetName(state.expression)))
                .playing(.fromProgress(0, toProgress: 1, loopMode: .playOnce))
                .animationDidFinish { _ in
                    // One play finished → rest, then trigger the next.
                    Task {
                        try? await Task.sleep(for: Self.idleBetweenPlays)
                        playTick += 1
                    }
                }
                .resizable()
                .id("\(state.expression)-\(playTick)")   // recreate on change / replay
                .frame(width: 180, height: 180)
                .transition(.opacity)
                .animation(.easeInOut(duration: Self.crossfade), value: state.expression)

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
