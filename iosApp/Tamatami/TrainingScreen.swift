import SwiftUI
import Shared

/// Training: shows the phase+energy-based recommendation (pure shared logic) and
/// the recent workouts list (observed Flow). Logging a workout uses the suspend
/// repository call.
struct TrainingScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = TrainingModel()

    var body: some View {
        NavigationStack {
            List {
                if let rec = model.recommendation {
                    Section("Recommended today") {
                        Text("Intensity: \(intensityLabel(rec.intensity))")
                        Text(rec.suggestedTypes.map { typeLabel($0) }.joined(separator: ", "))
                            .foregroundStyle(.secondary)
                    }
                }
                Section("Recent workouts") {
                    if model.workouts.isEmpty {
                        Text("None logged yet.").foregroundStyle(.secondary)
                    } else {
                        ForEach(model.workouts, id: \.id) { w in
                            HStack {
                                Text(typeLabel(w.type))
                                Spacer()
                                Text("\(w.durationMinutes) min").foregroundStyle(.secondary)
                            }
                        }
                    }
                }
                Section {
                    Button("Log a 30-min walk") {
                        Task { await model.logSample() }
                    }
                }
            }
            .navigationTitle("Training")
            .onAppear { model.start(sdk: app.sdk) }
            .onDisappear { model.stop() }
        }
    }

    private func intensityLabel(_ i: WorkoutIntensity) -> String {
        switch i {
        case .low: return "Low"
        case .moderate: return "Moderate"
        case .high: return "High"
        default: return "—"
        }
    }
    private func typeLabel(_ t: WorkoutType) -> String { t.name.capitalized }
}

@MainActor
final class TrainingModel: ObservableObject {
    @Published var recommendation: WorkoutRecommendation?
    @Published var workouts: [Workout] = []

    private var sdk: TamatamiSdk?
    private var cycleWatcher: FlowWatcher<CycleSnapshot>?
    private var dailyWatcher: FlowWatcher<DailySnapshot>?
    private var workoutWatcher: FlowWatcher<NSArray>?
    private var lastPhase: CyclePhase = .unknown
    private var lastEnergy: KotlinInt?

    func start(sdk: TamatamiSdk) {
        self.sdk = sdk
        let today = sdk.today()
        cycleWatcher = FlowWatcher<CycleSnapshot>({
            FlowObserver(flow: sdk.cycle.observeTodayCycle(today: today))
        }) { [weak self] snap in
            self?.lastPhase = snap.phase
            self?.recompute()
        }
        dailyWatcher = FlowWatcher<DailySnapshot>({
            FlowObserver(flow: sdk.daily.observeToday(date: today))
        }) { [weak self] daily in
            self?.lastEnergy = daily.energy
            self?.recompute()
        }
        workoutWatcher = FlowWatcher<NSArray>({
            FlowObserver(flow: sdk.workouts.observeRecent(limit: 30))
        }) { [weak self] arr in
            self?.workouts = (arr as? [Workout]) ?? []
        }
    }

    private func recompute() {
        recommendation = TrainingRecommender.shared.recommend(phase: lastPhase, energy: lastEnergy)
    }

    func logSample() async {
        guard let sdk else { return }
        try? await sdk.daily.logWorkout(
            date: sdk.today(), type: .walk, durationMinutes: 30,
            intensity: .low, notes: nil, id: 0
        )
    }

    func stop() {
        cycleWatcher?.cancel(); dailyWatcher?.cancel(); workoutWatcher?.cancel()
        cycleWatcher = nil; dailyWatcher = nil; workoutWatcher = nil
    }
}
