import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import ApplicationTable from './ApplicationTable';
import { ApplicationResponse } from '@/types/application';

vi.mock('@/lib/api-client', () => ({
  apiClient: vi.fn(),
}));

const mockApplications: ApplicationResponse[] = [
  {
    id: '1',
    applicantId: 'user1',
    applicantName: '田中太郎',
    approverId: null,
    type: 'CLOCK_FIX',
    status: 'PENDING',
    rejectionComment: null,
    appliedAt: '2026-07-10T09:00:00+09:00',
    decidedAt: null,
    clockFixDetail: {
      targetDate: '2026-07-09',
      correctedClockIn: '2026-07-09T09:00:00+09:00',
      correctedClockOut: null,
      reason: '打刻忘れ',
    },
  },
  {
    id: '2',
    applicantId: 'user1',
    applicantName: '田中太郎',
    approverId: 'approver1',
    type: 'CLOCK_FIX',
    status: 'APPROVED',
    rejectionComment: null,
    appliedAt: '2026-07-08T09:00:00+09:00',
    decidedAt: '2026-07-08T12:00:00+09:00',
    clockFixDetail: {
      targetDate: '2026-07-07',
      correctedClockIn: null,
      correctedClockOut: '2026-07-07T18:00:00+09:00',
      reason: '退勤忘れ',
    },
  },
];

describe('ApplicationTable', () => {
  it('申請一覧が表示される', () => {
    render(<ApplicationTable applications={mockApplications} onRefresh={vi.fn()} />);

    expect(screen.getAllByText('打刻修正')).toHaveLength(2);
    expect(screen.getByText('2026-07-09')).toBeInTheDocument();
    expect(screen.getByText('2026-07-07')).toBeInTheDocument();
  });

  it('ステータスフィルタで絞り込みできる', () => {
    render(<ApplicationTable applications={mockApplications} onRefresh={vi.fn()} />);

    const select = screen.getByLabelText('ステータスフィルタ');
    fireEvent.change(select, { target: { value: 'APPROVED' } });

    expect(screen.queryByText('2026-07-09')).not.toBeInTheDocument();
    expect(screen.getByText('2026-07-07')).toBeInTheDocument();
  });

  it('PENDING の申請に取り下げボタンが表示される', () => {
    render(<ApplicationTable applications={mockApplications} onRefresh={vi.fn()} />);

    const withdrawButtons = screen.getAllByRole('button', { name: '取り下げ' });
    expect(withdrawButtons).toHaveLength(1);
  });

  it('申請がない場合メッセージが表示される', () => {
    render(<ApplicationTable applications={[]} onRefresh={vi.fn()} />);

    expect(screen.getByText('申請がありません')).toBeInTheDocument();
  });
});
