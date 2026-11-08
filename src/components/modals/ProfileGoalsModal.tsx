import React, { useState } from 'react';
import { X, User, Target, Check } from 'lucide-react';
import { UserProfile } from '../../types/vital.ts';
import { calculateBMI } from '../../services/dailyInsightEngine.ts';
import { HealthConnectRepository } from '../../services/healthConnectRepository.ts';

interface ProfileGoalsModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: UserProfile;
  initialTab?: 'profile' | 'goals';
  onSaveUser: (updated: UserProfile) => void;
}

export const ProfileGoalsModal: React.FC<ProfileGoalsModalProps> = ({
  isOpen,
  onClose,
  user,
  initialTab = 'profile',
  onSaveUser,
}) => {
  const [activeTab, setActiveTab] = useState<'profile' | 'goals'>(initialTab);
  const [formData, setFormData] = useState<UserProfile>(user);
  const [savedToast, setSavedToast] = useState(false);

  if (!isOpen) return null;

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    onSaveUser(formData);
    HealthConnectRepository.saveProfile(formData);
    setSavedToast(true);
    setTimeout(() => {
      setSavedToast(false);
      onClose();
    }, 600);
  };

  const bmiCalc = calculateBMI(formData.weightKg, formData.heightCm);

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-end sm:items-center justify-center p-0 sm:p-4 animate-in fade-in select-none">
      <div className="w-full max-w-md bg-[#171719] border border-white/10 rounded-t-3xl sm:rounded-3xl p-5 max-h-[90vh] flex flex-col shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/5">
          <div className="flex items-center gap-2.5">
            {activeTab === 'profile' ? (
              <User className="w-5 h-5 text-white" />
            ) : (
              <Target className="w-5 h-5 text-white" />
            )}
            <h2 className="text-base font-bold text-white">
              {activeTab === 'profile' ? 'Perfil do Usuário' : 'Metas Diárias e Semanais'}
            </h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full text-[#85858C] hover:text-white hover:bg-[#242428]"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab switch */}
        <div className="flex rounded-xl bg-[#121214] p-1 my-3 border border-white/5">
          <button
            type="button"
            onClick={() => setActiveTab('profile')}
            className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all ${
              activeTab === 'profile' ? 'bg-[#242428] text-white' : 'text-[#85858C] hover:text-white'
            }`}
          >
            Dados Pessoais
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('goals')}
            className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all ${
              activeTab === 'goals' ? 'bg-[#242428] text-white' : 'text-[#85858C] hover:text-white'
            }`}
          >
            Metas &amp; Objetivos
          </button>
        </div>

        <form onSubmit={handleSave} className="flex-1 overflow-y-auto flex flex-col gap-3 py-1">
          {activeTab === 'profile' ? (
            <>
              <div>
                <label className="text-xs text-[#85858C] block mb-1">Nome Completo</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-2xl bg-[#242428] border border-white/5 text-white text-xs focus:outline-none focus:border-white/20"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs text-[#85858C] block mb-1">Idade (anos)</label>
                  <input
                    type="number"
                    value={formData.age}
                    onChange={(e) => setFormData({ ...formData, age: Number(e.target.value) })}
                    className="w-full px-3.5 py-2.5 rounded-2xl bg-[#242428] border border-white/5 text-white text-xs focus:outline-none focus:border-white/20"
                    min="1"
                    max="120"
                    required
                  />
                </div>

                <div>
                  <label className="text-xs text-[#85858C] block mb-1">Altura (cm)</label>
                  <input
                    type="number"
                    value={formData.heightCm}
                    onChange={(e) => setFormData({ ...formData, heightCm: Number(e.target.value) })}
                    className="w-full px-3.5 py-2.5 rounded-2xl bg-[#242428] border border-white/5 text-white text-xs focus:outline-none focus:border-white/20"
                    min="50"
                    max="250"
                    required
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs text-[#85858C] block mb-1">Peso Atual (kg)</label>
                  <input
                    type="number"
                    step="0.1"
                    value={formData.weightKg}
                    onChange={(e) => setFormData({ ...formData, weightKg: Number(e.target.value) })}
                    className="w-full px-3.5 py-2.5 rounded-2xl bg-[#242428] border border-white/5 text-white text-xs focus:outline-none focus:border-white/20"
                    min="20"
                    max="300"
                    required
                  />
                </div>

                <div>
                  <label className="text-xs text-[#85858C] block mb-1">Sexo (Opcional)</label>
                  <select
                    value={formData.gender}
                    onChange={(e) => setFormData({ ...formData, gender: e.target.value as any })}
                    className="w-full px-3.5 py-2.5 rounded-2xl bg-[#242428] border border-white/5 text-white text-xs focus:outline-none focus:border-white/20"
                  >
                    <option value="male">Masculino</option>
                    <option value="female">Feminino</option>
                    <option value="other">Outro</option>
                    <option value="prefer_not_to_say">Prefiro não dizer</option>
                  </select>
                </div>
              </div>

              {/* Live BMI preview */}
              <div className="p-3.5 rounded-2xl bg-[#121214] border border-white/5 flex items-center justify-between text-xs">
                <div>
                  <span className="text-[#85858C] block text-[11px]">Índice de Massa Corporal (IMC)</span>
                  <span className="text-white font-bold text-sm">{bmiCalc.bmi}</span>
                </div>
                <span className="px-2.5 py-1 rounded-full bg-[#242428] text-white text-xs font-medium">
                  {bmiCalc.classification}
                </span>
              </div>
            </>
          ) : (
            <>
              <div>
                <label className="text-xs text-[#85858C] block mb-1">Meta de Passos Diários</label>
                <input
                  type="number"
                  step="500"
                  value={formData.goals.dailySteps}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      goals: { ...formData.goals, dailySteps: Number(e.target.value) },
                    })
                  }
                  className="w-full px-3.5 py-2.5 rounded-2xl bg-[#242428] border border-white/5 text-white text-xs focus:outline-none focus:border-white/20"
                  min="1000"
                  max="50000"
                  required
                />
              </div>

              <div>
                <label className="text-xs text-[#85858C] block mb-1">Calorias Ativas Diárias (kcal)</label>
                <input
                  type="number"
                  step="50"
                  value={formData.goals.activeCalories}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      goals: { ...formData.goals, activeCalories: Number(e.target.value) },
                    })
                  }
                  className="w-full px-3.5 py-2.5 rounded-2xl bg-[#242428] border border-white/5 text-white text-xs focus:outline-none focus:border-white/20"
                  min="100"
                  max="5000"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs text-[#85858C] block mb-1">Meta de Sono (horas)</label>
                  <input
                    type="number"
                    step="0.5"
                    value={formData.goals.sleepMinutes / 60}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        goals: { ...formData.goals, sleepMinutes: Math.round(Number(e.target.value) * 60) },
                      })
                    }
                    className="w-full px-3.5 py-2.5 rounded-2xl bg-[#242428] border border-white/5 text-white text-xs focus:outline-none focus:border-white/20"
                    min="4"
                    max="14"
                    required
                  />
                </div>

                <div>
                  <label className="text-xs text-[#85858C] block mb-1">Treinos por Semana</label>
                  <input
                    type="number"
                    value={formData.goals.weeklyWorkouts}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        goals: { ...formData.goals, weeklyWorkouts: Number(e.target.value) },
                      })
                    }
                    className="w-full px-3.5 py-2.5 rounded-2xl bg-[#242428] border border-white/5 text-white text-xs focus:outline-none focus:border-white/20"
                    min="1"
                    max="7"
                    required
                  />
                </div>
              </div>
            </>
          )}

          <div className="pt-3 border-t border-white/5 flex items-center justify-end gap-2 mt-auto">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-full text-xs text-[#85858C] hover:text-white"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="flex items-center gap-1.5 px-5 py-2 rounded-full bg-white text-black text-xs font-semibold hover:bg-neutral-200 transition-colors"
            >
              {savedToast ? <Check className="w-3.5 h-3.5 text-black" /> : null}
              <span>{savedToast ? 'Salvo!' : 'Salvar Alterações'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
