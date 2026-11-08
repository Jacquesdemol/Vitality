package com.vital.health.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.LocalTime

class VitalNotificationWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val now = LocalTime.now()

        // Mensagens contextuais com base no período do dia
        when (now.hour) {
            in 8..11 -> {
                VitalNotificationHelper.sendHealthAlert(
                    appContext,
                    "Bom dia com VITAL ☀️",
                    "Comece o dia com uma boa hidratação e prepare-se para atingir sua meta diária de passos!"
                )
            }
            in 14..17 -> {
                VitalNotificationHelper.sendHealthAlert(
                    appContext,
                    "Pausa Ativa da Tarde 🚶",
                    "Que tal levantar, esticar o corpo e somar mais alguns passos para manter o metabolismo ativo?"
                )
            }
            in 20..22 -> {
                VitalNotificationHelper.sendHealthAlert(
                    appContext,
                    "Preparando para descansar 🌙",
                    "Evite telas brilhantes nos próximos 40 minutos para garantir uma transição suave para o sono profundo."
                )
            }
            else -> {
                VitalNotificationHelper.sendSampleNotification(appContext, "motivation")
            }
        }

        return Result.success()
    }
}
