package com.mobile.tamatami.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mobile.tamatami.MainActivity
import com.mobile.tamatami.R

/**
 * Thin wrapper around [NotificationManagerCompat] that posts a reminder. Keeps
 * the POST_NOTIFICATIONS check in one place: on API 33+ the OS drops a notify()
 * call unless the runtime permission is granted, so we check [areNotificationsEnabled]
 * and no-op gracefully — a background worker must never crash because the user
 * hasn't enabled notifications yet.
 */
class Notifier(private val context: Context) {

    fun post(channelId: String, notificationId: Int, title: String, text: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val tapIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }
}
