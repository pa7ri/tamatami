import SwiftUI
import Shared

/// The "Cycle & Training" tab. A top segmented control switches between the
/// cycle+nutrition guide (phase guidance, energy/mood/tips, phase-based
/// nutrition) and the Training segment (`TrainingContent`). The guide's own
/// phase picker (seeded from today's cycle) drives the guide content. All guide
/// reads are pure shared logic — no DB writes.
struct GuideScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = GuideModel()
    @State private var selected: CyclePhase = .menstrual
    @State private var segment = 0   // 0 = Guide, 1 = Training

    private let phases: [CyclePhase] = [.menstrual, .follicular, .ovulatory, .luteal]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker("Section", selection: $segment) {
                    Text("Cycle").tag(0)
                    Text("Training").tag(1)
                }
                .pickerStyle(.segmented)
                .padding()

                if segment == 0 {
                    guideList
                } else {
                    TrainingContent()
                }
            }
            .navigationTitle("Cycle & Training")
            .onAppear {
                model.start(sdk: app.sdk) { phase in
                    if phase != .unknown { selected = phase }
                }
            }
            .onDisappear { model.stop() }
        }
    }

    private var guideList: some View {
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

            let n = NutritionGuideKt.nutritionFor(phase: selected)
            Section("Macro emphasis") { Text(n.macroEmphasis) }
            if !n.keyMicronutrients.isEmpty {
                Section("Key micronutrients") {
                    ForEach(n.keyMicronutrients, id: \.self) { Text("• \($0)") }
                }
            }
            Section("Foods to favor") {
                ForEach(n.suggestedFoods, id: \.name) { food in
                    VStack(alignment: .leading, spacing: 2) {
                        Text(food.name).font(.body)
                        Text(food.why).font(.caption).foregroundStyle(.secondary)
                    }
                }
            }
            if !n.foodsToLimit.isEmpty {
                Section("Foods to limit") {
                    ForEach(n.foodsToLimit, id: \.self) { Text("• \($0)") }
                }
            }
        }
        .igListBackground()
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
final class GuideModel: ObservableObject {
    private var watcher: FlowWatcher<CycleSnapshot>?

    func start(sdk: TamatamiSdk, onPhase: @escaping (CyclePhase) -> Void) {
        watcher = FlowWatcher<CycleSnapshot>({
            FlowObserver(flow: sdk.cycle.observeTodayCycle(today: sdk.today()))
        }) { snapshot in onPhase(snapshot.phase) }
    }
    func stop() { watcher?.cancel(); watcher = nil }
}
