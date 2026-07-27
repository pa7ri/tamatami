import SwiftUI
import Shared

/// Bottom-tab shell mirroring the Android app's primary destinations. Only two
/// screens are wired in this scaffold (Home + Medication) to demonstrate the
/// shared-repository binding; add the rest the same way.
struct RootView: View {
    var body: some View {
        TabView {
            HomeScreen()
                .tabItem { Label("Home", systemImage: "house") }

            MedicationScreen()
                .tabItem { Label("Meds", systemImage: "pills") }
        }
    }
}
