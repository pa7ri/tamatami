import Foundation
import HealthKit
import SwiftUI
import Shared

/// Two-way bridge to Apple Health. Writes workouts logged in Tamatami into
/// HealthKit, and reads recent HealthKit workouts back so they can be imported
/// into the shared store. Gated by a user toggle (`syncEnabled`) and, of course,
/// by the system authorization the user grants.
@MainActor
final class HealthKitService: ObservableObject {
    static let shared = HealthKitService()

    /// User's opt-in to mirror workouts to Apple Health. Persisted across launches.
    @AppStorage("healthKitSyncEnabled") var syncEnabled: Bool = false
    @Published private(set) var isAuthorized: Bool = false

    private let store = HKHealthStore()

    var isAvailable: Bool { HKHealthStore.isHealthDataAvailable() }

    private var shareTypes: Set<HKSampleType> {
        [HKObjectType.workoutType(),
         HKQuantityType(.activeEnergyBurned)]
    }
    private var readTypes: Set<HKObjectType> {
        [HKObjectType.workoutType(),
         HKQuantityType(.activeEnergyBurned)]
    }

    /// Ask the user for read+write permission. Safe to call repeatedly; the
    /// system only prompts once. Updates `isAuthorized` on the main actor.
    func requestAuthorization() async {
        guard isAvailable else { isAuthorized = false; return }
        do {
            try await store.requestAuthorization(toShare: shareTypes, read: readTypes)
            // We can't read the share-status of read types; treat a successful
            // request (no throw) as "authorized enough to try".
            isAuthorized = store.authorizationStatus(for: HKObjectType.workoutType()) == .sharingAuthorized
        } catch {
            isAuthorized = false
        }
    }

    /// Write one workout to Apple Health. No-op when sync is off or unavailable.
    func saveWorkout(activity: Activity, start: Date, durationMinutes: Int) async {
        guard syncEnabled, isAvailable else { return }
        let end = start.addingTimeInterval(TimeInterval(durationMinutes * 60))
        let config = HKWorkoutConfiguration()
        config.activityType = activity.healthKitType

        let builder = HKWorkoutBuilder(healthStore: store, configuration: config, device: .local())
        do {
            try await builder.beginCollection(at: start)
            try await builder.endCollection(at: end)
            try await builder.finishWorkout()
        } catch {
            // Best-effort mirror — a HealthKit failure must not block local logging.
        }
    }

    /// A workout read back from Apple Health, reduced to what we import.
    struct ImportedWorkout {
        let activity: Activity
        let start: Date
        let durationMinutes: Int
    }

    /// Read recent workouts from Apple Health (newest first). Empty when
    /// unavailable or unauthorized.
    func readRecentWorkouts(limit: Int = 20) async -> [ImportedWorkout] {
        guard isAvailable else { return [] }
        let sort = NSSortDescriptor(key: HKSampleSortIdentifierEndDate, ascending: false)
        return await withCheckedContinuation { cont in
            let query = HKSampleQuery(
                sampleType: HKObjectType.workoutType(),
                predicate: nil,
                limit: limit,
                sortDescriptors: [sort]
            ) { _, samples, _ in
                let workouts = (samples as? [HKWorkout]) ?? []
                let mapped = workouts.map { w in
                    ImportedWorkout(
                        activity: ActivityCatalog.forHealthKit(w.workoutActivityType),
                        start: w.startDate,
                        durationMinutes: max(1, Int(w.duration / 60))
                    )
                }
                cont.resume(returning: mapped)
            }
            store.execute(query)
        }
    }
}
