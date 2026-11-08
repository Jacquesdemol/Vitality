import React from 'react';
import { Moon, Dumbbell, Home, Settings } from 'lucide-react';
import { TabType } from '../types/vital.ts';

interface VitalNavBarProps {
  currentTab: TabType;
  onSelectTab: (tab: TabType) => void;
}

export const VitalNavBar: React.FC<VitalNavBarProps> = ({ currentTab, onSelectTab }) => {
  const tabs: { id: TabType; label: string; icon: React.FC<{ className?: string }> }[] = [
    { id: 'sleep', label: 'Sono', icon: Moon },
    { id: 'exercise', label: 'Exercícios', icon: Dumbbell },
    { id: 'home', label: 'Início', icon: Home },
    { id: 'settings', label: 'Configurações', icon: Settings },
  ];

  return (
    <nav
      aria-label="Navegação Principal"
      className="fixed bottom-4 left-1/2 -translate-x-1/2 z-40 max-w-[370px] w-[calc(100%-2rem)] px-1"
    >
      <div className="relative flex items-center justify-between p-1.5 rounded-full bg-[#171719]/90 backdrop-blur-2xl border border-white/10 shadow-2xl shadow-black/90">
        {tabs.map((tab) => {
          const isSelected = currentTab === tab.id;
          const Icon = tab.icon;

          return (
            <button
              key={tab.id}
              onClick={() => onSelectTab(tab.id)}
              className={`relative flex flex-col items-center justify-center py-1.5 px-3 rounded-full transition-all duration-200 select-none flex-1 ${
                isSelected
                  ? 'bg-[#2E2E36] text-[#F2F2F4] font-medium shadow-md'
                  : 'text-[#85858C] hover:text-[#C5C5CC]'
              }`}
            >
              <Icon
                className={`w-4 h-4 mb-0.5 transition-transform duration-200 ${
                  isSelected ? 'scale-110 text-white fill-current' : 'text-[#85858C]'
                }`}
              />
              <span
                className={`text-[10px] sm:text-[11px] tracking-tight leading-none ${
                  isSelected ? 'text-white font-semibold' : 'text-[#85858C]'
                }`}
              >
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
