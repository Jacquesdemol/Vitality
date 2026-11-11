/**
 * HealthConnectRepository & State Manager
 * Emulates the Android Health Connect SDK integration, Room DB local cache,
 * aggregation of disparate records, and device source tracking.
 */

import {
  UserProfile,
  HeartRateData,
  StepsData,
  CaloriesData,
  ExerciseData,
  SleepData,
  BodyCompositionData,
  AdditionalMetrics,
  HealthConnectState,
  HealthConnectPermission,
  WidgetConfig,
  ZeppConnectorState,
} from '../types/vital.ts';

const STORAGE_KEYS = {
  USER_PROFILE: 'vital_user_profile',
  WIDGETS: 'vital_widgets_config',
  IS_DEMO_MODE: 'vital_is_demo_mode',
  HEALTH_CONNECT_PERMS: 'vital_hc_permissions',
  WORKOUTS: 'vital_workouts_list',
  LAST_SYNC: 'vital_last_sync_time',
  ZEPP_STATE: 'vital_zepp_state',
};

// Initial default widgets layout (customizable & reorderable)
export const DEFAULT_WIDGETS: WidgetConfig[] = [
  { id: 'heart_rate', name: 'Frequência Cardíaca', enabled: true, order: 0, size: 'full' },
  { id: 'calories', name: 'Calorias Ativas', enabled: true, order: 1, size: 'half' },
  { id: 'steps', name: 'Passos Diários', enabled: true, order: 2, size: 'half' },
  { id: 'weekly_exercise', name: 'Exercícios da Semana', enabled: true, order: 3, size: 'full' },
  { id: 'body_summary', name: 'Seu Corpo em Resumo', enabled: true, order: 4, size: 'full' },
  { id: 'additional_vitals', name: 'Outros Indicadores (SpO2, Respiração)', enabled: false, order: 5, size: 'full' },
];

export const INITIAL_USER_PROFILE: UserProfile = {
  name: 'Iury Henrique',
  age: 24,
  heightCm: 173,
  weightKg: 73.8,
  gender: 'male',
  goals: {
    dailySteps: 10000,
    activeCalories: 650,
    calorieGoalMode: 'active',
    sleepMinutes: 480, // 8h
    weeklyWorkouts: 5,
    targetWeightKg: 72.0,
  },
};

/**
 * Cálculo da Taxa Metabólica Basal (BMR) pela fórmula científica de Mifflin-St Jeor:
 * Homens: (10 × peso) + (6.25 × altura) - (5 × idade) + 5
 * Mulheres: (10 × peso) + (6.25 × altura) - (5 × idade) - 161
 */
export function calculateBmrMifflinStJeor(profile: UserProfile): number {
  const { weightKg, heightCm, age, gender } = profile;
  if (!weightKg || !heightCm || !age) return 1650;
  if (gender === 'male') {
    return Math.round(10 * weightKg + 6.25 * heightCm - 5 * age + 5);
  } else if (gender === 'female') {
    return Math.round(10 * weightKg + 6.25 * heightCm - 5 * age - 161);
  }
  return Math.round(10 * weightKg + 6.25 * heightCm - 5 * age - 78);
}

export const INITIAL_HEART_RATE: HeartRateData = {
  latestBpm: 72,
  restingBpm: 58,
  dailyAvgBpm: 68,
  minBpm: 52,
  maxBpm: 128,
  statusText: 'Normal',
  normalRange: '52–100 bpm',
  todaySamples: [
    { time: '00:00', bpm: 56 },
    { time: '03:00', bpm: 53 },
    { time: '06:00', bpm: 58 },
    { time: '08:00', bpm: 74 },
    { time: '10:00', bpm: 72 },
    { time: '12:00', bpm: 82 },
    { time: '14:00', bpm: 69 },
    { time: '16:00', bpm: 95 },
    { time: '18:00', bpm: 112 },
    { time: '20:00', bpm: 78 },
    { time: '22:00', bpm: 64 },
  ],
  weeklyAverages: [
    { day: 'Seg', avg: 70, resting: 59 },
    { day: 'Ter', avg: 68, resting: 58 },
    { day: 'Qua', avg: 72, resting: 60 },
    { day: 'Qui', avg: 69, resting: 58 },
    { day: 'Sex', avg: 74, resting: 61 },
    { day: 'Sáb', avg: 71, resting: 59 },
    { day: 'Dom', avg: 68, resting: 58 },
  ],
  monthlyAverages: [
    { week: 'Sem 1', avg: 69 },
    { week: 'Sem 2', avg: 71 },
    { week: 'Sem 3', avg: 68 },
    { week: 'Sem 4', avg: 70 },
  ],
  lastUpdated: 'Hoje, 09:30',
  source: 'Health Connect (Amazfit Balance via Zepp)',
};

