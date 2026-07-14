'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { apiClient } from '@/lib/api-client';
import { PaidLeaveBalanceResponse, LeaveRequestPayload } from '@/types/leave';
import PaidLeaveBalanceCard from './PaidLeaveBalanceCard';

const LEAVE_TYPES = [
  { value: 'FULL_DAY', label: '全日休' },
  { value: 'HALF_AM', label: '午前半休' },
  { value: 'HALF_PM', label: '午後半休' },
];

export default function LeaveRequestForm() {
  const router = useRouter();
  const [leaveDate, setLeaveDate] = useState('');
  const [leaveType, setLeaveType] = useState('FULL_DAY');
  const [balance, setBalance] = useState<PaidLeaveBalanceResponse | null>(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    apiClient<PaidLeaveBalanceResponse>('/paid-leave/balance')
      .then(setBalance)
      .catch(() => setBalance(null));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!leaveDate) {
      setError('休暇日は必須です');
      return;
    }

    if (balance && balance.remainingDays <= 0) {
      setError('有給残日数が不足しています');
      return;
    }

    setSubmitting(true);
    try {
      const payload: LeaveRequestPayload = { leaveDate, leaveType };
      await apiClient('/applications/leave', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      router.push('/applications');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('申請の提出に失敗しました');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6 max-w-lg">
      <PaidLeaveBalanceCard balance={balance} />

      <form onSubmit={handleSubmit} className="space-y-6">
        {error && (
          <div className="rounded-md bg-red-50 p-4" role="alert">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <div>
          <label htmlFor="leaveDate" className="block text-sm font-medium text-gray-700">
            休暇日 <span className="text-red-500">*</span>
          </label>
          <input
            type="date"
            id="leaveDate"
            value={leaveDate}
            onChange={e => setLeaveDate(e.target.value)}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label htmlFor="leaveType" className="block text-sm font-medium text-gray-700">
            休暇種別
          </label>
          <select
            id="leaveType"
            value={leaveType}
            onChange={e => setLeaveType(e.target.value)}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            {LEAVE_TYPES.map(t => (
              <option key={t.value} value={t.value}>{t.label}</option>
            ))}
          </select>
        </div>

        <div className="flex gap-3">
          <button
            type="submit"
            disabled={submitting}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? '提出中...' : '申請する'}
          </button>
          <button
            type="button"
            onClick={() => router.push('/applications')}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            キャンセル
          </button>
        </div>
      </form>
    </div>
  );
}
