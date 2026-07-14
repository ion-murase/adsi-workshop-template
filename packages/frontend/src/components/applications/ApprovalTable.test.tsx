import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import ApprovalTable from './ApprovalTable';
import { ApplicationResponse } from '@/types/application';

vi.mock('@/lib/api-client', () => ({
  apiClient: vi.fn(),
}));

const mockPendingApplications: ApplicationResponse[] = [
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
      reason: '打刻忘れのため',
    },
  },
];

describe('ApprovalTable', () => {
  it('承認待ち一覧が表示される', () => {
    render(<ApprovalTable applications={mockPendingApplications} onRefresh={vi.fn()} />);

    expect(screen.getByText('田中太郎')).toBeInTheDocument();
    expect(screen.getByText('打刻修正')).toBeInTheDocument();
    expect(screen.getByText('2026-07-09')).toBeInTheDocument();
    expect(screen.getByText('打刻忘れのため')).toBeInTheDocument();
  });

  it('承認ボタンと却下ボタンが表示される', () => {
    render(<ApprovalTable applications={mockPendingApplications} onRefresh={vi.fn()} />);

    expect(screen.getByText('承認')).toBeInTheDocument();
    expect(screen.getByText('却下')).toBeInTheDocument();
  });

  it('却下ボタンでダイアログが開く', () => {
    render(<ApprovalTable applications={mockPendingApplications} onRefresh={vi.fn()} />);

    fireEvent.click(screen.getByText('却下'));

    expect(screen.getByText('却下理由')).toBeInTheDocument();
    expect(screen.getByLabelText('却下理由')).toBeInTheDocument();
  });

  it('申請がない場合メッセージが表示される', () => {
    render(<ApprovalTable applications={[]} onRefresh={vi.fn()} />);

    expect(screen.getByText('承認待ちの申請はありません')).toBeInTheDocument();
  });
});
