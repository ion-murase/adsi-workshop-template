'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiClient } from '@/lib/api-client';
import { SiteResponse } from '@/types/auth';

export default function SitesPage() {
  const [sites, setSites] = useState<SiteResponse[]>([]);
  const [name, setName] = useState('');
  const [timezone, setTimezone] = useState('Asia/Tokyo');

  const fetchSites = useCallback(async () => {
    const data = await apiClient<SiteResponse[]>('/sites');
    setSites(data);
  }, []);

  useEffect(() => {
    fetchSites();
  }, [fetchSites]);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (name && timezone) {
      await apiClient('/sites', {
        method: 'POST',
        body: JSON.stringify({ name, timezone }),
      });
      setName('');
      await fetchSites();
    }
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">拠点管理</h1>
      <form onSubmit={handleAdd} className="flex gap-3 items-end">
        <div>
          <label htmlFor="site-name" className="block text-sm font-medium text-gray-700">名称</label>
          <input id="site-name" type="text" value={name} onChange={(e) => setName(e.target.value)}
            className="mt-1 rounded-md border border-gray-300 px-3 py-2 text-sm" required />
        </div>
        <div>
          <label htmlFor="site-tz" className="block text-sm font-medium text-gray-700">タイムゾーン</label>
          <input id="site-tz" type="text" value={timezone} onChange={(e) => setTimezone(e.target.value)}
            className="mt-1 rounded-md border border-gray-300 px-3 py-2 text-sm" required />
        </div>
        <button type="submit" className="rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700">追加</button>
      </form>
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">名称</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">タイムゾーン</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {sites.map((s) => (
            <tr key={s.id}>
              <td className="px-4 py-3 text-sm">{s.name}</td>
              <td className="px-4 py-3 text-sm">{s.timezone}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
