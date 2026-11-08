import React, { useState } from 'react';
import {
  Moon,
  TrendingUp,
  Lightbulb,
  MoreVertical,
  Check,
  Info,
  Clock,
  Heart,
  Calendar,
} from 'lucide-react';
import { SleepData } from '../../types/vital.ts';
import { SleepHypnogram } from '../charts/SleepHypnogram.tsx';
import { SleepTrendChart } from '../charts/SleepTrendChart.tsx';

interface SleepScreenProps {
  sleep: SleepData;
  onOpenOptionsMenu: () => void;
}

export const SleepScreen: React.FC<SleepScreenProps> = ({ sleep, onOpenOptionsMenu }) => {
  const [showFactorsModal, setShowFactorsModal] = useState(false);

  const hours = Math.floor(sleep.totalMinutes / 60);
  const minutes = sleep.totalMinutes % 60;

  return (
    <div className="w-full pb-36 pt-2 px-4 flex flex-col gap-3.5 max-w-[420px] mx-auto select-none">
      {/* Top Header */}
      <header className="flex items-center justify-between pt-1 pb-1">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-[#171719] border border-white/5 flex items-center justify-center text-white">
            <Moon className="w-6 h-6 fill-current text-white/90" />
          </div>
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-[#F2F2F4]">
              Sono
            </h1>
            <p className="text-sm text-[#85858C] font-normal">
              Durma bem. Viva melhor.
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

      {/* Main Card: Seu sono na noite passada */}
      <div className="rounded-3xl bg-[#171719] border border-white/5 p-4 sm:p-5 shadow-sm">
        <div className="flex items-start justify-between">
          <div>
            <span className="text-xs text-[#85858C] font-medium block">
              Seu sono na noite passada
            </span>
            <div className="flex items-baseline gap-1.5 mt-1">
              <span className="text-3xl font-bold text-[#F2F2F4] tracking-tight">
                {hours}
              </span>
              <span className="text-base text-[#85858C] font-normal mr-1">h</span>
              <span className="text-3xl font-bold text-[#F2F2F4] tracking-tight">
                {minutes}
              </span>
              <span className="text-base text-[#85858C] font-normal">min</span>
            </div>
            <div className="text-xs text-[#85858C] mt-0.5">
              Meta: {Math.floor(sleep.targetMinutesMin / 60)}–{Math.floor(sleep.targetMinutesMax / 60)} h
            </div>
          </div>

          {/* Badge: ✓ Bom */}
          <div className="flex items-center gap-1 px-3 py-1 rounded-full bg-[#242428] border border-white/10 text-xs font-medium text-white shadow-inner">
            <Check className="w-3.5 h-3.5 text-white stroke-[2.5]" />
            <span>{sleep.qualityBadge}</span>
          </div>
        </div>

        {/* Schedule chips */}
        <div className="flex items-center gap-4 text-xs text-[#85858C] mt-3 pt-3 border-t border-white/5">
          <div className="flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5" />
            <span>Dormiu: <strong className="text-white font-medium">{sleep.sleepTime}</strong></span>
          </div>
          <div className="flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5" />
            <span>Acordou: <strong className="text-white font-medium">{sleep.wakeTime}</strong></span>
          </div>
        </div>

        {/* Sleep Hypnogram Stages Chart */}
        <div className="mt-4 pt-1">
          <SleepHypnogram timeline={sleep.stagesTimeline} stages={sleep.stages} />
        </div>
      </div>

      {/* Card: Qualidade do sono */}
      <div className="rounded-3xl bg-[#171719] border border-white/5 p-4 sm:p-5 flex items-center justify-between shadow-sm">
        <div>
          <div className="flex items-center gap-1.5 text-xs text-[#85858C] font-medium mb-1">
            <TrendingUp className="w-4 h-4 text-white/90" />
            <span>Qualidade do sono</span>
          </div>

          <div className="flex items-baseline gap-2 mt-1">
            <span className="text-2xl font-bold text-[#F2F2F4]">
              {sleep.qualityScorePct}%
            </span>
            <span className="text-xs font-semibold text-emerald-400 flex items-center">
              ▲ +{sleep.qualityDeltaPct}%
            </span>
          </div>
          <span className="text-[11px] text-[#85858C] block mt-0.5">
            Média dos últimos 7 dias
          </span>
        </div>

        <div>
          <SleepTrendChart history={sleep.sevenDaysHistory} />
        </div>
      </div>

      {/* Card: Dica de hoje */}
      <div className="rounded-3xl bg-[#171719] border border-white/5 p-4 sm:p-5 flex items-start gap-3.5 shadow-sm">
        <div className="p-2.5 rounded-full bg-[#242428] text-white flex-shrink-0 mt-0.5">
          <Lightbulb className="w-5 h-5" />
        </div>

        <div className="flex-1">
          <span className="text-xs text-[#85858C] font-semibold tracking-wide uppercase block">
            Dica de hoje
          </span>
          <p className="text-xs sm:text-sm text-[#F2F2F4] font-normal leading-relaxed mt-0.5">
            {sleep.todayInsight}
          </p>
        </div>
      </div>

      {/* Sleep Factors Breakdown button / disclosure */}
      <button
        onClick={() => setShowFactorsModal(true)}
        className="w-full py-3 px-4 rounded-2xl bg-[#171719]/80 border border-white/5 text-xs text-[#85858C] hover:text-white flex items-center justify-between transition-colors"
      >
        <span className="flex items-center gap-2">
          <Info className="w-3.5 h-3.5" />
          Fatores da Pontuação do Sono (Transparência)
        </span>
        <span className="text-white font-medium">Ver detalhes ›</span>
      </button>

      {/* Modal / Sheet for Sleep Factors */}
      {showFactorsModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in">
          <div className="w-full max-w-md bg-[#171719] border border-white/10 rounded-t-3xl sm:rounded-3xl p-5 max-h-[85vh] overflow-y-auto">
            <div className="flex items-center justify-between pb-3 border-b border-white/5">
              <h3 className="text-base font-bold text-white">Fatores do Cálculo de Sono</h3>
              <button
                onClick={() => setShowFactorsModal(false)}
                className="text-xs text-[#85858C] hover:text-white px-2 py-1 rounded-lg bg-[#242428]"
              >
                Fechar
              </button>
            </div>

            <p className="text-xs text-[#85858C] mt-2 leading-relaxed">
              Em conformidade com as diretrizes do VITAL, este índice não constitui diagnóstico médico. Ele resume a consistência e arquitetura do sono registrado pelo Health Connect.
            </p>

            <div className="flex flex-col gap-2.5 mt-4">
              {sleep.factors.map((factor, i) => (
                <div key={i} className="p-3 rounded-2xl bg-[#242428]/60 border border-white/5">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-semibold text-white">{factor.name}</span>
                    <span className="text-xs text-[#85858C]">Peso: {factor.weight}</span>
                  </div>
                  <p className="text-[11px] text-[#85858C] mt-1 leading-snug">
                    {factor.description}
                  </p>
                  <div className="mt-2 w-full bg-[#171719] h-1.5 rounded-full overflow-hidden">
                    <div
                      className="bg-white h-full rounded-full"
                      style={{ width: `${factor.score}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-4 p-3 rounded-2xl bg-[#121214] border border-white/5 text-xs text-[#85858C]">
              <div className="flex items-center gap-1.5 text-white font-medium mb-1">
                <Heart className="w-3.5 h-3.5 text-white/80" />
                <span>Batimentos durante o repouso</span>
              </div>
              <span>Média de {sleep.avgSleepingHr} bpm ao longo da noite, indicando recuperação física estável.</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
