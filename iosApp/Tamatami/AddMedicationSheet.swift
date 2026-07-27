import SwiftUI
import Shared

/// Add-medication form: name + which TimeOfDay slots. Calls back with the chosen
/// values; the caller does the suspend `addMedication`.
struct AddMedicationSheet: View {
    @Environment(\.dismiss) private var dismiss
    let onSave: (String, Set<TimeOfDay>) -> Void

    @State private var name = ""
    @State private var slots: Set<TimeOfDay> = []

    private var canSave: Bool { !name.trimmingCharacters(in: .whitespaces).isEmpty && !slots.isEmpty }

    var body: some View {
        NavigationStack {
            Form {
                Section("Medication") {
                    TextField("Name (e.g. birth control, vitamin D)", text: $name)
                }
                Section("When do you take it?") {
                    ForEach(TimeOfDay.entries, id: \.self) { slot in
                        Toggle(slot.displayName, isOn: Binding(
                            get: { slots.contains(slot) },
                            set: { on in if on { slots.insert(slot) } else { slots.remove(slot) } }
                        ))
                    }
                }
            }
            .navigationTitle("Add medication")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSave(name.trimmingCharacters(in: .whitespaces), slots)
                        dismiss()
                    }.disabled(!canSave)
                }
            }
        }
    }
}
