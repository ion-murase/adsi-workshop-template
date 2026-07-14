import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import Home from './page';

describe('Home', () => {
  it('renders the title', () => {
    render(<Home />);
    expect(screen.getByText('勤怠管理')).toBeInTheDocument();
  });
});
