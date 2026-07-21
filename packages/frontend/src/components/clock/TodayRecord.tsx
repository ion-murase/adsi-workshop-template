'use client';

import { ClockStatusResponse } from '@/types/clock';

interface TodayRecordProps {
  status: ClockStatusResponse | null;
}

function formatTime(isoString: string | null): string {
  if (!isoString) return '--:--';
  return new Date(isoString).toLocaleTimeString('ja-JP', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

const STATE_LABELS: Record<string, string> = {
  NOT_CLOCKED_IN: '未出勤',
  WORKING: '勤務中',
  OUT: '外出中',
  CLOCKED_OUT: '退勤済み',
};

export function TodayRecord({ status }: TodayRecordProps) {
  if (!status) return null;

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <h3 className="text-sm font-medium text-gray-500 mb-2">本日の勤怠</h3>
      <div className="space-y-2">
        <div className="flex justify-between">
          <span className="text-gray-600">状態</span>
          <span className="font-medium">{STATE_LABELS[status.currentState]}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">出勤</span>
          <span className="font-medium">{formatTime(status.clockIn)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">退勤</span>
          <span className="font-medium">{formatTime(status.clockOut)}</span>
        </div>
        {status.entries.length > 0 && (
          <div className="border-t pt-2 mt-2">
            <span className="text-xs text-gray-500">外出/戻り</span>
            {status.entries.map((entry, i) => (
              <div key={i} className="flex justify-between text-sm">
                <span>{entry.entryType === 'GO_OUT' ? '外出' : '戻り'}</span>
                <span>{formatTime(entry.recordedAt)}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
