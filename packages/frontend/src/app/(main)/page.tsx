'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiClient } from '@/lib/api-client';
import { ClockButtons } from '@/components/clock/ClockButtons';
import { TodayRecord } from '@/components/clock/TodayRecord';
import { MonthSummaryCard } from '@/components/clock/MonthSummaryCard';
import { ClockStatusResponse, MonthlySummaryResponse } from '@/types/clock';

const TEMP_USER_ID = '00000000-0000-0000-0000-000000000010';

export default function DashboardPage() {
  const [status, setStatus] = useState<ClockStatusResponse | null>(null);
  const [summary, setSummary] = useState<MonthlySummaryResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const fetchStatus = useCallback(async () => {
    const data = await apiClient<ClockStatusResponse>(
      `/clock/status?userId=${TEMP_USER_ID}`
    );
    setStatus(data);
  }, []);

  const fetchSummary = useCallback(async () => {
    const now = new Date();
    const data = await apiClient<MonthlySummaryResponse>(
      `/time-records/summary?userId=${TEMP_USER_ID}&year=${now.getFullYear()}&month=${now.getMonth() + 1}`
    );
    setSummary(data);
  }, []);

  useEffect(() => {
    fetchStatus();
    fetchSummary();
  }, [fetchStatus, fetchSummary]);

  const handleClock = async (action: string) => {
    setIsLoading(true);
    try {
      await apiClient(`/clock/${action}?userId=${TEMP_USER_ID}`, { method: 'POST' });
      await fetchStatus();
      await fetchSummary();
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">ダッシュボード</h1>

      <ClockButtons
        status={status}
        onClockIn={() => handleClock('in')}
        onClockOut={() => handleClock('out')}
        onGoOut={() => handleClock('go-out')}
        onReturn={() => handleClock('return')}
        isLoading={isLoading}
      />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <TodayRecord status={status} />
        <MonthSummaryCard summary={summary} />
      </div>
    </div>
  );
}
