'use client';

import { useState } from 'react';
import { ApplicationResponse } from '@/types/application';
import { apiClient } from '@/lib/api-client';
import StatusBadge from './StatusBadge';
import RejectDialog from './RejectDialog';

interface ApprovalTableProps {
  applications: ApplicationResponse[];
  onRefresh: () => void;
}

export default function ApprovalTable({ applications, onRefresh }: ApprovalTableProps) {
  const [rejectTarget, setRejectTarget] = useState<string | null>(null);

  const handleApprove = async (id: string) => {
    if (!confirm('この申請を承認しますか？')) return;
    await apiClient(`/applications/${id}/approve`, { method: 'POST' });
    onRefresh();
  };

  const handleReject = async (comment: string) => {
    if (!rejectTarget) return;
    await apiClient(`/applications/${rejectTarget}/reject`, {
      method: 'POST',
      body: JSON.stringify({ comment }),
    });
    setRejectTarget(null);
    onRefresh();
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('ja-JP');
  };

  return (
    <div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">申請者</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">種類</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">対象日</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">申請日</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">理由</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {applications.map(app => (
              <tr key={app.id}>
                <td className="px-4 py-3 text-sm text-gray-900">{app.applicantName}</td>
                <td className="px-4 py-3 text-sm text-gray-900">
                  {app.type === 'CLOCK_FIX' ? '打刻修正' : '休暇申請'}
                </td>
                <td className="px-4 py-3 text-sm text-gray-900">
                  {app.clockFixDetail?.targetDate || '-'}
                </td>
                <td className="px-4 py-3 text-sm text-gray-900">{formatDate(app.appliedAt)}</td>
                <td className="px-4 py-3 text-sm text-gray-900 max-w-xs truncate">
                  {app.clockFixDetail?.reason || '-'}
                </td>
                <td className="px-4 py-3 text-sm">
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleApprove(app.id)}
                      className="rounded bg-green-600 px-3 py-1 text-xs font-medium text-white hover:bg-green-700"
                    >
                      承認
                    </button>
                    <button
                      onClick={() => setRejectTarget(app.id)}
                      className="rounded bg-red-600 px-3 py-1 text-xs font-medium text-white hover:bg-red-700"
                    >
                      却下
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {applications.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-sm text-gray-500">
                  承認待ちの申請はありません
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <RejectDialog
        isOpen={rejectTarget !== null}
        onClose={() => setRejectTarget(null)}
        onReject={handleReject}
      />
    </div>
  );
}
