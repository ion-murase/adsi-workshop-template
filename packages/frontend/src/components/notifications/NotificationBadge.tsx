'use client';

interface NotificationBadgeProps {
  count: number;
}

export function NotificationBadge({ count }: NotificationBadgeProps) {
  if (count === 0) {
    return null;
  }

  const display = count > 99 ? '99+' : String(count);

  return (
    <span className="inline-flex items-center justify-center min-w-5 h-5 px-1 text-xs font-bold text-white bg-red-500 rounded-full">
      {display}
    </span>
  );
}
