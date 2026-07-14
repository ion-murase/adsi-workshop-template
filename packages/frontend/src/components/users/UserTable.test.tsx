import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { UserTable } from './UserTable';
import { UserResponse } from '@/types/auth';

describe('UserTable', () => {
  const mockUsers: UserResponse[] = [
    {
      id: '1',
      email: 'user1@example.com',
      name: 'ユーザ1',
      role: 'GENERAL',
      primaryDepartmentId: 'dept-1',
      active: true,
      requirePasswordChange: false,
    },
    {
      id: '2',
      email: 'user2@example.com',
      name: 'ユーザ2',
      role: 'ADMIN',
      primaryDepartmentId: 'dept-1',
      active: false,
      requirePasswordChange: false,
    },
  ];

  it('ユーザがない場合メッセージを表示', () => {
    render(<UserTable users={[]} onDelete={vi.fn()} />);
    expect(screen.getByText('ユーザが登録されていません')).toBeInTheDocument();
  });

  it('ユーザ一覧を表示', () => {
    render(<UserTable users={mockUsers} onDelete={vi.fn()} />);
    expect(screen.getByText('ユーザ1')).toBeInTheDocument();
    expect(screen.getByText('ユーザ2')).toBeInTheDocument();
    expect(screen.getByText('user1@example.com')).toBeInTheDocument();
  });

  it('有効/無効の状態を表示', () => {
    render(<UserTable users={mockUsers} onDelete={vi.fn()} />);
    expect(screen.getByText('有効')).toBeInTheDocument();
    expect(screen.getByText('無効')).toBeInTheDocument();
  });

  it('削除ボタンクリックでonDeleteが呼ばれる', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();
    render(<UserTable users={mockUsers} onDelete={onDelete} />);

    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]);
    expect(onDelete).toHaveBeenCalledWith('1');
  });
});
