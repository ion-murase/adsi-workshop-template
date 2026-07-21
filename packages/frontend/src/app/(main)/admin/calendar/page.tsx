'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiClient } from '@/lib/api-client';
import { HolidayCalendarTable } from '@/components/calendar/HolidayCalendarTable';
import { HolidayResponse } from '@/types/clock';

export default function CalendarPage() {
  const [holidays, setHolidays] = useState<HolidayResponse[]>([]);
  const [year, setYear] = useState(new Date().getFullYear());

  const fetchHolidays = useCallback(async () => {
    const data = await apiClient<HolidayResponse[]>(`/calendar/holidays?year=${year}`);
    setHolidays(data);
  }, [year]);

  useEffect(() => {
    fetchHolidays();
  }, [fetchHolidays]);

  const handleAdd = async (date: string, name: string) => {
    await apiClient('/calendar/holidays', {
      method: 'POST',
      body: JSON.stringify({ holidayDate: date, holidayName: name }),
    });
    await fetchHolidays();
  };

  const handleDelete = async (id: string) => {
    await apiClient(`/calendar/holidays/${id}`, { method: 'DELETE' });
    await fetchHolidays();
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">会社カレンダー</h1>

      <div className="flex items-center gap-4">
        <button onClick={() => setYear(year - 1)} className="px-3 py-1 rounded border hover:bg-gray-100">
          ←
        </button>
        <span className="font-medium">{year}年度</span>
        <button onClick={() => setYear(year + 1)} className="px-3 py-1 rounded border hover:bg-gray-100">
          →
        </button>
      </div>

      <HolidayCalendarTable holidays={holidays} onAdd={handleAdd} onDelete={handleDelete} />
    </div>
  );
}
