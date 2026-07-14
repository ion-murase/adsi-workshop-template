'use client';

import { TimeRecordResponse } from '@/types/clock';

interface TimeRecordTableProps {
  records: TimeRecordResponse[];
}

function formatTime(isoString: string | null): string {
  if (!isoString) return '--:--';
  return new Date(isoString).toLocaleTimeString('ja-JP', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatMinutes(minutes: number | null): string {
  if (minutes === null) return '-';
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}:${String(m).padStart(2, '0')}`;
}

export function TimeRecordTable({ records }: TimeRecordTableProps) {
  if (records.length === 0) {
    return <p className="text-gray-500 text-center py-8">勤怠記録がありません</p>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">日付</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">出勤</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">退勤</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">勤務</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">残業</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">深夜</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {records.map((record) => (
            <tr key={record.id} className={record.isHoliday ? 'bg-red-50' : ''}>
              <td className="px-4 py-3 text-sm whitespace-nowrap">{record.workDate}</td>
              <td className="px-4 py-3 text-sm whitespace-nowrap">{formatTime(record.clockIn)}</td>
              <td className="px-4 py-3 text-sm whitespace-nowrap">{formatTime(record.clockOut)}</td>
              <td className="px-4 py-3 text-sm whitespace-nowrap">{formatMinutes(record.workMinutes)}</td>
              <td className="px-4 py-3 text-sm whitespace-nowrap text-orange-600">{formatMinutes(record.overtimeMinutes)}</td>
              <td className="px-4 py-3 text-sm whitespace-nowrap text-purple-600">{formatMinutes(record.nightMinutes)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
