import React, { useState } from 'react';
import { SleepStageTimeline } from '../../types/vital.ts';

interface SleepHypnogramProps {
  timeline: SleepStageTimeline[];
  stages: {
    awakeMinutes: number;
    remMinutes: number;
    lightMinutes: number;
    deepMinutes: number;
  };
}

export const SleepHypnogram: React.FC<SleepHypnogramProps> = ({ timeline, stages }) => {
  const [activeStage, setActiveStage] = useState<SleepStageTimeline | null>(null);

  const getStageColor = (stage: SleepStageTimeline['stage']) => {
    switch (stage) {
      case 'awake':
        return '#85858C'; // Acordado: cinza
      case 'rem':
        return '#9FA2B4'; // REM: cinza azulado
      case 'light':
        return '#5C5E69'; // Leve: cinza intermediário
      case 'deep':
        return '#383A42'; // Profundo: cinza escuro denso
    }
  };

  const getStageHeight = (stage: SleepStageTimeline['stage']) => {
    switch (stage) {
      case 'awake':
        return '65%';
      case 'rem':
        return '50%';
      case 'light':
        return '80%';
      case 'deep':
        return '98%';
    }
  };

  const getStageLabel = (stage: SleepStageTimeline['stage']) => {
    switch (stage) {
      case 'awake':
        return 'Acordado';
      case 'rem':
        return 'Sono REM';
      case 'light':
        return 'Sono Leve';
      case 'deep':
        return 'Sono Profundo';
    }
  };

  return (
    <div className="w-full select-none">
      {/* Interactive Tooltip on hover/touch */}
      <div className="h-5 flex items-center justify-between text-[11px] text-[#85858C] mb-1 px-1">
        {activeStage ? (
          <span className="text-white font-medium">
            {getStageLabel(activeStage.stage)}: {activeStage.startHourMin} – {activeStage.endHourMin} ({activeStage.durationMinutes} min)
          </span>
        ) : (
          <span>23:42</span>
        )}
        {!activeStage && <span>07:30</span>}
      </div>

      {/* The Hypnogram Bars Canvas Container */}
      <div className="h-16 w-full flex items-end gap-[3px] bg-[#121214] p-1.5 rounded-xl border border-white/5">
        {timeline.map((seg, idx) => {
          const height = getStageHeight(seg.stage);
          const color = getStageColor(seg.stage);
          const isHovered = activeStage === seg;

          return (
            <div
              key={idx}
              onMouseEnter={() => setActiveStage(seg)}
              onMouseLeave={() => setActiveStage(null)}
              onClick={() => setActiveStage(seg)}
              className="flex-1 rounded-sm transition-all duration-200 cursor-pointer hover:opacity-100"
              style={{
                height: height,
                backgroundColor: color,
                opacity: activeStage && !isHovered ? 0.4 : 0.85,
                transform: isHovered ? 'scaleY(1.05)' : 'none',
              }}
              title={`${getStageLabel(seg.stage)} (${seg.durationMinutes} min)`}
            />
          );
        })}
      </div>

      {/* Legend matching screenshot 2 */}
      <div className="grid grid-cols-4 gap-1 mt-4 text-[11px]">
        {/* Acordado */}
        <div className="flex flex-col items-center sm:items-start">
          <div className="flex items-center gap-1.5 text-[#85858C]">
            <span className="w-2 h-2 rounded-full bg-[#85858C]" />
            <span>Acordado</span>
          </div>
          <span className="text-white font-medium mt-0.5">{stages.awakeMinutes} min</span>
        </div>

        {/* REM */}
        <div className="flex flex-col items-center sm:items-start">
          <div className="flex items-center gap-1.5 text-[#85858C]">
            <span className="w-2 h-2 rounded-full bg-[#9FA2B4]" />
            <span>REM</span>
          </div>
          <span className="text-white font-medium mt-0.5">
            {Math.floor(stages.remMinutes / 60)} h {stages.remMinutes % 60} min
          </span>
        </div>

        {/* Leve */}
        <div className="flex flex-col items-center sm:items-start">
          <div className="flex items-center gap-1.5 text-[#85858C]">
            <span className="w-2 h-2 rounded-full bg-[#5C5E69]" />
            <span>Leve</span>
          </div>
          <span className="text-white font-medium mt-0.5">
            {Math.floor(stages.lightMinutes / 60)} h {stages.lightMinutes % 60} min
          </span>
        </div>

        {/* Profundo */}
        <div className="flex flex-col items-center sm:items-start">
          <div className="flex items-center gap-1.5 text-[#85858C]">
            <span className="w-2 h-2 rounded-full bg-[#383A42] border border-white/20" />
            <span>Profundo</span>
          </div>
          <span className="text-white font-medium mt-0.5">
            {Math.floor(stages.deepMinutes / 60)} h {stages.deepMinutes % 60} min
          </span>
        </div>
      </div>
    </div>
  );
};
