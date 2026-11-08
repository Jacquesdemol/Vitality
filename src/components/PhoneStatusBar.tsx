import React from 'react';
import { Wifi, Signal, Battery, Activity } from 'lucide-react';

interface PhoneStatusBarProps {
  isDemoMode: boolean;
  onToggleDemoMode: () => void;
  onOpenSync: () => void;
}

export const PhoneStatusBar: React.FC<PhoneStatusBarProps> = ({
  isDemoMode,
  onToggleDemoMode,
  onOpenSync,
}) => {
  return (
    <div className="w-full flex items-center justify-between px-6 pt-3 pb-2 select-none text-xs text-[#F2F2F4]">
      {/* Time */}
      <span className="font-semibold tracking-tight text-sm">9:41</span>

      {/* Center indicator: Mode badge */}
      <div className="flex items-center gap-1.5">
        <button
          onClick={onToggleDemoMode}
          title="Clique para alternar entre Modo Demonstração e Dados Conectados"
          className={`px-2 py-0.5 rounded-full text-[10px] font-medium tracking-wide transition-all border ${
            isDemoMode
              ? 'bg-[#171719] text-[#85858C] border-[#242428] hover:border-[#85858C]'
              : 'bg-[#242428] text-white border-white/20'
          }`}
        >
          {isDemoMode ? 'MODO DEMO' : 'HEALTH CONNECT ATIVO'}
        </button>
      </div>

      {/* Right icons: Signal, Wifi, Battery */}
      <div className="flex items-center gap-2 text-[#F2F2F4]/90">
        <Signal className="w-3.5 h-3.5" />
        <Wifi className="w-3.5 h-3.5" />
        <div className="flex items-center">
          <Battery className="w-4 h-4 fill-current" />
        </div>
      </div>
    </div>
  );
};
