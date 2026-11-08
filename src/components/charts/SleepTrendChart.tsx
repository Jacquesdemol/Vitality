import React from 'react';

interface SleepTrendChartProps {
  history: { day: string; hours: number; quality: number; dateStr: string }[];
}

export const SleepTrendChart: React.FC<SleepTrendChartProps> = ({ history }) => {
  const width = 140;
  const height = 44;
  const paddingX = 8;
  const paddingY = 8;

  const qualities = history.map((h) => h.quality);
  const min = Math.min(...qualities, 70);
  const max = Math.max(...qualities, 95);
  const range = max - min || 1;

  const stepX = (width - paddingX * 2) / (history.length - 1);
  const coords = history.map((item, idx) => ({
    x: paddingX + idx * stepX,
    y: height - paddingY - ((item.quality - min) / range) * (height - paddingY * 2),
    quality: item.quality,
    day: item.day,
  }));

  const pathD = coords.reduce((acc, curr, idx) => {
    return idx === 0 ? `M ${curr.x} ${curr.y}` : `${acc} L ${curr.x} ${curr.y}`;
  }, '');

  return (
    <div className="relative w-[140px] h-[44px] overflow-visible">
      <svg width={width} height={height} className="overflow-visible">
        <path
          d={pathD}
          fill="none"
          stroke="#F2F2F4"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        {coords.map((c, i) => (
          <circle
            key={i}
            cx={c.x}
            cy={c.y}
            r={i === coords.length - 1 ? 3 : 2}
            fill={i === coords.length - 1 ? '#FFFFFF' : '#85858C'}
            stroke="#171719"
            strokeWidth="1"
          />
        ))}
      </svg>
    </div>
  );
};
