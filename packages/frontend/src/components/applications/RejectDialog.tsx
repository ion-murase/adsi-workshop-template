'use client';

import { useState } from 'react';

interface RejectDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onReject: (comment: string) => void;
}

export default function RejectDialog({ isOpen, onClose, onReject }: RejectDialogProps) {
  const [comment, setComment] = useState('');
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = () => {
    if (!comment.trim()) {
      setError('却下理由は必須です');
      return;
    }
    onReject(comment.trim());
    setComment('');
    setError('');
  };

  const handleClose = () => {
    setComment('');
    setError('');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-medium text-gray-900 mb-4">却下理由</h3>

        {error && (
          <p className="text-sm text-red-600 mb-3">{error}</p>
        )}

        <textarea
          value={comment}
          onChange={e => setComment(e.target.value)}
          rows={3}
          className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          placeholder="却下理由を入力してください"
          aria-label="却下理由"
        />

        <div className="mt-4 flex justify-end gap-3">
          <button
            onClick={handleClose}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            キャンセル
          </button>
          <button
            onClick={handleSubmit}
            className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
          >
            却下する
          </button>
        </div>
      </div>
    </div>
  );
}