export const INITIAL_STEPS: StepsData = {
  current: 7842,
  goal: 10000,
  percentage: 78.4,
  weeklyHistory: [
    { day: 'Seg', steps: 10240, reached: true },
    { day: 'Ter', steps: 8900, reached: false },
    { day: 'Qua', steps: 11200, reached: true },
    { day: 'Qui', steps: 9450, reached: false },
    { day: 'Sex', steps: 10800, reached: true },
    { day: 'Sáb', steps: 6700, reached: false },
    { day: 'Dom', steps: 7842, reached: false },
  ],
  previousDayComparisonPct: 17,
  lastUpdated: 'Hoje, 09:38',
  source: 'Health Connect (StepsRecord)',
};

export const INITIAL_CALORIES: CaloriesData = {
  activeKcal: 612,
  totalKcal: 2180,
  activeGoal: 650,
  hourlyDistribution: [
    { hour: '06h', kcal: 45 },
    { hour: '08h', kcal: 110 },
    { hour: '10h', kcal: 85 },
    { hour: '12h', kcal: 90 },
    { hour: '14h', kcal: 60 },
    { hour: '16h', kcal: 130 },
    { hour: '18h', kcal: 92 },
  ],
  weeklyHistory: [
    { day: 'Seg', activeKcal: 640, totalKcal: 2240 },
    { day: 'Ter', activeKcal: 590, totalKcal: 2150 },
    { day: 'Qua', activeKcal: 710, totalKcal: 2350 },
    { day: 'Qui', activeKcal: 530, totalKcal: 2090 },
    { day: 'Sex', activeKcal: 680, totalKcal: 2280 },
    { day: 'Sáb', activeKcal: 480, totalKcal: 1980 },
    { day: 'Dom', activeKcal: 612, totalKcal: 2180 },
  ],
  comparisonLastWeekPct: 6,
  lastUpdated: 'Hoje, 09:35',
  source: 'Health Connect (ActiveCaloriesBurnedRecord)',
};

