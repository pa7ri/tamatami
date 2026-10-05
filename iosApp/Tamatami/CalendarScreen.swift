import SwiftUI
import Shared

/// The Calendar tab: the month grid plus a day-detail panel for the tapped date
/// (flow, mood, water, activities). Extracted from Home into its own tab. Owns
/// its own `HomeModel` (the shared model already exposes the grid + per-day
/// reads/writes); the legend lives behind the toolbar info button, and the
/// Settings gear matches the other tabs.
struct CalendarScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = HomeModel()
    @State private var showLegend = false
    @State private var showLogSheet = false

    private let weekdays = ["S", "M", "T", "W", "T", "F", "S"]
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 4), count: 7)

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    calendarCard
                    if model.selectedDay != nil {
                        DayDetail(model: model, onLogActivity: { showLogSheet = true })
                    }
                }
                .padding()
            }
            .background(IG.bg.ignoresSafeArea(edges: [.bottom, .horizontal]))
            .navigationTitle("Calendar")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button { showLegend = true } label: { Image(systemName: "info.circle") }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink { SettingsScreen() } label: { Image(systemName: "gearshape") }
                }
            }
            .sheet(isPresented: $showLegend) { LegendSheet() }
            .sheet(isPresented: $showLogSheet) {
                LogWorkoutSheet { activity, minutes, intensity in
                    Task { await model.logActivity(activity, minutes: minutes, intensity: intensity) }
                }
            }
            .onAppear { model.start(sdk: app.sdk) }
            .onDisappear { model.stop() }
        }
    }

    private var calendarCard: some View {
        VStack(spacing: 12) {
            HStack {
                Button { model.goPrevMonth() } label: { Image(systemName: "chevron.left") }
                Spacer()
                Text(model.monthTitle).font(.headline).foregroundStyle(IG.text)
                Spacer()
                Button { model.goNextMonth() } label: { Image(systemName: "chevron.right") }
            }
            .tint(.white)

            HStack {
                ForEach(Array(weekdays.enumerated()), id: \.offset) { _, d in
                    Text(d).font(.caption2).foregroundStyle(IG.subtext)
                        .frame(maxWidth: .infinity)
                }
            }

            LazyVGrid(columns: columns, spacing: 4) {
                ForEach(Array(model.days.enumerated()), id: \.offset) { _, day in
                    DayCell(day: day, selected: model.isSelected(day))
                        .onTapGesture { model.select(day) }
                }
            }
        }
        .igCard()
    }
}
