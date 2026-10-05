import SwiftUI
import Shared

/// Home = the daily surface: the Tamagotchi avatar and quick "today" trackers
/// (water / mood / activity). The month calendar + day-detail now live in their
/// own Calendar tab (see `CalendarScreen`).
struct HomeScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = HomeModel()
    @StateObject private var tama = TamagotchiModel()
    @State private var showLogSheet = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    avatarCard
                    todayCard
                }
                .padding()
            }
            .background(IG.bg.ignoresSafeArea(edges: [.bottom, .horizontal]))
            .navigationTitle("Tamatami")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink { SettingsScreen() } label: { Image(systemName: "gearshape") }
                }
            }
            .sheet(isPresented: $showLogSheet) {
                LogWorkoutSheet { activity, minutes, intensity in
                    Task { await model.logActivity(activity, minutes: minutes, intensity: intensity) }
                }
            }
            .onAppear { model.start(sdk: app.sdk); tama.start(sdk: app.sdk) }
            .onDisappear { model.stop(); tama.stop() }
        }
    }

    // MARK: - Avatar + phase headline

    private var avatarCard: some View {
        VStack(spacing: 10) {
            if let tamaState = tama.state {
                TamagotchiAvatar(state: tamaState)
            } else {
                ProgressView()
            }
            if let cycle = model.cycle {
                Text(phaseLabel(cycle.phase))
                    .font(.system(.title2, design: .rounded)).bold()
                    .foregroundStyle(IG.text)
                if cycle.cycleDay > 0 {
                    Text("Day \(cycle.cycleDay) of \(cycle.cycleLength)")
                        .font(.caption).foregroundStyle(IG.subtext)
                }
                if let days = cycle.daysUntilNextPeriod {
                    Text("Next period in \(days.intValue) days")
                        .font(.footnote).foregroundStyle(IG.subtext)
                }
            }
        }
        .frame(maxWidth: .infinity)
        .igCard()
    }

    // MARK: - Today quick trackers

    private var todayCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Today").font(.system(.headline, design: .rounded)).foregroundStyle(IG.text)

            HStack {
                Label("Water", systemImage: "drop.fill").foregroundStyle(IG.text)
                Spacer()
                Text("\(model.today?.waterGlasses ?? 0)/\(model.today?.waterGoal ?? 8)")
                    .foregroundStyle(IG.subtext)
                Button { Task { await model.addWaterToday() } } label: {
                    Image(systemName: "plus.circle.fill").font(.title3)
                }
                .tint(.white)
            }

            Divider().overlay(IG.hair)

            Text("Mood").font(.caption).foregroundStyle(IG.subtext)
            MoodPicker(selected: model.today?.mood) { mood in
                Task { await model.setMoodToday(mood) }
            }

            Divider().overlay(IG.hair)

            Button { showLogSheet = true } label: {
                Label("Log an activity", systemImage: "figure.run")
            }
            .buttonStyle(.bordered).tint(.white)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .igCard()
    }

    // MARK: - Phase label

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

// MARK: - Day cell

struct DayCell: View {
    let day: CalendarDay
    let selected: Bool

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 8)
                .fill(phaseColor(day.phase).opacity(day.inMonth ? 0.35 : 0.12))
            if day.isToday {
                RoundedRectangle(cornerRadius: 8).stroke(.white, lineWidth: 1.5)
            }
            if selected {
                RoundedRectangle(cornerRadius: 8).stroke(IG.gradient, lineWidth: 2)
            }
            VStack(spacing: 2) {
                Text("\(day.date.dayOfMonth)")
                    .font(.caption)
                    .foregroundStyle(day.inMonth ? IG.text : IG.subtext)
                HStack(spacing: 2) {
                    if day.isLoggedPeriod { Circle().fill(.red).frame(width: 5, height: 5) }
                    if day.isPredictedPeriod { Circle().stroke(.red, lineWidth: 1).frame(width: 5, height: 5) }
                    if day.isPredictedOvulation { Circle().fill(.teal).frame(width: 5, height: 5) }
                }
                .frame(height: 6)
            }
        }
        .frame(height: 44)
    }
}

// MARK: - Day detail (flow / mood / water / activities for the tapped date)

struct DayDetail: View {
    @ObservedObject var model: HomeModel
    let onLogActivity: () -> Void

    private let flows: [PeriodFlow] = [.none, .spotting, .light, .medium, .heavy]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(model.selectedTitle).font(.system(.headline, design: .rounded)).foregroundStyle(IG.text)

