package com.mobile.tamatami.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.ZoneId

/**
 * Reads daily step totals from Health Connect. Steps are **read-only** and
 * fetched live (never persisted to Room). Degrades gracefully when the Health
 * Connect app is missing or the SDK needs an update — [availability] tells the
 * UI which state to render.
 */
class StepDataSource(private val context: Context) {

    enum class Availability { AVAILABLE, UPDATE_REQUIRED, NOT_INSTALLED }

    /** The permissions we request — read steps only. */
    val permissions: Set<String> = setOf(HealthPermission.getReadPermission(StepsRecord::class))

    /** The ActivityResult contract to launch the Health Connect permission grant UI. */
    val permissionContract = PermissionController.createRequestPermissionResultContract()

    fun availability(): Availability = when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE -> Availability.AVAILABLE
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Availability.UPDATE_REQUIRED
        else -> Availability.NOT_INSTALLED
    }

    private fun clientOrNull(): HealthConnectClient? =
        if (availability() == Availability.AVAILABLE) HealthConnectClient.getOrCreate(context) else null

    suspend fun hasPermission(): Boolean {
        val client = clientOrNull() ?: return false
        return client.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    /**
     * Total steps for [date] in the device's default time zone, or `null` if
     * Health Connect is unavailable, permission is missing, or there's no data.
     */
    suspend fun stepsFor(date: LocalDate): Long? {
        val client = clientOrNull() ?: return null
        if (!hasPermission()) return null

        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()

        val result: AggregationResult = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
        )
        return result[StepsRecord.COUNT_TOTAL]
    }
}
