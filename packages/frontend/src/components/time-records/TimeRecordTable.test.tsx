import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { TimeRecordTable } from './TimeRecordTable';
import { TimeRecordResponse } from '@/types/clock';

describe('TimeRecordTable', () => {
  it('レコードがない場合、メッセージを表示', () => {
    render(<TimeRecordTable records={[]} />);
    expect(screen.getByText('勤怠記録がありません')).toBeInTheDocument();
  });

  it('レコードがある場合、テーブルに表示', () => {
    const records: TimeRecordResponse[] = [
      {
        id: '1',
        workDate: '2026-07-14',
        clockIn: '2026-07-14T09:00:00+09:00',
        clockOut: '2026-07-14T18:00:00+09:00',
        breakMinutes: 60,
        workMinutes: 480,
        overtimeMinutes: 30,
        nightMinutes: 0,
        holidayWorkMinutes: 0,
        isHoliday: false,
      },
    ];

    render(<TimeRecordTable records={records} />);
    expect(screen.getByText('2026-07-14')).toBeInTheDocument();
    expect(screen.getByText('8:00')).toBeInTheDocument();
    expect(screen.getByText('0:30')).toBeInTheDocument();
  });
});
