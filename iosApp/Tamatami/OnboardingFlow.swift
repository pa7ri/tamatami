import SwiftUI
import Shared

/// Single-screen onboarding (the Android version is a multi-step wizard; this
/// scaffold collects the same answers on one form and calls the shared
/// `completeOnboarding`). On success the root gate flips to the main tabs.
struct OnboardingFlow: View {
    @EnvironmentObject var app: AppState

    @State private var name = ""
    @State private var lastPeriod = Calendar.current.date(byAdding: .day, value: -3, to: Date())!
    @State private var cycleLength = 28
    @State private var periodLength = 5
    @State private var tryingToConceive = false
    @State private var onContraception = false
    @State private var irregular = false
    @State private var saving = false

    var body: some View {
        NavigationStack {
            Form {
                Section("Your Tama") {
                    TextField("Name", text: $name)
                }
                Section("Cycle") {
                    DatePicker("Last period start", selection: $lastPeriod,
                               displayedComponents: .date)
                    Stepper("Cycle length: \(cycleLength) days",
                            value: $cycleLength, in: 20...45)
                    Stepper("Period length: \(periodLength) days",
                            value: $periodLength, in: 1...10)
                }
                Section("About you") {
                    Toggle("Trying to conceive", isOn: $tryingToConceive)
                    Toggle("On hormonal contraception", isOn: $onContraception)
                    Toggle("Irregular cycles", isOn: $irregular)
                }
                Section {
                    Button {
                        Task { await finish() }
                    } label: {
                        if saving { ProgressView() } else { Text("Get started") }
                    }
                    .disabled(saving)
                }
            }
            .navigationTitle("Welcome")
            .igListBackground()
        }
    }

    private func finish() async {
        saving = true
        defer { saving = false }
        let sdk = app.sdk
        let c = Calendar.current.dateComponents([.year, .month, .day], from: lastPeriod)
        let start = sdk.localDate(year: Int32(c.year!), month: Int32(c.month!), day: Int32(c.day!))
        try? await sdk.completeOnboarding(
            tamaName: name,
            lastPeriodStart: start,
            avgCycleLengthDays: Int32(cycleLength),
            avgPeriodLengthDays: Int32(periodLength),
            tryingToConceive: tryingToConceive,
            onContraception: onContraception,
            irregularCycles: irregular
        )
        // The root gate observes onboarding-complete and switches automatically.
    }
}
