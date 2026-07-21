'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { apiClient } from '@/lib/api-client';
import { ApplicationResponse, ApplicationPageResponse } from '@/types/application';
import ApplicationTable from '@/components/applications/ApplicationTable';

export default function ApplicationsPage() {
  const [applications, setApplications] = useState<ApplicationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchApplications = useCallback(async () => {
    try {
      const data = await apiClient<ApplicationPageResponse>('/applications');
      setApplications(data.content);
    } catch {
      setApplications([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

  if (loading) {
    return <div className="text-center py-8 text-gray-500">読み込み中...</div>;
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-bold text-gray-900">申請一覧</h1>
        <Link
          href="/applications/clock-fix/new"
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          打刻修正申請
        </Link>
      </div>

      <ApplicationTable applications={applications} onRefresh={fetchApplications} />
    </div>
  );
}
