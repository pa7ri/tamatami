import SwiftUI
import Shared

/// Hormone log: observes recent readings (last 90 days) and adds a sample
/// reading. `HormoneLog.value_` (trailing underscore) is the generated column
/// name for the `value` field.
struct HormonesScreen: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = HormonesModel()
    @State private var marker: HormoneMarker = .estrogen
    @State private var showAddSheet = false

    var body: some View {
        NavigationStack {
            List {
                Picker("Marker", selection: $marker) {
                    ForEach(HormoneMarker.entries, id: \.self) { Text($0.displayName).tag($0) }
                }
                Section("Recent") {
                    if model.entries.isEmpty {
                        Text("Nothing logged yet.").foregroundStyle(.secondary)
                    } else {
                        ForEach(model.entries, id: \.id) { e in
                            HStack {
                                Text(e.hormone)
                                Spacer()
                                Text("\(String(format: "%.1f", e.value_)) \(e.unit)")
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }
                Section {
                    Button("Add reading") { showAddSheet = true }
                }
            }
            .navigationTitle("Hormones")
            .sheet(isPresented: $showAddSheet) {
                AddHormoneSheet(marker: marker) { value, unit in
                    Task { await model.add(marker: marker, value: value, unit: unit) }
                }
            }
            .onAppear { model.start(sdk: app.sdk) }
            .onDisappear { model.stop() }
        }
    }
}

@MainActor
final class HormonesModel: ObservableObject {
    @Published var entries: [HormoneLog] = []

    private var sdk: TamatamiSdk?
    private var watcher: FlowWatcher<NSArray>?

    func start(sdk: TamatamiSdk) {
        self.sdk = sdk
        // Last 90 days.
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
