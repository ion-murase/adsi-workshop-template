import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import ExportForm from './ExportForm';

vi.mock('@/lib/api-client', () => ({
  withBasePath: (path: string) => path,
}));

describe('ExportForm', () => {
  it('エクスポート対象を選択できる', () => {
    render(<ExportForm />);

    const select = screen.getByLabelText('エクスポート対象');
    expect(select).toBeInTheDocument();
    expect(select).toHaveValue('time-records');

    fireEvent.change(select, { target: { value: 'users' } });
    expect(select).toHaveValue('users');
  });

  it('勤務履歴選択時に年月入力が表示される', () => {
    render(<ExportForm />);

    expect(screen.getByLabelText('年')).toBeInTheDocument();
    expect(screen.getByLabelText('月')).toBeInTheDocument();
  });

  it('ユーザ一覧選択時に年月入力が非表示になる', () => {
    render(<ExportForm />);

    const select = screen.getByLabelText('エクスポート対象');
    fireEvent.change(select, { target: { value: 'users' } });

    expect(screen.queryByLabelText('年')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('月')).not.toBeInTheDocument();
  });

  it('ダウンロードボタンが表示される', () => {
    render(<ExportForm />);

    expect(screen.getByRole('button', { name: 'ダウンロード' })).toBeInTheDocument();
  });
});
