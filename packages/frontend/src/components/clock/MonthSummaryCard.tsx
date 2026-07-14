'use client';

import { MonthlySummaryResponse } from '@/types/clock';

interface MonthSummaryCardProps {
  summary: MonthlySummaryResponse | null;
}

function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h ${m}m`;
}

export function MonthSummaryCard({ summary }: MonthSummaryCardProps) {
  if (!summary) return null;

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <h3 className="text-sm font-medium text-gray-500 mb-3">
        {summary.year}年{summary.month}月 集計
      </h3>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <p className="text-xs text-gray-500">勤務日数</p>
          <p className="text-lg font-bold">{summary.workDays}日</p>
        </div>
        <div>
          <p className="text-xs text-gray-500">総勤務時間</p>
          <p className="text-lg font-bold">{formatMinutes(summary.totalWorkMinutes)}</p>
        </div>
        <div>
          <p className="text-xs text-gray-500">残業時間</p>
          <p className="text-lg font-bold text-orange-600">
            {formatMinutes(summary.totalOvertimeMinutes)}
          </p>
        </div>
        <div>
          <p className="text-xs text-gray-500">深夜時間</p>
          <p className="text-lg font-bold text-purple-600">
            {formatMinutes(summary.totalNightMinutes)}
          </p>
        </div>
      </div>
    </div>
  );
}
