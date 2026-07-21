'use client';

import { Notification } from '@/types/notification';
import { NotificationItem } from './NotificationItem';

interface NotificationListProps {
  notifications: Notification[];
  onMarkAsRead: (id: string) => void;
  onMarkAllAsRead: () => void;
}

export function NotificationList({ notifications, onMarkAsRead, onMarkAllAsRead }: NotificationListProps) {
  if (notifications.length === 0) {
    return (
      <div className="py-12 text-center text-gray-500">
        通知はありません
      </div>
    );
  }

  const hasUnread = notifications.some((n) => !n.isRead);

  return (
    <div>
      {hasUnread && (
        <div className="flex justify-end p-3 border-b border-gray-200">
          <button
            onClick={onMarkAllAsRead}
            className="text-sm text-blue-600 hover:text-blue-800"
            aria-label="すべて既読にする"
          >
            すべて既読にする
          </button>
        </div>
      )}
      <div>
        {notifications.map((notification) => (
          <NotificationItem
            key={notification.id}
            notification={notification}
            onMarkAsRead={onMarkAsRead}
          />
        ))}
      </div>
    </div>
  );
}
