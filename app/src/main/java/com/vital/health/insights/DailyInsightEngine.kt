package com.vital.health.insights

data class Insight(
    val id: String,
    val category: String, // "activity" | "sleep" | "recovery" | "habits" | "consistency"
    val title: String,
    val message: String
)

/**
 * DailyInsightEngine - Motor de Dicas Diárias
 * Produz recomendações curtas, compreensíveis e contextualizadas sem inventar valores fisiológicos
 * e sem produzir diagnósticos médicos ou recomendações de tratamentos.
 */
class DailyInsightEngine {
    companion object {
        fun generate(
            currentSteps: Long,
            stepGoal: Long,
            sleepMinutes: Long,
            weeklyAvgSleepMinutes: Long,
            workoutCount: Int,
            restingHrDelta: Int
        ): List<Insight> {
            val insights = mutableListOf<Insight>()

            // 1. Proximidade de Passos
            val ratio = currentSteps.toFloat() / stepGoal.coerceAtLeast(1L)
            if (ratio in 0.7f..0.99f) {
                insights.add(
                    Insight(
                        id = "step_prox",
                        category = "activity",
                        title = "Próximo da Meta",
                        message = "Você está próximo de alcançar sua meta de passos. Uma caminhada curta pode ajudar."
                    )
                )
            } else if (currentSteps < 2000) {
                insights.add(
                    Insight(
                        id = "inactivity",
                        category = "habits",
                        title = "Movimente-se",
                        message = "Você passou boa parte do dia sem registrar atividade. Uma pausa para movimentar o corpo pode ser interessante."
                    )
                )
            }

            // 2. Duração do sono vs média semanal
            if (sleepMinutes > 0 && weeklyAvgSleepMinutes > 0 && (sleepMinutes - weeklyAvgSleepMinutes) <= -40) {
                insights.add(
                    Insight(
                        id = "sleep_low",
                        category = "sleep",
                        title = "Duração do Sono",
                        message = "Sua duração de sono ficou abaixo da sua média semanal. Tente manter um horário regular para dormir."
                    )
                )
            }

            // 3. Frequência Cardíaca em Repouso
            if (kotlin.math.abs(restingHrDelta) >= 7) {
                insights.add(
                    Insight(
                        id = "hr_drift",
                        category = "recovery",
                        title = "Frequência Cardíaca",
                        message = "Sua frequência cardíaca em repouso está diferente da sua média habitual. Observe a tendência e considere buscar orientação profissional se houver sintomas."
                    )
                )
            }

            // 4. Consistência de treinos
            if (workoutCount >= 3) {
                insights.add(
                    Insight(
                        id = "workouts",
                        category = "consistency",
                        title = "Consistência de Treinos",
                        message = "Você treinou $workoutCount vezes nesta semana. Continue mantendo uma rotina consistente."
                    )
                )
            }

            if (insights.isEmpty()) {
                insights.add(
                    Insight(
                        id = "default_tip",
                        category = "habits",
                        title = "Dica de hoje",
                        message = "Manter um horário regular para dormir ajuda na qualidade do seu sono."
                    )
                )
            }

            return insights
        }
    }
}
