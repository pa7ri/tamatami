package com.mobile.tamatami.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Owns the reminder scheduling lifecycle: registers the notification channels
 * and enqueues the hourly [ReminderWorker]. Called once from
 * [com.mobile.tamatami.TamatamiApp.onCreate].
 */
class ReminderScheduler(private val context: Context) {

    /** Register channels and enqueue the periodic worker. Idempotent. */
    fun initialize() {
        NotificationChannels.register(context)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /** Cancel the periodic worker — for a future "mute all reminders" toggle. */
    fun cancelAll() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "tamatami.reminders"
    }
}
