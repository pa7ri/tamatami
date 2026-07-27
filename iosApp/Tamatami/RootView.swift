import SwiftUI
import Shared

/// Gates on whether onboarding is complete: shows the onboarding flow until a
/// profile exists, then the main tab shell. Observes the shared
/// `UserRepository.observeProfile` Flow so it flips automatically on finish.
struct RootView: View {
    @EnvironmentObject var app: AppState
    @StateObject private var model = RootModel()

    var body: some View {
        Group {
            switch model.phase {
            case .loading:
                ProgressView()
            case .onboarding:
                OnboardingFlow()
            case .ready:
                MainTabs()
            }
        }
        .onAppear { model.start(sdk: app.sdk) }
        .onDisappear { model.stop() }
    }
}

private struct MainTabs: View {
    var body: some View {
        TabView {
            HomeScreen()
                .tabItem { Label("Home", systemImage: "house") }
            CalendarScreen()
                .tabItem { Label("Calendar", systemImage: "calendar") }
            TrainingScreen()
                .tabItem { Label("Training", systemImage: "figure.run") }
            CycleInfoScreen()
                .tabItem { Label("Cycle", systemImage: "heart") }
            MedicationScreen()
                .tabItem { Label("Meds", systemImage: "pills") }
        }
    }
}

@MainActor
final class RootModel: ObservableObject {
    enum Phase { case loading, onboarding, ready }
    @Published var phase: Phase = .loading

    private var watcher: FlowWatcher<KotlinBoolean>?

    func start(sdk: TamatamiSdk) {
        // observeOnboardingComplete() emits a non-null Bool (null profile → false),
        // avoiding a fragile nullable-generic bridge.
        watcher = FlowWatcher<KotlinBoolean>({
            FlowObserver(flow: sdk.observeOnboardingComplete())
        }) { [weak self] done in
            self?.phase = done.boolValue ? .ready : .onboarding
        }
    }

    func stop() { watcher?.cancel(); watcher = nil }
}