            let s = model.selectedSnapshot

            Text("Flow").font(.caption).foregroundStyle(IG.subtext)
            HStack(spacing: 6) {
                ForEach(flows, id: \.self) { flow in
                    let active = (s?.periodFlow ?? .none) == flow
                    Button(flowLabel(flow)) { Task { await model.setFlow(flow) } }
                        .font(.caption)
                        .buttonStyle(.bordered)
                        .tint(active ? .pink : .gray)
                }
            }

            Divider().overlay(IG.hair)

            Text("Mood").font(.caption).foregroundStyle(IG.subtext)
            MoodPicker(selected: s?.mood) { mood in Task { await model.setMood(mood) } }

            Divider().overlay(IG.hair)

            HStack {
                Label("Water", systemImage: "drop.fill").foregroundStyle(IG.text)
                Spacer()
                Text("\(s?.waterGlasses ?? 0)/\(s?.waterGoal ?? 8)").foregroundStyle(IG.subtext)
                Button { Task { await model.addWater() } } label: {
                    Image(systemName: "plus.circle.fill").font(.title3)
                }
                .tint(.white)
            }

            Divider().overlay(IG.hair)

            HStack {
                Text("Activities").font(.caption).foregroundStyle(IG.subtext)
                Spacer()
                Button { onLogActivity() } label: { Image(systemName: "plus") }.tint(.white)
            }
            if let workouts = s?.workouts, !workouts.isEmpty {
                ForEach(workouts, id: \.id) { w in
                    HStack {
                        Image(systemName: ActivityCatalog.forWorkoutType(w.type).symbol)
                            .foregroundStyle(IG.subtext)
                        Text(ActivityCatalog.forWorkoutType(w.type).label).foregroundStyle(IG.text)
                        Spacer()
                        Text("\(w.durationMinutes) min").foregroundStyle(IG.subtext)
                    }
                    .font(.caption)
                }
            } else {
                Text("None logged.").font(.caption).foregroundStyle(IG.subtext)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .igCard()
    }

    private func flowLabel(_ f: PeriodFlow) -> String {
        switch f {
        case .none: return "None"
        case .spotting: return "Spot"
        case .light: return "Light"
        case .medium: return "Med"
        case .heavy: return "Heavy"
        default: return "?"
        }
    }
}

// MARK: - Shared mood picker (5 levels)

struct MoodPicker: View {
    let selected: Mood?
    let onSelect: (Mood) -> Void

    private let moods: [Mood] = [.great, .good, .neutral, .low, .awful]

    var body: some View {
        HStack(spacing: 10) {
            ForEach(moods, id: \.self) { mood in
                let active = selected == mood
                Text(emoji(mood))
                    .font(.system(size: 26))
                    .opacity(active ? 1 : 0.45)
                    .scaleEffect(active ? 1.15 : 1)
                    .onTapGesture { onSelect(mood) }
            }
        }
    }

    private func emoji(_ m: Mood) -> String {
        switch m {
        case .great: return "🤩"
        case .good: return "😊"
        case .neutral: return "🙂"
        case .low: return "😔"
        case .awful: return "😣"
        default: return "🙂"
        }
    }
}

// MARK: - Legend (behind info button)

struct LegendSheet: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section("Phase colors") {
                    legendRow(.red, "Menstrual")
                    legendRow(.green, "Follicular")
                    legendRow(.teal, "Ovulatory")
                    legendRow(.purple, "Luteal")
                }
                Section("Day markers") {
                    HStack { Circle().fill(.red).frame(width: 8, height: 8); Text("Logged period") }
                    HStack { Circle().stroke(.red, lineWidth: 1).frame(width: 8, height: 8); Text("Predicted period") }
                    HStack { Circle().fill(.teal).frame(width: 8, height: 8); Text("Predicted ovulation") }
                }
            }
            .navigationTitle("Legend")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .confirmationAction) { Button("Done") { dismiss() } } }
        }
    }

    private func legendRow(_ color: Color, _ label: String) -> some View {
        HStack {
            RoundedRectangle(cornerRadius: 4).fill(color.opacity(0.35)).frame(width: 18, height: 18)
            Text(label)
        }
    }
}

/// Phase → SwiftUI Color (parity with Android PhaseColors, approximated).
func phaseColor(_ phase: CyclePhase) -> Color {
    switch phase {
    case .menstrual: return .red
    case .follicular: return .green
    case .ovulatory: return .teal
    case .luteal: return .purple
    default: return .gray
    }
}

// MARK: - Model

