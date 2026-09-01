import SwiftUI
import Shared

/// Guide: the former Cycle-info and Nutrition screens merged. One phase picker
/// (seeded from today's cycle) drives both the phase guide (energy / mood / tips)
/// and the phase-based nutrition guidance. All pure shared reads — no DB writes.
struct GuideScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = GuideModel()
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
            .navigationTitle("Cycle")
            .igListBackground()
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
final class GuideModel: ObservableObject {
    private var watcher: FlowWatcher<CycleSnapshot>?

    func start(sdk: TamatamiSdk, onPhase: @escaping (CyclePhase) -> Void) {
        watcher = FlowWatcher<CycleSnapshot>({
            FlowObserver(flow: sdk.cycle.observeTodayCycle(today: sdk.today()))
        }) { snapshot in onPhase(snapshot.phase) }
    }
    func stop() { watcher?.cancel(); watcher = nil }
}
