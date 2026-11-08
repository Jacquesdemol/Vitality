import React from 'react';

interface ActivityBarChartProps {
  data: { day: string; minutes: number; active: boolean }[];
}

export const ActivityBarChart: React.FC<ActivityBarChartProps> = ({ data }) => {
  const maxMinutes = Math.max(...data.map((d) => d.minutes), 80);

  return (
    <div className="flex items-end gap-1.5 h-12 select-none">
      {data.map((item, idx) => {
        const heightPct = Math.max(15, Math.round((item.minutes / maxMinutes) * 100));
        const isToday = idx === data.length - 1;

        return (
          <div key={idx} className="flex flex-col items-center gap-1">
            <div
              className={`w-2.5 rounded-full transition-all duration-500 ${
                item.active || item.minutes > 0
                  ? isToday
                    ? 'bg-white shadow-[0_0_8px_rgba(255,255,255,0.4)]'
                    : 'bg-[#85858C] hover:bg-[#A0A0A8]'
                  : 'bg-[#242428]'
              }`}
              style={{ height: `${heightPct}%` }}
              title={`${item.day}: ${item.minutes} min`}
            />
          </div>
        );
      })}
    </div>
  );
};
