package com.mobile.tamatami.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobile.tamatami.R
import com.mobile.tamatami.TamatamiApp
import com.mobile.tamatami.di.AppContainer
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Periodic reminder poll. Reads the day's snapshots off the repositories, hands
 * the yes/no decisions to [ReminderEvaluator] (which delegates to the pure
 * `ReminderLogic`), and posts whatever should fire. Deliberately a thin shell:
 * no decision logic lives here, so it needs no unit test of its own.
 *
 * Notification ids are fixed per channel so a re-poll *replaces* the previous
 * notification rather than stacking duplicates.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val container = (applicationContext as TamatamiApp).container
        evaluateAndNotify(container)
        Result.success()
    } catch (t: Throwable) {
        Result.retry()
    }

    private suspend fun evaluateAndNotify(container: AppContainer) {
        val today = container.clock.today()
        // Current local wall-clock time for the reminder window checks.
        val now = container.clock.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time

        val cycle = container.cycleRepository.observeTodayCycle(today).first()
        val daily = container.dailyLogRepository.observeToday(today).first()
        val profile = container.userRepository.getProfile()
        val activeMeds = container.medicationRepository.activeMedications()
        val takenByMed = activeMeds.associate { med ->
            med.id to container.medicationRepository.takenSlotsFor(today, med.id)
        }

        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = cycle.daysUntilNextPeriod,
            glasses = daily.waterGlasses,
            goal = daily.waterGoal,
            now = now,
            activeMeds = activeMeds,
            takenByMed = takenByMed,
            // No profile yet (pre-onboarding) → fall back to defaults.
            remindPeriod = profile?.remindPeriodEnabled ?: true,
            remindWater = profile?.remindWaterEnabled ?: true,
            remindPills = profile?.remindPillsEnabled ?: true,
        )

        val notifier = container.notifier

        if (decision.period) {
            notifier.post(
                NotificationChannels.PERIOD,
                ID_PERIOD,
                getString(R.string.reminder_period_title),
                getString(R.string.reminder_period_text),
            )
        }
        if (decision.water) {
            notifier.post(
                NotificationChannels.WATER,
                ID_WATER,
                getString(R.string.reminder_water_title),
                getString(R.string.reminder_water_text),
            )
        }
        decision.pillSlots.forEach { (medId, slot) ->
            val med = activeMeds.first { it.id == medId }
            notifier.post(
                NotificationChannels.PILLS,
                ID_PILLS_BASE + medId.toInt(),
                getString(R.string.reminder_pills_title),
                getString(R.string.reminder_pills_text, med.name, slot.displayName),
            )
        }
    }

    private fun getString(resId: Int, vararg args: Any): String =
        applicationContext.getString(resId, *args)

    companion object {
        private const val ID_PERIOD = 1001
        private const val ID_WATER = 1002
        private const val ID_PILLS_BASE = 2000
    }
}
