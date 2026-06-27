package com.mobile.tamatami.domain.hormones

/**
 * Hormones we let users log + display metadata. `storageKey` is what we write
 * to [com.mobile.tamatami.data.db.entity.HormoneLogEntity.hormone] so this
 * enum is the canonical source for both reads and writes.
 */
enum class HormoneMarker(
    val displayName: String,
    val storageKey: String,
    val defaultUnit: String,
    val expectedRange: ClosedFloatingPointRange<Float>,
) {
    ESTROGEN("Estradiol", "ESTROGEN", "pg/mL", 10f..400f),
    PROGESTERONE("Progesterone", "PROGESTERONE", "ng/mL", 0.1f..25f),
    LH("LH", "LH", "mIU/mL", 1f..80f),
    FSH("FSH", "FSH", "mIU/mL", 1f..25f),
    THYROID_TSH("TSH", "THYROID_TSH", "mIU/L", 0.4f..4.5f),
    ;

    companion object {
        fun fromStorageKey(key: String): HormoneMarker? =
            entries.firstOrNull { it.storageKey == key }
    }
}
