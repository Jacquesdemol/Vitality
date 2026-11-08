import React from 'react';
import {
  Settings,
  User,
  Target,
  Watch,
  Shield,
  Info,
  ChevronRight,
  SlidersHorizontal,
  MoreVertical,
} from 'lucide-react';
import { UserProfile, ZeppConnectorState } from '../../types/vital.ts';

interface SettingsScreenProps {
  user: UserProfile;
  zeppState: ZeppConnectorState;
  isDemoMode: boolean;
  onOpenProfile: () => void;
  onOpenGoals: () => void;
  onOpenDevices: () => void;
  onOpenPrivacy: () => void;
  onOpenAbout: () => void;
  onOpenCustomizeWidgets: () => void;
  onToggleDemoMode: () => void;
  onOpenOptionsMenu: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  user,
  zeppState,
  isDemoMode,
  onOpenProfile,
  onOpenGoals,
  onOpenDevices,
  onOpenPrivacy,
  onOpenAbout,
  onOpenCustomizeWidgets,
  onToggleDemoMode,
  onOpenOptionsMenu,
}) => {
  return (
    <div className="w-full pb-36 pt-2 px-4 flex flex-col gap-3.5 max-w-[420px] mx-auto select-none">
      {/* Top Header */}
      <header className="flex items-center justify-between pt-1 pb-1">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-[#171719] border border-white/5 flex items-center justify-center text-white">
            <Settings className="w-5 h-5 text-white/90" />
          </div>
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-[#F2F2F4]">
              Configurações
            </h1>
            <p className="text-sm text-[#85858C] font-normal">
              Personalize sua experiência
            </p>
          </div>
        </div>

        <button
          onClick={onOpenOptionsMenu}
          className="p-2 rounded-full text-[#85858C] hover:text-[#F2F2F4] hover:bg-[#171719] transition-colors"
          aria-label="Opções"
        >
          <MoreVertical className="w-5 h-5" />
        </button>
      </header>

      {/* Profile Card */}
      <div
        onClick={onOpenProfile}
        className="rounded-3xl bg-[#171719] border border-white/5 p-4 sm:p-5 flex items-center justify-between cursor-pointer hover:border-white/10 active:scale-[0.99] transition-all group shadow-sm"
      >
        <div className="flex items-center gap-3.5">
          <div className="w-12 h-12 rounded-full bg-[#242428] flex items-center justify-center text-white flex-shrink-0">
            <User className="w-6 h-6 text-[#85858C]" />
          </div>

          <div>
            <h2 className="text-base font-bold text-[#F2F2F4]">
              {user.name}
            </h2>
            <p className="text-xs text-[#85858C] mt-0.5">
              {user.age} anos • {(user.heightCm / 100).toFixed(2).replace('.', ',')} m • {user.weightKg.toString().replace('.', ',')} kg
            </p>
          </div>
        </div>

        <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
      </div>

      {/* Main Settings List */}
      <div className="flex flex-col gap-2 rounded-3xl bg-[#171719] border border-white/5 p-2 sm:p-3 shadow-sm">
        {/* Metas */}
        <button
          onClick={onOpenGoals}
          className="w-full p-3 rounded-2xl flex items-center justify-between text-left hover:bg-[#242428]/50 transition-colors group"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-9 h-9 rounded-xl bg-[#242428] flex items-center justify-center text-white flex-shrink-0">
              <Target className="w-4 h-4 text-white/90" />
            </div>
            <div>
              <span className="text-sm font-semibold text-[#F2F2F4] block">Metas</span>
              <span className="text-xs text-[#85858C]">Passos, calorias, sono e mais</span>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
        </button>

        {/* Dispositivos conectados */}
        <button
          onClick={onOpenDevices}
          className="w-full p-3 rounded-2xl flex items-center justify-between text-left hover:bg-[#242428]/50 transition-colors group"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-9 h-9 rounded-xl bg-[#242428] flex items-center justify-center text-white flex-shrink-0">
              <Watch className="w-4 h-4 text-white/90" />
            </div>
            <div>
              <span className="text-sm font-semibold text-[#F2F2F4] block">Dispositivos conectados</span>
              <span className="text-xs text-[#85858C]">
                Pulseira, relógio e sensores {zeppState.deviceName ? `(${zeppState.deviceName.split(' ')[0]})` : ''}
              </span>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
        </button>

        {/* Personalizar Widgets */}
        <button
          onClick={onOpenCustomizeWidgets}
          className="w-full p-3 rounded-2xl flex items-center justify-between text-left hover:bg-[#242428]/50 transition-colors group"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-9 h-9 rounded-xl bg-[#242428] flex items-center justify-center text-white flex-shrink-0">
              <SlidersHorizontal className="w-4 h-4 text-white/90" />
            </div>
            <div>
              <span className="text-sm font-semibold text-[#F2F2F4] block">Personalizar Widgets</span>
              <span className="text-xs text-[#85858C]">Reorganizar, redimensionar e ocultar</span>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
        </button>

        {/* Privacidade */}
        <button
          onClick={onOpenPrivacy}
          className="w-full p-3 rounded-2xl flex items-center justify-between text-left hover:bg-[#242428]/50 transition-colors group"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-9 h-9 rounded-xl bg-[#242428] flex items-center justify-center text-white flex-shrink-0">
              <Shield className="w-4 h-4 text-white/90" />
            </div>
            <div>
              <span className="text-sm font-semibold text-[#F2F2F4] block">Privacidade</span>
              <span className="text-xs text-[#85858C]">Seus dados e permissões</span>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
        </button>

        {/* Sobre o app & GitHub */}
        <button
          onClick={onOpenAbout}
          className="w-full p-3 rounded-2xl flex items-center justify-between text-left hover:bg-[#242428]/50 transition-colors group"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-9 h-9 rounded-xl bg-[#242428] flex items-center justify-center text-white flex-shrink-0">
              <Info className="w-4 h-4 text-white/90" />
            </div>
            <div>
              <span className="text-sm font-semibold text-[#F2F2F4] block">Sobre o VITAL</span>
              <span className="text-xs text-[#85858C]">Versão 1.0.0 • Repositório GitHub</span>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-[#85858C] group-hover:text-white transition-colors" />
        </button>
      </div>

      {/* Mode Switch Card */}
      <div className="rounded-3xl bg-[#171719] border border-white/5 p-4 flex items-center justify-between">
        <div>
          <span className="text-xs text-[#85858C] block">Fonte de Dados Atual</span>
          <span className="text-sm font-bold text-white">
            {isDemoMode ? 'Modo de Demonstração Ativo' : 'Health Connect Local Conectado'}
          </span>
          <p className="text-[11px] text-[#85858C] mt-0.5 max-w-[240px]">
            {isDemoMode
              ? 'Exibindo amostras realistas para validação de layout e animações.'
              : 'Consultando repositório com dados estritamente locais.'}
          </p>
        </div>

        <button
          onClick={onToggleDemoMode}
          className={`px-3 py-1.5 rounded-full text-xs font-semibold transition-all ${
            isDemoMode
              ? 'bg-[#242428] text-white hover:bg-[#323238]'
              : 'bg-white text-black hover:bg-neutral-200'
          }`}
        >
          {isDemoMode ? 'Usar Dados Locais' : 'Usar Modo Demo'}
        </button>
      </div>
    </div>
  );
};
