import React from 'react';

export const HumanSilhouette: React.FC<{ className?: string }> = ({ className = 'w-16 h-28' }) => {
  return (
    <div className={`flex items-center justify-center ${className} select-none`}>
      <svg
        viewBox="0 0 100 200"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="w-full h-full opacity-70 hover:opacity-90 transition-opacity"
      >
        {/* Head */}
        <circle cx="50" cy="24" r="14" fill="#85858C" />
        {/* Neck */}
        <rect x="46" y="38" width="8" height="8" rx="2" fill="#85858C" />
        {/* Torso & Shoulders */}
        <path
          d="M 28 50 C 35 46 65 46 72 50 C 76 54 75 75 73 98 C 72 108 68 114 62 118 L 62 125 L 38 125 L 38 118 C 32 114 28 108 27 98 C 25 75 24 54 28 50 Z"
          fill="#85858C"
        />
        {/* Left Arm */}
        <path
          d="M 25 52 C 22 55 16 75 16 95 C 16 108 17 122 19 130 C 20 133 24 133 25 130 C 26 122 27 105 28 92 Z"
          fill="#85858C"
        />
        {/* Right Arm */}
        <path
          d="M 75 52 C 78 55 84 75 84 95 C 84 108 83 122 81 130 C 80 133 76 133 75 130 C 74 122 73 105 72 92 Z"
          fill="#85858C"
        />
        {/* Left Leg */}
        <path
          d="M 39 126 C 41 138 41 155 42 170 C 42 182 40 192 41 195 C 42 198 47 198 48 195 C 49 188 49 170 49 155 C 49 140 48 126 48 126 Z"
          fill="#85858C"
        />
        {/* Right Leg */}
        <path
          d="M 52 126 C 52 126 51 140 51 155 C 51 170 51 188 52 195 C 53 198 58 198 59 195 C 60 192 58 182 58 170 C 59 155 59 138 61 126 Z"
          fill="#85858C"
        />
      </svg>
    </div>
  );
};
