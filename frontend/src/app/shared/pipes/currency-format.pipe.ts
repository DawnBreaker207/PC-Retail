import { Pipe, PipeTransform } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

@Pipe({
  name: 'currencyFormat',
  standalone: true,
})
export class CurrencyFormatPipe implements PipeTransform {
  private currency = new CurrencyPipe('vi-VN');

  transform(value: number) {
    return this.currency.transform(value, 'VND', 'symbol', '1.0-0');
  }
}
