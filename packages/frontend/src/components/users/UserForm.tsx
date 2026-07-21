'use client';

import { useState } from 'react';
import { Role } from '@/types/api';
import { DepartmentResponse } from '@/types/auth';

interface UserFormProps {
  departments: DepartmentResponse[];
  onSubmit: (data: { email: string; name: string; role: Role; primaryDepartmentId: string }) => void;
  isLoading: boolean;
}

export function UserForm({ departments, onSubmit, isLoading }: UserFormProps) {
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [role, setRole] = useState<Role>('GENERAL');
  const [departmentId, setDepartmentId] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (email && name && departmentId) {
      onSubmit({ email, name, role, primaryDepartmentId: departmentId });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 max-w-md">
      <div>
        <label htmlFor="user-email" className="block text-sm font-medium text-gray-700">メール</label>
        <input id="user-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" required />
      </div>
      <div>
        <label htmlFor="user-name" className="block text-sm font-medium text-gray-700">名前</label>
        <input id="user-name" type="text" value={name} onChange={(e) => setName(e.target.value)}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" required />
      </div>
      <div>
        <label htmlFor="user-role" className="block text-sm font-medium text-gray-700">ロール</label>
        <select id="user-role" value={role} onChange={(e) => setRole(e.target.value as Role)}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm">
          <option value="GENERAL">一般</option>
          <option value="APPROVER">承認者</option>
          <option value="ADMIN">管理者</option>
        </select>
      </div>
      <div>
        <label htmlFor="user-dept" className="block text-sm font-medium text-gray-700">部署</label>
        <select id="user-dept" value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" required>
          <option value="">選択してください</option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>{d.name}</option>
          ))}
        </select>
      </div>
      <button type="submit" disabled={isLoading}
        className="rounded-md bg-blue-600 px-4 py-2 text-white text-sm hover:bg-blue-700 disabled:opacity-50">
        {isLoading ? '保存中...' : '登録'}
      </button>
    </form>
  );
}
