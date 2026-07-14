import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { LoginForm } from './LoginForm';

describe('LoginForm', () => {
  const defaultProps = {
    onSubmit: vi.fn(),
    isLoading: false,
    error: null,
  };

  it('メールとパスワードの入力フィールドが表示される', () => {
    render(<LoginForm {...defaultProps} />);
    expect(screen.getByLabelText('メールアドレス')).toBeInTheDocument();
    expect(screen.getByLabelText('パスワード')).toBeInTheDocument();
  });

  it('入力してsubmitするとonSubmitが呼ばれる', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<LoginForm {...defaultProps} onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText('メールアドレス'), 'test@example.com');
    await user.type(screen.getByLabelText('パスワード'), 'password');
    await user.click(screen.getByRole('button', { name: 'ログイン' }));

    expect(onSubmit).toHaveBeenCalledWith('test@example.com', 'password');
  });

  it('エラーメッセージが表示される', () => {
    render(<LoginForm {...defaultProps} error="ログインに失敗しました" />);
    expect(screen.getByRole('alert')).toHaveTextContent('ログインに失敗しました');
  });

  it('ローディング中はボタンが無効', () => {
    render(<LoginForm {...defaultProps} isLoading={true} />);
    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByRole('button')).toHaveTextContent('ログイン中...');
  });
});
