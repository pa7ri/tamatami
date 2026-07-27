import SwiftUI
import Shared

/// Medication tracker — the reference screen showing the shared data layer
/// end-to-end: it observes `MedicationRepository.observeToday` (a Kotlin Flow)
/// via `FlowWatcher`, and mutates through the same repository's suspend
/// functions (which bridge to Swift `async`).
struct MedicationScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = MedicationModel()
    @State private var showAddSheet = false

    var body: some View {
        NavigationStack {
            List {
                if model.meds.isEmpty {
                    Text("Nothing tracked yet. Add a medication with the + button.")
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(model.meds, id: \.medication.id) { today in
                        MedicationRow(today: today) { slot, taken in
                            Task { await model.toggle(today.medication.id, slot, taken) }
                        }
                    }
                }
            }
            .navigationTitle("Medication")
            .toolbar {
                Button {
                    showAddSheet = true
                } label: { Image(systemName: "plus") }
            }
            .sheet(isPresented: $showAddSheet) {
                AddMedicationSheet { name, slots in
                    Task { await model.add(name: name, slots: slots) }
                }
            }
            .onAppear { model.start(sdk: app.sdk) }
            .onDisappear { model.stop() }
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
                // TimeOfDay.entries is exposed as a Kotlin array of enum cases.
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

    /// Ordered scheduled slots for stable display.
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
        // observeToday returns Flow<List<MedicationToday>>; List bridges to NSArray.
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

    /// Persist a new medication from the add sheet.
    func add(name: String, slots: Set<TimeOfDay>) async {
        guard let sdk else { return }
        try? await sdk.medication.addMedication(
            name: name,
            dosesPerDay: Int32(slots.count),
            slots: slots,
            now: sdk.now()
        )
    }
}
