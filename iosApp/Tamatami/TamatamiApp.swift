import SwiftUI

/// App entry point. Owns the single `TamatamiSdk` instance (which builds the
/// shared SQLDelight database with the iOS native driver) for the app's whole
/// lifetime and injects it into the SwiftUI environment.
@main
struct TamatamiApp: App {
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(appState)
                .task {
                    await appState.requestNotificationPermission()
                }
        }
    }
}

/// Holds the shared SDK + app-wide services. `TamatamiSdk` comes from the
/// Kotlin `Shared` framework (module `Shared`).
import Shared

@MainActor
final class AppState: ObservableObject {
    let sdk = TamatamiSdk()
    let notifications = NotificationScheduler()

    func requestNotificationPermission() async {
        await notifications.requestAuthorization()
    }
}
