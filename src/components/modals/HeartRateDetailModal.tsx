import React from 'react';
import { X, Heart, Activity, Info, Calendar } from 'lucide-react';
import { HeartRateData } from '../../types/vital.ts';

interface HeartRateDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  data: HeartRateData;
}

export const HeartRateDetailModal: React.FC<HeartRateDetailModalProps> = ({
  isOpen,
  onClose,
  data,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in select-none">
      <div className="w-full max-w-lg bg-[#171719] border border-white/10 rounded-t-3xl sm:rounded-3xl p-5 max-h-[90vh] flex flex-col shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/5">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-[#242428] flex items-center justify-center text-white">
              <Heart className="w-5 h-5 fill-current text-white/90" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">Frequência Cardíaca</h2>
              <span className="text-xs text-[#85858C]">{data.source}</span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full text-[#85858C] hover:text-white hover:bg-[#242428]"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto flex flex-col gap-3 py-2">
          {/* Key Stat Cards */}
          <div className="grid grid-cols-3 gap-2 text-center">
            <div className="p-3 rounded-2xl bg-[#121214] border border-white/5">
              <span className="text-[11px] text-[#85858C] block">Última Leitura</span>
              <span className="text-xl font-bold text-white mt-0.5 block">{data.latestBpm}</span>
              <span className="text-[10px] text-[#85858C]">bpm</span>
            </div>
            <div className="p-3 rounded-2xl bg-[#121214] border border-white/5">
              <span className="text-[11px] text-[#85858C] block">Em Repouso</span>
              <span className="text-xl font-bold text-white mt-0.5 block">{data.restingBpm}</span>
              <span className="text-[10px] text-[#85858C]">bpm</span>
            </div>
            <div className="p-3 rounded-2xl bg-[#121214] border border-white/5">
              <span className="text-[11px] text-[#85858C] block">Média Diária</span>
              <span className="text-xl font-bold text-white mt-0.5 block">{data.dailyAvgBpm}</span>
              <span className="text-[10px] text-[#85858C]">bpm</span>
            </div>
          </div>

          {/* Range */}
          <div className="p-3.5 rounded-2xl bg-[#242428]/40 border border-white/5 flex items-center justify-between text-xs">
            <div>
              <span className="text-[#85858C] block">Variação do Dia</span>
              <span className="text-white font-semibold">
                Mínima: {data.minBpm} bpm • Máxima: {data.maxBpm} bpm
              </span>
            </div>
            <span className="px-2 py-1 rounded-full bg-[#171719] text-xs text-white border border-white/10">
              Faixa Normal
            </span>
          </div>

          {/* 24h Timeline Chart */}
          <div className="p-4 rounded-2xl bg-[#121214] border border-white/5">
            <div className="flex items-center justify-between mb-3 text-xs">
              <span className="text-white font-semibold">Evolução ao Longo do Dia</span>
              <span className="text-[#85858C] text-[11px]">{data.lastUpdated}</span>
            </div>

            <div className="h-28 flex items-end gap-1.5 pt-2">
              {data.todaySamples.map((sample, idx) => {
                const heightPct = Math.max(15, Math.min(100, ((sample.bpm - 40) / 90) * 100));
                return (
                  <div key={idx} className="flex-1 flex flex-col items-center gap-1">
                    <div
                      className="w-full bg-[#323238] hover:bg-white rounded-t-sm transition-colors"
                      style={{ height: `${heightPct}%` }}
                      title={`${sample.time}: ${sample.bpm} bpm`}
                    />
                    <span className="text-[9px] text-[#85858C]">{sample.time.split(':')[0]}h</span>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Weekly Averages */}
          <div className="p-4 rounded-2xl bg-[#121214] border border-white/5">
            <span className="text-xs font-semibold text-white block mb-2">
              Histórico Semanal (Média e Repouso)
            </span>
            <div className="grid grid-cols-7 gap-1 text-center">
              {data.weeklyAverages.map((day, idx) => (
                <div key={idx} className="p-2 rounded-xl bg-[#242428]/40">
                  <span className="text-[10px] text-[#85858C] block">{day.day}</span>
                  <span className="text-xs font-bold text-white block mt-0.5">{day.avg}</span>
                  <span className="text-[9px] text-[#85858C] block">r: {day.resting}</span>
                </div>
              ))}
            </div>
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
