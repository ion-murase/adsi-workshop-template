'use client';

import { useEffect, useState, useCallback } from 'react';
import { apiClient } from '@/lib/api-client';
import { ApplicationResponse, ApplicationPageResponse } from '@/types/application';
import ApprovalTable from '@/components/applications/ApprovalTable';

export default function ApprovalsPage() {
  const [applications, setApplications] = useState<ApplicationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchPending = useCallback(async () => {
    try {
      const data = await apiClient<ApplicationPageResponse>('/applications/pending');
      setApplications(data.content);
    } catch {
      setApplications([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPending();
  }, [fetchPending]);

  if (loading) {
    return <div className="text-center py-8 text-gray-500">読み込み中...</div>;
  }

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-6">承認待ち一覧</h1>
      <ApprovalTable applications={applications} onRefresh={fetchPending} />
    </div>
  );
}
