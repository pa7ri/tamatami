import SwiftUI
import Shared

/// Health: the former Medication and Hormones screens merged into one tab.
/// Medications (today's schedule + adherence, add/toggle) sit above the hormone
/// log (recent readings + add). Reuses the existing `AddMedicationSheet` and
/// `AddHormoneSheet`.
struct HealthScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var meds = MedicationModel()
    @StateObject private var hormones = HormonesModel()
    @State private var marker: HormoneMarker = .estrogen
    @State private var showAddMed = false
    @State private var showAddHormone = false

    var body: some View {
        NavigationStack {
            List {
                Section("Medications") {
                    if meds.meds.isEmpty {
                        Text("Nothing tracked yet. Add one with the button below.")
                            .foregroundStyle(.secondary)
                    } else {
                        ForEach(meds.meds, id: \.medication.id) { today in
                            MedicationRow(today: today) { slot, taken in
                                Task { await meds.toggle(today.medication.id, slot, taken) }
                            }
                        }
                    }
                    Button("Add medication") { showAddMed = true }
                }

                Section {
                    Picker("Marker", selection: $marker) {
                        ForEach(HormoneMarker.entries, id: \.self) { Text($0.displayName).tag($0) }
                    }
                    if hormones.entries.isEmpty {
                        Text("Nothing logged yet.").foregroundStyle(.secondary)
                    } else {
                        ForEach(hormones.entries, id: \.id) { e in
                            HStack {
                                Text(e.hormone)
                                Spacer()
                                Text("\(String(format: "%.1f", e.value_)) \(e.unit)")
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                    Button("Add reading") { showAddHormone = true }
                } header: {
                    Text("Hormones")
                }
            }
            .navigationTitle("Health")
            .igListBackground()
            .sheet(isPresented: $showAddMed) {
                AddMedicationSheet { name, slots in
                    Task { await meds.add(name: name, slots: slots) }
                }
            }
            .sheet(isPresented: $showAddHormone) {
                AddHormoneSheet(marker: marker) { value, unit in
                    Task { await hormones.add(marker: marker, value: value, unit: unit) }
                }
            }
            .onAppear { meds.start(sdk: app.sdk); hormones.start(sdk: app.sdk) }
            .onDisappear { meds.stop(); hormones.stop() }
        }
    }
}

private struct MedicationRow: View {
    let today: MedicationToday
    let onToggle: (TimeOfDay, Bool) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(today.medication.name).font(.headline)
            let a = today.adherence
            Text(a.complete ? "All done today ✓" : "\(a.taken)/\(a.expected) taken today")
                .font(.caption).foregroundStyle(.secondary)

            HStack {
                ForEach(scheduledSlots(), id: \.self) { slot in
                    let taken = today.takenSlots.contains(slot)
                    Button(slot.displayName) { onToggle(slot, !taken) }
                        .buttonStyle(.bordered)
                        .tint(taken ? .accentColor : .gray)
                }
            }
        }
        .padding(.vertical, 4)
    }

    private func scheduledSlots() -> [TimeOfDay] {
        TimeOfDay.entries.filter { today.scheduledSlots.contains($0) }
    }
}

@MainActor
final class MedicationModel: ObservableObject {
    @Published var meds: [MedicationToday] = []

    private var sdk: TamatamiSdk?
    private var watcher: FlowWatcher<NSArray>?

    func start(sdk: TamatamiSdk) {
        self.sdk = sdk
        let today = sdk.today()
        watcher = FlowWatcher<NSArray>({
            FlowObserver(flow: sdk.medication.observeToday(date: today))
        }) { [weak self] array in
            self?.meds = (array as? [MedicationToday]) ?? []
        }
    }

    func stop() { watcher?.cancel(); watcher = nil }

    func toggle(_ medId: Int64, _ slot: TimeOfDay, _ taken: Bool) async {
        guard let sdk else { return }
        try? await sdk.medication.setTaken(
            medicationId: medId, date: sdk.today(), slot: slot, taken: taken
        )
    }

    func add(name: String, slots: Set<TimeOfDay>) async {
        guard let sdk else { return }
        try? await sdk.medication.addMedication(
            name: name,
            dosesPerDay: Int32(slots.count),
            slots: slots,
            frequency: MedicationFrequency.companion.Daily,
            now: sdk.now()
        )
    }
}

@MainActor
final class HormonesModel: ObservableObject {
    @Published var entries: [HormoneLog] = []

    private var sdk: TamatamiSdk?
    private var watcher: FlowWatcher<NSArray>?

    func start(sdk: TamatamiSdk) {
        self.sdk = sdk
        let since = sdk.localDate(
            year: Int32(Calendar.current.component(.year, from: Date())),
            month: 1, day: 1
        )
        watcher = FlowWatcher<NSArray>({
            FlowObserver(flow: sdk.hormones.observeRecent(sinceDate: since))
        }) { [weak self] arr in
            self?.entries = (arr as? [HormoneLog]) ?? []
        }
    }

    func add(marker: HormoneMarker, value: Float, unit: String) async {
        guard let sdk else { return }
        try? await sdk.hormones.upsert(
            id: 0, date: sdk.today(), marker: marker,
            value: value, unit: unit, notes: nil
        )
    }

    func stop() { watcher?.cancel(); watcher = nil }
}
