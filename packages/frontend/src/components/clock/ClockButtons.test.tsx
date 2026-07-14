import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ClockButtons } from './ClockButtons';
import { ClockStatusResponse } from '@/types/clock';

function makeStatus(state: ClockStatusResponse['currentState']): ClockStatusResponse {
  return {
    workDate: '2026-07-14',
    clockIn: state !== 'NOT_CLOCKED_IN' ? '2026-07-14T09:00:00+09:00' : null,
    clockOut: state === 'CLOCKED_OUT' ? '2026-07-14T18:00:00+09:00' : null,
    currentState: state,
    entries: [],
  };
}

describe('ClockButtons', () => {
  const handlers = {
    onClockIn: vi.fn(),
    onClockOut: vi.fn(),
    onGoOut: vi.fn(),
    onReturn: vi.fn(),
  };

  it('未出勤状態: 出勤ボタンのみ有効', () => {
    render(
      <ClockButtons status={makeStatus('NOT_CLOCKED_IN')} isLoading={false} {...handlers} />
    );

    expect(screen.getByRole('button', { name: '出勤' })).not.toBeDisabled();
    expect(screen.getByRole('button', { name: '退勤' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '外出' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '戻り' })).toBeDisabled();
  });

  it('勤務中: 退勤・外出が有効、出勤・戻りが無効', () => {
    render(
      <ClockButtons status={makeStatus('WORKING')} isLoading={false} {...handlers} />
    );

    expect(screen.getByRole('button', { name: '出勤' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '退勤' })).not.toBeDisabled();
    expect(screen.getByRole('button', { name: '外出' })).not.toBeDisabled();
    expect(screen.getByRole('button', { name: '戻り' })).toBeDisabled();
  });

  it('外出中: 戻りのみ有効', () => {
    render(
      <ClockButtons status={makeStatus('OUT')} isLoading={false} {...handlers} />
    );

    expect(screen.getByRole('button', { name: '出勤' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '退勤' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '外出' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '戻り' })).not.toBeDisabled();
  });

  it('退勤済み: 全ボタン無効', () => {
    render(
      <ClockButtons status={makeStatus('CLOCKED_OUT')} isLoading={false} {...handlers} />
    );

    expect(screen.getByRole('button', { name: '出勤' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '退勤' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '外出' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '戻り' })).toBeDisabled();
  });

  it('出勤ボタンクリックでonClockInが呼ばれる', async () => {
    const user = userEvent.setup();
    render(
      <ClockButtons status={makeStatus('NOT_CLOCKED_IN')} isLoading={false} {...handlers} />
    );

    await user.click(screen.getByRole('button', { name: '出勤' }));
    expect(handlers.onClockIn).toHaveBeenCalledOnce();
  });
});
