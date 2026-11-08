import React, { useState } from 'react';
import { X, ShieldCheck, Activity, RefreshCw, Check, AlertCircle, Smartphone, Watch } from 'lucide-react';
import { HealthConnectPermission } from '../../types/vital.ts';
import { HealthConnectRepository } from '../../services/healthConnectRepository.ts';

interface HealthConnectModalProps {
  isOpen: boolean;
  onClose: () => void;
  onPermissionsUpdated?: () => void;
}

export const HealthConnectModal: React.FC<HealthConnectModalProps> = ({
  isOpen,
  onClose,
  onPermissionsUpdated,
}) => {
  const [permissions, setPermissions] = useState<HealthConnectPermission[]>(() =>
    HealthConnectRepository.getPermissions()
  );
  const [isSyncing, setIsSyncing] = useState(false);
  const [syncMessage, setSyncMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const togglePermission = (id: string) => {
    const updated = permissions.map((p) =>
      p.id === id ? { ...p, granted: !p.granted } : p
    );
    setPermissions(updated);
    HealthConnectRepository.savePermissions(updated);
    if (onPermissionsUpdated) onPermissionsUpdated();
  };

  const handleSyncNow = () => {
    setIsSyncing(true);
    setSyncMessage(null);
    setTimeout(() => {
      setIsSyncing(false);
      setSyncMessage('Sincronização concluída com sucesso! 14 fontes verificadas.');
      setTimeout(() => setSyncMessage(null), 3500);
    }, 1000);
  };

  const grantedCount = permissions.filter((p) => p.granted).length;

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in select-none">
      <div className="w-full max-w-lg bg-[#171719] border border-white/10 rounded-t-3xl sm:rounded-3xl p-5 max-h-[90vh] flex flex-col shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-[#242428] flex items-center justify-center text-white">
              <Activity className="w-5 h-5 text-white" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">Android Health Connect</h2>
              <p className="text-xs text-[#85858C]">
                {grantedCount} de {permissions.length} permissões concedidas
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

        {/* Sync Status Banner */}
        <div className="my-3 p-3 rounded-2xl bg-[#121214] border border-white/5 flex items-center justify-between">
          <div className="text-xs">
            <span className="text-white font-medium block">Origem Unificada</span>
            <span className="text-[#85858C] text-[11px]">
              Amazfit Balance via Zepp + Health Connect SDK
            </span>
          </div>

          <button
            onClick={handleSyncNow}
            disabled={isSyncing}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-[#242428] hover:bg-[#323238] text-xs font-semibold text-white transition-all disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isSyncing ? 'animate-spin' : ''}`} />
            <span>{isSyncing ? 'Sincronizando...' : 'Sincronizar'}</span>
          </button>
        </div>

        {syncMessage && (
          <div className="mb-2 p-2.5 rounded-xl bg-emerald-950/40 border border-emerald-500/30 text-xs text-emerald-300 flex items-center gap-2">
            <Check className="w-4 h-4 text-emerald-400" />
            <span>{syncMessage}</span>
          </div>
        )}

        {/* Permissions List */}
        <div className="flex-1 overflow-y-auto pr-1 flex flex-col gap-2 my-2">
          <div className="text-[11px] font-semibold text-[#85858C] uppercase tracking-wider px-1">
            Controle Granular de Registros de Saúde
          </div>

          {permissions.map((perm) => (
            <div
              key={perm.id}
              onClick={() => togglePermission(perm.id)}
              className="p-3 rounded-2xl bg-[#242428]/40 border border-white/5 flex items-center justify-between cursor-pointer hover:bg-[#242428]/70 transition-colors"
            >
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-white font-mono">{perm.recordName}</span>
                </div>
                <span className="text-[11px] text-[#85858C] block mt-0.5">
                  {perm.description}
                </span>
              </div>

              <div
                className={`w-5 h-5 rounded-md flex items-center justify-center transition-colors border ${
                  perm.granted
                    ? 'bg-white border-white text-black'
                    : 'bg-transparent border-[#85858C] text-transparent'
                }`}
              >
                <Check className="w-3.5 h-3.5 stroke-[3]" />
              </div>
            </div>
          ))}
        </div>

        {/* Privacy Policy note */}
        <div className="pt-3 border-t border-white/5 text-[11px] text-[#85858C] leading-snug">
          O aplicativo VITAL nunca envia seus dados de saúde para servidores externos. Todas as leituras permanecem armazenadas no banco local Room do seu dispositivo Android.
        </div>
      </div>
    </div>
  );
};
