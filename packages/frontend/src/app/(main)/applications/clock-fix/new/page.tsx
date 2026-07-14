'use client';

import ClockFixForm from '@/components/applications/ClockFixForm';

export default function NewClockFixPage() {
  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-6">打刻修正申請</h1>
      <ClockFixForm />
    </div>
  );
}
