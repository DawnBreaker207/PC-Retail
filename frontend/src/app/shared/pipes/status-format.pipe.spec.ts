import { StatusFormatPipe } from './status-format.pipe';

describe('StatusFormatPipe', () => {
  const pipe = new StatusFormatPipe();

  it('returns color for known status', () => {
    expect(pipe.transform('PAID')).toBe('blue');
    expect(pipe.transform('CANCELLED')).toBe('error');
    expect(pipe.transform('DELIVERED')).toBe('success');
  });

  it('returns empty string for unknown status', () => {
    expect(pipe.transform('UNKNOWN')).toBe('');
    expect(pipe.transform('')).toBe('');
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
  });

  it('returns label for known status', () => {
    expect(pipe.transform('PAID', 'label')).toBe('Đã thanh toán');
    expect(pipe.transform('CANCELLED', 'label')).toBe('Đã hủy');
    expect(pipe.transform('DELIVERED', 'label')).toBe('Đã giao');
  });

  it('returns raw key as label for unknown status', () => {
    expect(pipe.transform('UNKNOWN_STATUS', 'label')).toBe('UNKNOWN_STATUS');
  });

  it('returns fallback label for null/undefined', () => {
    expect(pipe.transform(null, 'label')).toBe('—');
    expect(pipe.transform(undefined, 'label')).toBe('—');
  });
});
