import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { NotificationList } from './NotificationList';
import { Notification } from '@/types/notification';

function makeNotification(overrides: Partial<Notification> = {}): Notification {
  return {
    id: '1',
    type: 'APPLICATION_APPROVED',
    title: '申請が承認されました',
    message: '打刻修正申請が承認されました。',
    referenceId: null,
    isRead: false,
    createdAt: '2026-07-01T09:00:00+09:00',
    ...overrides,
  };
}

describe('NotificationList', () => {
  it('通知一覧が表示される', () => {
    const notifications = [
      makeNotification({ id: '1', title: '通知1' }),
      makeNotification({ id: '2', title: '通知2' }),
    ];

    render(
      <NotificationList
        notifications={notifications}
        onMarkAsRead={vi.fn()}
        onMarkAllAsRead={vi.fn()}
      />
    );

    expect(screen.getByText('通知1')).toBeInTheDocument();
    expect(screen.getByText('通知2')).toBeInTheDocument();
  });

  it('既読ボタンクリックで既読化APIが呼ばれる', async () => {
    const user = userEvent.setup();
    const onMarkAsRead = vi.fn();
    const notifications = [makeNotification({ id: 'abc' })];

    render(
      <NotificationList
        notifications={notifications}
        onMarkAsRead={onMarkAsRead}
        onMarkAllAsRead={vi.fn()}
      />
    );

    await user.click(screen.getByRole('button', { name: '既読にする' }));
    expect(onMarkAsRead).toHaveBeenCalledWith('abc');
  });

  it('全て既読ボタンで全既読APIが呼ばれる', async () => {
    const user = userEvent.setup();
    const onMarkAllAsRead = vi.fn();
    const notifications = [makeNotification()];

    render(
      <NotificationList
        notifications={notifications}
        onMarkAsRead={vi.fn()}
        onMarkAllAsRead={onMarkAllAsRead}
      />
    );

    await user.click(screen.getByRole('button', { name: 'すべて既読にする' }));
    expect(onMarkAllAsRead).toHaveBeenCalledOnce();
  });

  it('通知が空のとき「通知はありません」表示', () => {
    render(
      <NotificationList
        notifications={[]}
        onMarkAsRead={vi.fn()}
        onMarkAllAsRead={vi.fn()}
      />
    );

    expect(screen.getByText('通知はありません')).toBeInTheDocument();
  });
});