@MainActor
final class HomeModel: ObservableObject {
    @Published var cycle: CycleSnapshot?
    @Published var today: DailySnapshot?
    @Published var days: [CalendarDay] = []
    @Published var selectedDay: CalendarDay?
    @Published var selectedSnapshot: DailySnapshot?

    private var sdk: TamatamiSdk?
    private var cycleWatcher: FlowWatcher<CycleSnapshot>?
    private var todayWatcher: FlowWatcher<DailySnapshot>?
    private var gridWatcher: FlowWatcher<NSArray>?
    private var dayWatcher: FlowWatcher<DailySnapshot>?

    private var year = 0
    private var month = 0

    var monthTitle: String {
        let f = DateComponents(calendar: .current, year: year, month: month).date ?? Date()
        let df = DateFormatter(); df.dateFormat = "MMMM yyyy"
        return df.string(from: f)
    }

    var selectedTitle: String {
        guard let d = selectedDay?.date else { return "" }
        return "\(d.year)-\(String(format: "%02d", d.monthNumber))-\(String(format: "%02d", d.dayOfMonth))"
    }

    func start(sdk: TamatamiSdk) {
        self.sdk = sdk
        let t = sdk.today()
        year = Int(t.year); month = Int(t.monthNumber)

        cycleWatcher = FlowWatcher<CycleSnapshot>({
            FlowObserver(flow: sdk.cycle.observeTodayCycle(today: t))
        }) { [weak self] snap in self?.cycle = snap }

        todayWatcher = FlowWatcher<DailySnapshot>({
            FlowObserver(flow: sdk.daily.observeToday(date: t))
        }) { [weak self] snap in self?.today = snap }

        observeGrid()
    }

    private func observeGrid() {
        guard let sdk else { return }
        gridWatcher?.cancel()
        gridWatcher = FlowWatcher<NSArray>({
            FlowObserver(flow: sdk.observeMonth(year: Int32(self.year), monthNumber: Int32(self.month)))
        }) { [weak self] arr in
            self?.days = (arr as? [CalendarDay]) ?? []
        }
    }

    func goPrevMonth() {
        if month == 1 { month = 12; year -= 1 } else { month -= 1 }
        observeGrid()
    }
    func goNextMonth() {
        if month == 12 { month = 1; year += 1 } else { month += 1 }
        observeGrid()
    }

    func isSelected(_ day: CalendarDay) -> Bool {
        guard let s = selectedDay?.date else { return false }
        return s.year == day.date.year && s.monthNumber == day.date.monthNumber
            && s.dayOfMonth == day.date.dayOfMonth
    }

    func select(_ day: CalendarDay) {
        selectedDay = day
        guard let sdk else { return }
        dayWatcher?.cancel()
        dayWatcher = FlowWatcher<DailySnapshot>({
            FlowObserver(flow: sdk.daily.observeToday(date: day.date))
        }) { [weak self] s in self?.selectedSnapshot = s }
    }

    // Today mutations
    func addWaterToday() async {
        guard let sdk else { return }
        try? await sdk.daily.incrementWater(date: sdk.today(), goal: 8)
    }
    func setMoodToday(_ mood: Mood) async {
        guard let sdk else { return }
        try? await sdk.daily.setMood(date: sdk.today(), mood: mood, energy: 3, notes: nil)
    }
    func logActivity(_ activity: Activity, minutes: Int, intensity: WorkoutIntensity) async {
        guard let sdk else { return }
        try? await sdk.daily.logWorkout(
            date: sdk.today(), type: activity.workoutType, durationMinutes: Int32(minutes),
            intensity: intensity, notes: nil, id: 0
        )
    }

    // Selected-day mutations
    func addWater() async {
        guard let sdk, let d = selectedDay?.date else { return }
        try? await sdk.daily.incrementWater(date: d, goal: 8)
    }
    func setFlow(_ flow: PeriodFlow) async {
        guard let sdk, let d = selectedDay?.date else { return }
        try? await sdk.daily.setFlow(date: d, flow: flow)
    }
    func setMood(_ mood: Mood) async {
        guard let sdk, let d = selectedDay?.date else { return }
        try? await sdk.daily.setMood(date: d, mood: mood, energy: 3, notes: nil)
    }

    func stop() {
        cycleWatcher?.cancel(); todayWatcher?.cancel(); gridWatcher?.cancel(); dayWatcher?.cancel()
        cycleWatcher = nil; todayWatcher = nil; gridWatcher = nil; dayWatcher = nil
    }
}
