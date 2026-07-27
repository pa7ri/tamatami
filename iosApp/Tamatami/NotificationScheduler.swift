import Foundation
import UserNotifications
import Shared

/// Native iOS local-notification scheduling, driven by the SHARED reminder
/// decision logic (`ReminderEvaluator` / `ReminderLogic` in commonMain) — the
/// same brain the Android `ReminderWorker` uses. Only the delivery mechanism is
/// platform-specific: Android uses WorkManager + NotificationManager, iOS uses
/// UNUserNotificationCenter (and, in a full app, BGTaskScheduler to re-evaluate
/// periodically in the background).
@MainActor
final class NotificationScheduler {
    private let center = UNUserNotificationCenter.current()

    func requestAuthorization() async {
        _ = try? await center.requestAuthorization(options: [.alert, .sound, .badge])
    }

    /// Evaluate the shared reminder logic for `now` and schedule whatever should
    /// fire. Call this from a BGAppRefreshTask handler (and/or on foreground).
    func evaluateAndSchedule(sdk: TamatamiSdk) async {
        let today = sdk.today()
        let profile = try? await sdk.user.getProfile()

        // Gather the same inputs the Android worker feeds ReminderEvaluator.
        let cycle = try? await sdk.currentCycle(date: today)
        let activeMeds = (try? await sdk.medication.activeMedications()) ?? []
        // (A full impl also passes water + takenByMed; omitted here for brevity —
        //  the point is that the decision itself lives in shared code.)

        let decision = ReminderEvaluator.shared.evaluate(
            daysUntilNextPeriod: cycle?.daysUntilNextPeriod,
            glasses: 0,
            goal: 8,
            now: nowLocalTime(),
            activeMeds: activeMeds,
            takenByMed: [:],
            remindPeriod: profile?.remindPeriodEnabled ?? true,
            remindWater: profile?.remindWaterEnabled ?? false,
            remindPills: profile?.remindPillsEnabled ?? true
        )

        if decision.period {
            schedule(id: "period", title: "Period coming tomorrow",
                     body: "Your next period is expected tomorrow.")
        }
        for (medId, slot) in decision.pillSlots {
            if let med = activeMeds.first(where: { $0.id == medId.int64Value }) {
                schedule(id: "pill-\(medId)", title: "Medication reminder",
                         body: "Time to take \(med.name) (\(slot.displayName)).")
            }
        }
    }

    private func schedule(id: String, title: String, body: String) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        // Immediate (nil trigger) for the scaffold; a real app computes a
        // DateComponents trigger from the slot's time.
        let request = UNNotificationRequest(identifier: id, content: content, trigger: nil)
        center.add(request)
    }

    private func nowLocalTime() -> Kotlinx_datetimeLocalTime {
        let c = Calendar.current.dateComponents([.hour, .minute, .second], from: Date())
        return Kotlinx_datetimeLocalTime(
            hour: Int32(c.hour!), minute: Int32(c.minute!), second: Int32(c.second!), nanosecond: 0
        )
    }
}
