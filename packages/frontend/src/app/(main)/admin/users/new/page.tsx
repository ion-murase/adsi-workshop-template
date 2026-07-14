'use client';

import { useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiClient } from '@/lib/api-client';
import { UserForm } from '@/components/users/UserForm';
import { DepartmentResponse } from '@/types/auth';

export default function NewUserPage() {
  const router = useRouter();
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const fetchDepartments = useCallback(async () => {
    const data = await apiClient<DepartmentResponse[]>('/departments');
    setDepartments(data);
  }, []);

  useEffect(() => {
    fetchDepartments();
  }, [fetchDepartments]);

  const handleSubmit = async (data: { email: string; name: string; role: string; primaryDepartmentId: string }) => {
    setIsLoading(true);
    try {
      await apiClient('/users', {
        method: 'POST',
        body: JSON.stringify(data),
      });
      router.push('/admin/users');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">ユーザ登録</h1>
      <UserForm departments={departments} onSubmit={handleSubmit} isLoading={isLoading} />
    </div>
  );
}