export const INITIAL_SLEEP: SleepData = {
  totalMinutes: 468, // 7h 48min
  targetMinutesMin: 420, // 7h
  targetMinutesMax: 540, // 9h
  sleepTime: '23:42',
  wakeTime: '07:30',
  qualityBadge: 'Bom',
  qualityScorePct: 87,
  qualityDeltaPct: 6,
  stages: {
    awakeMinutes: 18,
    remMinutes: 94,   // 1h 34min
    lightMinutes: 242, // 4h 02min
    deepMinutes: 74,  // 1h 14min
  },
  stagesTimeline: [
    { stage: 'awake', startHourMin: '23:42', endHourMin: '23:55', durationMinutes: 13, intensity: 4 },
    { stage: 'light', startHourMin: '23:55', endHourMin: '00:45', durationMinutes: 50, intensity: 2 },
    { stage: 'deep', startHourMin: '00:45', endHourMin: '01:30', durationMinutes: 45, intensity: 1 },
    { stage: 'light', startHourMin: '01:30', endHourMin: '02:15', durationMinutes: 45, intensity: 2 },
    { stage: 'rem', startHourMin: '02:15', endHourMin: '02:45', durationMinutes: 30, intensity: 3 },
    { stage: 'light', startHourMin: '02:45', endHourMin: '03:50', durationMinutes: 65, intensity: 2 },
    { stage: 'deep', startHourMin: '03:50', endHourMin: '04:19', durationMinutes: 29, intensity: 1 },
    { stage: 'rem', startHourMin: '04:19', endHourMin: '04:55', durationMinutes: 36, intensity: 3 },
    { stage: 'light', startHourMin: '04:55', endHourMin: '06:10', durationMinutes: 75, intensity: 2 },
    { stage: 'rem', startHourMin: '06:10', endHourMin: '06:38', durationMinutes: 28, intensity: 3 },
    { stage: 'awake', startHourMin: '06:38', endHourMin: '06:43', durationMinutes: 5, intensity: 4 },
    { stage: 'light', startHourMin: '06:43', endHourMin: '07:30', durationMinutes: 47, intensity: 2 },
  ],
  regularityScorePct: 91,
  avgSleepingHr: 54,
  sevenDaysHistory: [
    { day: 'Seg', hours: 7.2, quality: 82, dateStr: '02 nov' },
    { day: 'Ter', hours: 7.5, quality: 85, dateStr: '03 nov' },
    { day: 'Qua', hours: 6.8, quality: 78, dateStr: '04 nov' },
    { day: 'Qui', hours: 7.6, quality: 84, dateStr: '05 nov' },
    { day: 'Sex', hours: 7.4, quality: 83, dateStr: '06 nov' },
    { day: 'Sáb', hours: 8.2, quality: 90, dateStr: '07 nov' },
    { day: 'Dom', hours: 7.8, quality: 87, dateStr: '08 nov' },
  ],
  todayInsight: 'Manter um horário regular para dormir ajuda na qualidade do seu sono.',
  factors: [
    { name: 'Duração total', description: '7h 48min dentro da faixa meta de 7-9h', score: 92, weight: 'Alta' },
    { name: 'Sono Profundo', description: '1h 14min (16% do total) favorecendo recuperação física', score: 85, weight: 'Alta' },
    { name: 'Sono REM', description: '1h 34min (20% do total) essencial para memória e foco', score: 88, weight: 'Média' },
    { name: 'Tempo Acordado', description: 'Apenas 18 minutos de microdespertares', score: 90, weight: 'Média' },
    { name: 'Regularidade', description: 'Variação de 15 minutos em relação ao horário de acordar', score: 91, weight: 'Alta' },
  ],
  lastUpdated: 'Hoje, 07:45',
  source: 'Health Connect (SleepSessionRecord)',
};

export const INITIAL_EXERCISE: ExerciseData = {
  weeklyDurationMinutes: 260, // 4h 20min
  weeklyWorkoutsCount: 3,
  weeklyTargetWorkouts: 5,
  weeklyComparisonPct: 12,
  activeCaloriesWeekly: 1820,
  activeCaloriesDeltaPct: 8,
  totalDistanceWeeklyKm: 14.6,
  distanceDeltaPct: 10,
  dailyMinutes: [
    { day: 'Seg', minutes: 70, active: true },
    { day: 'Ter', minutes: 0, active: false },
    { day: 'Qua', minutes: 55, active: true },
    { day: 'Qui', minutes: 0, active: false },
    { day: 'Sex', minutes: 80, active: true },
    { day: 'Sáb', minutes: 0, active: false },
    { day: 'Dom', minutes: 55, active: true },
  ],
  recentWorkouts: [
    {
      id: 'w_1',
      title: 'Musculação',
      category: 'strength',
      dateStr: 'Hoje',
      timestamp: 'Hoje • 07:15',
      durationMinutes: 80,
      calories: 412,
      avgHr: 124,
      maxHr: 156,
      source: 'Health Connect (ExerciseSessionRecord)',
      zones: [
        { name: 'Aquecimento (Z1)', minBpm: 100, maxBpm: 118, minutes: 15, color: '#85858C' },
        { name: 'Queima de Gordura (Z2)', minBpm: 119, maxBpm: 136, minutes: 35, color: '#A0A0A8' },
        { name: 'Aeróbico (Z3)', minBpm: 137, maxBpm: 152, minutes: 22, color: '#D4D4D8' },
        { name: 'Anaeróbico (Z4)', minBpm: 153, maxBpm: 168, minutes: 8, color: '#F2F2F4' },
      ],
    },
    {
      id: 'w_2',
      title: 'Caminhada ao Ar Livre',
      category: 'walking',
      dateStr: 'Ontem',
      timestamp: 'Ontem • 18:00',
      durationMinutes: 45,
      calories: 195,
      distanceKm: 3.8,
      avgHr: 108,
      maxHr: 122,
      source: 'Health Connect (ExerciseSessionRecord)',
      zones: [
        { name: 'Aquecimento (Z1)', minBpm: 95, maxBpm: 110, minutes: 30, color: '#85858C' },
        { name: 'Queima de Gordura (Z2)', minBpm: 111, maxBpm: 125, minutes: 15, color: '#A0A0A8' },
      ],
    },
    {
      id: 'w_3',
      title: 'Treino Funcional',
      category: 'functional',
      dateStr: '05 nov',
      timestamp: '05 nov • 19:10',
      durationMinutes: 50,
      calories: 380,
      avgHr: 138,
      maxHr: 165,
      source: 'Health Connect (ExerciseSessionRecord)',
    },
  ],
  lastUpdated: 'Hoje, 08:45',
};

