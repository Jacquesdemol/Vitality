import React from 'react';
import { X, Info, Code2, Layers, ShieldCheck, Heart } from 'lucide-react';

interface AboutModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOpenAndroidExport: () => void;
}

export const AboutModal: React.FC<AboutModalProps> = ({
  isOpen,
  onClose,
  onOpenAndroidExport,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in select-none">
      <div className="w-full max-w-md bg-[#171719] border border-white/10 rounded-t-3xl sm:rounded-3xl p-5 max-h-[85vh] flex flex-col shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/5">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-[#242428] flex items-center justify-center text-white">
              <Info className="w-5 h-5 text-white/90" />
            </div>
            <div>
              <h2 className="text-base font-bold text-white">Sobre o VITAL</h2>
              <span className="text-xs text-[#85858C]">Versão 1.0.0 (Build 2026.11)</span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full text-[#85858C] hover:text-white hover:bg-[#242428]"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto flex flex-col gap-3 py-3 text-xs text-[#85858C] leading-relaxed">
          <div className="p-4 rounded-2xl bg-[#121214] border border-white/5 flex flex-col gap-1.5">
            <div className="flex items-center gap-2 text-white font-bold text-sm">
              <Heart className="w-4 h-4 text-white" />
              <span>VITAL — Saúde e Bem-estar</span>
            </div>
            <p>
              Painel centralizado de saúde com identidade sóbria, minimalista e monocromática inspirada na elegância do ecossistema Samsung Health e navegação Galaxy Store.
            </p>
          </div>

          <div className="p-3.5 rounded-2xl bg-[#242428]/40 border border-white/5">
            <span className="text-xs font-bold text-white block mb-2">Arquitetura Técnica Nativa</span>
            <ul className="space-y-1.5 text-[11px]">
              <li className="flex items-center gap-2 text-[#F2F2F4]">
                <span className="w-1.5 h-1.5 rounded-full bg-white" />
                <strong>Linguagem:</strong> Kotlin 2.0+
              </li>
              <li className="flex items-center gap-2 text-[#F2F2F4]">
                <span className="w-1.5 h-1.5 rounded-full bg-white" />
                <strong>UI:</strong> Jetpack Compose + Material 3
              </li>
              <li className="flex items-center gap-2 text-[#F2F2F4]">
                <span className="w-1.5 h-1.5 rounded-full bg-white" />
                <strong>Padrão:</strong> MVVM com Coroutines e StateFlow
              </li>
              <li className="flex items-center gap-2 text-[#F2F2F4]">
                <span className="w-1.5 h-1.5 rounded-full bg-white" />
                <strong>Banco Local:</strong> Room Database (cache off-line)
              </li>
              <li className="flex items-center gap-2 text-[#F2F2F4]">
                <span className="w-1.5 h-1.5 rounded-full bg-white" />
                <strong>Integração:</strong> Android Health Connect SDK
              </li>
              <li className="flex items-center gap-2 text-[#F2F2F4]">
                <span className="w-1.5 h-1.5 rounded-full bg-white" />
                <strong>Background:</strong> WorkManager para sincronizações periódicas
              </li>
            </ul>
          </div>

          <button
            onClick={() => {
              onClose();
              onOpenAndroidExport();
            }}
            className="w-full py-2.5 px-4 rounded-xl bg-[#242428] text-white hover:bg-[#323238] transition-colors flex items-center justify-center gap-2 font-semibold"
          >
            <Code2 className="w-4 h-4" />
            <span>Ver e Baixar Projeto Android Studio (.zip)</span>
          </button>
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
