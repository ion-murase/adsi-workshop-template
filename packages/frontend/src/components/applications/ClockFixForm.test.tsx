import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ClockFixForm from './ClockFixForm';

const mockPush = vi.fn();
vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: mockPush }),
}));

vi.mock('@/lib/api-client', () => ({
  apiClient: vi.fn(),
}));

describe('ClockFixForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('対象日が未入力の場合エラーメッセージを表示する', async () => {
    render(<ClockFixForm />);

    const reasonInput = screen.getByLabelText(/理由/);
    fireEvent.change(reasonInput, { target: { value: '打刻忘れ' } });

    const submitButton = screen.getByRole('button', { name: '申請する' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('対象日は必須です');
    });
  });

  it('理由が未入力の場合エラーメッセージを表示する', async () => {
    render(<ClockFixForm />);

    const dateInput = screen.getByLabelText(/対象日/);
    fireEvent.change(dateInput, { target: { value: '2026-07-10' } });

    const clockInInput = screen.getByLabelText(/修正後 出勤時刻/);
    fireEvent.change(clockInInput, { target: { value: '09:00' } });

    const submitButton = screen.getByRole('button', { name: '申請する' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('理由は必須です');
    });
  });

  it('出勤時刻と退勤時刻が両方未入力の場合エラーを表示する', async () => {
    render(<ClockFixForm />);

    const dateInput = screen.getByLabelText(/対象日/);
    fireEvent.change(dateInput, { target: { value: '2026-07-10' } });

    const reasonInput = screen.getByLabelText(/理由/);
    fireEvent.change(reasonInput, { target: { value: '打刻忘れ' } });

    const submitButton = screen.getByRole('button', { name: '申請する' });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('修正後の出勤時刻または退勤時刻を入力してください');
    });
  });

  it('キャンセルボタンで申請一覧に遷移する', () => {
    render(<ClockFixForm />);

    const cancelButton = screen.getByRole('button', { name: 'キャンセル' });
    fireEvent.click(cancelButton);

    expect(mockPush).toHaveBeenCalledWith('/applications');
  });
});
