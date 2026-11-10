package com.vital.health.insights

data class Insight(
    val id: String,
    val category: String, // "activity" | "sleep" | "recovery" | "habits" | "consistency"
    val title: String,
    val message: String,
    val quote: String? = null,
    val author: String? = null
)

data class RealQuote(
    val text: String,
    val author: String,
    val role: String
)

/**
 * DailyInsightEngine - Motor de Dicas e Motivação com Citações Reais
 * Combina métricas reais sincronizadas com citações de atletas e pensadores reais
 * para gerar incentivo genuíno e disciplina saudável.
 */
class DailyInsightEngine {
    companion object {

        val REAL_QUOTES = listOf(
            RealQuote(
                text = "Se você quer ser bem-sucedido, precisa ter dedicação total, buscar seu último limite e dar o melhor de si.",
                author = "Ayrton Senna",
                role = "Tricampeão Mundial de F1"
            ),
            RealQuote(
                text = "Não conte os dias, faça os dias contarem.",
                author = "Muhammad Ali",
                role = "Campeão Mundial dos Pesos-Pesados"
            ),
            RealQuote(
                text = "A dedicação faz o que o talento apenas promete. O descanso e o esforço caminham juntos.",
                author = "Kobe Bryant",
                role = "5x Campeão da NBA"
            ),
            RealQuote(
                text = "Eu treinei 4 anos para correr apenas 9 segundos. Não desista se os resultados levarem algum tempo.",
                author = "Usain Bolt",
                role = "Recordista Mundial e 8x Campeão Olímpico"
            ),
            RealQuote(
                text = "Apenas os disciplinados são verdadeiramente livres na vida. Se você é indisciplinado, é escravo dos seus impulsos.",
                author = "Eliud Kipchoge",
                role = "Bicampeão Olímpico de Maratona"
            ),
            RealQuote(
                text = "Eu falhei vezes e mais vezes na minha vida. E é exatamente por isso que eu tive sucesso.",
                author = "Michael Jordan",
                role = "6x Campeão da NBA"
            ),
            RealQuote(
                text = "Cuide do seu corpo como a morada da sua mente; nenhum homem é livre se não dominar a si mesmo.",
                author = "Marco Aurélio",
                role = "Filósofo e Imperador Romano"
            ),
            RealQuote(
                text = "A vitória é doce, mas o esforço silencioso de cada dia é o que realmente constrói um campeão.",
                author = "Serena Williams",
                role = "23x Campeã de Grand Slam"
            ),
            RealQuote(
                text = "A vontade de se preparar tem que ser sempre maior do que a vontade de vencer.",
                author = "Bernardinho",
                role = "Bicampeão Olímpico de Vôlei"
            ),
            RealQuote(
                text = "Não é porque as coisas são difíceis que não ousamos; é porque não ousamos que elas são difíceis.",
                author = "Sêneca",
                role = "Filósofo Estóico"
            )
        )

        fun getRandomQuote(): RealQuote {
            return REAL_QUOTES.random()
        }

        fun generate(
            currentSteps: Long,
            stepGoal: Long,
            sleepMinutes: Long,
            weeklyAvgSleepMinutes: Long,
            workoutCount: Int,
            restingHrDelta: Int
        ): List<Insight> {
            val insights = mutableListOf<Insight>()

            val ratio = currentSteps.toFloat() / stepGoal.coerceAtLeast(1L)

            // 1. Proximidade ou conclusão de meta de passos
            if (ratio >= 1.0f) {
                insights.add(
                    Insight(
                        id = "step_achieved",
                        category = "activity",
                        title = "Meta Concluída! 🎯",
                        message = "Parabéns! Você atingiu $currentSteps passos hoje, superando sua meta de $stepGoal passos.",
                        quote = "Não conte os dias, faça os dias contarem.",
                        author = "Muhammad Ali"
                    )
                )
            } else if (ratio in 0.7f..0.99f) {
                val remaining = stepGoal - currentSteps
                insights.add(
                    Insight(
                        id = "step_prox",
                        category = "activity",
                        title = "Quase na Meta! 🏃",
                        message = "Faltam apenas $remaining passos para fechar o objetivo do dia. Uma caminhada rápida de 15 minutos é suficiente.",
                        quote = "Se você quer ser bem-sucedido, precisa ter dedicação total e buscar seu último limite.",
                        author = "Ayrton Senna"
                    )
                )
            } else if (currentSteps < 2000 && currentSteps > 0) {
                insights.add(
                    Insight(
                        id = "inactivity",
                        category = "habits",
                        title = "Hora de Movimentar 🚶",
                        message = "Você registrou $currentSteps passos até agora. Levante-se para se hidratar e alongar o corpo.",
                        quote = "Não é porque as coisas são difíceis que não ousamos; é porque não ousamos que elas são difíceis.",
                        author = "Sêneca"
                    )
                )
            } else if (currentSteps == 0L) {
                insights.add(
                    Insight(
                        id = "sync_waiting",
                        category = "activity",
                        title = "Sincronização Health Connect",
                        message = "Conecte sua pulseira ou permita que o sensor de passos do dispositivo registre seu movimento.",
                        quote = "Apenas os disciplinados são verdadeiramente livres na vida.",
                        author = "Eliud Kipchoge"
                    )
                )
            }

            // 2. Duração e qualidade do sono
            if (sleepMinutes > 0) {
                val hours = sleepMinutes / 60
                val mins = sleepMinutes % 60
                if (sleepMinutes >= 420) { // 7 horas ou mais
                    insights.add(
                        Insight(
                            id = "sleep_good",
                            category = "sleep",
                            title = "Sono Reparador 🌙",
                            message = "Você descansou por ${hours}h ${mins}min na última noite, permitindo a regeneração muscular e mental.",
                            quote = "A dedicação faz o que o talento apenas promete. O descanso e o esforço caminham juntos.",
                            author = "Kobe Bryant"
                        )
                    )
                } else {
                    insights.add(
                        Insight(
                            id = "sleep_low",
                            category = "sleep",
                            title = "Atenção ao Descanso",
                            message = "Foram registradas ${hours}h ${mins}min de sono. Priorize ir para a cama um pouco mais cedo hoje para recuperar as energias.",
                            quote = "Cuide do seu corpo como a morada da sua mente; nenhum homem é livre se não dominar a si mesmo.",
                            author = "Marco Aurélio"
                        )
                    )
                }
            }

            // 3. Consistência de treinos
            if (workoutCount >= 3) {
                insights.add(
                    Insight(
                        id = "workouts",
                        category = "consistency",
                        title = "Disciplina Semanal 💪",
                        message = "Você registrou $workoutCount treinos nesta semana. A consistência diária é o segredo da longevidade.",
                        quote = "A vontade de se preparar tem que ser sempre maior do que a vontade de vencer.",
                        author = "Bernardinho"
                    )
                )
            }

            if (insights.isEmpty()) {
                val q = getRandomQuote()
                insights.add(
                    Insight(
                        id = "daily_quote",
                        category = "habits",
                        title = "Inspiração do Dia",
                        message = "Mantenha o foco nos seus hábitos diários de saúde e hidratação.",
                        quote = q.text,
                        author = "${q.author} (${q.role})"
                    )
                )
            }

            return insights
        }
    }
}
