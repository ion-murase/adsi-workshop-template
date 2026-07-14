'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiClient } from '@/lib/api-client';
import { DepartmentResponse, SiteResponse } from '@/types/auth';

export default function DepartmentsPage() {
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [sites, setSites] = useState<SiteResponse[]>([]);
  const [name, setName] = useState('');
  const [siteId, setSiteId] = useState('');

  const fetchData = useCallback(async () => {
    const [depts, siteList] = await Promise.all([
      apiClient<DepartmentResponse[]>('/departments'),
      apiClient<SiteResponse[]>('/sites'),
    ]);
    setDepartments(depts);
    setSites(siteList);
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (name && siteId) {
      await apiClient('/departments', {
        method: 'POST',
        body: JSON.stringify({ name, siteId }),
      });
      setName('');
      setSiteId('');
      await fetchData();
    }
  };

  const handleDelete = async (id: string) => {
    await apiClient(`/departments/${id}`, { method: 'DELETE' });
    await fetchData();
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">部署管理</h1>
      <form onSubmit={handleAdd} className="flex gap-3 items-end">
        <div>
          <label htmlFor="dept-name" className="block text-sm font-medium text-gray-700">名称</label>
          <input id="dept-name" type="text" value={name} onChange={(e) => setName(e.target.value)}
            className="mt-1 rounded-md border border-gray-300 px-3 py-2 text-sm" required />
        </div>
        <div>
          <label htmlFor="dept-site" className="block text-sm font-medium text-gray-700">拠点</label>
          <select id="dept-site" value={siteId} onChange={(e) => setSiteId(e.target.value)}
            className="mt-1 rounded-md border border-gray-300 px-3 py-2 text-sm" required>
            <option value="">選択</option>
            {sites.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </div>
        <button type="submit" className="rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700">追加</button>
      </form>
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">名称</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">操作</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {departments.map((d) => (
            <tr key={d.id}>
              <td className="px-4 py-3 text-sm">{d.name}</td>
              <td className="px-4 py-3 text-sm">
                <button onClick={() => handleDelete(d.id)} className="text-red-600 hover:text-red-800 text-sm">削除</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
