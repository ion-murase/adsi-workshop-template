'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiClient } from '@/lib/api-client';
import { TimeRecordTable } from '@/components/time-records/TimeRecordTable';
import { TimeRecordResponse } from '@/types/clock';

export default function TimeRecordsPage() {
  const [records, setRecords] = useState<TimeRecordResponse[]>([]);
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [userId, setUserId] = useState<string | null>(null);

  useEffect(() => {
    setUserId(localStorage.getItem('userId'));
  }, []);

  const fetchRecords = useCallback(async () => {
    if (!userId) return;
    const data = await apiClient<TimeRecordResponse[]>(
      `/time-records?userId=${userId}&year=${year}&month=${month}`
    );
    setRecords(data);
  }, [userId, year, month]);

  useEffect(() => {
    fetchRecords();
  }, [fetchRecords]);

  const handlePrev = () => {
    if (month === 1) {
      setYear(year - 1);
      setMonth(12);
    } else {
      setMonth(month - 1);
    }
  };

  const handleNext = () => {
    if (month === 12) {
      setYear(year + 1);
      setMonth(1);
    } else {
      setMonth(month + 1);
    }
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">勤務履歴</h1>

      <div className="flex items-center gap-4">
        <button onClick={handlePrev} className="px-3 py-1 rounded border hover:bg-gray-100">
          ←
        </button>
        <span className="font-medium">{year}年{month}月</span>
        <button onClick={handleNext} className="px-3 py-1 rounded border hover:bg-gray-100">
          →
        </button>
      </div>

      <TimeRecordTable records={records} />
    </div>
  );
}
