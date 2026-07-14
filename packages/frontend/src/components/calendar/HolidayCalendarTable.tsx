'use client';

import { useState } from 'react';
import { HolidayResponse } from '@/types/clock';

interface HolidayCalendarTableProps {
  holidays: HolidayResponse[];
  onAdd: (date: string, name: string) => void;
  onDelete: (id: string) => void;
}

export function HolidayCalendarTable({ holidays, onAdd, onDelete }: HolidayCalendarTableProps) {
  const [date, setDate] = useState('');
  const [name, setName] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (date && name) {
      onAdd(date, name);
      setDate('');
      setName('');
    }
  };

  return (
    <div className="space-y-4">
      <form onSubmit={handleSubmit} className="flex gap-3 items-end">
        <div>
          <label htmlFor="holiday-date" className="block text-sm font-medium text-gray-700">日付</label>
          <input
            id="holiday-date"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="mt-1 rounded-md border border-gray-300 px-3 py-2 text-sm"
            required
          />
        </div>
        <div>
          <label htmlFor="holiday-name" className="block text-sm font-medium text-gray-700">名称</label>
          <input
            id="holiday-name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="mt-1 rounded-md border border-gray-300 px-3 py-2 text-sm"
            placeholder="例: お盆休み"
            required
          />
        </div>
        <button
          type="submit"
          className="rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
        >
          追加
        </button>
      </form>

      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">日付</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">名称</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">操作</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {holidays.map((holiday) => (
            <tr key={holiday.id}>
              <td className="px-4 py-3 text-sm">{holiday.holidayDate}</td>
              <td className="px-4 py-3 text-sm">{holiday.holidayName}</td>
              <td className="px-4 py-3 text-sm">
                <button
                  onClick={() => onDelete(holiday.id)}
                  className="text-red-600 hover:text-red-800 text-sm"
                >
                  削除
                </button>
              </td>
            </tr>
          ))}
          {holidays.length === 0 && (
            <tr>
              <td colSpan={3} className="px-4 py-6 text-center text-gray-500 text-sm">
                休日が登録されていません
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
