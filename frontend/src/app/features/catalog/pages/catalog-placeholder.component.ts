import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzResultModule } from 'ng-zorro-antd/result';

@Component({
  selector: 'app-catalog-placeholder',
  imports: [RouterLink, NzButtonModule, NzResultModule],
  template: `
    <nz-result
      nzStatus="404"
      nzTitle="Đang phát triển"
      nzSubTitle="Trang danh mục sản phẩm đang được xây dựng."
    >
      <div nz-result-extra>
        <button nz-button nzType="primary" routerLink="/">Về trang chủ</button>
      </div>
    </nz-result>
  `,
})
export class CatalogPlaceholderComponent {}
