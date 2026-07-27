import SwiftUI
import Shared

/// Month-grid calendar mirroring the Android screen: a 6×7 grid of phase-colored
/// day cells (built by the shared `MonthBuilder`), month navigation, and a
/// selected-day detail panel that logs flow/water for the tapped date.
struct CalendarScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = CalendarModel()

    private let weekdays = ["S", "M", "T", "W", "T", "F", "S"]
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 4), count: 7)

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 12) {
                    monthHeader
                    weekdayHeader
                    grid
                    if let day = model.selectedDay {
                        DayDetail(day: day, model: model)
                    }
                }
                .padding()
            }
            .navigationTitle("Calendar")
            .onAppear { model.start(sdk: app.sdk) }
            .onDisappear { model.stop() }
        }
    }

    private var monthHeader: some View {
        HStack {
            Button { model.goPrevMonth() } label: { Image(systemName: "chevron.left") }
            Spacer()
            Text(model.monthTitle).font(.headline)
            Spacer()
            Button { model.goNextMonth() } label: { Image(systemName: "chevron.right") }
        }
    }

    private var weekdayHeader: some View {
        HStack {
            ForEach(Array(weekdays.enumerated()), id: \.offset) { _, d in
                Text(d).font(.caption2).foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity)
            }
        }
    }

    private var grid: some View {
        LazyVGrid(columns: columns, spacing: 4) {
            ForEach(Array(model.days.enumerated()), id: \.offset) { _, day in
                DayCell(day: day, selected: model.isSelected(day))
                    .onTapGesture { model.select(day) }
            }
        }
    }
}

private struct DayCell: View {
    let day: CalendarDay
    let selected: Bool

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 8)
                .fill(phaseColor(day.phase).opacity(day.inMonth ? 0.35 : 0.12))
            if day.isToday {
                RoundedRectangle(cornerRadius: 8).stroke(.primary, lineWidth: 1.5)
            }
            if selected {
                RoundedRectangle(cornerRadius: 8).stroke(.blue, lineWidth: 2)
            }
            VStack(spacing: 2) {
                Text("\(day.date.dayOfMonth)")
                    .font(.caption)
                    .foregroundStyle(day.inMonth ? .primary : .secondary)
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

private struct DayDetail: View {
    let day: CalendarDay
    @ObservedObject var model: CalendarModel

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(model.selectedTitle).font(.headline)
            if let s = model.selectedSnapshot {
                Text("Water: \(s.waterGlasses)/\(s.waterGoal)")
                Text("Flow: \(flowLabel(s.periodFlow))").foregroundStyle(.secondary)
            }
            HStack {
                Button("+ Water") { Task { await model.addWater() } }
                Button("Flow") { Task { await model.logFlow(.medium) } }
                Button("Clear", role: .destructive) { Task { await model.logFlow(.none) } }
            }
            .buttonStyle(.bordered)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(RoundedRectangle(cornerRadius: 12).fill(.gray.opacity(0.1)))
    }

    private func flowLabel(_ f: PeriodFlow?) -> String {
        guard let f, f != .none else { return "none" }
        return f.name.capitalized
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

@MainActor
final class CalendarModel: ObservableObject {
    @Published var days: [CalendarDay] = []
    @Published var selectedDay: CalendarDay?
    @Published var selectedSnapshot: DailySnapshot?

    private var sdk: TamatamiSdk?
    private var gridWatcher: FlowWatcher<NSArray>?
    private var dayWatcher: FlowWatcher<DailySnapshot>?

    // Displayed month as year/month ints (kotlinx has no YearMonth in Swift-land).
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

    func addWater() async {
        guard let sdk, let d = selectedDay?.date else { return }
        try? await sdk.daily.incrementWater(date: d, goal: 8)
    }
    func logFlow(_ flow: PeriodFlow) async {
        guard let sdk, let d = selectedDay?.date else { return }
        try? await sdk.daily.setFlow(date: d, flow: flow)
    }

    func stop() {
        gridWatcher?.cancel(); dayWatcher?.cancel()
        gridWatcher = nil; dayWatcher = nil
    }
}
