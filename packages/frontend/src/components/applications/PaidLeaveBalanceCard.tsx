'use client';

import { PaidLeaveBalanceResponse } from '@/types/leave';

interface PaidLeaveBalanceCardProps {
  balance: PaidLeaveBalanceResponse | null;
}

export default function PaidLeaveBalanceCard({ balance }: PaidLeaveBalanceCardProps) {
  if (!balance) {
    return (
      <div className="rounded-lg border border-gray-200 p-4">
        <p className="text-sm text-gray-500">有給残日数を読み込み中...</p>
      </div>
    );
  }

  return (
    <div className="rounded-lg border border-gray-200 p-4">
      <h3 className="text-sm font-medium text-gray-700 mb-3">有給休暇残日数</h3>
      <div className="grid grid-cols-2 gap-3 text-sm">
        <div>
          <span className="text-gray-500">付与日数:</span>
          <span className="ml-2 font-medium">{balance.grantedDays}日</span>
        </div>
        <div>
          <span className="text-gray-500">繰越日数:</span>
          <span className="ml-2 font-medium">{balance.carriedOverDays}日</span>
        </div>
        <div>
          <span className="text-gray-500">使用日数:</span>
          <span className="ml-2 font-medium">{balance.usedDays}日</span>
        </div>
        <div>
          <span className="text-gray-500">残日数:</span>
          <span className="ml-2 font-bold text-blue-600">{balance.remainingDays}日</span>
        </div>
      </div>
    </div>
  );
}
