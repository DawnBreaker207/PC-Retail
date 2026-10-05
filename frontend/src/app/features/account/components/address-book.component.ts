import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { EmptyStateComponent } from '@shared/components/empty-state.component';
import { UserService } from '@core/services/user.service';
import { Address } from '@core/models/common.model';
import { AddressFormComponent } from './address-form.component';

@Component({
  selector: 'app-address-book',
  imports: [NzButtonModule, NzIconModule, NzTagModule, NzSpinModule, NzPopconfirmModule, NzModalModule, EmptyStateComponent],
  templateUrl: './address-book.component.html',
})
export class AddressBookComponent {
  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);
  private modal = inject(NzModalService);
  private message = inject(NzMessageService);

  addresses = signal<Address[]>([]);
  loading = signal(true);

  constructor() {
    this.loadData();
  }

  private loadData() {
    this.loading.set(true);
    this.userService.getAddresses().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (addresses) => {
        this.addresses.set(addresses);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openCreateModal() {
    this.openForm(null);
  }

  openEditModal(address: Address) {
    this.openForm(address);
  }

  private openForm(address: Address | null) {
    const modalRef = this.modal.create({
      nzTitle: address ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới',
      nzContent: AddressFormComponent,
      nzData: { address },
      nzFooter: null,
      nzWidth: 480,
    });
    modalRef.afterClose.subscribe((success) => {
      if (success) {
        this.message.success(address ? 'Cập nhật địa chỉ thành công' : 'Thêm địa chỉ thành công');
        this.loadData();
      }
    });
  }

  deleteAddress(id: number) {
    this.userService.deleteAddress(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.message.success('Xóa địa chỉ thành công');
        this.loadData();
      },
      error: () => this.message.error('Xóa địa chỉ thất bại'),
    });
  }
}
