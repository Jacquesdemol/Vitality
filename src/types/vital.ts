/**
 * Types for VITAL Health & Wellness Dashboard
 * Aligned with Android Health Connect SDK & Zepp Open Platform schemas
 */

export type TabType = 'sleep' | 'exercise' | 'home' | 'settings';

export interface UserProfile {
  name: string;
  age: number;
  heightCm: number;
  weightKg: number;
  gender: 'male' | 'female' | 'other' | 'prefer_not_to_say';
  goals: {
    dailySteps: number;
    activeCalories: number;
    sleepMinutes: number;
    weeklyWorkouts: number;
    targetWeightKg?: number;
  };
}

export interface HeartRateSample {
  time: string;
  bpm: number;
}

export interface HeartRateData {
  latestBpm: number;
  restingBpm: number;
  dailyAvgBpm: number;
  minBpm: number;
  maxBpm: number;
  statusText: string;
  normalRange: string;
  todaySamples: HeartRateSample[];
  weeklyAverages: { day: string; avg: number; resting: number }[];
  monthlyAverages: { week: string; avg: number }[];
  lastUpdated: string;
  source: string;
}

export interface StepsData {
  current: number;
  goal: number;
  percentage: number;
  weeklyHistory: { day: string; steps: number; reached: boolean }[];
  previousDayComparisonPct: number;
  lastUpdated: string;
  source: string;
}

export interface CaloriesData {
  activeKcal: number;
  totalKcal: number;
  activeGoal: number;
  hourlyDistribution: { hour: string; kcal: number }[];
  weeklyHistory: { day: string; activeKcal: number; totalKcal: number }[];
  comparisonLastWeekPct: number;
  lastUpdated: string;
  source: string;
}

export type ExerciseCategory =
  | 'strength'
  | 'walking'
  | 'running'
  | 'cycling'
  | 'treadmill'
  | 'swimming'
  | 'functional'
  | 'other';

export interface HeartRateZone {
  name: string;
  minBpm: number;
  maxBpm: number;
  minutes: number;
  color: string;
}

export interface WorkoutSession {
  id: string;
  title: string;
  category: ExerciseCategory;
  dateStr: string; // e.g. "Hoje", "Ontem", "05 nov"
  timestamp: string;
  durationMinutes: number;
  calories: number;
  distanceKm?: number;
  avgHr: number;
  maxHr: number;
  zones?: HeartRateZone[];
  source: string;
}

export interface ExerciseData {
  weeklyDurationMinutes: number;
  weeklyWorkoutsCount: number;
  weeklyTargetWorkouts: number;
  weeklyComparisonPct: number;
  activeCaloriesWeekly: number;
  activeCaloriesDeltaPct: number;
  totalDistanceWeeklyKm: number;
  distanceDeltaPct: number;
  dailyMinutes: { day: string; minutes: number; active: boolean }[];
  recentWorkouts: WorkoutSession[];
  lastUpdated: string;
}

export interface SleepStageTimeline {
  stage: 'awake' | 'rem' | 'light' | 'deep';
  startHourMin: string;
  endHourMin: string;
  durationMinutes: number;
  intensity: number; // 1-4 for visual bar height
}

export interface SleepData {
  totalMinutes: number;
  targetMinutesMin: number;
  targetMinutesMax: number;
  sleepTime: string; // e.g. "23:42"
  wakeTime: string;  // e.g. "07:30"
  qualityBadge: 'Bom' | 'Excelente' | 'Regular' | 'Abaixo do ideal';
  qualityScorePct: number;
  qualityDeltaPct: number;
  stages: {
    awakeMinutes: number;
    remMinutes: number;
    lightMinutes: number;
    deepMinutes: number;
  };
  stagesTimeline: SleepStageTimeline[];
  regularityScorePct: number;
  avgSleepingHr: number;
  sevenDaysHistory: { day: string; hours: number; quality: number; dateStr: string }[];
  todayInsight: string;
  factors: {
    name: string;
    description: string;
    score: number;
    weight: string;
  }[];
  lastUpdated: string;
  source: string;
}

export interface BodyCompositionData {
  weightKg: number;
  bmi: number;
  bodyFatPercent?: number; // optional, never estimate without measurement
  bodyFatValid: boolean;
  history: { date: string; weight: number; bmi: number }[];
  lastUpdated: string;
  source: string;
}

export interface AdditionalMetrics {
  spO2Percent?: number;
  respiratoryRateRpm?: number;
  hrvRmssdMs?: number;
  bloodPressureSysMmHg?: number;
  bloodPressureDiaMmHg?: number;
  bodyTemperatureC?: number;
  lastUpdated: string;
  availableKeys: string[];
}

export type InsightCategory = 'activity' | 'sleep' | 'recovery' | 'habits' | 'consistency';

export interface DailyInsight {
  id: string;
  category: InsightCategory;
  title: string;
  message: string;
  importance: 'high' | 'medium' | 'normal';
  timestamp: string;
}

export interface WidgetConfig {
  id: string;
  name: string;
  enabled: boolean;
  order: number;
  size: 'standard' | 'half' | 'full';
}

export interface HealthConnectPermission {
  id: string;
  recordName: string;
  description: string;
  granted: boolean;
  category: 'activity' | 'vitals' | 'sleep' | 'body';
}

export interface HealthConnectState {
  isAvailable: boolean;
  isSimulated: boolean;
  lastSyncTimestamp: string;
  connectedDevices: {
    name: string;
    type: 'phone' | 'watch' | 'band' | 'scale';
    manufacturer: string;
    lastSeen: string;
    active: boolean;
  }[];
  permissions: HealthConnectPermission[];
}

export interface ZeppConnectorState {
  status: 'disconnected' | 'awaiting_config' | 'connected_oauth' | 'synced_via_health_connect';
  clientIdConfigured: boolean;
  lastSync: string | null;
  deviceName?: string;
  features: string[];
}
