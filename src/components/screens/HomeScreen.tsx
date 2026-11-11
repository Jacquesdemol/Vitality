import React, { useState } from 'react';
import {
  Heart,
  Flame,
  Footprints,
  Dumbbell,
  Activity,
  MoreVertical,
  ChevronRight,
  SlidersHorizontal,
  Sparkles,
  Zap,
  TrendingUp,
  TrendingDown,
} from 'lucide-react';
import {
  UserProfile,
  HeartRateData,
  StepsData,
  CaloriesData,
  ExerciseData,
  BodyCompositionData,
  AdditionalMetrics,
  WidgetConfig,
  DailyInsight,
} from '../../types/vital.ts';
import { HeartRateMiniChart } from '../charts/HeartRateChart.tsx';
import { StepsRing } from '../charts/StepsRing.tsx';
import { HumanSilhouette } from '../common/HumanSilhouette.tsx';

interface HomeScreenProps {
  user: UserProfile;
  heartRate: HeartRateData;
  steps: StepsData;
  calories: CaloriesData;
  exercise: ExerciseData;
  body: BodyCompositionData;
  additionalMetrics: AdditionalMetrics;
  widgets: WidgetConfig[];
  insights: DailyInsight[];
  onOpenHeartRateDetail: () => void;
  onOpenExerciseTab: () => void;
  onOpenBodyDetail: () => void;
  onOpenCustomizeWidgets: () => void;
  onOpenHealthConnect: () => void;
  onOpenOptionsMenu: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  user,
  heartRate,
  steps,
  calories,
  exercise,
  body,
  additionalMetrics,
  widgets,
  insights,
  onOpenHeartRateDetail,
  onOpenExerciseTab,
  onOpenBodyDetail,
  onOpenCustomizeWidgets,
  onOpenHealthConnect,
  onOpenOptionsMenu,
}) => {
  const [isWeeklyEnergyView, setIsWeeklyEnergyView] = useState(false);
  const firstName = user.name.split(' ')[0] || 'Iury';

  // Cálculo BMR (Mifflin-St Jeor)
  const bmrKcal = Math.round(
    10 * user.weightKg + 6.25 * user.heightCm - 5 * user.age + (user.gender === 'female' ? -161 : 5)
  );
  const activeKcal = calories.activeKcal;
  const totalEnergyKcal = bmrKcal + activeKcal;
  const isTotalMode = user.goals.calorieGoalMode === 'total';
  const dailyGoalKcal = user.goals.activeCalories || 650;

  const currentBurn = isTotalMode ? totalEnergyKcal : activeKcal;
  const remainingKcal = Math.max(0, dailyGoalKcal - currentBurn);
  const diffKcal = currentBurn - dailyGoalKcal;
  const dailyFatEquivalenceGrams = Math.round((diffKcal / 7.7) * 10) / 10;

  // Semanal
  const weeklyActiveKcal = calories.weeklyHistory.reduce((acc, d) => acc + d.activeKcal, 0);
  const weeklyBasalKcal = bmrKcal * 7;
  const weeklyTotalKcal = weeklyBasalKcal + weeklyActiveKcal;
  const weeklyGoalKcal = dailyGoalKcal * 7;
  const weeklyCurrentBurn = isTotalMode ? weeklyTotalKcal : weeklyActiveKcal;
  const weeklyDiffKcal = weeklyCurrentBurn - weeklyGoalKcal;
  const weeklyFatEquivalenceGrams = Math.round((weeklyDiffKcal / 7.7) * 10) / 10;

  // Highlight insight for the banner
  const topInsight = insights[0];

  return (
    <div className="w-full pb-36 pt-2 px-4 flex flex-col gap-3.5 max-w-[420px] mx-auto select-none">
      {/* Top Header */}
      <header className="flex items-center justify-between pt-1 pb-1">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-[#F2F2F4]">
            Olá, {firstName}!
          </h1>
          <p className="text-sm text-[#85858C] font-normal flex items-center gap-1.5 mt-0.5">
            Cuide de você hoje. <span className="text-white/80">🤍</span>
          </p>
        </div>

        <div className="flex items-center gap-1">
          {/* ECG Pulse icon button */}
          <button
            onClick={onOpenHealthConnect}
            title="Status dos Sensores e Health Connect"
            className="p-2 rounded-full text-[#85858C] hover:text-[#F2F2F4] hover:bg-[#171719] transition-colors"
            aria-label="Status dos Sensores"
          >
            <Activity className="w-5 h-5" />
          </button>

          {/* 3 dots menu button */}
          <button
            onClick={onOpenOptionsMenu}
            title="Menu de Opções"
            className="p-2 rounded-full text-[#85858C] hover:text-[#F2F2F4] hover:bg-[#171719] transition-colors"
            aria-label="Opções"
          >
            <MoreVertical className="w-5 h-5" />
          </button>
        </div>
      </header>

      {/* Dynamic Daily Insight Notice (if high importance) */}
      {topInsight && topInsight.importance === 'high' && (
        <div className="rounded-2xl bg-[#171719]/90 border border-white/10 p-3.5 flex items-start gap-3">
          <div className="p-2 rounded-xl bg-[#242428] text-white">
            <Sparkles className="w-4 h-4" />
          </div>
          <div className="flex-1">
            <div className="text-[11px] font-medium tracking-wide uppercase text-[#85858C]">
              {topInsight.title}
            </div>
            <p className="text-xs text-[#F2F2F4] mt-0.5 leading-snug">
              {topInsight.message}
            </p>
          </div>
        </div>
      )}

      {/* Widgets list based on configuration order and visibility */}
      {widgets
        .filter((w) => w.enabled)
        .map((widget) => {
          switch (widget.id) {
            case 'heart_rate':
              return (
                <div
                  key="heart_rate"
                  onClick={onOpenHeartRateDetail}
                  className="rounded-3xl bg-[#171719] border border-white/5 p-4 sm:p-5 cursor-pointer hover:border-white/10 transition-all duration-200 active:scale-[0.99] group shadow-sm"
                >
                  <div className="flex items-center justify-between text-[#85858C] text-xs font-medium mb-2">
                    <div className="flex items-center gap-2">
                      <Heart className="w-4 h-4 fill-current text-white/90" />
                      <span className="text-[#F2F2F4] text-sm font-semibold tracking-tight">
                        Batimentos
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] text-[#85858C] bg-[#242428] px-2 py-0.5 rounded-full font-medium">
                        Zepp (Huami)
                      </span>
                      <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
                    </div>
                  </div>

                  <div className="flex items-end justify-between mt-2">
                    <div>
                      <div className="flex items-baseline gap-1.5">
                        <span className="text-3xl font-bold tracking-tight text-[#F2F2F4]">
                          {heartRate.latestBpm}
                        </span>
                        <span className="text-sm font-normal text-[#85858C]">bpm</span>
                      </div>
                      <div className="text-xs text-[#85858C] mt-1 font-normal">
                        {heartRate.statusText} • {heartRate.normalRange}
                      </div>
                    </div>

                    <div className="pb-1">
                      <HeartRateMiniChart
                        points={heartRate.todaySamples.map((s) => s.bpm)}
                      />
                    </div>
                  </div>
                </div>
              );

            case 'calories':
            case 'steps':
              if (widget.id === 'calories') {
                return (
                  <div key="row_calories_steps" className="grid grid-cols-2 gap-3.5">
                    {/* Calories Card */}
                    <div className="rounded-3xl bg-[#171719] border border-white/5 p-4 flex flex-col justify-between hover:border-white/10 transition-all">
                      <div>
                        <div className="flex items-center justify-between text-[#85858C] text-xs mb-2">
                          <div className="flex items-center gap-1.5">
                            <Flame className="w-3.5 h-3.5 text-white/90" />
                            <span className="text-[#85858C] font-medium text-xs">Calorias</span>
                          </div>
                          <span className="text-[9px] text-[#85858C] bg-[#242428] px-1.5 py-0.5 rounded-full font-medium">
                            Zepp
                          </span>
                        </div>
                        <div className="flex items-baseline gap-1">
                          <span className="text-2xl font-bold text-[#F2F2F4]">
                            {calories.activeKcal}
                          </span>
                          <span className="text-xs text-[#85858C]">kcal</span>
                        </div>
                      </div>

                      <div className="flex items-end gap-1 h-9 mt-3">
                        {calories.hourlyDistribution.map((h, i) => (
                          <div
                            key={i}
                            className="flex-1 bg-[#242428] rounded-xs hover:bg-[#85858C] transition-colors"
                            style={{
                              height: `${Math.max(20, Math.min(100, (h.kcal / 130) * 100))}%`,
                              backgroundColor: i === calories.hourlyDistribution.length - 1 ? '#85858C' : '#242428',
                            }}
                            title={`${h.hour}: ${h.kcal} kcal`}
                          />
                        ))}
                      </div>
                    </div>

                    {/* Steps Card */}
                    <div className="rounded-3xl bg-[#171719] border border-white/5 p-4 flex flex-col justify-between hover:border-white/10 transition-all">
                      <div className="flex items-start justify-between">
                        <div>
                          <div className="flex items-center justify-between text-[#85858C] text-xs mb-2">
                            <div className="flex items-center gap-1.5">
                              <Footprints className="w-3.5 h-3.5 text-white/90" />
                              <span className="text-[#85858C] font-medium text-xs">Passos</span>
                            </div>
                            <span className="text-[9px] text-[#85858C] bg-[#242428] px-1.5 py-0.5 rounded-full font-medium">
                              Zepp
                            </span>
                          </div>
                          <div className="text-2xl font-bold text-[#F2F2F4]">
                            {steps.current.toLocaleString('pt-BR')}
                          </div>
                          <div className="text-[11px] text-[#85858C] mt-0.5">
                            Meta: {steps.goal.toLocaleString('pt-BR')}
                          </div>
                        </div>

                        <div className="pt-1">
                          <StepsRing current={steps.current} goal={steps.goal} size={54} strokeWidth={4} />
                        </div>
                      </div>
                    </div>

                    {/* Widget Balanço Energético & Queima Calórica */}
                    <div className="col-span-2 rounded-3xl bg-[#171719] border border-white/5 p-4.5 sm:p-5 flex flex-col gap-3.5 shadow-sm">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <div className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center text-white">
                            <Zap className="w-4 h-4 text-white" />
                          </div>
                          <div>
                            <span className="text-sm font-bold text-[#F2F2F4] block tracking-tight">
                              Balanço Energético
                            </span>
                            <span className="text-[11px] text-[#85858C]">
                              {isTotalMode ? 'Meta: Gasto Total Diário' : 'Meta: Calorias Ativas'}
                            </span>
                          </div>
                        </div>

                        {/* Toggle Hoje / Semana */}
                        <div className="flex rounded-xl bg-[#242428] p-0.5 border border-white/5 text-[11px]">
                          <button
                            type="button"
                            onClick={() => setIsWeeklyEnergyView(false)}
                            className={`px-2.5 py-1 rounded-lg font-semibold transition-all ${
                              !isWeeklyEnergyView ? 'bg-[#171719] text-white shadow-xs' : 'text-[#85858C] hover:text-white'
                            }`}
                          >
                            Hoje
                          </button>
                          <button
                            type="button"
                            onClick={() => setIsWeeklyEnergyView(true)}
                            className={`px-2.5 py-1 rounded-lg font-semibold transition-all ${
                              isWeeklyEnergyView ? 'bg-[#171719] text-white shadow-xs' : 'text-[#85858C] hover:text-white'
                            }`}
                          >
                            Semana
                          </button>
                        </div>
                      </div>

                      {/* 3 blocos de métricas */}
                      <div className="grid grid-cols-3 gap-2 pt-1 border-t border-white/5">
                        <div>
                          <span className="text-[10px] text-[#85858C] block uppercase font-medium">Basal (BMR)</span>
                          <span className="text-base font-bold text-[#F2F2F4] block mt-0.5">
                            {!isWeeklyEnergyView ? bmrKcal : weeklyBasalKcal}
                          </span>
                          <span className="text-[9px] text-[#85858C] block">Mifflin-St Jeor</span>
                        </div>

                        <div className="text-center">
                          <span className="text-[10px] text-[#85858C] block uppercase font-medium">Ativas</span>
                          <span className="text-base font-bold text-white block mt-0.5">
                            {!isWeeklyEnergyView ? activeKcal : weeklyActiveKcal}
                          </span>
                          <span className="text-[9px] text-[#85858C] block">Zepp (Huami)</span>
                        </div>

                        <div className="text-right">
                          <span className="text-[10px] text-[#85858C] block uppercase font-medium">Gasto Total</span>
                          <span className="text-base font-bold text-[#F2F2F4] block mt-0.5">
                            {!isWeeklyEnergyView ? totalEnergyKcal : weeklyTotalKcal} <span className="text-[10px] font-normal text-[#85858C]">kcal</span>
                          </span>
                          <span className="text-[9px] text-[#85858C] block">Basal + Ativas</span>
                        </div>
                      </div>

                      {/* Barra de progresso para a meta */}
                      <div className="flex flex-col gap-1.5 pt-1">
                        <div className="flex items-center justify-between text-[11px]">
                          <span className="text-[#85858C]">
                            {(!isWeeklyEnergyView ? currentBurn : weeklyCurrentBurn) >= (!isWeeklyEnergyView ? dailyGoalKcal : weeklyGoalKcal)
                              ? 'Meta de queima cumprida!'
                              : `Faltam ${Math.round((!isWeeklyEnergyView ? dailyGoalKcal : weeklyGoalKcal) - (!isWeeklyEnergyView ? currentBurn : weeklyCurrentBurn))} kcal`}
                          </span>
                          <span className="text-[#F2F2F4] font-semibold">
                            Meta: {!isWeeklyEnergyView ? dailyGoalKcal : weeklyGoalKcal} kcal
                          </span>
                        </div>
                        <div className="w-full h-2 rounded-full bg-[#242428] overflow-hidden">
                          <div
                            className="h-full bg-white rounded-full transition-all"
                            style={{
                              width: `${Math.min(
                                100,
                                Math.max(
                                  5,
                                  ((!isWeeklyEnergyView ? currentBurn : weeklyCurrentBurn) /
                                    Math.max(1, !isWeeklyEnergyView ? dailyGoalKcal : weeklyGoalKcal)) *
                                    100
                                )
                              )}%`,
                            }}
                          />
                        </div>
                      </div>

                      {/* Equivalência em gordura corporal */}
                      {(() => {
                        const fatGrams = !isWeeklyEnergyView ? dailyFatEquivalenceGrams : weeklyFatEquivalenceGrams;
                        const isSurplus = fatGrams >= 0;
                        const absGrams = Math.abs(fatGrams);
                        return (
                          <div className="rounded-2xl bg-[#242428]/60 p-3 flex flex-col gap-1 text-[11px] border border-white/5">
                            <div className="flex items-center gap-1.5 font-semibold text-[#F2F2F4]">
                              {isSurplus ? (
                                <TrendingUp className="w-3.5 h-3.5 text-white" />
                              ) : (
                                <TrendingDown className="w-3.5 h-3.5 text-amber-400" />
                              )}
                              <span>
                                {isSurplus ? 'Superávit' : 'Déficit'} energético equivalente a ~{absGrams}g de gordura
                              </span>
                            </div>
                            <p className="text-[10px] text-[#85858C] leading-snug">
                              Aproximação matemática baseada em 7.700 kcal por kg de tecido adiposo. Não é uma previsão de alteração direta de peso na balança.
                            </p>
                          </div>
                        );
                      })()}
                    </div>
                  </div>
                );
              }
              return null;

            case 'weekly_exercise':
              return (
                <div
                  key="weekly_exercise"
                  onClick={onOpenExerciseTab}
                  className="rounded-3xl bg-[#171719] border border-white/5 p-4 sm:p-5 cursor-pointer hover:border-white/10 transition-all duration-200 active:scale-[0.99] group shadow-sm"
                >
                  <div className="flex items-center justify-between text-[#85858C] text-xs font-medium mb-3">
                    <div className="flex items-center gap-2">
                      <Dumbbell className="w-4 h-4 text-white/90" />
                      <span className="text-[#F2F2F4] text-sm font-semibold tracking-tight">
                        Exercícios da semana
                      </span>
                    </div>
                    <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
                  </div>

                  <div className="flex items-end justify-between">
                    <div className="flex items-baseline gap-1">
                      <span className="text-2xl font-bold text-[#F2F2F4]">
                        {exercise.weeklyWorkoutsCount}
                      </span>
                      <span className="text-sm font-normal text-[#85858C]">
                        / {exercise.weeklyTargetWorkouts} dias
                      </span>
                    </div>

                    {/* Mini weekly bars */}
                    <div className="flex items-end gap-1.5 h-7 pb-1">
                      {[1, 2, 3, 4, 5].map((day) => {
                        const completed = day <= exercise.weeklyWorkoutsCount;
                        return (
                          <div
                            key={day}
                            className={`w-2 rounded-full transition-all ${
                              completed ? 'bg-white h-6' : 'bg-[#242428] h-3'
                            }`}
                          />
                        );
                      })}
                    </div>
                  </div>
                </div>
              );

            case 'body_summary':
              return (
                <div
                  key="body_summary"
                  onClick={onOpenBodyDetail}
                  className="rounded-3xl bg-[#171719] border border-white/5 p-4 sm:p-5 cursor-pointer hover:border-white/10 transition-all duration-200 active:scale-[0.99] group shadow-sm"
                >
                  <div className="flex items-center justify-between text-[#85858C] text-xs font-medium mb-3">
                    <span className="text-[#F2F2F4] text-sm font-semibold tracking-tight">
                      Seu corpo em resumo
                    </span>
                    <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
                  </div>

                  <div className="flex items-center justify-between">
                    {/* Left: Human silhouette figure */}
                    <div className="w-14 h-24 flex items-center justify-center">
                      <HumanSilhouette className="w-14 h-24" />
                    </div>

                    {/* Middle metrics: Peso, IMC, Gordura corporal */}
                    <div className="flex flex-col gap-1.5 pl-2">
                      <div className="flex items-baseline justify-between gap-4">
                        <span className="text-xs text-[#85858C]">Peso</span>
                        <span className="text-xs font-semibold text-[#F2F2F4]">
                          {body.weightKg.toString().replace('.', ',')} kg
                        </span>
                      </div>
                      <div className="flex items-baseline justify-between gap-4">
                        <span className="text-xs text-[#85858C]">IMC</span>
                        <span className="text-xs font-semibold text-[#F2F2F4]">
                          {body.bmi.toString().replace('.', ',')}
                        </span>
                      </div>
                      <div className="flex items-baseline justify-between gap-4">
                        <span className="text-xs text-[#85858C] leading-tight">Gordura corporal</span>
                        <span className="text-xs font-semibold text-[#F2F2F4]">
                          {body.bodyFatPercent ? `${body.bodyFatPercent.toString().replace('.', ',')}%` : '—'}
                        </span>
                      </div>
                    </div>

                    {/* Right: Trend line */}
                    <div className="w-24 h-12 flex items-center justify-end">
                      <svg width="84" height="40" className="overflow-visible">
                        <path
                          d="M 4 28 L 22 22 L 42 24 L 62 16 L 80 12"
                          fill="none"
                          stroke="#F2F2F4"
                          strokeWidth="1.8"
                          strokeLinecap="round"
                        />
                        <circle cx="4" cy="28" r="2.5" fill="#85858C" />
                        <circle cx="22" cy="22" r="2.5" fill="#85858C" />
                        <circle cx="42" cy="24" r="2.5" fill="#85858C" />
                        <circle cx="62" cy="16" r="2.5" fill="#85858C" />
                        <circle cx="80" cy="12" r="3" fill="#FFFFFF" />
                      </svg>
                    </div>
                  </div>
                </div>
              );

            case 'additional_vitals':
              return (
                <div
                  key="additional_vitals"
                  className="rounded-3xl bg-[#171719] border border-white/5 p-4"
                >
                  <div className="text-xs text-[#85858C] font-semibold mb-2.5">
                    Outros Indicadores Registrados
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <div className="p-2.5 rounded-2xl bg-[#242428]/60">
                      <span className="text-[#85858C] block text-[11px]">SpO2 (Oxigênio)</span>
                      <span className="text-white font-semibold text-sm">
                        {additionalMetrics.spO2Percent}%
                      </span>
                    </div>
                    <div className="p-2.5 rounded-2xl bg-[#242428]/60">
                      <span className="text-[#85858C] block text-[11px]">Respiração</span>
                      <span className="text-white font-semibold text-sm">
                        {additionalMetrics.respiratoryRateRpm} rpm
                      </span>
                    </div>
                    <div className="p-2.5 rounded-2xl bg-[#242428]/60">
                      <span className="text-[#85858C] block text-[11px]">Variabilidade (HRV)</span>
                      <span className="text-white font-semibold text-sm">
                        {additionalMetrics.hrvRmssdMs} ms
                      </span>
                    </div>
                    <div className="p-2.5 rounded-2xl bg-[#242428]/60">
                      <span className="text-[#85858C] block text-[11px]">Pressão Arterial</span>
                      <span className="text-white font-semibold text-sm">
                        {additionalMetrics.bloodPressureSysMmHg}/{additionalMetrics.bloodPressureDiaMmHg} mmHg
                      </span>
                    </div>
                  </div>
                </div>
              );

            default:
              return null;
          }
        })}

      {/* Button to customize dashboard layout */}
      <div className="pt-2 flex justify-center">
        <button
          onClick={onOpenCustomizeWidgets}
          className="flex items-center gap-2 px-4 py-2 rounded-full bg-[#171719] border border-[#242428] text-xs text-[#85858C] hover:text-white hover:border-[#85858C] transition-all"
        >
          <SlidersHorizontal className="w-3.5 h-3.5" />
          <span>Personalizar Widgets</span>
        </button>
      </div>
    </div>
  );
};
