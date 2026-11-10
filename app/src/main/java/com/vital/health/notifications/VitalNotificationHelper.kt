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
import com.vital.health.insights.DailyInsightEngine
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object VitalNotificationHelper {

    const val CHANNEL_ID = "vital_health_channel"
    private const val CHANNEL_NAME = "Alertas e Incentivos VITAL"
    private const val CHANNEL_DESC = "Notificações com citações reais, metas e dicas de saúde"

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
        val quote = DailyInsightEngine.getRandomQuote()

        when (type) {
            "motivation" -> sendHealthAlert(
                context,
                "${quote.author} • Inspiração Real ✨",
                "\"${quote.text}\" — ${quote.role}.\nMantenha a consistência nos seus passos e atividade hoje!"
            )
            "sleep" -> sendHealthAlert(
                context,
                "Kobe Bryant • Recuperação Inteligente 🌙",
                "\"A dedicação faz o que o talento apenas promete. O descanso e o esforço caminham juntos.\"\nExcelente noite de descanso para recuperar sua energia!"
            )
            "inactivity" -> sendHealthAlert(
                context,
                "Muhammad Ali • Faça o dia contar 🥊",
                "\"Não conte os dias, faça os dias contarem.\"\nVocê está em repouso prolongado. Levante-se para beber água e caminhar alguns minutos!"
            )
            else -> sendHealthAlert(
                context,
                "Ayrton Senna • Foco Total 🏁",
                "\"Se você quer ser bem-sucedido, precisa ter dedicação total, buscar seu último limite e dar o melhor de si.\""
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
