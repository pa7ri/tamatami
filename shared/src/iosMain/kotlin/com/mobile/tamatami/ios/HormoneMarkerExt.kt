package com.mobile.tamatami.ios

import com.mobile.tamatami.domain.hormones.HormoneMarker

/**
 * Swift-friendly accessors for a marker's reference range. The Kotlin range's
 * `start`/`endInclusive` bridge to Swift as `Any` (Comparable), which is painful
 * to format there — expose plain `Float`s instead.
 */
val HormoneMarker.rangeLow: Float get() = expectedRange.start
val HormoneMarker.rangeHigh: Float get() = expectedRange.endInclusive
