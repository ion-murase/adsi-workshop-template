'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiClient } from '@/lib/api-client';
import { ClockFixRequest } from '@/types/application';

export default function ClockFixForm() {
  const router = useRouter();
  const [targetDate, setTargetDate] = useState('');
  const [correctedClockIn, setCorrectedClockIn] = useState('');
  const [correctedClockOut, setCorrectedClockOut] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!targetDate) {
      setError('対象日は必須です');
      return;
    }
    if (!reason.trim()) {
      setError('理由は必須です');
      return;
    }
    if (!correctedClockIn && !correctedClockOut) {
      setError('修正後の出勤時刻または退勤時刻を入力してください');
      return;
    }

    setSubmitting(true);
    try {
      const request: ClockFixRequest = {
        targetDate,
        correctedClockIn: correctedClockIn ? `${targetDate}T${correctedClockIn}:00+09:00` : null,
        correctedClockOut: correctedClockOut ? `${targetDate}T${correctedClockOut}:00+09:00` : null,
        reason: reason.trim(),
      };

      await apiClient('/applications/clock-fix', {
        method: 'POST',
        body: JSON.stringify(request),
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
    <form onSubmit={handleSubmit} className="space-y-6 max-w-lg">
      {error && (
        <div className="rounded-md bg-red-50 p-4" role="alert">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      <div>
        <label htmlFor="targetDate" className="block text-sm font-medium text-gray-700">
          対象日 <span className="text-red-500">*</span>
        </label>
        <input
          type="date"
          id="targetDate"
          value={targetDate}
          onChange={e => setTargetDate(e.target.value)}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
      </div>

      <div>
        <label htmlFor="correctedClockIn" className="block text-sm font-medium text-gray-700">
          修正後 出勤時刻
        </label>
        <input
          type="time"
          id="correctedClockIn"
          value={correctedClockIn}
          onChange={e => setCorrectedClockIn(e.target.value)}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
      </div>

      <div>
        <label htmlFor="correctedClockOut" className="block text-sm font-medium text-gray-700">
          修正後 退勤時刻
        </label>
        <input
          type="time"
          id="correctedClockOut"
          value={correctedClockOut}
          onChange={e => setCorrectedClockOut(e.target.value)}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
      </div>

      <div>
        <label htmlFor="reason" className="block text-sm font-medium text-gray-700">
          理由 <span className="text-red-500">*</span>
        </label>
        <textarea
          id="reason"
          value={reason}
          onChange={e => setReason(e.target.value)}
          rows={3}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          placeholder="打刻修正の理由を入力してください"
        />
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
  );
}
