package com.vital.health.healthconnect

import android.util.Log
import com.vital.health.util.AppLifecycleTracker
import kotlinx.coroutines.Job
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext

/**
 * HealthConnectLogger - Instrumentação oficial exigida na auditoria com a tag VITAL_HC_DEBUG.
 * Registra ciclo de vida, duração, identificador único, job, cancelamentos e estado do app.
 */
object HealthConnectLogger {

    const val TAG = "VITAL_HC_DEBUG"
    private val querySequence = AtomicLong(1000)

    fun nextQueryId(prefix: String = "Q"): String {
        return "$prefix-${querySequence.incrementAndGet()}"
    }

    fun logQueryStart(
        queryId: String,
        recordType: String,
        component: String,
        updateReason: String,
        coroutineContext: CoroutineContext? = null,
        attempt: Int = 1
    ) {
        val appState = AppLifecycleTracker.stateLabel
        val job = coroutineContext?.get(Job)
        val jobInfo = job?.let { "Job@${Integer.toHexString(it.hashCode())}[isActive=${it.isActive}]" } ?: "NoJobContext"

        Log.i(
            TAG,
            "[$queryId] INÍCIO | Tipo: $recordType | Comp: $component | Motivo: $updateReason | " +
                    "App: $appState | Tentativa: $attempt | Coroutine: $jobInfo | " +
                    "Horário: ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
        )
    }

    fun logQuerySuccess(
        queryId: String,
        recordType: String,
        component: String,
        durationMs: Long,
        recordsCount: Int,
        coroutineContext: CoroutineContext? = null
    ) {
        val appState = AppLifecycleTracker.stateLabel
        Log.i(
            TAG,
            "[$queryId] SUCESSO | Tipo: $recordType | Comp: $component | Duração: ${durationMs}ms | " +
                    "Registros: $recordsCount | App: $appState | " +
                    "Horário: ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
        )
    }

    fun logQueryError(
        queryId: String,
        recordType: String,
        component: String,
        durationMs: Long,
        exception: Throwable,
        attempt: Int = 1,
        coroutineContext: CoroutineContext? = null
    ) {
        val appState = AppLifecycleTracker.stateLabel
        val isSecurity = exception is SecurityException
        val errorPrefix = if (isSecurity) "ERRO DE PERMISSÃO / BACKGROUND" else "FALHA"

        Log.e(
            TAG,
            "[$queryId] $errorPrefix | Tipo: $recordType | Comp: $component | Duração: ${durationMs}ms | " +
                    "Tentativa: $attempt | App: $appState | " +
                    "Exceção: ${exception.javaClass.simpleName} - ${exception.message} | " +
                    "Horário: ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
        )
    }

    fun logQueryCancelled(
        queryId: String,
        recordType: String,
        component: String,
        durationMs: Long,
        reason: String,
        coroutineContext: CoroutineContext? = null
    ) {
        val appState = AppLifecycleTracker.stateLabel
        val job = coroutineContext?.get(Job)
        val jobInfo = job?.let { "Job@${Integer.toHexString(it.hashCode())}" } ?: "UnknownJob"

        Log.w(
            TAG,
            "[$queryId] CANCELADO | Tipo: $recordType | Comp: $component | Duração: ${durationMs}ms | " +
                    "App: $appState | Motivo: $reason | Job: $jobInfo | " +
                    "Horário: ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
        )
    }

    fun logDaySelection(
        component: String,
        selectedDate: String,
        dateRange: String,
        recordsCount: Int
    ) {
        Log.d(
            TAG,
            "[NAVEGAÇÃO_DIA] Comp: $component | DataSelecionada: $selectedDate | " +
                    "Intervalo: $dateRange | RegistrosRetornados: $recordsCount | " +
                    "App: ${AppLifecycleTracker.stateLabel}"
        )
    }

    fun logWeekNavigation(
        component: String,
        weekOffset: Long,
        weekStartDate: String,
        weekEndDate: String
    ) {
        Log.d(
            TAG,
            "[NAVEGAÇÃO_SEMANA] Comp: $component | Offset: $weekOffset | " +
                    "De: $weekStartDate até $weekEndDate | App: ${AppLifecycleTracker.stateLabel}"
        )
    }
}
