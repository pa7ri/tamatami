import SwiftUI
import Shared

/// Log-workout form: type, duration, intensity. Calls back with the values.
struct LogWorkoutSheet: View {
    @Environment(\.dismiss) private var dismiss
    let onSave: (WorkoutType, Int, WorkoutIntensity) -> Void

    @State private var type: WorkoutType = .walk
    @State private var minutes = 30
    @State private var intensity: WorkoutIntensity = .moderate

    private let types: [WorkoutType] = [.rest, .yoga, .walk, .strength, .cardio, .hiit, .other]
    private let intensities: [WorkoutIntensity] = [.low, .moderate, .high]

    var body: some View {
        NavigationStack {
            Form {
                Picker("Type", selection: $type) {
                    ForEach(types, id: \.self) { Text($0.name.capitalized).tag($0) }
                }
                Stepper("Duration: \(minutes) min", value: $minutes, in: 5...240, step: 5)
                Picker("Intensity", selection: $intensity) {
                    ForEach(intensities, id: \.self) { Text($0.name.capitalized).tag($0) }
                }
                .pickerStyle(.segmented)
            }
            .navigationTitle("Log workout")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { onSave(type, minutes, intensity); dismiss() }
                }
            }
        }
    }
}
