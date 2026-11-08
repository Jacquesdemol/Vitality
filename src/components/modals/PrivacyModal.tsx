import React, { useState } from 'react';
import { X, Shield, Download, Trash2, Check, Lock, HardDrive } from 'lucide-react';
import { HealthConnectRepository } from '../../services/healthConnectRepository.ts';

interface PrivacyModalProps {
  isOpen: boolean;
  onClose: () => void;
  onDataReset: () => void;
}

export const PrivacyModal: React.FC<PrivacyModalProps> = ({ isOpen, onClose, onDataReset }) => {
  const [exported, setExported] = useState(false);
  const [resetConfirm, setResetConfirm] = useState(false);

  if (!isOpen) return null;

  const handleExportJSON = () => {
    const jsonStr = HealthConnectRepository.exportAllDataAsJSON();
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `vital-health-backup-${new Date().toISOString().slice(0, 10)}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    setExported(true);
    setTimeout(() => setExported(false), 3000);
  };

  const handleClearData = () => {
    HealthConnectRepository.clearAllLocalData();
    onDataReset();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in select-none">
      <div className="w-full max-w-lg bg-[#171719] border border-white/10 rounded-t-3xl sm:rounded-3xl p-5 max-h-[90vh] flex flex-col shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/5">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-[#242428] flex items-center justify-center text-white">
              <Shield className="w-5 h-5 text-white/90" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">Privacidade &amp; Seus Dados</h2>
              <span className="text-xs text-[#85858C]">Controle e soberania de dados pessoais</span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full text-[#85858C] hover:text-white hover:bg-[#242428]"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto flex flex-col gap-3 py-3 text-xs leading-relaxed text-[#85858C]">
          {/* Commitment Card */}
          <div className="p-4 rounded-2xl bg-[#121214] border border-white/5 flex items-start gap-3">
            <Lock className="w-5 h-5 text-white flex-shrink-0 mt-0.5" />
            <div>
              <span className="text-white font-bold text-xs block mb-1">
                Armazenamento 100% Local (On-Device)
              </span>
              <p>
                O VITAL segue a filosofia de soberania de dados. Suas métricas de sono, frequência cardíaca, composição corporal e treinos residem unicamente no seu dispositivo Android via Room Database e Android Health Connect.
              </p>
            </div>
          </div>

          {/* Export Section */}
          <div className="p-4 rounded-2xl bg-[#242428]/40 border border-white/5 flex flex-col gap-2">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-xs font-bold text-white block">Exportação de Dados (JSON)</span>
                <span className="text-[11px] text-[#85858C]">
                  Baixe um arquivo padronizado com todas as suas informações de saúde
                </span>
              </div>
            </div>

            <button
              onClick={handleExportJSON}
              className="mt-1 flex items-center justify-center gap-2 w-full py-2.5 px-4 rounded-xl bg-white text-black font-semibold text-xs hover:bg-neutral-200 transition-colors"
            >
              {exported ? <Check className="w-4 h-4 text-emerald-600" /> : <Download className="w-4 h-4" />}
              <span>{exported ? 'Backup Exportado com Sucesso!' : 'Exportar Meus Dados'}</span>
            </button>
          </div>

          {/* Wipe Local Data */}
          <div className="p-4 rounded-2xl bg-[#242428]/40 border border-white/5 flex flex-col gap-2">
            <span className="text-xs font-bold text-white block">Excluir Dados Locais</span>
            <p className="text-[11px] text-[#85858C]">
              Limpa o cache local do aplicativo e redefine as preferências para os valores iniciais. Não exclui os registros mestres do Health Connect do sistema Android.
            </p>

            {resetConfirm ? (
              <div className="flex items-center gap-2 mt-1">
                <button
                  onClick={handleClearData}
                  className="flex-1 py-2 rounded-xl bg-rose-600 text-white font-semibold text-xs hover:bg-rose-700"
                >
                  Confirmar Exclusão
                </button>
                <button
                  onClick={() => setResetConfirm(false)}
                  className="flex-1 py-2 rounded-xl bg-[#242428] text-white text-xs hover:bg-[#323238]"
                >
                  Cancelar
                </button>
              </div>
            ) : (
              <button
                onClick={() => setResetConfirm(true)}
                className="mt-1 flex items-center justify-center gap-1.5 w-full py-2 px-3 rounded-xl bg-[#242428] text-rose-300 font-medium text-xs hover:bg-[#2A2A30] transition-colors"
              >
                <Trash2 className="w-3.5 h-3.5" />
                <span>Excluir Armazenamento Local</span>
              </button>
            )}
          </div>
        </div>

        <div className="pt-3 border-t border-white/5 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-full bg-white text-black text-xs font-semibold hover:bg-neutral-200"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  );
};
