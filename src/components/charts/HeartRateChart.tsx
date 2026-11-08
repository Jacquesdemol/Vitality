import React from 'react';

interface Point {
  x: number;
  y: number;
  val: number;
}

export const HeartRateMiniChart: React.FC<{ points?: number[] }> = ({
  points = [62, 58, 65, 78, 72, 85, 110, 95, 82, 72],
}) => {
  const width = 140;
  const height = 48;
  const paddingY = 8;
  const paddingX = 6;

  const min = Math.min(...points);
  const max = Math.max(...points);
  const range = max - min || 1;

  const stepX = (width - paddingX * 2) / (points.length - 1);
  const coords: Point[] = points.map((val, idx) => ({
    x: paddingX + idx * stepX,
    y: height - paddingY - ((val - min) / range) * (height - paddingY * 2),
    val,
  }));

  const pathD = coords.reduce((acc, curr, idx) => {
    return idx === 0 ? `M ${curr.x} ${curr.y}` : `${acc} L ${curr.x} ${curr.y}`;
  }, '');

  return (
    <div className="relative w-[140px] h-[48px] overflow-visible">
      <svg width={width} height={height} className="overflow-visible">
        <defs>
          <linearGradient id="hrLineGrad" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stopColor="#85858C" stopOpacity="0.6" />
            <stop offset="60%" stopColor="#F2F2F4" stopOpacity="0.9" />
            <stop offset="100%" stopColor="#FFFFFF" stopOpacity="1" />
          </linearGradient>
        </defs>
        {/* The line */}
        <path
          d={pathD}
          fill="none"
          stroke="url(#hrLineGrad)"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        {/* The glowing dots along the path */}
        {coords.map((c, i) => (
          <circle
            key={i}
            cx={c.x}
            cy={c.y}
            r={i === coords.length - 1 ? 3.5 : 2.5}
            fill={i === coords.length - 1 ? '#FFFFFF' : '#D4D4D8'}
            stroke="#171719"
            strokeWidth="1.5"
            className="transition-all duration-300"
          />
        ))}
      </svg>
    </div>
  );
};
