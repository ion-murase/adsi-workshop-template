'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { apiClient } from '@/lib/api-client';
import { NotificationBadge } from '@/components/notifications/NotificationBadge';
import { UnreadCountResponse } from '@/types/notification';

const NAV_ITEMS = [
  { href: '/', label: 'ダッシュボード' },
  { href: '/time-records', label: '勤務履歴' },
  { href: '/notifications', label: '通知' },
  { href: '/applications', label: '申請' },
  { href: '/approvals', label: '承認' },
  { href: '/export', label: 'エクスポート' },
  { href: '/admin/calendar', label: 'カレンダー' },
];

export default function MainLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [unreadCount, setUnreadCount] = useState(0);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setIsAuthenticated(false);
      router.push('/login');
      return;
    }
    setIsAuthenticated(true);

    const userId = localStorage.getItem('userId');
    if (!userId) return;

    const fetchCount = () => {
      apiClient<UnreadCountResponse>(`/notifications/unread-count?userId=${userId}`)
        .then((data) => setUnreadCount(data.count))
        .catch(() => {});
    };

    fetchCount();
    const interval = setInterval(fetchCount, 30000);
    return () => clearInterval(interval);
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('user');
    router.push('/login');
  };

  if (isAuthenticated === null || isAuthenticated === false) {
    return null;
  }

  return (
    <div className="min-h-screen">
      <nav className="bg-white border-b border-gray-200">
        <div className="max-w-5xl mx-auto px-4">
          <div className="flex h-14 items-center justify-between">
            <span className="font-bold text-gray-900">勤怠管理</span>
            <div className="flex gap-4 items-center">
              {NAV_ITEMS.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`text-sm flex items-center gap-1 ${
                    pathname === item.href
                      ? 'text-blue-600 font-medium'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  {item.label}
                  {item.href === '/notifications' && (
                    <NotificationBadge count={unreadCount} />
                  )}
                </Link>
              ))}
              <button
                onClick={handleLogout}
                className="text-sm text-gray-500 hover:text-red-600 ml-2"
              >
                ログアウト
              </button>
            </div>
          </div>
        </div>
      </nav>
      <main className="max-w-5xl mx-auto px-4 py-6">{children}</main>
    </div>
  );
}
