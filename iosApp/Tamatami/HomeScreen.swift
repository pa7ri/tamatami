import SwiftUI
import Shared

/// Minimal Home screen: observes the shared `CycleRepository.observeTodayCycle`
/// Flow and shows the current phase + days until next period. Demonstrates
/// binding a single-value Kotlin Flow into SwiftUI state.
struct HomeScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = HomeModel()
    @StateObject private var tama = TamagotchiModel()

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                if let tamaState = tama.state {
                    TamagotchiAvatar(state: tamaState)
                }
                if let cycle = model.cycle {
                    Text(phaseLabel(cycle.phase))
                        .font(.largeTitle).bold()
                    if cycle.cycleDay > 0 {
                        Text("Day \(cycle.cycleDay) of \(cycle.cycleLength)")
                            .foregroundStyle(.secondary)
                    }
                    if let days = cycle.daysUntilNextPeriod {
                        Text("Next period in \(days.intValue) days")
                            .font(.headline)
                    }
                } else {
                    ProgressView()
                }
            }
            .padding()
            .navigationTitle("Tamatami")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink { SettingsScreen() } label: {
                        Image(systemName: "gearshape")
                    }
                }
                ToolbarItem(placement: .topBarLeading) {
                    NavigationLink { HormonesScreen() } label: {
                        Image(systemName: "waveform.path.ecg")
                    }
                }
            }
            .onAppear { model.start(sdk: app.sdk); tama.start(sdk: app.sdk) }
            .onDisappear { model.stop(); tama.stop() }
        }
    }

    private func phaseLabel(_ phase: CyclePhase) -> String {
        switch phase {
        case .menstrual: return "Menstrual"
        case .follicular: return "Follicular"
        case .ovulatory: return "Ovulatory"
        case .luteal: return "Luteal"
        default: return "—"
        }
    }
}

@MainActor
final class HomeModel: ObservableObject {
    @Published var cycle: CycleSnapshot?

    private var watcher: FlowWatcher<CycleSnapshot>?

    func start(sdk: TamatamiSdk) {
        let today = sdk.today()
        watcher = FlowWatcher<CycleSnapshot>({
            FlowObserver(flow: sdk.cycle.observeTodayCycle(today: today))
        }) { [weak self] snapshot in
            self?.cycle = snapshot
        }
    }

    func stop() { watcher?.cancel(); watcher = nil }
}
