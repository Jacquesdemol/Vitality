import React, { useState } from 'react';
import {
  Dumbbell,
  Flame,
  MapPin,
  MoreVertical,
  ChevronRight,
  TrendingUp,
  Activity,
  Plus,
  Heart,
  Timer,
  Award,
} from 'lucide-react';
import { ExerciseData, WorkoutSession, ExerciseCategory } from '../../types/vital.ts';
import { ActivityBarChart } from '../charts/ActivityBarChart.tsx';

interface ExerciseScreenProps {
  exercise: ExerciseData;
  onOpenOptionsMenu: () => void;
  onSelectWorkout: (workout: WorkoutSession) => void;
}

export const ExerciseScreen: React.FC<ExerciseScreenProps> = ({
  exercise,
  onOpenOptionsMenu,
  onSelectWorkout,
}) => {
  const [selectedCategory, setSelectedCategory] = useState<string>('all');

  const hours = Math.floor(exercise.weeklyDurationMinutes / 60);
  const minutes = exercise.weeklyDurationMinutes % 60;

  const categories = [
    { id: 'all', label: 'Todos' },
    { id: 'strength', label: 'Musculação' },
    { id: 'walking', label: 'Caminhada' },
    { id: 'running', label: 'Corrida' },
    { id: 'cycling', label: 'Ciclismo' },
    { id: 'functional', label: 'Funcional' },
  ];

  const filteredWorkouts =
    selectedCategory === 'all'
      ? exercise.recentWorkouts
      : exercise.recentWorkouts.filter((w) => w.category === selectedCategory);

  return (
    <div className="w-full pb-36 pt-2 px-4 flex flex-col gap-3.5 max-w-[420px] mx-auto select-none">
      {/* Top Header */}
      <header className="flex items-center justify-between pt-1 pb-1">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-[#171719] border border-white/5 flex items-center justify-center text-white">
            <Dumbbell className="w-5 h-5 text-white/90" />
          </div>
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-[#F2F2F4]">
              Exercícios
            </h1>
            <p className="text-sm text-[#85858C] font-normal">
              Movimente-se. Seu corpo agradece!
            </p>
          </div>
        </div>

        <button
          onClick={onOpenOptionsMenu}
          className="p-2 rounded-full text-[#85858C] hover:text-[#F2F2F4] hover:bg-[#171719] transition-colors"
          aria-label="Opções"
        >
          <MoreVertical className="w-5 h-5" />
        </button>
      </header>

      {/* Main Card: Atividade da semana */}
      <div className="rounded-3xl bg-[#171719] border border-white/5 p-4 sm:p-5 shadow-sm">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3.5">
            {/* Running icon circle */}
            <div className="w-10 h-10 rounded-full bg-[#242428] flex items-center justify-center text-white">
              <Activity className="w-5 h-5 text-white" />
            </div>

            <div>
              <span className="text-xs text-[#85858C] font-medium block">
                Atividade da semana
              </span>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-2xl font-bold text-[#F2F2F4]">
                  {hours} h {minutes} min
                </span>
                <span className="text-xs font-semibold text-emerald-400 flex items-center">
                  ▲ +{exercise.weeklyComparisonPct}%
                </span>
              </div>
            </div>
          </div>

          <div>
            <ActivityBarChart data={exercise.dailyMinutes} />
          </div>
        </div>
      </div>

      {/* 2-Column Row: Calorias ativas & Distância */}
      <div className="grid grid-cols-2 gap-3.5">
        {/* Calorias ativas */}
        <div className="rounded-3xl bg-[#171719] border border-white/5 p-4 flex flex-col justify-between shadow-sm">
          <div className="flex items-center gap-1.5 text-[#85858C] text-xs">
            <Flame className="w-3.5 h-3.5 text-white/90" />
            <span className="text-[#85858C] font-medium text-xs">Calorias ativas</span>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-[#F2F2F4]">
              {exercise.activeCaloriesWeekly.toLocaleString('pt-BR')} <span className="text-xs font-normal text-[#85858C]">kcal</span>
            </div>
            <div className="text-xs font-medium text-emerald-400 flex items-center gap-0.5 mt-1">
              <span>▲</span> +{exercise.activeCaloriesDeltaPct}%
            </div>
          </div>
        </div>

        {/* Distância */}
        <div className="rounded-3xl bg-[#171719] border border-white/5 p-4 flex flex-col justify-between shadow-sm">
          <div className="flex items-center gap-1.5 text-[#85858C] text-xs">
            <MapPin className="w-3.5 h-3.5 text-white/90" />
            <span className="text-[#85858C] font-medium text-xs">Distância</span>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-[#F2F2F4]">
              {exercise.totalDistanceWeeklyKm.toString().replace('.', ',')} <span className="text-xs font-normal text-[#85858C]">km</span>
            </div>
            <div className="text-xs font-medium text-emerald-400 flex items-center gap-0.5 mt-1">
              <span>▲</span> +{exercise.distanceDeltaPct}%
            </div>
          </div>
        </div>
      </div>

      {/* Activity Filter Chips */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 pt-1 no-scrollbar">
        {categories.map((cat) => (
          <button
            key={cat.id}
            onClick={() => setSelectedCategory(cat.id)}
            className={`px-3 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition-all ${
              selectedCategory === cat.id
                ? 'bg-white text-black font-semibold'
                : 'bg-[#171719] text-[#85858C] border border-[#242428] hover:text-white'
            }`}
          >
            {cat.label}
          </button>
        ))}
      </div>

      {/* Suas atividades Section */}
      <div className="flex flex-col gap-2.5">
        <div className="flex items-center justify-between px-1">
          <h2 className="text-sm font-semibold text-[#F2F2F4]">Suas atividades</h2>
          <button className="text-xs text-[#85858C] hover:text-white flex items-center gap-0.5 transition-colors">
            Ver todas <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {filteredWorkouts.map((workout) => (
          <div
            key={workout.id}
            onClick={() => onSelectWorkout(workout)}
            className="rounded-3xl bg-[#171719] border border-white/5 p-4 flex items-center justify-between cursor-pointer hover:border-white/10 active:scale-[0.99] transition-all group shadow-sm"
          >
            <div className="flex items-center gap-3.5">
              <div className="w-11 h-11 rounded-full bg-[#242428] flex items-center justify-center text-white flex-shrink-0">
                <Dumbbell className="w-5 h-5 text-white/90" />
              </div>

              <div>
                <h3 className="text-sm font-bold text-[#F2F2F4]">
                  {workout.title}
                </h3>
                <p className="text-xs text-[#85858C] mt-0.5">
                  {workout.dateStr} • {Math.floor(workout.durationMinutes / 60) > 0 ? `${Math.floor(workout.durationMinutes / 60)} h ` : ''}{workout.durationMinutes % 60} min • {workout.calories} kcal
                  {workout.distanceKm ? ` • ${workout.distanceKm} km` : ''}
                </p>
              </div>
            </div>

            <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
          </div>
        ))}
      </div>

      {/* Health Connect Notice */}
      <div className="p-3 rounded-2xl bg-[#121214] border border-white/5 text-[11px] text-[#85858C] flex items-start gap-2">
        <Activity className="w-4 h-4 text-[#85858C] flex-shrink-0 mt-0.5" />
        <span>
          Registros consultados via <strong>ExerciseSessionRecord</strong> do Health Connect. Valores de frequência cardíaca refletem médias e picos salvos no dispositivo.
        </span>
      </div>
    </div>
  );
};
