/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useMemo } from 'react';
import {
  TabType,
  UserProfile,
  WidgetConfig,
  ZeppConnectorState,
  WorkoutSession,
  HeartRateData,
  StepsData,
  CaloriesData,
  SleepData,
  ExerciseData,
  BodyCompositionData,
  AdditionalMetrics,
} from './types/vital.ts';
import {
  HealthConnectRepository,
  INITIAL_USER_PROFILE,
  INITIAL_HEART_RATE,
  INITIAL_STEPS,
  INITIAL_CALORIES,
  INITIAL_SLEEP,
  INITIAL_EXERCISE,
  INITIAL_BODY_COMPOSITION,
  INITIAL_ADDITIONAL_METRICS,
} from './services/healthConnectRepository.ts';
import { DailyInsightEngine } from './services/dailyInsightEngine.ts';
import { PhoneStatusBar } from './components/PhoneStatusBar.tsx';
import { VitalNavBar } from './components/VitalNavBar.tsx';
import { HomeScreen } from './components/screens/HomeScreen.tsx';
import { SleepScreen } from './components/screens/SleepScreen.tsx';
import { ExerciseScreen } from './components/screens/ExerciseScreen.tsx';
import { SettingsScreen } from './components/screens/SettingsScreen.tsx';

// Modals
import { AndroidProjectModal } from './components/modals/AndroidProjectModal.tsx';
import { HealthConnectModal } from './components/modals/HealthConnectModal.tsx';
import { ZeppModal } from './components/modals/ZeppModal.tsx';
import { CustomizeWidgetsModal } from './components/modals/CustomizeWidgetsModal.tsx';
import { ProfileGoalsModal } from './components/modals/ProfileGoalsModal.tsx';
import { HeartRateDetailModal } from './components/modals/HeartRateDetailModal.tsx';
import { WorkoutDetailModal } from './components/modals/WorkoutDetailModal.tsx';
import { PrivacyModal } from './components/modals/PrivacyModal.tsx';
import { AboutModal } from './components/modals/AboutModal.tsx';
import { OptionsMenuModal } from './components/modals/OptionsMenuModal.tsx';
import { Smartphone, Code2 } from 'lucide-react';

