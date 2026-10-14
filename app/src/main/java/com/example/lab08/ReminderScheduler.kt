
package com.example.lab08

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        task: Task,
        reminderTimeMillis: Long
    ) {
        // Calcular cuánto falta para el recordatorio
        val delay = reminderTimeMillis -
                System.currentTimeMillis()

        // Evitar programar fechas pasadas
        if (delay <= 0) return

        // Datos que recibirá ReminderWorker
        val inputData = Data.Builder()
            .putString(
                "task_description",
                task.description
            )
            .putInt(
                "notification_id",
                task.id
            )
            .build()

        // Crear el trabajo programado
        val workRequest =
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

        // Programar un recordatorio por tarea
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "reminder_task_${task.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    // Cancelar el recordatorio de una tarea
    fun cancelReminder(
        context: Context,
        taskId: Int
    ) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(
                "reminder_task_$taskId"
            )
    }
}
