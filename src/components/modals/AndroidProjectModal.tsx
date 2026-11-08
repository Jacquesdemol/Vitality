import React, { useState } from 'react';
import {
  X,
  Download,
  FileCode,
  FolderTree,
  Copy,
  Check,
  Smartphone,
  ExternalLink,
} from 'lucide-react';
import {
  getAndroidStudioProjectFiles,
  downloadAndroidProjectZip,
  AndroidFileItem,
} from '../../services/androidExportData.ts';

interface AndroidProjectModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AndroidProjectModal: React.FC<AndroidProjectModalProps> = ({ isOpen, onClose }) => {
  const files = getAndroidStudioProjectFiles();
  const [selectedFile, setSelectedFile] = useState<AndroidFileItem>(files[6]); // default to VitalBottomBar.kt or MainActivity
  const [copied, setCopied] = useState(false);
  const [downloading, setDownloading] = useState(false);

  if (!isOpen) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(selectedFile.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadZip = async () => {
    try {
      setDownloading(true);
      const blob = await downloadAndroidProjectZip();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'VITAL-Android-Studio-Kotlin-JetpackCompose.zip';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error('Erro ao gerar ZIP:', e);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-3 sm:p-6 animate-in fade-in">
      <div className="w-full max-w-4xl bg-[#171719] border border-white/10 rounded-3xl flex flex-col h-[90vh] shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex flex-col gap-2.5 px-4 py-3 border-b border-white/5 bg-[#121214]">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-xl bg-white text-black flex items-center justify-center font-bold">
                <Smartphone className="w-4 h-4" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-white leading-tight">
                  Projeto Android Studio Nativo
                </h2>
                <span className="text-[10px] text-[#85858C]">
                  Kotlin • Jetpack Compose • Health Connect
                </span>
              </div>
            </div>

            <button
              onClick={onClose}
              className="p-1.5 rounded-full text-[#85858C] hover:text-white hover:bg-[#242428] transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <div className="flex items-center justify-between gap-2 pt-1">
            <span className="text-[11px] text-[#85858C] hidden sm:inline">
              Código completo pronto para compilar no Android Studio
            </span>
            <button
              onClick={handleDownloadZip}
              disabled={downloading}
              className="flex-1 sm:flex-initial flex items-center justify-center gap-2 px-3.5 py-1.5 rounded-full bg-white text-black font-semibold text-xs hover:bg-neutral-200 transition-all disabled:opacity-50"
            >
              <Download className="w-3.5 h-3.5" />
              <span>{downloading ? 'Compactando...' : 'Baixar Projeto (.ZIP)'}</span>
            </button>
          </div>
        </div>

        {/* Content Body: Sidebar list + Code preview */}
        <div className="flex-1 flex flex-col sm:flex-row overflow-hidden">
          {/* File Explorer Sidebar */}
          <div className="w-full sm:w-72 border-r border-white/5 bg-[#141416] p-3 overflow-y-auto flex flex-col gap-1 select-none">
            <div className="text-[11px] font-semibold text-[#85858C] uppercase tracking-wider px-2 py-1 flex items-center gap-1.5">
              <FolderTree className="w-3.5 h-3.5" />
              <span>Arquivos do Projeto</span>
            </div>

            {files.map((file) => {
              const isSelected = selectedFile.path === file.path;
              return (
                <button
                  key={file.path}
                  onClick={() => setSelectedFile(file)}
                  className={`w-full text-left px-2.5 py-2 rounded-xl text-xs transition-colors flex items-center justify-between gap-2 ${
                    isSelected
                      ? 'bg-[#242428] text-white font-medium'
                      : 'text-[#85858C] hover:text-[#F2F2F4] hover:bg-[#1A1A1D]'
                  }`}
                >
                  <div className="flex items-center gap-2 truncate">
                    <FileCode className="w-3.5 h-3.5 text-white/70 flex-shrink-0" />
                    <span className="truncate font-mono text-[11px]">{file.path}</span>
                  </div>
                  <span className="text-[9px] uppercase tracking-wider text-[#85858C] font-mono px-1 py-0.5 rounded bg-black/40">
                    {file.category}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Main Code View */}
          <div className="flex-1 flex flex-col bg-[#0F0F11] overflow-hidden">
            {/* Tab header */}
            <div className="flex items-center justify-between px-4 py-2.5 border-b border-white/5 bg-[#121214]">
              <div className="flex items-center gap-2">
                <span className="font-mono text-xs text-white">{selectedFile.path}</span>
                <span className="text-xs text-[#85858C]">— {selectedFile.description}</span>
              </div>

              <button
                onClick={handleCopy}
                className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-[#242428] text-xs text-white hover:bg-[#323238] transition-colors"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copied ? 'Copiado!' : 'Copiar'}</span>
              </button>
            </div>

            {/* Code Pre container */}
            <div className="flex-1 p-4 overflow-auto font-mono text-xs text-[#E1E1E6] leading-relaxed selection:bg-white/20">
              <pre className="whitespace-pre">{selectedFile.content}</pre>
            </div>
          </div>
        </div>

        {/* Footer Instructions */}
        <div className="px-5 py-3 border-t border-white/5 bg-[#121214] flex flex-wrap items-center justify-between gap-2 text-xs text-[#85858C]">
          <span>
            💡 Compatível com <strong>Android Studio Ladybug (2024.2+)</strong>, <strong>Health Connect SDK 1.1.0</strong> e Android 9.0+ (API 28+).
          </span>
          <span className="text-white/80">
            Total de {files.length} arquivos gerados e empacotados.
          </span>
        </div>
      </div>
    </div>
  );
};
