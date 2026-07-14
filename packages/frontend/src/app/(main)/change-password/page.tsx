'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiClient } from '@/lib/api-client';

export default function ChangePasswordPage() {
  const router = useRouter();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    try {
      await apiClient('/auth/password', {
        method: 'PUT',
        body: JSON.stringify({ currentPassword, newPassword }),
      });
      router.push('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'パスワード変更に失敗しました');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-sm mx-auto space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">パスワード変更</h1>
      {error && <div className="rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</div>}
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="current" className="block text-sm font-medium text-gray-700">現在のパスワード</label>
          <input id="current" type="password" value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" required />
        </div>
        <div>
          <label htmlFor="new" className="block text-sm font-medium text-gray-700">新しいパスワード（8文字以上）</label>
          <input id="new" type="password" value={newPassword} minLength={8}
            onChange={(e) => setNewPassword(e.target.value)}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" required />
        </div>
        <button type="submit" disabled={isLoading}
          className="w-full rounded-md bg-blue-600 px-4 py-2 text-white font-medium hover:bg-blue-700 disabled:opacity-50">
          {isLoading ? '変更中...' : '変更'}
        </button>
      </form>
    </div>
  );
}
