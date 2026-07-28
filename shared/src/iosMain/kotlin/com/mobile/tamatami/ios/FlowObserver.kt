package com.mobile.tamatami.ios

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Bridges a Kotlin [Flow] into something Swift can consume. Kotlin/Native
 * doesn't expose `Flow` or suspend collection to Swift, so the SwiftUI layer
 * calls [watch] with a callback and gets back a [FlowCancellable] to stop
 * collecting when the view disappears.
 *
 * Usage from Swift:
 * ```swift
 * let handle = FlowObserver(flow: repo.observeSomething()).watch { value in
 *     // update @State
 * }
 * // later: handle.cancel()
 * ```
 */
class FlowObserver<T : Any>(private val flow: Flow<T>) {

    private val scope = CoroutineScope(Dispatchers.Main)

    fun watch(onEach: (T) -> Unit): FlowCancellable {
        val job: Job = scope.launch {
            flow.collect { value -> onEach(value) }
        }
        return FlowCancellable { job.cancel() }
    }
}

/**
 * A stop handle Swift can call to end a [FlowObserver.watch] subscription.
 *
 * Named `FlowCancellable` rather than `Cancellable` so it never collides with
 * `Combine.Cancellable` on the Swift side (SwiftUI transitively re-exports
 * Combine, which would otherwise make an unqualified `Cancellable` ambiguous).
 */
class FlowCancellable(private val onCancel: () -> Unit) {
    fun cancel() = onCancel()
}
