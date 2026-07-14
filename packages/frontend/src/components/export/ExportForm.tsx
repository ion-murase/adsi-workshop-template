'use client';

import { useState } from 'react';
import { withBasePath } from '@/lib/api-client';

const EXPORT_TYPES = [
  { value: 'time-records', label: '勤務履歴' },
  { value: 'monthly-summary', label: '月別集計' },
  { value: 'users', label: 'ユーザ一覧' },
];

export default function ExportForm() {
  const now = new Date();
  const [exportType, setExportType] = useState('time-records');
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [error, setError] = useState('');

  const needsDateParams = exportType !== 'users';

  const handleDownload = () => {
    const token = localStorage.getItem('token');
    if (!token) return;
    setError('');

    let url: string;
    if (exportType === 'users') {
      url = withBasePath('/api/export/users');
    } else {
      url = withBasePath(`/api/export/${exportType}?year=${year}&month=${month}`);
    }

    fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then(res => {
        if (!res.ok) throw new Error('エクスポートに失敗しました');
        return res.blob();
      })
      .then(blob => {
        const blobUrl = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = blobUrl;
        link.setAttribute('download', '');
        link.click();
        URL.revokeObjectURL(blobUrl);
      })
      .catch(() => setError('エクスポートに失敗しました'));
  };

  return (
    <div className="space-y-6 max-w-lg">
      {error && (
        <div className="rounded-md bg-red-50 p-4" role="alert">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      <div>
        <label htmlFor="exportType" className="block text-sm font-medium text-gray-700">
          エクスポート対象
        </label>
        <select
          id="exportType"
          value={exportType}
          onChange={e => setExportType(e.target.value)}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
        >
          {EXPORT_TYPES.map(t => (
            <option key={t.value} value={t.value}>{t.label}</option>
          ))}
        </select>
      </div>

      {needsDateParams && (
        <div className="flex gap-4">
          <div>
            <label htmlFor="year" className="block text-sm font-medium text-gray-700">年</label>
            <input
              type="number"
              id="year"
              value={year}
              onChange={e => setYear(Number(e.target.value))}
              className="mt-1 block w-24 rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label htmlFor="month" className="block text-sm font-medium text-gray-700">月</label>
            <input
              type="number"
              id="month"
              value={month}
              min={1}
              max={12}
              onChange={e => setMonth(Number(e.target.value))}
              className="mt-1 block w-20 rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
        </div>
      )}

      <button
        onClick={handleDownload}
        className="rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700"
      >
        ダウンロード
      </button>
    </div>
  );
}
