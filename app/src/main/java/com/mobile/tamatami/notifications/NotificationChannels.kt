package com.mobile.tamatami.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.mobile.tamatami.R

/**
 * The reminder notification channels. Split into three so the user can mute each
 * reminder type independently from system settings. minSdk is 31, so
 * [NotificationChannel] always exists — no SDK guard needed.
 */
object NotificationChannels {
    const val PERIOD = "reminders.period"
    const val WATER = "reminders.water"
    const val PILLS = "reminders.pills"

    /** Create all channels. Idempotent — safe to call on every app start. */
    fun register(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannels(
            listOf(
                channel(context, PERIOD, R.string.channel_period_name, R.string.channel_period_desc),
                channel(context, WATER, R.string.channel_water_name, R.string.channel_water_desc),
                channel(context, PILLS, R.string.channel_pills_name, R.string.channel_pills_desc),
            )
        )
    }

    private fun channel(context: Context, id: String, nameRes: Int, descRes: Int) =
        NotificationChannel(id, context.getString(nameRes), NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = context.getString(descRes)
        }
}
