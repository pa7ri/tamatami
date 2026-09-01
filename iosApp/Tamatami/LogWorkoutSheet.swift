import SwiftUI
import Shared

/// Log-activity form: pick from the full `ActivityCatalog` (a grid of SF Symbol
/// tiles), set duration and intensity. Calls back with the chosen `Activity`
/// (which carries both the shared `WorkoutType` and the HealthKit type).
struct LogWorkoutSheet: View {
    @Environment(\.dismiss) private var dismiss
    let onSave: (Activity, Int, WorkoutIntensity) -> Void

    @State private var selected: Activity = ActivityCatalog.forWorkoutType(.walk)
    @State private var minutes = 30
    @State private var intensity: WorkoutIntensity = .moderate

    private let intensities: [WorkoutIntensity] = [.low, .moderate, .high]
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 10), count: 4)

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("Activity").font(.headline).foregroundStyle(IG.text)
                    LazyVGrid(columns: columns, spacing: 10) {
                        ForEach(ActivityCatalog.all) { activity in
                            ActivityTile(activity: activity, selected: activity == selected)
                                .onTapGesture { selected = activity }
                        }
                    }

                    Stepper("Duration: \(minutes) min", value: $minutes, in: 5...240, step: 5)
                        .foregroundStyle(IG.text)

                    Text("Intensity").font(.headline).foregroundStyle(IG.text)
                    Picker("Intensity", selection: $intensity) {
                        ForEach(intensities, id: \.self) { Text(label($0)).tag($0) }
                    }
                    .pickerStyle(.segmented)
                }
                .padding()
            }
            .background(IG.bg.ignoresSafeArea())
            .navigationTitle("Log activity")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { onSave(selected, minutes, intensity); dismiss() }
                }
            }
        }
    }

    private func label(_ i: WorkoutIntensity) -> String {
        switch i {
        case .low: return "Low"
        case .moderate: return "Moderate"
        case .high: return "High"
        default: return "—"
        }
    }
}

private struct ActivityTile: View {
    let activity: Activity
    let selected: Bool

    var body: some View {
        VStack(spacing: 6) {
            Image(systemName: activity.symbol)
                .font(.system(size: 22))
                .foregroundStyle(selected ? AnyShapeStyle(IG.gradient) : AnyShapeStyle(IG.text))
            Text(activity.label)
                .font(.caption2)
                .foregroundStyle(selected ? IG.text : IG.subtext)
                .lineLimit(1)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .fill(selected ? IG.card2 : IG.card)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(selected ? AnyShapeStyle(IG.gradient) : AnyShapeStyle(IG.hair),
                        lineWidth: selected ? 1.5 : 0.5)
        )
    }
}
