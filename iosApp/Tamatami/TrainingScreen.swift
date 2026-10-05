import SwiftUI
import Shared

/// Training content, embedded as a segment of the "Cycle & Training" tab (see
/// `GuideScreen`). Phase+energy-based recommendation (pure shared logic), the
/// recent workouts list (observed Flow), a rich activity-picker log sheet, and
/// two-way Apple Health sync. Renders a bare `List` (no NavigationStack/title) so
/// the host screen owns the navigation chrome.
struct TrainingContent: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = TrainingModel()
    @StateObject private var health = HealthKitService.shared
    @State private var showLogSheet = false

    var body: some View {
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
                            Image(systemName: ActivityCatalog.forWorkoutType(w.type).symbol)
                                .foregroundStyle(.secondary)
                            Text(typeLabel(w.type))
                            Spacer()
                            Text("\(w.durationMinutes) min").foregroundStyle(.secondary)
                        }
                    }
                }
            }
            if health.isAvailable {
                Section("Apple Health") {
                    Toggle("Sync workouts", isOn: $health.syncEnabled)
                    Button("Import recent from Health") {
                        Task { await model.importFromHealth(health) }
                    }
                }
            }
            Section {
                Button("Log activity") { showLogSheet = true }
            }
        }
        .igListBackground()
        .sheet(isPresented: $showLogSheet) {
            LogWorkoutSheet { activity, minutes, intensity in
                Task { await model.log(activity: activity, minutes: minutes, intensity: intensity, health: health) }
            }
        }
        .onAppear {
            model.start(sdk: app.sdk)
            if health.syncEnabled { Task { await health.requestAuthorization() } }
        }
        .onDisappear { model.stop() }
    }

    private func intensityLabel(_ i: WorkoutIntensity) -> String {
        switch i {
        case .low: return "Low"
        case .moderate: return "Moderate"
        case .high: return "High"
        default: return "—"
        }
    }
    private func typeLabel(_ t: WorkoutType) -> String {
        ActivityCatalog.forWorkoutType(t).label
    }
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

    /// Log against today. Persists locally, then mirrors to Apple Health when sync is on.
    func log(activity: Activity, minutes: Int, intensity: WorkoutIntensity, health: HealthKitService) async {
        guard let sdk else { return }
        try? await sdk.daily.logWorkout(
            date: sdk.today(), type: activity.workoutType, durationMinutes: Int32(minutes),
            intensity: intensity, notes: nil, id: 0
        )
        await health.saveWorkout(activity: activity, start: Date(), durationMinutes: minutes)
    }

    /// Pull recent workouts from Apple Health and store any we don't already have.
    func importFromHealth(_ health: HealthKitService) async {
        guard let sdk else { return }
        await health.requestAuthorization()
        let imported = await health.readRecentWorkouts(limit: 20)
        for w in imported {
            let comps = Calendar.current.dateComponents([.year, .month, .day], from: w.start)
            let date = sdk.localDate(
                year: Int32(comps.year ?? 2000),
                month: Int32(comps.month ?? 1),
                day: Int32(comps.day ?? 1)
            )
            try? await sdk.daily.logWorkout(
                date: date, type: w.activity.workoutType, durationMinutes: Int32(w.durationMinutes),
                intensity: .moderate, notes: "Imported from Apple Health", id: 0
            )
        }
    }

    func stop() {
        cycleWatcher?.cancel(); dailyWatcher?.cancel(); workoutWatcher?.cancel()
        cycleWatcher = nil; dailyWatcher = nil; workoutWatcher = nil
    }
}
