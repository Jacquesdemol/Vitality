import test from 'node:test';
import assert from 'node:assert/strict';
import { DailyInsightEngine, calculateBMI, calculateSleepScore, InsightInputData } from './dailyInsightEngine.ts';

test('calculateBMI returns correct BMI and classification for standard values', () => {
  const result = calculateBMI(73.8, 173);
  assert.equal(result.bmi, 24.7);
  assert.equal(result.classification, 'Eutrofia / Normal');
});

test('calculateBMI handles zero or invalid values gracefully without throwing', () => {
  const result = calculateBMI(0, 0);
  assert.equal(result.bmi, 0);
  assert.equal(result.classification, 'Dados insuficientes');
});

test('calculateSleepScore computes proportional score and assigns reasonable badge', () => {
  // 7h 48m = 468 min, target 480 min (8h), deep 74m, awake 18m
  const result = calculateSleepScore(468, 480, 74, 18);
  assert.ok(result.score >= 80 && result.score <= 100);
  assert.ok(result.badge === 'Bom' || result.badge === 'Excelente');
});

test('DailyInsightEngine generates step proximity insight when between 70% and 99%', () => {
  const mockData: InsightInputData = {
    steps: {
      current: 7842,
      goal: 10000,
      percentage: 78.4,
      weeklyHistory: [],
      previousDayComparisonPct: 5,
      lastUpdated: new Date().toISOString(),
      source: 'Health Connect',
    },
    sleep: {
      totalMinutes: 468,
      targetMinutesMin: 420,
      targetMinutesMax: 540,
      sleepTime: '23:42',
      wakeTime: '07:30',
      qualityBadge: 'Bom',
      qualityScorePct: 87,
      qualityDeltaPct: 6,
      stages: {
        awakeMinutes: 18,
        remMinutes: 94,
        lightMinutes: 242,
        deepMinutes: 74,
      },
      stagesTimeline: [],
      regularityScorePct: 88,
      avgSleepingHr: 58,
      sevenDaysHistory: [
        { day: 'Seg', hours: 7.5, quality: 85, dateStr: '01 nov' },
        { day: 'Ter', hours: 8.0, quality: 89, dateStr: '02 nov' },
        { day: 'Qua', hours: 7.8, quality: 87, dateStr: '03 nov' },
      ],
      todayInsight: 'Manter um horário regular para dormir ajuda na qualidade do seu sono.',
      factors: [],
      lastUpdated: new Date().toISOString(),
      source: 'Health Connect',
    },
    exercise: {
      weeklyDurationMinutes: 260,
      weeklyWorkoutsCount: 3,
      weeklyTargetWorkouts: 5,
      weeklyComparisonPct: 12,
      activeCaloriesWeekly: 1820,
      activeCaloriesDeltaPct: 8,
      totalDistanceWeeklyKm: 14.6,
      distanceDeltaPct: 10,
      dailyMinutes: [],
      recentWorkouts: [],
      lastUpdated: new Date().toISOString(),
    },
    heartRate: {
      latestBpm: 72,
      restingBpm: 60,
      dailyAvgBpm: 68,
      minBpm: 52,
      maxBpm: 124,
      statusText: 'Normal',
      normalRange: '52-100 bpm',
      todaySamples: [],
      weeklyAverages: [{ day: 'Seg', avg: 68, resting: 60 }],
      monthlyAverages: [],
      lastUpdated: new Date().toISOString(),
      source: 'Health Connect',
    },
    userGoalSteps: 10000,
  };

  const insights = DailyInsightEngine.generateInsights(mockData);
  assert.ok(insights.length > 0);

  const proximityInsight = insights.find((i) => i.id === 'step_proximity');
  assert.ok(proximityInsight !== undefined);
  assert.equal(
    proximityInsight?.message,
    'Você está próximo de alcançar sua meta de passos. Uma caminhada curta pode ajudar.'
  );

  const consistencyInsight = insights.find((i) => i.id === 'workout_consistency');
  assert.ok(consistencyInsight !== undefined);
  assert.equal(
    consistencyInsight?.message,
    'Você treinou 3 vezes nesta semana. Continue mantendo uma rotina consistente.'
  );
});
