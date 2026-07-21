import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { NotificationBadge } from './NotificationBadge';

describe('NotificationBadge', () => {
  it('未読件数が0のとき非表示', () => {
    render(<NotificationBadge count={0} />);
    expect(screen.queryByText('0')).not.toBeInTheDocument();
  });

  it('未読件数が > 0 のとき件数を表示', () => {
    render(<NotificationBadge count={5} />);
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it('未読件数が99を超える場合は99+を表示', () => {
    render(<NotificationBadge count={150} />);
    expect(screen.getByText('99+')).toBeInTheDocument();
  });
});
