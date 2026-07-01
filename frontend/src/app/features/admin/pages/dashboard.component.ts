import { Component, inject, DestroyRef } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzStatisticModule } from 'ng-zorro-antd/statistic';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ApiEndpoints } from '@core/api/api.endpoints';

interface DashboardSummary {
  totalRevenue: number;
  totalOrders: number;
  totalProducts: number;
  totalCustomers: number;
  recentOrders: { id: number; customerName: string; total: number; status: string; createdAt: string }[];
}

@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink, DatePipe, NzCardModule, NzStatisticModule, NzGridModule, NzIconModule,
    NzTableModule, NzTagModule, NzSpinModule, CurrencyFormatPipe,
  ],
  template: `
    <div class="mb-6">
      <h1 class="text-2xl font-bold">Dashboard</h1>
      <p class="text-muted text-sm">Tổng quan cửa hàng PC Retail</p>
    </div>

    @if (!data) {
      <div class="flex justify-center py-20"><nz-spin nzSize="large"></nz-spin></div>
    } @else {
      <div nz-row [nzGutter]="[16, 16]" class="mb-8">
        <div nz-col nzXs="24" nzSm="12" nzLg="6">
          <nz-card class="rounded-card shadow-sm">
            <nz-statistic [nzValue]="data.totalRevenue" nzTitle="Doanh thu" nzPrefix="₫" [nzValueStyle]="{ 'font-size': '24px' }">
              <span nz-icon nzType="rise" nzTheme="outline" nzSlot="prefix" class="text-success"></span>
            </nz-statistic>
          </nz-card>
        </div>
        <div nz-col nzXs="24" nzSm="12" nzLg="6">
          <nz-card class="rounded-card shadow-sm">
            <nz-statistic [nzValue]="data.totalOrders" nzTitle="Đơn hàng" [nzSuffix]="suffixOrder">
              <span nz-icon nzType="shopping-cart" nzTheme="outline" nzSlot="prefix"></span>
            </nz-statistic>
            <ng-template #suffixOrder><span class="text-sm">đơn</span></ng-template>
          </nz-card>
        </div>
        <div nz-col nzXs="24" nzSm="12" nzLg="6">
          <nz-card class="rounded-card shadow-sm">
            <nz-statistic [nzValue]="data.totalProducts" nzTitle="Sản phẩm" [nzSuffix]="suffixProduct">
              <span nz-icon nzType="appstore" nzTheme="outline" nzSlot="prefix"></span>
            </nz-statistic>
            <ng-template #suffixProduct><span class="text-sm">sp</span></ng-template>
          </nz-card>
        </div>
        <div nz-col nzXs="24" nzSm="12" nzLg="6">
          <nz-card class="rounded-card shadow-sm">
            <nz-statistic [nzValue]="data.totalCustomers" nzTitle="Khách hàng" [nzSuffix]="suffixCustomer">
              <span nz-icon nzType="team" nzTheme="outline" nzSlot="prefix"></span>
            </nz-statistic>
            <ng-template #suffixCustomer><span class="text-sm">kh</span></ng-template>
          </nz-card>
        </div>
      </div>

      <nz-card nzTitle="Đơn hàng gần đây" class="rounded-card shadow-sm">
        <nz-table [nzData]="data.recentOrders" [nzFrontPagination]="false" [nzShowPagination]="false" nzSize="small">
          <thead>
            <tr>
              <th>ID</th>
              <th>Khách hàng</th>
              <th>Tổng tiền</th>
              <th>Trạng thái</th>
              <th>Ngày</th>
            </tr>
          </thead>
          <tbody>
            @for (order of data.recentOrders; track order.id) {
              <tr>
                <td><a [routerLink]="['/admin/orders', order.id]">#{{ order.id }}</a></td>
                <td>{{ order.customerName }}</td>
                <td>{{ order.total | currencyFormat }}</td>
                <td>
                  <nz-tag [nzColor]="statusColor[order.status] || 'default'">{{ order.status }}</nz-tag>
                </td>
                <td>{{ order.createdAt | date:'dd/MM/yyyy' }}</td>
              </tr>
            }
          </tbody>
        </nz-table>
      </nz-card>
    }
  `,
})
export class DashboardComponent {
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);

  data: DashboardSummary | null = null;
  statusColor: Record<string, string> = {
    PENDING: 'orange',
    CONFIRMED: 'blue',
    SHIPPING: 'cyan',
    COMPLETED: 'green',
    CANCELLED: 'red',
  };

  constructor() {
    this.http.get<DashboardSummary>(ApiEndpoints.admin.dashboard).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((d) => this.data = d);
  }
}
