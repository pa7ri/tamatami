import SwiftUI
import Shared

/// Compact calendar/day view: shows today's logged snapshot and lets you log
/// period flow + water. (The Android version renders a full month grid via the
/// shared `MonthBuilder`; that grid is a good next addition — the data is all
/// available through `cycle.observeRecentPeriodDays()` + `MonthBuilder`.)
struct CalendarScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = CalendarModel()

    var body: some View {
        NavigationStack {
            List {
                Section("Today") {
                    if let s = model.snapshot {
                        Text("Water: \(s.waterGlasses) / \(s.waterGoal) glasses")
                        Text("Flow: \(flowLabel(s.periodFlow))")
                        if let mood = s.mood { Text("Mood: \(mood.name.capitalized)") }
                    } else {
                        ProgressView()
                    }
                }
                Section("Log") {
                    Button("Add a glass of water") { Task { await model.addWater() } }
                    Button("Log medium flow") { Task { await model.logFlow(.medium) } }
                    Button("Clear flow", role: .destructive) { Task { await model.logFlow(.none) } }
                }
            }
            .navigationTitle("Calendar")
            .onAppear { model.start(sdk: app.sdk) }
            .onDisappear { model.stop() }
        }
    }

    private func flowLabel(_ f: PeriodFlow?) -> String {
        guard let f, f != .none else { return "none" }
        return f.name.capitalized
    }
}

@MainActor
final class CalendarModel: ObservableObject {
    @Published var snapshot: DailySnapshot?

    private var sdk: TamatamiSdk?
    private var watcher: FlowWatcher<DailySnapshot>?

    func start(sdk: TamatamiSdk) {
        self.sdk = sdk
        watcher = FlowWatcher<DailySnapshot>({
            FlowObserver(flow: sdk.daily.observeToday(date: sdk.today()))
        }) { [weak self] s in self?.snapshot = s }
    }

    func addWater() async {
        guard let sdk else { return }
        try? await sdk.daily.incrementWater(date: sdk.today(), goal: 8)
    }

    func logFlow(_ flow: PeriodFlow) async {
        guard let sdk else { return }
        try? await sdk.daily.setFlow(date: sdk.today(), flow: flow)
    }

    func stop() { watcher?.cancel(); watcher = nil }
}
