import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { MonthSummaryCard } from './MonthSummaryCard';

describe('MonthSummaryCard', () => {
  it('nullの場合は何も表示しない', () => {
    const { container } = render(<MonthSummaryCard summary={null} />);
    expect(container.firstChild).toBeNull();
  });

  it('集計データを正しく表示', () => {
    const summary = {
      year: 2026,
      month: 7,
      workDays: 15,
      totalWorkMinutes: 7200,
      totalOvertimeMinutes: 450,
      totalNightMinutes: 60,
      totalHolidayWorkMinutes: 0,
    };

    render(<MonthSummaryCard summary={summary} />);
    expect(screen.getByText('2026年7月 集計')).toBeInTheDocument();
    expect(screen.getByText('15日')).toBeInTheDocument();
    expect(screen.getByText('120h 0m')).toBeInTheDocument();
    expect(screen.getByText('7h 30m')).toBeInTheDocument();
  });
});
