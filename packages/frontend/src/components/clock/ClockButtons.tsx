'use client';

import { ClockStatusResponse } from '@/types/clock';

interface ClockButtonsProps {
  status: ClockStatusResponse | null;
  onClockIn: () => void;
  onClockOut: () => void;
  onGoOut: () => void;
  onReturn: () => void;
  isLoading: boolean;
}

export function ClockButtons({
  status,
  onClockIn,
  onClockOut,
  onGoOut,
  onReturn,
  isLoading,
}: ClockButtonsProps) {
  const state = status?.currentState ?? 'NOT_CLOCKED_IN';

  return (
    <div className="flex flex-wrap gap-3">
      <button
        onClick={onClockIn}
        disabled={isLoading || state !== 'NOT_CLOCKED_IN'}
        className="rounded-lg bg-blue-600 px-6 py-3 text-white font-medium disabled:opacity-40 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors"
      >
        出勤
      </button>
      <button
        onClick={onClockOut}
        disabled={isLoading || state !== 'WORKING'}
        className="rounded-lg bg-red-600 px-6 py-3 text-white font-medium disabled:opacity-40 disabled:cursor-not-allowed hover:bg-red-700 transition-colors"
      >
        退勤
      </button>
      <button
        onClick={onGoOut}
        disabled={isLoading || state !== 'WORKING'}
        className="rounded-lg bg-yellow-600 px-6 py-3 text-white font-medium disabled:opacity-40 disabled:cursor-not-allowed hover:bg-yellow-700 transition-colors"
      >
        外出
      </button>
      <button
        onClick={onReturn}
        disabled={isLoading || state !== 'OUT'}
        className="rounded-lg bg-green-600 px-6 py-3 text-white font-medium disabled:opacity-40 disabled:cursor-not-allowed hover:bg-green-700 transition-colors"
      >
        戻り
      </button>
    </div>
  );
}
