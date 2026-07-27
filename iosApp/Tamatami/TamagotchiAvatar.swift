import SwiftUI
import Shared

/// The domain `TamagotchiState` (mood/accessories/bounceHz) is bridged as
/// `TamagotchiState_` — Kotlin/Native appended `_` because the SQLDelight row
/// type `db.TamagotchiState` claimed the unsuffixed Swift name. Alias it for
/// readability.
typealias TamaState = TamagotchiState_

/// The Tamagotchi mascot, driven by the shared `TamagotchiMoodEngine` (via
/// `tamagotchi.observe`). This is an emoji-based stand-in for the Android
/// vector avatar — mood picks the face, accessories add badges, and `bounceHz`
/// drives a continuous bounce. Swap the emoji for real art/Canvas later.
struct TamagotchiAvatar: View {
    let state: TamaState
    @State private var bouncing = false

    var body: some View {
        VStack(spacing: 8) {
            Text(face(state.mood))
                .font(.system(size: 96))
                .offset(y: bouncing ? -10 : 0)
                .animation(
                    .easeInOut(duration: max(0.2, 1.0 / Double(state.bounceHz)))
                        .repeatForever(autoreverses: true),
                    value: bouncing
                )
                .onAppear { bouncing = true }

            if !accessoryBadges.isEmpty {
                HStack(spacing: 6) {
                    ForEach(accessoryBadges, id: \.self) { Text($0) }
                }
                .font(.title3)
            }

            Text(moodLabel(state.mood))
                .font(.caption).foregroundStyle(.secondary)
        }
    }

    private var accessoryBadges: [String] {
        state.accessories.map { badge($0) }
    }

    private func face(_ mood: TamagotchiMood) -> String {
        switch mood {
        case .sad: return "😔"
        case .neutral: return "🙂"
        case .content: return "😌"
        case .happy: return "😊"
        case .glowing: return "🤩"
        default: return "🙂"
        }
    }

    private func moodLabel(_ mood: TamagotchiMood) -> String {
        mood.name.capitalized
    }

    private func badge(_ a: Accessory) -> String {
        switch a {
        case .thirstyDroplet: return "💧"
        case .tiredZzz: return "💤"
        case .heart: return "❤️"
        case .sparkle: return "✨"
        case .bandaid: return "🩹"
        default: return "•"
        }
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
