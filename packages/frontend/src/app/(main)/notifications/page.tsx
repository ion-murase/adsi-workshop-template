'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiClient } from '@/lib/api-client';
import { Notification, NotificationPageResponse } from '@/types/notification';
import { NotificationList } from '@/components/notifications/NotificationList';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  const userId = typeof window !== 'undefined' ? localStorage.getItem('userId') : null;

  const fetchNotifications = useCallback(async () => {
    if (!userId) return;
    try {
      const data = await apiClient<NotificationPageResponse>(
        `/notifications?userId=${userId}&size=50`
      );
      setNotifications(data.content);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const handleMarkAsRead = async (id: string) => {
    if (!userId) return;
    await apiClient(`/notifications/${id}/read?userId=${userId}`, { method: 'POST' });
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
    );
  };

  const handleMarkAllAsRead = async () => {
    if (!userId) return;
    await apiClient(`/notifications/read-all?userId=${userId}`, { method: 'POST' });
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
  };

  if (loading) {
    return <div className="py-8 text-center text-gray-500">読み込み中...</div>;
  }

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-4">通知</h1>
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <NotificationList
          notifications={notifications}
          onMarkAsRead={handleMarkAsRead}
          onMarkAllAsRead={handleMarkAllAsRead}
        />
      </div>
    </div>
  );
}
