'use client';

import { ApplicationStatus } from '@/types/api';

interface StatusBadgeProps {
  status: ApplicationStatus;
}

const STATUS_CONFIG: Record<ApplicationStatus, { label: string; className: string }> = {
  PENDING: { label: '承認待ち', className: 'bg-yellow-100 text-yellow-800' },
  APPROVED: { label: '承認済み', className: 'bg-green-100 text-green-800' },
  REJECTED: { label: '却下', className: 'bg-red-100 text-red-800' },
  WITHDRAWN: { label: '取り下げ', className: 'bg-gray-100 text-gray-800' },
};

export default function StatusBadge({ status }: StatusBadgeProps) {
  const config = STATUS_CONFIG[status];
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${config.className}`}>
      {config.label}
    </span>
  );
}
