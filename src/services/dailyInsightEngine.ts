/**
 * DailyInsightEngine - Motor de Dicas Diárias
 * Produz recomendações contextuais, não-médicas, transparentes e baseadas em regras determinísticas.
 */

import { DailyInsight, StepsData, SleepData, ExerciseData, HeartRateData, InsightCategory } from '../types/vital.ts';

export interface InsightInputData {
  steps: StepsData;
  sleep: SleepData;
  exercise: ExerciseData;
  heartRate: HeartRateData;
  userGoalSteps: number;
}

export class DailyInsightEngine {
  /**
   * Generates prioritized list of non-diagnostic, contextual daily insights.
   */
  static generateInsights(data: InsightInputData): DailyInsight[] {
    const insights: DailyInsight[] = [];
    const now = new Date().toISOString();

    // 1. Regra de Passos / Proximidade da Meta
    const stepRatio = data.steps.current / (data.steps.goal || 10000);
    if (stepRatio >= 1.0) {
      insights.push({
        id: 'step_goal_reached',
        category: 'activity',
        title: 'Meta de Passos Concluída',
        message: 'Excelente! Você completou sua meta diária de passos planejada.',
        quote: 'Não conte os dias, faça os dias contarem.',
        author: 'Muhammad Ali',
        importance: 'medium',
        timestamp: now,
      });
    } else if (stepRatio >= 0.7 && stepRatio < 1.0) {
      insights.push({
        id: 'step_proximity',
        category: 'activity',
        title: 'Próximo da Meta',
        message: 'Você está próximo de alcançar sua meta de passos. Uma caminhada curta pode ajudar.',
        quote: 'Se você quer ser bem-sucedido, precisa ter dedicação total e buscar seu último limite.',
        author: 'Ayrton Senna',
        importance: 'high',
        timestamp: now,
      });
    } else if (data.steps.current < 2000) {
      insights.push({
        id: 'activity_sedentary_pause',
        category: 'habits',
        title: 'Movimente-se',
        message: 'Você passou boa parte do dia sem registrar atividade. Uma pausa para movimentar o corpo pode ser interessante.',
        quote: 'Não é porque as coisas são difíceis que não ousamos; é porque não ousamos que elas são difíceis.',
        author: 'Sêneca',
        importance: 'medium',
        timestamp: now,
      });
    }

    // 2. Regra de Sono vs Média Semanal
    if (data.sleep.sevenDaysHistory.length >= 3) {
      const avgWeeklyMinutes =
        (data.sleep.sevenDaysHistory.reduce((acc, curr) => acc + curr.hours, 0) /
          data.sleep.sevenDaysHistory.length) *
        60;
      
      const diffMinutes = data.sleep.totalMinutes - avgWeeklyMinutes;

      if (diffMinutes <= -40) {
        insights.push({
          id: 'sleep_below_average',
          category: 'sleep',
          title: 'Duração do Sono',
          message: 'Sua duração de sono ficou abaixo da sua média semanal. Tente manter um horário regular para dormir.',
          importance: 'high',
          timestamp: now,
        });
      } else if (data.sleep.regularityScorePct >= 85) {
        insights.push({
          id: 'sleep_regularity_high',
          category: 'sleep',
          title: 'Sono Regular',
          message: 'Seu sono apresentou maior regularidade nos últimos dias.',
          importance: 'normal',
          timestamp: now,
        });
      }
    } else {
      // Sem histórico suficiente de 3 dias
      insights.push({
        id: 'sleep_collecting_data',
        category: 'sleep',
        title: 'Acumulando Dados',
        message: 'Continue utilizando o monitoramento de sono nos próximos dias para que possamos comparar com sua média pessoal.',
        importance: 'normal',
        timestamp: now,
      });
    }

    // 3. Regra de Consistência de Treinos
    if (data.exercise.weeklyWorkoutsCount >= 3) {
      insights.push({
        id: 'workout_consistency',
        category: 'consistency',
        title: 'Consistência de Treinos',
        message: `Você treinou ${data.exercise.weeklyWorkoutsCount} vezes nesta semana. Continue mantendo uma rotina consistente.`,
        importance: 'medium',
        timestamp: now,
      });
    } else if (data.exercise.weeklyComparisonPct > 5) {
      insights.push({
        id: 'workout_improvement',
        category: 'activity',
        title: 'Mais Atividade',
        message: 'Hoje você acumulou mais minutos de atividade do que ontem.',
        importance: 'normal',
        timestamp: now,
      });
    }

    // 4. Regra de Frequência Cardíaca em Repouso (Tendência, sem diagnóstico médico)
    const normalResting = data.heartRate.restingBpm || 60;
    const weeklyAvgResting =
      data.heartRate.weeklyAverages.length > 0
        ? data.heartRate.weeklyAverages.reduce((acc, curr) => acc + curr.resting, 0) /
          data.heartRate.weeklyAverages.length
        : normalResting;

    if (Math.abs(normalResting - weeklyAvgResting) >= 7) {
      insights.push({
        id: 'hr_resting_drift',
        category: 'recovery',
        title: 'Frequência Cardíaca em Repouso',
        message: 'Sua frequência cardíaca em repouso está diferente da sua média habitual. Observe a tendência e considere buscar orientação profissional se houver sintomas.',
        importance: 'high',
        timestamp: now,
      });
    }

    // Se nenhuma dica urgente, garantir que há a principal do dia sobre sono ou recuperação
    if (insights.length === 0) {
      insights.push({
        id: 'daily_habits_default',
        category: 'habits',
        title: 'Dica do Dia',
        message: 'Manter um horário regular para dormir ajuda na qualidade do seu sono.',
        importance: 'normal',
        timestamp: now,
      });
    }

    return insights;
  }
}

