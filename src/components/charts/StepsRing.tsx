import React from 'react';
import { Footprints } from 'lucide-react';

interface StepsRingProps {
  current: number;
  goal: number;
  size?: number;
  strokeWidth?: number;
}

export const StepsRing: React.FC<StepsRingProps> = ({
  current,
  goal,
  size = 64,
  strokeWidth = 5,
}) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const pct = Math.min(1, Math.max(0, current / (goal || 10000)));
  const strokeDashoffset = circumference - pct * circumference;

  return (
    <div
      className="relative flex items-center justify-center select-none"
      style={{ width: size, height: size }}
    >
      <svg width={size} height={size} className="-rotate-90">
        {/* Background track */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="#242428"
          strokeWidth={strokeWidth}
        />
        {/* Progress arc */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="#F2F2F4"
          strokeWidth={strokeWidth}
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          strokeLinecap="round"
          className="transition-all duration-700 ease-out"
        />
      </svg>
      {/* Icon in center */}
      <div className="absolute inset-0 flex items-center justify-center text-[#F2F2F4]">
        <Footprints className="w-5 h-5 text-white/90" />
      </div>
    </div>
  );
};
