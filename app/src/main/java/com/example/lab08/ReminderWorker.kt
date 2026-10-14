
package com.example.lab08

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        // Obtener los datos del recordatorio
        val taskDescription = inputData.getString("task_description")
            ?: "Tienes una tarea pendiente"

        val notificationId = inputData.getInt(
            "notification_id",
            1
        )

        val channelId = "task_reminders"

        // Crear el canal de notificaciones
        val notificationManager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Recordatorios de tareas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones para recordar tareas pendientes"
        }

        notificationManager.createNotificationChannel(channel)

        // Verificar permiso en Android 13 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                applicationContext.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }
        }

        // Crear la notificación
        val notification = NotificationCompat.Builder(
            applicationContext,
            channelId
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("💜 Recordatorio de tarea")
            .setContentText(taskDescription)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(taskDescription)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Mostrar la notificación
        NotificationManagerCompat.from(applicationContext)
            .notify(notificationId, notification)

        return Result.success()
    }
}
