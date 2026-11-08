import React, { useState } from 'react';
import { X, Watch, Shield, CheckCircle2, AlertTriangle, Key, ExternalLink } from 'lucide-react';
import { ZeppConnectorState } from '../../types/vital.ts';
import { HealthConnectRepository } from '../../services/healthConnectRepository.ts';

interface ZeppModalProps {
  isOpen: boolean;
  onClose: () => void;
  zeppState: ZeppConnectorState;
  onUpdateState: (newState: ZeppConnectorState) => void;
}

export const ZeppModal: React.FC<ZeppModalProps> = ({
  isOpen,
  onClose,
  zeppState,
  onUpdateState,
}) => {
  const [activeTab, setActiveTab] = useState<'status' | 'architecture'>('status');

  if (!isOpen) return null;

  const handleUseHealthConnectFallback = () => {
    const updated: ZeppConnectorState = {
      ...zeppState,
      status: 'synced_via_health_connect',
      deviceName: 'Amazfit Balance (via Health Connect)',
    };
    onUpdateState(updated);
    HealthConnectRepository.saveZeppState(updated);
  };

  const handleSetAwaitingConfig = () => {
    const updated: ZeppConnectorState = {
      ...zeppState,
      status: 'awaiting_config',
      clientIdConfigured: false,
    };
    onUpdateState(updated);
    HealthConnectRepository.saveZeppState(updated);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in select-none">
      <div className="w-full max-w-lg bg-[#171719] border border-white/10 rounded-t-3xl sm:rounded-3xl p-5 max-h-[90vh] flex flex-col shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-[#242428] flex items-center justify-center text-white">
              <Watch className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-bold text-white">Zepp Open Platform</h2>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-white/10 text-white font-mono">
                  Amazfit
                </span>
              </div>
              <p className="text-xs text-[#85858C]">
                Conector oficial para pulseiras e smartwatches Zepp OS
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 rounded-full text-[#85858C] hover:text-white hover:bg-[#242428] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab switch */}
        <div className="flex rounded-xl bg-[#121214] p-1 my-3 border border-white/5">
          <button
            onClick={() => setActiveTab('status')}
            className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all ${
              activeTab === 'status' ? 'bg-[#242428] text-white' : 'text-[#85858C] hover:text-white'
            }`}
          >
            Status da Conexão
          </button>
          <button
            onClick={() => setActiveTab('architecture')}
            className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all ${
              activeTab === 'architecture' ? 'bg-[#242428] text-white' : 'text-[#85858C] hover:text-white'
            }`}
          >
            Segurança &amp; OAuth 2.0
          </button>
        </div>

        {activeTab === 'status' ? (
          <div className="flex-1 overflow-y-auto flex flex-col gap-3 py-1">
            {/* Status card */}
            <div className="p-4 rounded-2xl bg-[#121214] border border-white/5 flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="text-xs text-[#85858C]">Modo Operacional Atual</span>
                <span
                  className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                    zeppState.status === 'synced_via_health_connect'
                      ? 'bg-emerald-950 text-emerald-300 border border-emerald-500/30'
                      : 'bg-amber-950 text-amber-300 border border-amber-500/30'
                  }`}
                >
                  {zeppState.status === 'synced_via_health_connect'
                    ? 'SINCRONIZADO VIA HEALTH CONNECT'
                    : 'AGUARDANDO CONFIGURAÇÃO ZEPP OPEN PLATFORM'}
                </span>
              </div>

              <div className="text-xs text-[#F2F2F4] mt-1 font-medium">
                {zeppState.status === 'synced_via_health_connect'
                  ? 'Os dados do seu relógio Amazfit Balance são lidos diretamente dos registros sincronizados pelo aplicativo Zepp no Android Health Connect.'
                  : 'Aguardando credenciais seguras de desenvolvedor na Zepp Open Platform oficial.'}
              </div>

              <div className="text-[11px] text-[#85858C] mt-1">
                Última leitura: <strong>{zeppState.lastSync || 'Hoje, 09:30'}</strong>
              </div>
            </div>

            {/* Features Supported */}
            <div className="p-3.5 rounded-2xl bg-[#242428]/40 border border-white/5">
              <span className="text-xs font-bold text-white block mb-2">
                Recursos e Métricas Mapeadas
              </span>
              <div className="grid grid-cols-2 gap-2 text-xs">
                {zeppState.features.map((feat, idx) => (
                  <div key={idx} className="flex items-center gap-1.5 text-[#F2F2F4]">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 flex-shrink-0" />
                    <span className="text-[11px]">{feat}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Mode Actions */}
            <div className="flex flex-col gap-2 mt-1">
              <button
                onClick={handleUseHealthConnectFallback}
                className="w-full py-2.5 px-4 rounded-xl bg-white text-black text-xs font-semibold hover:bg-neutral-200 transition-colors"
              >
                Manter Sincronização via Zepp + Health Connect (Recomendado)
              </button>
              <button
                onClick={handleSetAwaitingConfig}
                className="w-full py-2.5 px-4 rounded-xl bg-[#242428] text-white text-xs font-semibold hover:bg-[#323238] transition-colors"
              >
                Alternar para "Aguardando Aprovação Zepp Open Platform"
              </button>
            </div>
          </div>
        ) : (
          <div className="flex-1 overflow-y-auto flex flex-col gap-3 py-1 text-xs text-[#85858C] leading-relaxed">
            <div className="p-3.5 rounded-2xl bg-[#121214] border border-white/5">
              <div className="flex items-center gap-2 text-white font-bold mb-1.5">
                <Shield className="w-4 h-4 text-emerald-400" />
                <span>Diretriz de Segurança Rigorosa</span>
              </div>
              <p>
                Conforme as regras do projeto VITAL, <strong>nunca são utilizados endpoints privados, engenharia reversa ou extração não documentada de tokens</strong>.
              </p>
            </div>

            <div className="p-3.5 rounded-2xl bg-[#242428]/40 border border-white/5 flex flex-col gap-2">
              <div className="flex items-center gap-2 text-white font-semibold">
                <Key className="w-4 h-4 text-white/80" />
                <span>Fluxo OAuth 2.0 Seguro</span>
              </div>
              <p>
                1. O aplicativo Android inicia a intenção OAuth da Zepp Open Platform solicitando autorização do usuário.
              </p>
              <p>
                2. O código de autorização é enviado a um backend seguro, onde os segredos (client secret) residem isolados.
              </p>
              <p>
                3. Sem segredos no APK do Android, seus dados e o app permanecem 100% seguros e em conformidade com as políticas do Google Play e da Zepp.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
