package com.vital.health.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.vital.health.MainActivity
import com.vital.health.R
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object VitalNotificationHelper {

    const val CHANNEL_ID = "vital_health_channel"
    private const val CHANNEL_NAME = "Alertas e Incentivos VITAL"
    private const val CHANNEL_DESC = "Notificações de metas, alertas de inatividade e dicas diárias"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendHealthAlert(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = Random.nextInt(1000, 9999)
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permissão POST_NOTIFICATIONS não concedida
        }
    }

    fun sendSampleNotification(context: Context, type: String = "motivation") {
        when (type) {
            "motivation" -> sendHealthAlert(
                context,
                "Quase lá na meta diária! 🏃",
                "Você já completou 7.842 de 10.000 passos hoje (78%). Uma caminhada leve de 15 minutos é o suficiente para fechar sua meta!"
            )
            "sleep" -> sendHealthAlert(
                context,
                "Qualidade de Sono Excelente 🌙",
                "Você dormiu 7h 48min na última noite, com sono profundo acima da média semanal (+6%). Excelente recuperação!"
            )
            "inactivity" -> sendHealthAlert(
                context,
                "Hora de se movimentar! 💧",
                "Você permaneceu focado por mais de 90 minutos. Que tal levantar, beber água e dar uma caminhada rápida?"
            )
            else -> sendHealthAlert(
                context,
                "VITAL Saúde",
                "Mantenha a consistência nos treinos e cuide do seu sono para maximizar seu bem-estar diário."
            )
        }
    }

    fun schedulePeriodicHealthReminders(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<VitalNotificationWorker>(
            4, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "VitalPeriodicHealthAlerts",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
