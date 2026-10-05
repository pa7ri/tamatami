// FILE: iosApp/iosApp/UI/EmptyCard.swift
import SwiftUI

struct EmptyCard: View {
    let title: String
    let subtitle: String
    let symbol: String
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: symbol).font(.system(size: 28)).foregroundStyle(IG.gradient)
            Text(title).font(.system(.headline, design: .rounded)).foregroundStyle(IG.text)
            Text(subtitle).font(.footnote).foregroundStyle(IG.subtext).multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
    }
}
