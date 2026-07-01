import { CurrencyFormatPipe } from './currency-format.pipe';
import { TestBed } from '@angular/core/testing';
import { CurrencyPipe, registerLocaleData } from '@angular/common';
import localeVi from '@angular/common/locales/vi';

describe('CurrencyFormatPipe', () => {
  let pipe: CurrencyFormatPipe;

  beforeAll(() => {
    registerLocaleData(localeVi);
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [CurrencyPipe, CurrencyFormatPipe],
    }).compileComponents();
    pipe = TestBed.inject(CurrencyFormatPipe);
  });

  it('should format VND currency', () => {
    const result = pipe.transform(1500000);
    expect(result).toContain('1,500,000');
  });

  it('should handle zero', () => {
    const result = pipe.transform(0);
    expect(result).toContain('0');
  });

  it('should handle large numbers', () => {
    const result = pipe.transform(100000000);
    expect(result).toContain('100,000,000');
  });
});
