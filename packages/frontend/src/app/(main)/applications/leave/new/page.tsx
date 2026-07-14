'use client';

import LeaveRequestForm from '@/components/applications/LeaveRequestForm';

export default function NewLeaveRequestPage() {
  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-6">有給休暇申請</h1>
      <LeaveRequestForm />
    </div>
  );
}
