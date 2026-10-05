
import SwiftUI
import Shared

/// The Kotlin `HormoneMarker` exposes its reference band as
/// `expectedRange: ClosedFloatingPointRange<Float>`, which bridges to Swift as a
/// `KotlinClosedRange` whose `start`/`endInclusive` are `Any` (boxed
/// `KotlinFloat`). These accessors unbox them to plain `Float` for the UI.
private extension HormoneMarker {
    var rangeLow: Float { (expectedRange.start as? KotlinFloat)?.floatValue ?? 0 }
    var rangeHigh: Float { (expectedRange.endInclusive as? KotlinFloat)?.floatValue ?? 0 }
}

/// Add-hormone-reading form: value + unit for the given marker.
struct AddHormoneSheet: View {
    @Environment(\.dismiss) private var dismiss
    let marker: HormoneMarker
    let onSave: (Float, String) -> Void

    @State private var valueText = ""
    @State private var unit: String

    init(marker: HormoneMarker, onSave: @escaping (Float, String) -> Void) {
        self.marker = marker
        self.onSave = onSave
        _unit = State(initialValue: marker.defaultUnit)
    }

    private var parsed: Float? { Float(valueText) }

    var body: some View {
        NavigationStack {
            Form {
                Section(marker.displayName) {
                    TextField("Value", text: $valueText)
                        .keyboardType(.decimalPad)
                    TextField("Unit", text: $unit)
                    let lo = String(format: "%.1f", marker.rangeLow)
                    let hi = String(format: "%.1f", marker.rangeHigh)
                    Text("Reference: \(lo)–\(hi) \(marker.defaultUnit)")
                        .font(.caption).foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Add reading")
            .igListBackground()
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        if let v = parsed { onSave(v, unit); dismiss() }
                    }.disabled(parsed == nil)
                }
            }
        }
    }
}
