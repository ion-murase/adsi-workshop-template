'use client';

import { UserResponse } from '@/types/auth';

interface UserTableProps {
  users: UserResponse[];
  onDelete: (id: string) => void;
}

export function UserTable({ users, onDelete }: UserTableProps) {
  if (users.length === 0) {
    return <p className="text-gray-500 text-center py-8">ユーザが登録されていません</p>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">名前</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">メール</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">ロール</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">状態</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500">操作</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {users.map((user) => (
            <tr key={user.id}>
              <td className="px-4 py-3 text-sm">{user.name}</td>
              <td className="px-4 py-3 text-sm">{user.email}</td>
              <td className="px-4 py-3 text-sm">{user.role}</td>
              <td className="px-4 py-3 text-sm">
                <span className={`px-2 py-1 rounded text-xs ${user.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                  {user.active ? '有効' : '無効'}
                </span>
              </td>
              <td className="px-4 py-3 text-sm">
                <button
                  onClick={() => onDelete(user.id)}
                  className="text-red-600 hover:text-red-800 text-sm"
                >
                  削除
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
