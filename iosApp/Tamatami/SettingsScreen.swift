import SwiftUI
import Shared

/// Settings: loads the profile once, edits in-memory, saves via the shared
/// `updateSettings` helper (which read-modify-writes, preserving createdAt etc.).
struct SettingsScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = SettingsModel()

    var body: some View {
        Form {
            if model.loaded {
                Section("Profile") {
                    TextField("Tama's name", text: $model.name)
                }
                Section("Cycle") {
                    Stepper("Cycle length: \(model.cycleLength) days",
                            value: $model.cycleLength, in: 20...45)
                    Stepper("Period length: \(model.periodLength) days",
                            value: $model.periodLength, in: 1...10)
                }
                Section("Daily goals") {
                    Stepper("Steps: \(model.stepsGoal)", value: $model.stepsGoal, in: 1000...30000, step: 500)
                    Stepper("Sleep: \(model.sleepHours) h", value: $model.sleepHours, in: 4...12)
                    Stepper("Water: \(model.waterGlasses) glasses", value: $model.waterGlasses, in: 1...16)
                }
                Section("Reminders") {
                    Toggle("Period reminders", isOn: $model.remindPeriod)
                    Toggle("Water reminders", isOn: $model.remindWater)
                    Toggle("Medication reminders", isOn: $model.remindPills)
                }
                Section {
                    Button("Save changes") { Task { await model.save() } }
                        .disabled(model.saving)
                }
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Settings")
        .onAppear { Task { await model.load(sdk: app.sdk) } }
    }
}

@MainActor
final class SettingsModel: ObservableObject {
    @Published var loaded = false
    @Published var saving = false
    @Published var name = ""
    @Published var cycleLength = 28
    @Published var periodLength = 5
    @Published var stepsGoal = 8000
    @Published var sleepHours = 8
    @Published var waterGlasses = 8
    @Published var remindPeriod = true
    @Published var remindWater = false
    @Published var remindPills = true

    private var sdk: TamatamiSdk?

    func load(sdk: TamatamiSdk) async {
        self.sdk = sdk
        guard let p = try? await sdk.user.getProfile() else { loaded = true; return }
        name = p.tamaName
        cycleLength = Int(p.avgCycleLengthDays)
        periodLength = Int(p.avgPeriodLengthDays)
        stepsGoal = Int(p.dailyStepsGoal)
        sleepHours = max(1, Int(p.sleepGoalMinutes) / 60)
        waterGlasses = Int(p.waterGoalGlasses)
        remindPeriod = p.remindPeriodEnabled
        remindWater = p.remindWaterEnabled
        remindPills = p.remindPillsEnabled
        loaded = true
    }

    func save() async {
        guard let sdk else { return }
        saving = true; defer { saving = false }
        try? await sdk.updateSettings(
            tamaName: name,
            avgCycleLengthDays: Int32(cycleLength),
            avgPeriodLengthDays: Int32(periodLength),
            dailyStepsGoal: Int32(stepsGoal),
            sleepGoalMinutes: Int32(sleepHours * 60),
            waterGoalGlasses: Int32(waterGlasses),
            remindPeriodEnabled: remindPeriod,
            remindWaterEnabled: remindWater,
            remindPillsEnabled: remindPills
        )
    }
}
