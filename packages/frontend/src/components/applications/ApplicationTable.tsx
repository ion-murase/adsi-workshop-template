'use client';

import { useState } from 'react';
import { ApplicationResponse } from '@/types/application';
import { ApplicationStatus } from '@/types/api';
import { apiClient } from '@/lib/api-client';
import StatusBadge from './StatusBadge';

interface ApplicationTableProps {
  applications: ApplicationResponse[];
  onRefresh: () => void;
}

const STATUS_OPTIONS: { value: string; label: string }[] = [
  { value: '', label: 'すべて' },
  { value: 'PENDING', label: '承認待ち' },
  { value: 'APPROVED', label: '承認済み' },
  { value: 'REJECTED', label: '却下' },
  { value: 'WITHDRAWN', label: '取り下げ' },
];

export default function ApplicationTable({ applications, onRefresh }: ApplicationTableProps) {
  const [filter, setFilter] = useState('');

  const filtered = filter
    ? applications.filter(a => a.status === filter)
    : applications;

  const handleWithdraw = async (id: string) => {
    if (!confirm('この申請を取り下げますか？')) return;
    await apiClient(`/applications/${id}/withdraw`, { method: 'POST' });
    onRefresh();
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('ja-JP');
  };

  return (
    <div>
      <div className="mb-4">
        <select
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
          aria-label="ステータスフィルタ"
        >
          {STATUS_OPTIONS.map(opt => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">申請日</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">種類</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">対象日</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ステータス</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filtered.map(app => (
              <tr key={app.id}>
                <td className="px-4 py-3 text-sm text-gray-900">{formatDate(app.appliedAt)}</td>
                <td className="px-4 py-3 text-sm text-gray-900">
                  {app.type === 'CLOCK_FIX' ? '打刻修正' : '休暇申請'}
                </td>
                <td className="px-4 py-3 text-sm text-gray-900">
                  {app.clockFixDetail?.targetDate || '-'}
                </td>
                <td className="px-4 py-3 text-sm">
                  <StatusBadge status={app.status} />
                </td>
                <td className="px-4 py-3 text-sm">
                  {app.status === 'PENDING' && (
                    <button
                      onClick={() => handleWithdraw(app.id)}
                      className="text-red-600 hover:text-red-800 text-sm"
                    >
                      取り下げ
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-sm text-gray-500">
                  申請がありません
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
