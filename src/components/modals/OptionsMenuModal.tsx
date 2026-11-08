import React from 'react';
import {
  X,
  SlidersHorizontal,
  RefreshCw,
  Code2,
  Watch,
  Activity,
  Target,
  ToggleLeft,
  ToggleRight,
} from 'lucide-react';

interface OptionsMenuModalProps {
  isOpen: boolean;
  onClose: () => void;
  isDemoMode: boolean;
  onToggleDemoMode: () => void;
  onOpenCustomizeWidgets: () => void;
  onOpenHealthConnect: () => void;
  onOpenZepp: () => void;
  onOpenGoals: () => void;
}

export const OptionsMenuModal: React.FC<OptionsMenuModalProps> = ({
  isOpen,
  onClose,
  isDemoMode,
  onToggleDemoMode,
  onOpenCustomizeWidgets,
  onOpenHealthConnect,
  onOpenZepp,
  onOpenGoals,
}) => {
  if (!isOpen) return null;

  return (
    <div
      onClick={onClose}
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-start justify-end p-4 pt-12 animate-in fade-in select-none"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="w-72 bg-[#171719] border border-white/10 rounded-3xl p-2.5 shadow-2xl flex flex-col gap-1"
      >
        <div className="flex items-center justify-between px-3 py-1.5 border-b border-white/5 mb-1">
          <span className="text-xs font-bold text-white">Menu VITAL</span>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-[#85858C] hover:text-white"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Toggle Demo Mode */}
        <button
          onClick={() => {
            onToggleDemoMode();
            onClose();
          }}
          className="w-full px-3 py-2.5 rounded-2xl flex items-center justify-between text-xs text-left hover:bg-[#242428] transition-colors"
        >
          <div className="flex items-center gap-2.5">
            {isDemoMode ? (
              <ToggleRight className="w-4 h-4 text-white" />
            ) : (
              <ToggleLeft className="w-4 h-4 text-[#85858C]" />
            )}
            <span className="text-white font-medium">Modo Demonstração</span>
          </div>
          <span className="text-[10px] text-[#85858C]">{isDemoMode ? 'Ativo' : 'Inativo'}</span>
        </button>

        {/* Customize Widgets */}
        <button
          onClick={() => {
            onClose();
            onOpenCustomizeWidgets();
          }}
          className="w-full px-3 py-2.5 rounded-2xl flex items-center gap-2.5 text-xs text-left text-white hover:bg-[#242428] transition-colors"
        >
          <SlidersHorizontal className="w-4 h-4 text-[#85858C]" />
          <span>Personalizar Widgets</span>
        </button>

        {/* Health Connect Status */}
        <button
          onClick={() => {
            onClose();
            onOpenHealthConnect();
          }}
          className="w-full px-3 py-2.5 rounded-2xl flex items-center gap-2.5 text-xs text-left text-white hover:bg-[#242428] transition-colors"
        >
          <Activity className="w-4 h-4 text-[#85858C]" />
          <span>Android Health Connect</span>
        </button>

        {/* Zepp Connector */}
        <button
          onClick={() => {
            onClose();
            onOpenZepp();
          }}
          className="w-full px-3 py-2.5 rounded-2xl flex items-center gap-2.5 text-xs text-left text-white hover:bg-[#242428] transition-colors"
        >
          <Watch className="w-4 h-4 text-[#85858C]" />
          <span>Zepp Open Platform</span>
        </button>

        {/* Metas */}
        <button
          onClick={() => {
            onClose();
            onOpenGoals();
          }}
          className="w-full px-3 py-2.5 rounded-2xl flex items-center gap-2.5 text-xs text-left text-white hover:bg-[#242428] transition-colors"
        >
          <Target className="w-4 h-4 text-[#85858C]" />
          <span>Metas e Objetivos</span>
        </button>
      </div>
    </div>
  );
};
