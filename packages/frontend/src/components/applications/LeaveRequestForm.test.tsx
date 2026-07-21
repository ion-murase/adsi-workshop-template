import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import LeaveRequestForm from './LeaveRequestForm';

const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

vi.mock('@/lib/api-client', () => ({
  apiClient: vi.fn().mockResolvedValue({
    fiscalYear: 2026,
    grantedDays: 10,
    usedDays: 2,
    carriedOverDays: 0,
    remainingDays: 8,
  }),
}));

describe('LeaveRequestForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('残日数が表示される', async () => {
    render(<LeaveRequestForm />);

    await waitFor(() => {
      expect(screen.getByText('8日')).toBeInTheDocument();
    });
  });

  it('休暇種別が選択できる', () => {
    render(<LeaveRequestForm />);

    const select = screen.getByLabelText('休暇種別');
    expect(select).toBeInTheDocument();

    fireEvent.change(select, { target: { value: 'HALF_AM' } });
    expect(select).toHaveValue('HALF_AM');
  });

  it('休暇日が未入力の場合エラーが表示される', async () => {
    render(<LeaveRequestForm />);

    const submitButton = screen.getByRole('button', { name: '申請する' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('休暇日は必須です');
    });
  });

  it('キャンセルで申請一覧に遷移する', () => {
    render(<LeaveRequestForm />);

    fireEvent.click(screen.getByRole('button', { name: 'キャンセル' }));
    expect(mockPush).toHaveBeenCalledWith('/applications');
  });
});
