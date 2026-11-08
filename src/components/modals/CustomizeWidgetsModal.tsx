import React, { useState } from 'react';
import { X, ArrowUp, ArrowDown, Eye, EyeOff, RotateCcw, Check, SlidersHorizontal } from 'lucide-react';
import { WidgetConfig } from '../../types/vital.ts';
import { DEFAULT_WIDGETS, HealthConnectRepository } from '../../services/healthConnectRepository.ts';

interface CustomizeWidgetsModalProps {
  isOpen: boolean;
  onClose: () => void;
  widgets: WidgetConfig[];
  onSaveWidgets: (updated: WidgetConfig[]) => void;
}

export const CustomizeWidgetsModal: React.FC<CustomizeWidgetsModalProps> = ({
  isOpen,
  onClose,
  widgets,
  onSaveWidgets,
}) => {
  const [items, setItems] = useState<WidgetConfig[]>(widgets);

  if (!isOpen) return null;

  const moveItem = (index: number, direction: 'up' | 'down') => {
    const targetIdx = direction === 'up' ? index - 1 : index + 1;
    if (targetIdx < 0 || targetIdx >= items.length) return;

    const newItems = [...items];
    const temp = newItems[index];
    newItems[index] = newItems[targetIdx];
    newItems[targetIdx] = temp;

    // Update orders
    const reordered = newItems.map((item, idx) => ({ ...item, order: idx }));
    setItems(reordered);
    onSaveWidgets(reordered);
    HealthConnectRepository.saveWidgetsConfig(reordered);
  };

  const toggleWidget = (id: string) => {
    const updated = items.map((w) => (w.id === id ? { ...w, enabled: !w.enabled } : w));
    setItems(updated);
    onSaveWidgets(updated);
    HealthConnectRepository.saveWidgetsConfig(updated);
  };

  const resetToDefault = () => {
    setItems(DEFAULT_WIDGETS);
    onSaveWidgets(DEFAULT_WIDGETS);
    HealthConnectRepository.saveWidgetsConfig(DEFAULT_WIDGETS);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in select-none">
      <div className="w-full max-w-md bg-[#171719] border border-white/10 rounded-t-3xl sm:rounded-3xl p-5 max-h-[85vh] flex flex-col shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/5">
          <div className="flex items-center gap-2.5">
            <SlidersHorizontal className="w-5 h-5 text-white" />
            <h2 className="text-base font-bold text-white">Personalizar Painel Inicial</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full text-[#85858C] hover:text-white hover:bg-[#242428]"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <p className="text-xs text-[#85858C] my-2 leading-relaxed">
          Reorganize a ordem dos widgets na tela Início, oculte métricas que preferir não acompanhar ou restaure a distribuição padrão.
        </p>

        {/* Widgets List */}
        <div className="flex-1 overflow-y-auto flex flex-col gap-2 my-2 pr-1">
          {items.map((widget, idx) => (
            <div
              key={widget.id}
              className={`p-3 rounded-2xl border flex items-center justify-between transition-colors ${
                widget.enabled
                  ? 'bg-[#242428]/60 border-white/10'
                  : 'bg-[#171719] border-white/5 opacity-60'
              }`}
            >
              <div className="flex items-center gap-2.5">
                <button
                  onClick={() => toggleWidget(widget.id)}
                  className={`p-1.5 rounded-lg text-xs transition-colors ${
                    widget.enabled
                      ? 'bg-white text-black'
                      : 'bg-[#242428] text-[#85858C]'
                  }`}
                  title={widget.enabled ? 'Ocultar widget' : 'Exibir widget'}
                >
                  {widget.enabled ? <Eye className="w-4 h-4" /> : <EyeOff className="w-4 h-4" />}
                </button>
                <span className="text-xs font-semibold text-white">{widget.name}</span>
              </div>

              {/* Order Controls */}
              <div className="flex items-center gap-1">
                <button
                  onClick={() => moveItem(idx, 'up')}
                  disabled={idx === 0}
                  className="p-1.5 rounded-lg text-[#85858C] hover:text-white hover:bg-[#2A2A30] disabled:opacity-30 disabled:hover:bg-transparent"
                  title="Mover para cima"
                >
                  <ArrowUp className="w-4 h-4" />
                </button>
                <button
                  onClick={() => moveItem(idx, 'down')}
                  disabled={idx === items.length - 1}
                  className="p-1.5 rounded-lg text-[#85858C] hover:text-white hover:bg-[#2A2A30] disabled:opacity-30 disabled:hover:bg-transparent"
                  title="Mover para baixo"
                >
                  <ArrowDown className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Footer Actions */}
        <div className="pt-3 border-t border-white/5 flex items-center justify-between">
          <button
            onClick={resetToDefault}
            className="flex items-center gap-1.5 text-xs text-[#85858C] hover:text-white"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Restaurar Padrão</span>
          </button>

          <button
            onClick={onClose}
            className="px-4 py-2 rounded-full bg-white text-black text-xs font-semibold hover:bg-neutral-200 transition-colors"
          >
            Concluir
          </button>
        </div>
      </div>
    </div>
  );
};