export const INITIAL_BODY_COMPOSITION: BodyCompositionData = {
  weightKg: 73.8,
  bmi: 24.1,
  bodyFatPercent: 20.5,
  bodyFatValid: true,
  history: [
    { date: '10 out', weight: 75.2, bmi: 24.6 },
    { date: '17 out', weight: 74.8, bmi: 24.5 },
    { date: '24 out', weight: 74.3, bmi: 24.3 },
    { date: '31 out', weight: 74.0, bmi: 24.2 },
    { date: '08 nov', weight: 73.8, bmi: 24.1 },
  ],
  lastUpdated: '08 nov, 07:10',
  source: 'Balança de Bioimpedância (WeightRecord & BodyFatRecord)',
};

export const INITIAL_ADDITIONAL_METRICS: AdditionalMetrics = {
  spO2Percent: 98,
  respiratoryRateRpm: 14,
  hrvRmssdMs: 48,
  bloodPressureSysMmHg: 118,
  bloodPressureDiaMmHg: 76,
  bodyTemperatureC: 36.6,
  lastUpdated: 'Hoje, 07:35',
  availableKeys: ['spO2Percent', 'respiratoryRateRpm', 'hrvRmssdMs', 'bloodPressure'],
};

export const INITIAL_HC_PERMISSIONS: HealthConnectPermission[] = [
  { id: 'steps', recordName: 'StepsRecord', description: 'Leitura de passos diários e cadência', granted: true, category: 'activity' },
  { id: 'heart_rate', recordName: 'HeartRateRecord', description: 'Frequência cardíaca contínua e amostras', granted: true, category: 'vitals' },
  { id: 'resting_hr', recordName: 'RestingHeartRateRecord', description: 'Batimentos cardíacos basais em repouso', granted: true, category: 'vitals' },
  { id: 'sleep', recordName: 'SleepSessionRecord', description: 'Sessões de sono e estágios hipnograma', granted: true, category: 'sleep' },
  { id: 'exercise', recordName: 'ExerciseSessionRecord', description: 'Atividades físicas e treinos registrados', granted: true, category: 'activity' },
  { id: 'active_cal', recordName: 'ActiveCaloriesBurnedRecord', description: 'Gasto calórico em movimento', granted: true, category: 'activity' },
  { id: 'total_cal', recordName: 'TotalCaloriesBurnedRecord', description: 'Gasto metabólico basal + ativo', granted: true, category: 'activity' },
  { id: 'distance', recordName: 'DistanceRecord', description: 'Distância percorrida em treinos e passos', granted: true, category: 'activity' },
  { id: 'weight', recordName: 'WeightRecord', description: 'Histórico de pesagens corporais', granted: true, category: 'body' },
  { id: 'body_fat', recordName: 'BodyFatRecord', description: 'Percentual de gordura por bioimpedância', granted: true, category: 'body' },
  { id: 'spo2', recordName: 'OxygenSaturationRecord', description: 'Oximetria de pulso (SpO2)', granted: true, category: 'vitals' },
  { id: 'respiratory', recordName: 'RespiratoryRateRecord', description: 'Frequência respiratória em repouso', granted: true, category: 'vitals' },
  { id: 'hrv', recordName: 'HeartRateVariabilityRmssdRecord', description: 'Variabilidade cardíaca (HRV RMSSD)', granted: true, category: 'vitals' },
  { id: 'blood_pressure', recordName: 'BloodPressureRecord', description: 'Pressão arterial sistólica e diastólica', granted: false, category: 'vitals' },
];

