// FILE: iosApp/iosApp/UI/Theme.swift
import SwiftUI

enum IG {
    static let bg      = Color(red: 0.00, green: 0.00, blue: 0.00)
    static let card    = Color(red: 0.07, green: 0.07, blue: 0.07)
    static let card2   = Color(red: 0.10, green: 0.10, blue: 0.10)
    static let hair    = Color(red: 0.15, green: 0.15, blue: 0.15)
    static let text    = Color.white
    static let subtext = Color(red: 0.66, green: 0.66, blue: 0.66)

    static let gradient = LinearGradient(
        colors: [
            Color(red: 0.996, green: 0.855, blue: 0.459),
            Color(red: 0.980, green: 0.494, blue: 0.118),
            Color(red: 0.839, green: 0.161, blue: 0.463),
            Color(red: 0.588, green: 0.184, blue: 0.749),
            Color(red: 0.310, green: 0.357, blue: 0.835)
        ],
        startPoint: .topLeading, endPoint: .bottomTrailing
    )

    static let gradientSoft = LinearGradient(
        colors: [
            Color(red: 0.996, green: 0.855, blue: 0.459).opacity(0.20),
            Color(red: 0.839, green: 0.161, blue: 0.463).opacity(0.20),
            Color(red: 0.310, green: 0.357, blue: 0.835).opacity(0.20)
        ],
        startPoint: .topLeading, endPoint: .bottomTrailing
    )
}

struct IGCard: ViewModifier {
    var padding: CGFloat = 14
    func body(content: Content) -> some View {
        content
            .padding(padding)
            .background(RoundedRectangle(cornerRadius: 16, style: .continuous).fill(IG.card))
            .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(IG.hair, lineWidth: 0.5))
    }
}
extension View { func igCard(padding: CGFloat = 14) -> some View { modifier(IGCard(padding: padding)) } }

/// Makes a `List`/`Form` sit on the true-black IG background instead of the
/// system's dark-grouped gray, so `List`-based screens match the pure-black
/// Home/Training surfaces. Rows keep their own `.card` fill via `igRow()`.
struct IGListBackground: ViewModifier {
    func body(content: Content) -> some View {
        content
            .scrollContentBackground(.hidden)
            // Fill only the bottom/horizontal edges — leave the top safe area to
            // the nav bar's own opaque-black appearance so the large title stays
            // visible at rest (extending IG.bg through the top hid it until scroll).
            .background(IG.bg.ignoresSafeArea(edges: [.bottom, .horizontal]))
    }
}
extension View { func igListBackground() -> some View { modifier(IGListBackground()) } }

/// Dark row fill for List/Form rows so they read as IG cards on the black
/// background (SwiftUI's default row background is a lighter system gray).
extension View {
    func igRow() -> some View { self.listRowBackground(IG.card) }
}


struct StoryRing<Content: View>: View {
    var lineWidth: CGFloat = 2
    var padding: CGFloat = 3
    @ViewBuilder var content: () -> Content
    var body: some View {
        content()
            .padding(padding)
            .background(Circle().stroke(IG.gradient, lineWidth: lineWidth))
    }
}

enum IGAppearance {
    static func apply() {
        let nav = UINavigationBarAppearance()
        nav.configureWithOpaqueBackground()
        nav.backgroundColor = .black
        nav.titleTextAttributes = [.foregroundColor: UIColor.white]
        nav.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
        nav.shadowColor = .clear
        UINavigationBar.appearance().standardAppearance = nav
        UINavigationBar.appearance().scrollEdgeAppearance = nav
        UINavigationBar.appearance().compactAppearance = nav
        UINavigationBar.appearance().tintColor = .white

        let tab = UITabBarAppearance()
        tab.configureWithOpaqueBackground()
        tab.backgroundColor = .black
        tab.shadowColor = UIColor(white: 0.15, alpha: 1)
        UITabBar.appearance().standardAppearance = tab
        UITabBar.appearance().scrollEdgeAppearance = tab
        UITabBar.appearance().tintColor = .white
        UITabBar.appearance().unselectedItemTintColor = UIColor(white: 0.55, alpha: 1)
    }
}
