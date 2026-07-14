'use client';

import ExportForm from '@/components/export/ExportForm';

export default function ExportPage() {
  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-6">CSVエクスポート</h1>
      <ExportForm />
    </div>
  );
}