export default function App() {
  // Navigation State (Início é a tela principal)
  const [currentTab, setCurrentTab] = useState<TabType>('home');

  // Application Data States
  const [userProfile, setUserProfile] = useState<UserProfile>(() =>
    HealthConnectRepository.getProfile()
  );
  const [widgets, setWidgets] = useState<WidgetConfig[]>(() =>
    HealthConnectRepository.getWidgetsConfig()
  );
  const [isDemoMode, setIsDemoMode] = useState<boolean>(() =>
    HealthConnectRepository.isDemoMode()
  );
  const [zeppState, setZeppState] = useState<ZeppConnectorState>(() =>
    HealthConnectRepository.getZeppState()
  );

  // Metrics Data
  const [heartRate, setHeartRate] = useState<HeartRateData>(INITIAL_HEART_RATE);
  const [steps, setSteps] = useState<StepsData>(INITIAL_STEPS);
  const [calories, setCalories] = useState<CaloriesData>(INITIAL_CALORIES);
  const [sleep, setSleep] = useState<SleepData>(INITIAL_SLEEP);
  const [exercise, setExercise] = useState<ExerciseData>(INITIAL_EXERCISE);
  const [bodyComposition, setBodyComposition] =
    useState<BodyCompositionData>(INITIAL_BODY_COMPOSITION);
  const [additionalMetrics, setAdditionalMetrics] =
    useState<AdditionalMetrics>(INITIAL_ADDITIONAL_METRICS);

  // Modals Visibility
  const [isAndroidModalOpen, setIsAndroidModalOpen] = useState(false);
  const [isHealthConnectModalOpen, setIsHealthConnectModalOpen] = useState(false);
  const [isZeppModalOpen, setIsZeppModalOpen] = useState(false);
  const [isCustomizeWidgetsOpen, setIsCustomizeWidgetsOpen] = useState(false);
  const [profileGoalsTab, setProfileGoalsTab] = useState<'profile' | 'goals'>('profile');
  const [isProfileGoalsOpen, setIsProfileGoalsOpen] = useState(false);
  const [isHeartRateDetailOpen, setIsHeartRateDetailOpen] = useState(false);
  const [selectedWorkout, setSelectedWorkout] = useState<WorkoutSession | null>(null);
  const [isPrivacyOpen, setIsPrivacyOpen] = useState(false);
  const [isAboutOpen, setIsAboutOpen] = useState(false);
  const [isOptionsMenuOpen, setIsOptionsMenuOpen] = useState(false);

  // Responsive device container frame mode (for comfortable desktop evaluation)
  const [showPhoneFrame, setShowPhoneFrame] = useState(true);

  // Generate real-time daily insights
  const dailyInsights = useMemo(() => {
    return DailyInsightEngine.generateInsights({
      steps,
      sleep,
      exercise,
      heartRate,
      userGoalSteps: userProfile.goals.dailySteps,
    });
  }, [steps, sleep, exercise, heartRate, userProfile.goals.dailySteps]);

  const handleToggleDemoMode = () => {
    const nextVal = !isDemoMode;
    setIsDemoMode(nextVal);
    HealthConnectRepository.setDemoMode(nextVal);
  };

  const handleDataReset = () => {
    setUserProfile(INITIAL_USER_PROFILE);
    setWidgets(HealthConnectRepository.getWidgetsConfig());
    setIsDemoMode(true);
    setZeppState(HealthConnectRepository.getZeppState());
  };

  return (
    <div className="min-h-screen bg-[#090909] text-[#F2F2F4] flex flex-col items-center justify-start overflow-x-hidden">
      {/* Desktop Top Bar utility: Export Android Code & Viewport switch */}
      <div className="w-full max-w-4xl px-4 py-2 flex items-center justify-between text-xs text-[#85858C] border-b border-white/5 select-none hidden sm:flex">
        <div className="flex items-center gap-2">
          <span className="font-bold text-white tracking-wide">VITAL</span>
          <span>• Painel de Saúde Android (Material 3 &amp; Health Connect)</span>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowPhoneFrame(!showPhoneFrame)}
            className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#171719] border border-white/10 hover:text-white transition-colors"
          >
            <Smartphone className="w-3.5 h-3.5" />
            <span>{showPhoneFrame ? 'Modo Tela Cheia' : 'Moldura Android (390px)'}</span>
          </button>

          <button
            onClick={() => setIsAndroidModalOpen(true)}
            className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-white text-black font-semibold hover:bg-neutral-200 transition-colors shadow-sm"
          >
            <Code2 className="w-3.5 h-3.5" />
            <span>Código Android Studio (.zip)</span>
          </button>
        </div>
      </div>

      {/* Main App Container: Framed or Full width */}
      <main
        className={`w-full relative flex flex-col min-h-screen transition-all ${
          showPhoneFrame
            ? 'max-w-[430px] sm:my-3 sm:min-h-[860px] sm:rounded-[44px] sm:border sm:border-white/10 sm:shadow-[0_0_50px_rgba(0,0,0,0.9)] bg-[#090909] overflow-hidden'
            : 'max-w-2xl'
        }`}
      >
        {/* Android Status Bar */}
        <PhoneStatusBar
          isDemoMode={isDemoMode}
          onToggleDemoMode={handleToggleDemoMode}
          onOpenSync={() => setIsHealthConnectModalOpen(true)}
        />

        {/* Tab Screens */}
        <div className="flex-1 w-full overflow-y-auto">
          {currentTab === 'home' && (
            <HomeScreen
              user={userProfile}
              heartRate={heartRate}
              steps={steps}
              calories={calories}
              exercise={exercise}
              body={bodyComposition}
              additionalMetrics={additionalMetrics}
              widgets={widgets}
              insights={dailyInsights}
              onOpenHeartRateDetail={() => setIsHeartRateDetailOpen(true)}
              onOpenExerciseTab={() => setCurrentTab('exercise')}
              onOpenBodyDetail={() => {
                setProfileGoalsTab('profile');
                setIsProfileGoalsOpen(true);
              }}
              onOpenCustomizeWidgets={() => setIsCustomizeWidgetsOpen(true)}
              onOpenHealthConnect={() => setIsHealthConnectModalOpen(true)}
              onOpenOptionsMenu={() => setIsOptionsMenuOpen(true)}
            />
          )}

          {currentTab === 'sleep' && (
            <SleepScreen
              sleep={sleep}
              onOpenOptionsMenu={() => setIsOptionsMenuOpen(true)}
            />
          )}

          {currentTab === 'exercise' && (
            <ExerciseScreen
              exercise={exercise}
              onOpenOptionsMenu={() => setIsOptionsMenuOpen(true)}
              onSelectWorkout={(w) => setSelectedWorkout(w)}
            />
          )}

          {currentTab === 'settings' && (
            <SettingsScreen
              user={userProfile}
              zeppState={zeppState}
              isDemoMode={isDemoMode}
              onOpenProfile={() => {
                setProfileGoalsTab('profile');
                setIsProfileGoalsOpen(true);
              }}
              onOpenGoals={() => {
                setProfileGoalsTab('goals');
                setIsProfileGoalsOpen(true);
              }}
              onOpenDevices={() => setIsZeppModalOpen(true)}
              onOpenPrivacy={() => setIsPrivacyOpen(true)}
              onOpenAbout={() => setIsAboutOpen(true)}
              onOpenCustomizeWidgets={() => setIsCustomizeWidgetsOpen(true)}
              onToggleDemoMode={handleToggleDemoMode}
              onOpenOptionsMenu={() => setIsOptionsMenuOpen(true)}
            />
          )}
        </div>

        {/* Floating Pill Navigation (Galaxy Store inspired) */}
        <VitalNavBar currentTab={currentTab} onSelectTab={(tab) => setCurrentTab(tab)} />
      </main>

      {/* Modals Container */}
      <AndroidProjectModal
        isOpen={isAndroidModalOpen}
        onClose={() => setIsAndroidModalOpen(false)}
      />

      <HealthConnectModal
        isOpen={isHealthConnectModalOpen}
        onClose={() => setIsHealthConnectModalOpen(false)}
      />

      <ZeppModal
        isOpen={isZeppModalOpen}
        onClose={() => setIsZeppModalOpen(false)}
        zeppState={zeppState}
        onUpdateState={(st) => setZeppState(st)}
      />

      <CustomizeWidgetsModal
        isOpen={isCustomizeWidgetsOpen}
        onClose={() => setIsCustomizeWidgetsOpen(false)}
        widgets={widgets}
        onSaveWidgets={(updated) => setWidgets(updated)}
      />

      <ProfileGoalsModal
        isOpen={isProfileGoalsOpen}
        onClose={() => setIsProfileGoalsOpen(false)}
        user={userProfile}
        initialTab={profileGoalsTab}
        onSaveUser={(u) => setUserProfile(u)}
      />

      <HeartRateDetailModal
        isOpen={isHeartRateDetailOpen}
        onClose={() => setIsHeartRateDetailOpen(false)}
        data={heartRate}
      />

      <WorkoutDetailModal
        workout={selectedWorkout}
        onClose={() => setSelectedWorkout(null)}
      />

      <PrivacyModal
        isOpen={isPrivacyOpen}
        onClose={() => setIsPrivacyOpen(false)}
        onDataReset={handleDataReset}
      />

      <AboutModal
        isOpen={isAboutOpen}
        onClose={() => setIsAboutOpen(false)}
      />

      <OptionsMenuModal
        isOpen={isOptionsMenuOpen}
        onClose={() => setIsOptionsMenuOpen(false)}
        isDemoMode={isDemoMode}
        onToggleDemoMode={handleToggleDemoMode}
        onOpenCustomizeWidgets={() => setIsCustomizeWidgetsOpen(true)}
        onOpenHealthConnect={() => setIsHealthConnectModalOpen(true)}
        onOpenZepp={() => setIsZeppModalOpen(true)}
        onOpenGoals={() => {
          setProfileGoalsTab('goals');
          setIsProfileGoalsOpen(true);
        }}
      />
    </div>
  );
}