export const INITIAL_ZEPP_STATE: ZeppConnectorState = {
  status: 'synced_via_health_connect',
  clientIdConfigured: false,
  lastSync: 'Hoje, 09:30',
  deviceName: 'Amazfit Balance (Zepp OS 3.5)',
  features: ['Sleep Stages', 'Continuous Heart Rate', 'HRV', 'Active Calories', 'Workouts'],
};

export class HealthConnectRepository {
  static getProfile(): UserProfile {
    try {
      const stored = localStorage.getItem(STORAGE_KEYS.USER_PROFILE);
      return stored ? JSON.parse(stored) : INITIAL_USER_PROFILE;
    } catch {
      return INITIAL_USER_PROFILE;
    }
  }

  static saveProfile(profile: UserProfile): void {
    try {
      localStorage.setItem(STORAGE_KEYS.USER_PROFILE, JSON.stringify(profile));
    } catch (e) {
      console.warn('Erro ao salvar perfil:', e);
    }
  }

  static getWidgetsConfig(): WidgetConfig[] {
    try {
      const stored = localStorage.getItem(STORAGE_KEYS.WIDGETS);
      if (stored) {
        const parsed: WidgetConfig[] = JSON.parse(stored);
        return parsed.sort((a, b) => a.order - b.order);
      }
      return DEFAULT_WIDGETS;
    } catch {
      return DEFAULT_WIDGETS;
    }
  }

  static saveWidgetsConfig(configs: WidgetConfig[]): void {
    try {
      localStorage.setItem(STORAGE_KEYS.WIDGETS, JSON.stringify(configs));
    } catch (e) {
      console.warn('Erro ao salvar widgets:', e);
    }
  }

  static isDemoMode(): boolean {
    try {
      const stored = localStorage.getItem(STORAGE_KEYS.IS_DEMO_MODE);
      return stored ? JSON.parse(stored) : true; // default demo on first run for visual fidelity
    } catch {
      return true;
    }
  }

  static setDemoMode(isDemo: boolean): void {
    try {
      localStorage.setItem(STORAGE_KEYS.IS_DEMO_MODE, JSON.stringify(isDemo));
    } catch (e) {
      console.warn('Erro ao alternar modo demo:', e);
    }
  }

  static getPermissions(): HealthConnectPermission[] {
    try {
      const stored = localStorage.getItem(STORAGE_KEYS.HEALTH_CONNECT_PERMS);
      return stored ? JSON.parse(stored) : INITIAL_HC_PERMISSIONS;
    } catch {
      return INITIAL_HC_PERMISSIONS;
    }
  }

  static savePermissions(perms: HealthConnectPermission[]): void {
    try {
      localStorage.setItem(STORAGE_KEYS.HEALTH_CONNECT_PERMS, JSON.stringify(perms));
    } catch (e) {
      console.warn('Erro ao salvar permissões:', e);
    }
  }

  static getZeppState(): ZeppConnectorState {
    try {
      const stored = localStorage.getItem(STORAGE_KEYS.ZEPP_STATE);
      return stored ? JSON.parse(stored) : INITIAL_ZEPP_STATE;
    } catch {
      return INITIAL_ZEPP_STATE;
    }
  }

  static saveZeppState(state: ZeppConnectorState): void {
    try {
      localStorage.setItem(STORAGE_KEYS.ZEPP_STATE, JSON.stringify(state));
    } catch (e) {
      console.warn('Erro ao salvar estado Zepp:', e);
    }
  }

  static exportAllDataAsJSON(): string {
    const data = {
      app: 'VITAL Android Health',
      version: '1.0.0',
      exportedAt: new Date().toISOString(),
      user: this.getProfile(),
      healthConnect: {
        permissions: this.getPermissions(),
        status: 'Active',
      },
      metrics: {
        heartRate: INITIAL_HEART_RATE,
        steps: INITIAL_STEPS,
        calories: INITIAL_CALORIES,
        sleep: INITIAL_SLEEP,
        exercise: INITIAL_EXERCISE,
        bodyComposition: INITIAL_BODY_COMPOSITION,
        additional: INITIAL_ADDITIONAL_METRICS,
      },
    };
    return JSON.stringify(data, null, 2);
  }

  static clearAllLocalData(): void {
    Object.values(STORAGE_KEYS).forEach((k) => localStorage.removeItem(k));
  }
}
