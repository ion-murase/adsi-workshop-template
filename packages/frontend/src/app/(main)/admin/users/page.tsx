'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { apiClient } from '@/lib/api-client';
import { UserTable } from '@/components/users/UserTable';
import { UserResponse } from '@/types/auth';

export default function UsersPage() {
  const [users, setUsers] = useState<UserResponse[]>([]);

  const fetchUsers = useCallback(async () => {
    const data = await apiClient<UserResponse[]>('/users');
    setUsers(data);
  }, []);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleDelete = async (id: string) => {
    await apiClient(`/users/${id}`, { method: 'DELETE' });
    await fetchUsers();
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">ユーザ管理</h1>
        <Link href="/admin/users/new"
          className="rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700">
          新規登録
        </Link>
      </div>
      <UserTable users={users} onDelete={handleDelete} />
    </div>
  );
}
