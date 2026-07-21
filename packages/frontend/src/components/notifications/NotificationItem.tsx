'use client';

import { Notification } from '@/types/notification';

interface NotificationItemProps {
  notification: Notification;
  onMarkAsRead: (id: string) => void;
}

export function NotificationItem({ notification, onMarkAsRead }: NotificationItemProps) {
  const formattedDate = new Date(notification.createdAt).toLocaleString('ja-JP', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div
      className={`p-4 border-b border-gray-100 ${
        notification.isRead ? 'bg-white' : 'bg-blue-50'
      }`}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-gray-900">{notification.title}</p>
          <p className="mt-1 text-sm text-gray-600">{notification.message}</p>
          <p className="mt-1 text-xs text-gray-400">{formattedDate}</p>
        </div>
        {!notification.isRead && (
          <button
            onClick={() => onMarkAsRead(notification.id)}
            className="shrink-0 text-xs text-blue-600 hover:text-blue-800"
            aria-label="既読にする"
          >
            既読にする
          </button>
        )}
      </div>
    </div>
  );
}
