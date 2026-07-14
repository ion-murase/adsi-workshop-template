'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiClient } from '@/lib/api-client';
import { TimeRecordTable } from '@/components/time-records/TimeRecordTable';
import { TimeRecordResponse } from '@/types/clock';

const TEMP_USER_ID = '00000000-0000-0000-0000-000000000010';

export default function TimeRecordsPage() {
  const [records, setRecords] = useState<TimeRecordResponse[]>([]);
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);

  const fetchRecords = useCallback(async () => {
    const data = await apiClient<TimeRecordResponse[]>(
      `/time-records?userId=${TEMP_USER_ID}&year=${year}&month=${month}`
    );
    setRecords(data);
  }, [year, month]);

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
