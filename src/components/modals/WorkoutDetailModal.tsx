import React from 'react';
import { X, Dumbbell, Flame, Timer, Heart, MapPin, Activity, ShieldAlert } from 'lucide-react';
import { WorkoutSession } from '../../types/vital.ts';

interface WorkoutDetailModalProps {
  workout: WorkoutSession | null;
  onClose: () => void;
}

export const WorkoutDetailModal: React.FC<WorkoutDetailModalProps> = ({
  workout,
  onClose,
}) => {
  if (!workout) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in select-none">
      <div className="w-full max-w-lg bg-[#171719] border border-white/10 rounded-t-3xl sm:rounded-3xl p-5 max-h-[90vh] flex flex-col shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-[#242428] flex items-center justify-center text-white">
              <Dumbbell className="w-5 h-5 text-white/90" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">{workout.title}</h2>
              <span className="text-xs text-[#85858C]">{workout.timestamp}</span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full text-[#85858C] hover:text-white hover:bg-[#242428]"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto flex flex-col gap-3 py-3">
          {/* Key Metrics Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-center">
            <div className="p-3 rounded-2xl bg-[#121214] border border-white/5">
              <span className="text-[11px] text-[#85858C] block">Duração</span>
              <span className="text-lg font-bold text-white block mt-0.5">
                {Math.floor(workout.durationMinutes / 60) > 0 ? `${Math.floor(workout.durationMinutes / 60)}h ` : ''}
                {workout.durationMinutes % 60}m
              </span>
            </div>
            <div className="p-3 rounded-2xl bg-[#121214] border border-white/5">
              <span className="text-[11px] text-[#85858C] block">Calorias</span>
              <span className="text-lg font-bold text-white block mt-0.5">
                {workout.calories} <span className="text-[10px] text-[#85858C]">kcal</span>
              </span>
            </div>
            <div className="p-3 rounded-2xl bg-[#121214] border border-white/5">
              <span className="text-[11px] text-[#85858C] block">FC Média</span>
              <span className="text-lg font-bold text-white block mt-0.5">
                {workout.avgHr} <span className="text-[10px] text-[#85858C]">bpm</span>
              </span>
            </div>
            <div className="p-3 rounded-2xl bg-[#121214] border border-white/5">
              <span className="text-[11px] text-[#85858C] block">FC Máxima</span>
              <span className="text-lg font-bold text-white block mt-0.5">
                {workout.maxHr} <span className="text-[10px] text-[#85858C]">bpm</span>
              </span>
            </div>
          </div>

          {/* Heart Rate Zones Breakdown (if present) */}
          {workout.zones && workout.zones.length > 0 && (
            <div className="p-4 rounded-2xl bg-[#121214] border border-white/5">
              <span className="text-xs font-semibold text-white block mb-3">
                Distribuição das Zonas de Frequência Cardíaca
              </span>

              <div className="flex flex-col gap-2.5">
                {workout.zones.map((zone, idx) => {
                  const pct = Math.round((zone.minutes / workout.durationMinutes) * 100);
                  return (
                    <div key={idx} className="flex flex-col gap-1">
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-white font-medium">{zone.name}</span>
                        <span className="text-[#85858C]">
                          {zone.minutes} min ({pct}%) • {zone.minBpm}-{zone.maxBpm} bpm
                        </span>
                      </div>
                      <div className="h-1.5 w-full bg-[#242428] rounded-full overflow-hidden">
                        <div
                          className="h-full rounded-full transition-all"
                          style={{ width: `${pct}%`, backgroundColor: zone.color }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Source Notice */}
          <div className="p-3 rounded-2xl bg-[#242428]/40 border border-white/5 text-[11px] text-[#85858C] leading-snug">
            Fonte: <strong>{workout.source}</strong>. A frequência cardíaca apresentada é baseada nas amostras gravadas durante a sessão de treino.
          </div>
        </div>

        <div className="pt-3 border-t border-white/5 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-full bg-white text-black text-xs font-semibold hover:bg-neutral-200"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  );
};
