import Foundation
import Shared

/// Swift-side convenience over the Kotlin `FlowObserver`. Collects a Kotlin
/// `Flow` and delivers each value to `onEach` on the main actor, returning a
/// handle you keep for the lifetime of the observation and cancel when done.
///
/// The Kotlin side (`com.mobile.tamatami.ios.FlowObserver`) already hops to the
/// main dispatcher, so callbacks are safe to use for SwiftUI state updates.
final class FlowWatcher<T: AnyObject> {
    private var cancellable: Cancellable?

    /// - Parameters:
    ///   - flow: any Kotlin `Flow<T>` (e.g. `sdk.medication.observeToday(date:)`).
    ///   - onEach: called with each emitted value.
    init(_ flow: @escaping () -> FlowObserver<T>, onEach: @escaping (T) -> Void) {
        cancellable = flow().watch { value in
            onEach(value)
        }
    }

    func cancel() {
        cancellable?.cancel()
        cancellable = nil
    }

    deinit { cancel() }
}
