import SwiftUI
import Shared

/// Cycle-phase guide. Pure read: computes the current phase from the shared
/// cycle snapshot, then renders `guideFor(phase)` (a pure shared function, no DB).
struct CycleInfoScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = CycleInfoModel()
    @State private var selected: CyclePhase = .menstrual

    private let phases: [CyclePhase] = [.menstrual, .follicular, .ovulatory, .luteal]

    var body: some View {
        NavigationStack {
            List {
                Picker("Phase", selection: $selected) {
                    ForEach(phases, id: \.self) { Text(label($0)).tag($0) }
                }
                .pickerStyle(.segmented)

                let guide = PhaseGuideKt.guideFor(phase: selected)
                Section(guide.headline) { Text(guide.body) }
                Section("Energy") { Text(guide.energyExpectation) }
                Section("Mood") { Text(guide.moodTendency) }
                if !guide.practicalTips.isEmpty {
                    Section("Tips") {
                        ForEach(guide.practicalTips, id: \.self) { Text("• \($0)") }
                    }
                }
            }
            .navigationTitle("Cycle info")
            .onAppear {
                model.start(sdk: app.sdk) { phase in
                    if phase != .unknown { selected = phase }
                }
            }
            .onDisappear { model.stop() }
        }
    }

    private func label(_ p: CyclePhase) -> String {
        switch p {
        case .menstrual: return "Menstrual"
        case .follicular: return "Follicular"
        case .ovulatory: return "Ovulatory"
        case .luteal: return "Luteal"
        default: return "—"
        }
    }
}

@MainActor
final class CycleInfoModel: ObservableObject {
    private var watcher: FlowWatcher<CycleSnapshot>?

    func start(sdk: TamatamiSdk, onPhase: @escaping (CyclePhase) -> Void) {
        watcher = FlowWatcher<CycleSnapshot>({
            FlowObserver(flow: sdk.cycle.observeTodayCycle(today: sdk.today()))
        }) { snapshot in onPhase(snapshot.phase) }
    }
    func stop() { watcher?.cancel(); watcher = nil }
}