/**
 * Pure calculation helpers for health metrics
 */
export function calculateBMI(weightKg: number, heightCm: number): { bmi: number; classification: string } {
  if (!weightKg || !heightCm || heightCm <= 0) {
    return { bmi: 0, classification: 'Dados insuficientes' };
  }
  const heightM = heightCm / 100;
  const rawBmi = weightKg / (heightM * heightM);
  const bmi = Math.round(rawBmi * 10) / 10;

  let classification = 'Normal';
  if (bmi < 18.5) classification = 'Abaixo do peso';
  else if (bmi < 25.0) classification = 'Eutrofia / Normal';
  else if (bmi < 30.0) classification = 'Sobrepeso';
  else classification = 'Obesidade';

  return { bmi, classification };
}

export function calculateSleepScore(
  totalMinutes: number,
  targetMinutes: number,
  deepMinutes: number,
  awakeMinutes: number
): { score: number; badge: 'Excelente' | 'Bom' | 'Regular' | 'Abaixo do ideal' } {
  if (totalMinutes <= 0) return { score: 0, badge: 'Abaixo do ideal' };

  // Duração (max 45 pts)
  const durationRatio = Math.min(1.2, totalMinutes / (targetMinutes || 480));
  let durationPts = 45 * Math.min(1, durationRatio);
  if (durationRatio > 1.15) durationPts -= 5; // sono excessivo deduz ligeiramente

  // Sono Profundo (ideal 15% - 25% do tempo, max 30 pts)
  const deepRatio = deepMinutes / totalMinutes;
  let deepPts = 0;
  if (deepRatio >= 0.15) deepPts = 30;
  else deepPts = Math.round((deepRatio / 0.15) * 30);

  // Tempo acordado (penalidade se acordado > 30 min, max 25 pts)
  let awakePts = 25;
  if (awakeMinutes > 45) awakePts = 10;
  else if (awakeMinutes > 25) awakePts = 18;

  const total = Math.min(100, Math.max(10, Math.round(durationPts + deepPts + awakePts)));

  let badge: 'Excelente' | 'Bom' | 'Regular' | 'Abaixo do ideal' = 'Bom';
  if (total >= 88) badge = 'Excelente';
  else if (total >= 75) badge = 'Bom';
  else if (total >= 60) badge = 'Regular';
  else badge = 'Abaixo do ideal';

  return { score: total, badge };
}
